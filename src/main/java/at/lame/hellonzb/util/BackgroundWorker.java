/*******************************************************************************
 * HelloNzb -- The Binary Usenet Tool
 * Copyright (C) 2010-2011 Matthias F. Brandstetter
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package at.lame.hellonzb.util;

import java.io.*;
import java.util.*;
import java.nio.*;
import java.nio.channels.*;
import at.lame.hellonzb.HelloNzb;
import at.lame.hellonzb.HelloNzbCradle;
import at.lame.hellonzb.HelloNzbToolkit;
import at.lame.hellonzb.parser.*;


/**
 * This class performs multiple different background tasks during HelloNzb
 * program execution:
 *  - check for new files to download (from other program instances)
 *  - check for new files to download (from auto-load directory)
 *  
 * This class should be used as a thread, it runs in background performing
 * its tasks once per second.
 *  
 * @author Matthias F. Brandstetter
 */
public class BackgroundWorker extends Thread
{	
	private static final int BPS_HISTORY_SIZE = 5;
	
	private HelloNzbCradle mainApp;
	private MyLogger logger;
	private int bufferSize;
	private RandomAccessFile raf;
	private MappedByteBuffer mbb;
	private FixedSizeQueue<Long> bpsHistory;
	private boolean shutdown;
	
	
	/**
	 * Class constructor.
	 * 
	 * @param mainApp The main application object
	 * @param bufferSize The buffer size to use for instance communication
	 * @throws IOException
	 */
	public BackgroundWorker(HelloNzbCradle mainApp, int bufferSize) throws IOException
	{
		this.mainApp = mainApp;
		this.logger = mainApp.getLogger();
		this.bufferSize = bufferSize;
		this.shutdown = false;
		
		String tempDir = System.getProperty("java.io.tmpdir");
		String mapFile = tempDir + "/HelloNzb-memMap";
		
		File f = new File(mapFile);
		if(f.exists())
			f.delete();
		
		raf = new RandomAccessFile(mapFile, "rw");
		mbb = raf.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, bufferSize);
		bpsHistory = new FixedSizeQueue<Long>(BPS_HISTORY_SIZE);
	}
	
	/**
	 * Called via thread.start()
	 */
	public void run()
	{
		SpeedGraphPanel speedGraph = null;
		
		while(!shutdown)
		{
			try
			{
				// check if another program instance told us something
				checkCommFromInstance();
				
				// check new files in auto-load directory
				checkAutoLoadDir();
				
				// get total size of all NZB files in queue
				long totalBytesInQueue = totalBytesInQueue();
				
				// update main app's ETA and total file size label
				long lastBps = updateEtaLabels(totalBytesInQueue);
				
				// update speed history graph
				if(speedGraph == null)
					speedGraph = mainApp.getSpeedGraphPanel();
				else
				{
					speedGraph.add(lastBps);
					speedGraph.repaint();
				}
				
				// let the thread sleep a bit
				try
				{
					Thread.sleep(950);
				}
				catch(InterruptedException e) {}
			}
			catch(Exception e) 
			{ 
				logger.printStackTrace(e);
			}
		}
		
		// cleanup
		try
		{
			raf.close();
		}
		catch(IOException e)
		{
			logger.printStackTrace(e);
		}
	}
	
	/**
	 * Called in order to shutdown this thread.
	 */
	public void shutdown()
	{
		shutdown = true;
	}
	
	/**
	 * Called to update the ETA and related lables on main window.
	 * 
	 * @param totalBytesInQueue The last total amount of bytes value
	 * @return The newly calculated lastBps value
	 */
	private long updateEtaLabels(long totalBytesInQueue)
	{
		long etaSecs = 0;
		long lastBps = 0;
		long origLastBps = ((HelloNzb) mainApp).lastBpsValue();
		
		// calculate avg. value of bps history
		bpsHistory.add(origLastBps);
		int i = 0;
		for(; i < bpsHistory.size(); i++)
			lastBps += bpsHistory.get(i);
		lastBps /= i;
		((HelloNzb) mainApp).setCurrDlSpeedLabel(lastBps);
		
		// update GUI
		if(lastBps > 0)
			etaSecs = (long) (totalBytesInQueue / lastBps);
		if(totalBytesInQueue == 0)
			mainApp.setEtaAndTotalLabel("");
		else
		{
			String sizeString = HelloNzbToolkit.prettyPrintFilesize(totalBytesInQueue);
			String etaString = HelloNzbToolkit.prettyPrintSeconds(etaSecs);
			mainApp.setEtaAndTotalLabel(sizeString + " / " + etaString);
		}
		
		return lastBps;
	}
	
	/**
	 * Called to calculate the toal amount of bytes that is currently
	 * in the download queue.
	 * 
	 * @return The calculated total
	 */
	private long totalBytesInQueue()
	{
		long totalBytesInQueue = 0L;
		
		Vector<NzbParser> parsers = mainApp.getNzbQueue();
		for(int i = 0; i < parsers.size(); i++)
			totalBytesInQueue += parsers.get(i).getCurrTotalSize();
		
		return totalBytesInQueue;
	}
	
	/**
	 * Check if another program instance told us something.
	 */
	private void checkCommFromInstance()
	{
		byte [] data = new byte[bufferSize];
		String dataString = null;
		
		try
		{
			data[0] = 0;
			mbb.rewind();

			// any bytes to read from the mapping buffer?
			int bytecnt = mbb.remaining();
			if(bytecnt > bufferSize)
				bytecnt = bufferSize;

			// new data to read?
			if(bytecnt > 0)
			{
				mbb.get(data, 0, bytecnt);
				
				if(data[0] != 0)
				{
					int i = 0;
					while(i < data.length && data[i] != 0)
						i++;
					
					dataString = new String(data);
					dataString = dataString.substring(0, i);
					
					// parse command
					if(dataString.length() > 4)
					{
						if(dataString.substring(0, 3).equals("NZB"))
						{
							// new NZB file to load
							String filename = dataString.substring(4, dataString.length());
							logger.msg("Loading new NZB file from external instance: " + filename, 
									MyLogger.SEV_INFO);
							mainApp.addNzbToQueue(new NzbParser(mainApp, filename));
						}
						else
							throw new Exception("invalid command received from external instance (" +
									dataString + ")");
					}
					else
						throw new Exception("invalid command received from external instance (" +
								dataString + ")");
				}
			}
		}
		catch(Exception e) 
		{ 
			logger.printStackTrace(e); 
		}
		finally
		{
			mbb.clear();
			mbb.put(new byte[] { 0 });
		}
	}

	/**
	 * Check for new files in auto-load directory.
	 */
	private void checkAutoLoadDir()
	{
		final String pref = mainApp.getPrefValue("GeneralSettingsAutoLoadDir");
		if(pref.isEmpty())
			return;
		
		final File autoLoadDir = new File(pref);
		if(!autoLoadDir.isDirectory() || !autoLoadDir.canRead() || !autoLoadDir.canExecute())
			return;

		// directory exists, so load all nzb files from it
		boolean okToDel = false;
		File [] files = autoLoadDir.listFiles();
		for(File file : files)
		{
			okToDel = false;
			
			try
			{
				// directory
				if(file.isDirectory())
					continue;
				
				// nzb file?
				String filename = file.getCanonicalPath();
				if(!filename.substring(filename.length() - 4, filename.length()).equalsIgnoreCase(".nzb"))
					continue;
				
				// yes, so add it to the download queue
				logger.msg("Loading new NZB file from auto-load directory: " + filename, MyLogger.SEV_INFO);
				mainApp.addNzbToQueue(new NzbParser(mainApp, file.getAbsolutePath()));
				okToDel = true;
			}
			catch(Exception e)
			{
				logger.msg("Failed to load NZB file from auto-load directory", MyLogger.SEV_ERROR);
				logger.printStackTrace(e);
			}
			finally
			{
				if(okToDel && file != null && file.canWrite())
					file.delete();
			}
		}
	}
}

































