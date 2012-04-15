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

package at.lame.hellonzb.nntpclient;

import at.lame.hellonzb.*;
import at.lame.hellonzb.nntpclient.nioengine.*;
import at.lame.hellonzb.parser.*;
import at.lame.hellonzb.util.MyLogger;

import java.io.*;
import java.util.*;
import java.nio.*;
import java.nio.charset.*;
import javax.swing.*;

/**
 * This class is used to download a file. It creates as many client threads as
 * specified by the user settings. When all segments were completely downloaded,
 * they are put together and saved to the output file.
 * 
 * @author Matthias F. Brandstetter
 */
public class NntpFileDownloader implements Runnable
{
	/** The main HelloNzb application object */
	private final HelloNzb mainApp;

	/** central logger object */
	private MyLogger logger;

	/** Nio client object */
	private NettyNioClient nioClient;

	/** The file that should has to be downloaded */
	private SegmentQueue segQueue;

	/** map a DownloadFile to all associated RspHandler objects */
	private HashMap<DownloadFile, Vector<RspHandler>> dlFileRspHandlerMap;

	/** A set of active response handlers */
	private Vector<RspHandler> activeRspHandlers;

	/** The download directory on local disk */
	private File dlDir;

	/** Flag set from main app when this thread should pause working */
	private boolean pause;

	/** Flag set from main app when this thread should shutdown */
	private boolean shutdown;

	/** CRC32 error flag */
	private boolean crc32Error;

	/**
	 * This is the constructor of the class. It has to receive the file that has
	 * to be downloaded by this downloader object.
	 * 
	 * @param file The DownloadFile object that has to be downloaded
	 */
	public NntpFileDownloader(NettyNioClient nioClient, SegmentQueue segQueue, File dlDir, HelloNzb mainApp)
	{
		this.mainApp = mainApp;
		this.logger = mainApp.getLogger();
		this.nioClient = nioClient;
		this.segQueue = segQueue;
		this.dlFileRspHandlerMap = new HashMap<DownloadFile, Vector<RspHandler>>();
		this.activeRspHandlers = new Vector<RspHandler>();
		this.dlDir = dlDir;
		this.pause = false;
		this.shutdown = false;
		this.crc32Error = false;
	}

	/**
	 * This method starts the thread and begins to download the file.
	 */
	public void run()
	{
		int maxThreads = Integer.parseInt(mainApp.getPrefValue("ServerSettingsThreadCount"));
		int runningThreads = 0;
		HashMap<String, Integer> downloadedBytes = new HashMap<String, Integer>();
		HashMap<String, Integer> lastProgBarUpdate = new HashMap<String, Integer>();

		// loop at all segments of the download file
		while(!shutdown && (segQueue.hasMoreSegments() || runningThreads > 0))
		{
			// more segments to go?
			while(segQueue.hasMoreSegments() && runningThreads < maxThreads && !pause && nioClient.hasFreeSlot())
			{
				// get next download segment of the download file
				DownloadFileSegment seg = segQueue.nextSegment();
				if(seg == null)
					break;
				String filename = seg.getDlFile().getFilename();
				logger.msg("Downloading next segment of file: " + filename, MyLogger.SEV_DEBUG);

				// create new response handler
				RspHandler newHandler = new RspHandler(seg);
				activeRspHandlers.add(newHandler);

				// map the new response handler to the download file
				Vector<RspHandler> tmpVector = dlFileRspHandlerMap.get(seg.getDlFile());
				if(tmpVector == null)
					tmpVector = new Vector<RspHandler>();
				tmpVector.add(newHandler);
				dlFileRspHandlerMap.put(seg.getDlFile(), tmpVector);

				// start data download
				nioClient.fetchArticleData(seg.getGroups().firstElement(), seg.getArticleId(), newHandler);

				// increase thread counter
				runningThreads++;
			}

			// check if the next element of the result set is already finished
			Vector<RspHandler> toRemoveVector = new Vector<RspHandler>();
			for(int i = 0; i < activeRspHandlers.size(); i++)
			{
				RspHandler handler = activeRspHandlers.get(i);

				// handle error response from NNTP server
				if(handler.getError() == RspHandler.ERR_NONE)
				{
					// no error, do nothing
				}
				else if(handler.getError() == RspHandler.ERR_AUTH)
				{
					// do nothing for this error (?)
				}
				else if(handler.getError() == RspHandler.ERR_FETCH)
				{
					// TODO: handle "430 no such article" error (?)
					String msg = "no such article found: <"
							+ handler.dlFileSeg().getArticleId() + "> ("
							+ handler.getErrorMsg() + ")";
					logger.msg(msg, MyLogger.SEV_WARNING);
				}
				else
				{
					// all other errors
					shutdown = true;
				}

				// update downloaded byte counter ...
				DownloadFile dlFile = handler.dlFileSeg().getDlFile();
				String filename = dlFile.getFilename();
				int bytes = 0;
				Integer bytesInt = downloadedBytes.get(filename);
				if(bytesInt != null)
					bytes = bytesInt;
				bytes += handler.newByteCount();
				downloadedBytes.put(filename, bytes);

				// ... and progres bar in main window
				int last = 0;
				Integer lastInt = lastProgBarUpdate.get(filename);
				if(lastInt != null)
					last = lastInt;
				last = updateProgressBar(bytes, last, dlFile);
				lastProgBarUpdate.put(filename, last);

				// all data downloaded?
				if(handler.isFinished())
				{
					toRemoveVector.add(handler);
					runningThreads--;
					decrSegCount(filename); // decrease main window segment
											// counter

					// segment done, so check if whole download file is finished
					// now
					dlFile.removeSegment(handler.dlFileSeg().getIndex());
					if(!dlFile.hasMoreSegments())
					{
						try
						{
							handleFinishedDlFile(dlFile);
						}
						catch(Exception e)
						{
							logger.printStackTrace(e);
						}
					}
				}
			}
			activeRspHandlers.removeAll(toRemoveVector);
			toRemoveVector.removeAllElements();

			// all tasks done?
			if(!segQueue.hasMoreSegments() && runningThreads == 0)
			{
				break;
			}

			try
			{
				// let the thread sleep a bit
				Thread.sleep(10);
			}
			catch(InterruptedException e)
			{
				// shutdown if interrupted
				shutdown = true;
			}
		} // end of main loop

		logger.msg("FileDownloader has finished downloading all files", MyLogger.SEV_DEBUG);
	}

	/**
	 * Called from main app when this thread should pause working.
	 */
	public void setPaused(boolean p)
	{
		pause = p;
	}

	/**
	 * Returns the paused state of this thread.
	 * 
	 * @return The boolean value
	 */
	public boolean isPaused()
	{
		return pause;
	}

	/**
	 * Called from main app when this thread should shutdown.
	 */
	public void shutdown()
	{
		shutdown = true;
	}

	/**
	 * This method is called from the HelloYenc object when it encounters an
	 * crc32 error at a Yenc part. Can be ignored via application settings.
	 */
	public void crc32Error()
	{
		// disabled
		/*
		 * String pref =
		 * mainApp.getPrefValue("DownloadSettingsIgnoreCrc32Error");
		 * if(!pref.equals("true")) crc32Error = true;
		 */
	}

	/**
	 * This method is called when a whole download file has been finished
	 * downloading. It updates main application window and starts the decoding
	 * thread.
	 * 
	 * @param dlFile The DownloadFile object that is finished
	 */
	private void handleFinishedDlFile(final DownloadFile dlFile)
	{
		final String filename = dlFile.getFilename();
		logger.msg("File downloading finished: " + filename, MyLogger.SEV_INFO);

		// notify application that download has finished
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				mainApp.fileDownloadFinished(filename);
				mainApp.setProgBarToDecoding(filename, dlFile.getSegCount());
			}
		});

		// create result vector
		Vector<byte[]> articleData = new Vector<byte[]>();
		Vector<RspHandler> rspHandlers = dlFileRspHandlerMap.get(dlFile);
		for(int i = 0; i < rspHandlers.size(); i++)
		{
			byte[] tmpArray = removeFirstLine(rspHandlers.get(i).getData(true));
			articleData.add(tmpArray);
			rspHandlers.set(i, null); // free some memory
		}

		// call garbage collector
		rspHandlers = null;
		dlFileRspHandlerMap.remove(dlFile);
		Runtime.getRuntime().gc();

		logger.msg("First line(s) dump:\n" + HelloNzbToolkit.firstLineFromByteData(
				articleData.get(0), 2), MyLogger.SEV_DEBUG);

		// determine data encoding (yenc or UU)
		String encoding = null;
		boolean bHasData = false;
		for(int i = 0; i < articleData.size(); i++)
		{
			byte[] abyteHelp = articleData.get(i);
			if(abyteHelp.length > 0)
			{
				bHasData = true;
				if(bytesEqualsString(abyteHelp, "=ybegin"))
				{
					encoding = "yenc";
					break;
				}
				else if(bytesEqualsString(abyteHelp, "begin "))
				{
					encoding = "uu";
					break;
				}
			}
		}
		if(encoding == null)
		{
			if(bHasData)
			{
				encoding = "yenc";
				logger.msg("No suitable decoder (no data) found for downloaded file: " + 
						dlFile.getFilename() + " -- Assuming yenc.", MyLogger.SEV_WARNING);
			}
			else
			{
				// too bad, no decoder found for this file :(
				logger.msg("No suitable decoder found for downloaded file (no data): "
						+ dlFile.getFilename(), MyLogger.SEV_ERROR);

				// update main application window
				SwingUtilities.invokeLater(new Runnable()
				{
					public void run()
					{
						mainApp.fileDecodingFinished(dlFile.getFilename());
					}
				});

				return;
			}
		}

		/*
		 * // determine data encoding String encoding = null;
		 * if(bytesEqualsString(articleData.get(0), "=ybegin")) encoding =
		 * "yenc"; else if(bytesEqualsString(articleData.get(0), "begin "))
		 * encoding = "uu"; else { // too bad, no decoder found for this file :(
		 * logger.msg("No suitable decoder found for downloaded file: " +
		 * dlFile.getFilename(), MyLogger.SEV_ERROR);
		 * 
		 * // update main application window SwingUtilities.invokeLater(new
		 * Runnable() { public void run() {
		 * mainApp.fileDecodingFinished(dlFile.getFilename()); } } );
		 * 
		 * return; }
		 */
		
		// start data decoding background thread
		FileDecoder fileDecoder = new FileDecoder(mainApp, dlDir, dlFile, articleData, encoding);
		Thread t = new Thread(fileDecoder);
		t.start();
	}

	/**
	 * Update the progress bar of the currently downloaded file in main window.
	 * Only update if progess has at least increased by one percent of the total
	 * file size of the downloaded file.
	 * 
	 * @param downloadedBytes The current amount of downloaded bytes
	 * @param lastProgBarUpdate The byte count at the last progress bar update
	 * @param file The download file
	 * @return The byte count at the last progress bar update
	 */
	private int updateProgressBar(int downloadedBytes, int lastProgBarUpdate,
			DownloadFile file)
	{
		int totalSize = (int) file.getTotalFileSize();

		// only update progess bar if progess has at least increased by one percent
		int diff = downloadedBytes - lastProgBarUpdate;
		int onePercent = (int) totalSize / 100;
		if(diff >= onePercent)
		{
			final String filename = file.getFilename();
			final int db = downloadedBytes;
			SwingUtilities.invokeLater(new Runnable()
			{
				public void run()
				{
					mainApp.updateDownloadQueue(filename, db);
				}
			});

			return downloadedBytes; // prog bar updated, so return the new byte count
		}

		return lastProgBarUpdate; // no update, so return the previous byte count
	}

	/**
	 * Decrease the segment count in the according row of the main window
	 * download table.
	 * 
	 * @param filename The filename of the row to update in main window
	 */
	private void decrSegCount(final String filename)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				mainApp.decrSegCount(filename);
			}
		});
	}

	/**
	 * Remove the first line from the passed byte array.
	 * 
	 * @param inputArray The byte array to process
	 * @return The processed byte array
	 */
	private static byte[] removeFirstLine(byte[] inputArray)
	{
		int offset = 0;

		while(offset < inputArray.length && (inputArray[offset] == 10 || inputArray[offset] == 13))
			offset++; 
		while(offset < inputArray.length && inputArray[offset] != 10 && inputArray[offset] != 13)
			offset++;
		while(offset < inputArray.length && (inputArray[offset] == 10 || inputArray[offset] == 13))
			offset++;

		byte[] newArray = new byte[inputArray.length - offset];
		System.arraycopy(inputArray, offset, newArray, 0, newArray.length);

		return newArray;
	}

	/**
	 * Check if the first X characters of a byte stream match a String.
	 * 
	 * @param data
	 *            The byte array to process
	 * @param pattern
	 *            The String to match
	 * @return True if the pattern was found, false otherwise
	 */
	private static boolean bytesEqualsString(byte[] data, String pattern)
	{
		byte[] bytes = new byte[pattern.length()];
		Charset csets = Charset.forName("US-ASCII");
		boolean fin = false;
		int currChar = 0;

		// remove any CR and/or LF characters at the beginning of the article
		// data
		while(!fin)
		{
			if(currChar >= data.length)
				break;

			byte in = data[currChar];
			ByteBuffer bb = ByteBuffer.wrap(new byte[] { (byte) in });
			CharBuffer cb = csets.decode(bb);
			char c = cb.charAt(0);

			if(data.length > 0 && (c == '\n' || c == '\r'))
				currChar++;
			else
				fin = true;

			if(data.length == 0)
				fin = true;
		}

		// extract bytes (chars) to check from article data
		for(int i = 0; i < bytes.length && i < data.length; i++, currChar++)
		{
			byte in = data[currChar];
			bytes[i] = (byte) in;
		}

		// decode byte data to characters
		ByteBuffer bb = ByteBuffer.wrap(bytes);
		CharBuffer cb = csets.decode(bb);

		// compare these characters to the pattern String
		for(int i = 0; i < pattern.length(); i++)
			if(cb.charAt(i) != pattern.charAt(i))
				return false;

		return true;
	}
}
