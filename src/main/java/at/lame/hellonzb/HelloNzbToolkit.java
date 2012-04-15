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
package at.lame.hellonzb;

import at.lame.hellonzb.nntpclient.nioengine.*;
import at.lame.hellonzb.parser.*;

import java.awt.Color;
import java.awt.Font;
import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.FileChannel;
import java.nio.charset.*;
import java.util.*;
import javax.swing.*;
import javax.swing.plaf.ColorUIResource;


/**
 * This class contains several static methods as helper functions
 * for the HelloNzb application.
 * 
 * @author Matthias F. Brandstetter
 */
public class HelloNzbToolkit
{
	private static long NANO_MODIFIER = 1000000000;
	private static String PROG_VERSION_FILE = "http://hellonzb.sourceforge.net/version.txt";


	/**
	 * This method receives an absolut filename (or path) and extracts the
	 * last name out of it. For example: C:\dir\file.txt --> file.txt
	 * 
	 * @param filename The (absolute) filename to parse
	 * @return The last filename found
	 */
	public static String getLastFilename(String filename)
	{
		String sep = File.separator;
		String ret = filename;
		
		for(int idx = ret.indexOf(sep); idx != -1; idx = ret.indexOf(sep))
		{
			ret = ret.substring(idx + 1, ret.length());
		}
		
		return ret;
	}
	
	/**
	 * Delete all files within a directory, but keep sub-directory
	 * and parent directory untouched.
	 * 
	 * @param dir The directory to empty
	 * @return True if all files have successfully been deleted
	 */
	public static boolean emptyDir(File dir)
	{
		boolean result = true;
		
		if(dir == null)
			return true;
		
		File [] files = dir.listFiles();
		if(files == null)
			return true;
		
		for(File f : files)
		{
			if(f.isDirectory() || !f.canWrite())
				continue;
			
			f.delete();
		}
		
		return result;
	}
	
	/**
	 * Delete a (non-empty) directy on local file system.
	 * 
	 * @param dir Directory to delete
	 * @return true if deletion was successful, false otherwise
	 */
	public static boolean deleteNonEmptyDir(File dir)
	{
		if(dir.isDirectory())
		{
			String [] children = dir.list();
			for(String child : children)
			{
				boolean success = deleteNonEmptyDir(new File(dir, child));
				if(!success)
					return false;
			}
		}
		
		return dir.delete();
	}
	
	/**
	 * Check for correct connection to NNTP server.
	 * 
	 * @param host The HelloNzb main application
	 * @param showSuccess Whether or not to show test result in popup window
	 * @return Whether or not the connection test was successful
	 */
	public static boolean testServerConnection(HelloNzb mainApp, boolean showSuccess)
	{
		boolean error = false;
		String msg = "";
		String title;
		long start = 0;
		long curr = 0;
		long diff = 0;

		
		String hostname = mainApp.getPrefValue("ServerSettingsHost");
		String port     = mainApp.getPrefValue("ServerSettingsPort");
		String timeout  = mainApp.getPrefValue("ServerSettingsTimeout");
	
		// check for specified hostname
		if(hostname.length() == 0 || port.length() == 0 || timeout.length() == 0)
		{
			msg = mainApp.getLocaler().getBundleText("PopupServerNotSet"); 
			error = true;
		}
		else
		{
			try
			{
				// create NIO client and background thread
				NettyNioClient client = new NettyNioClient(mainApp);
				RspHandler handler = new RspHandler(null);
				Thread t = new Thread(client);
				t.setDaemon(true);
				t.start();
				
				// test authentication
				client.testAuth(handler);
				start = System.nanoTime();
				while(!handler.isFinished())
				{
					curr = System.nanoTime();
					diff = curr - start;
					if(diff > (Integer.valueOf(timeout) * NANO_MODIFIER))
						break;

					try
					{
						Thread.sleep(10);
					}
					catch(InterruptedException ex)
					{
						// do nothing ...
					}
				}
				
				if(client != null)
					client.shutdown(true, start + (Integer.valueOf(timeout) * NANO_MODIFIER));
					
				mainApp.resetThreadView();
				
				if(diff > (Integer.valueOf(timeout) * NANO_MODIFIER))
				{
					msg = mainApp.getLocaler().getBundleText("PopupServerConnectionError");
					error = true;
				}
				else if((handler.getError() == RspHandler.ERR_AUTH))				   
				{
					msg = mainApp.getLocaler().getBundleText("PopupAuthFailed");
					error = true;
				}
			}
			catch(UnknownHostException ex)
			{
				msg = mainApp.getLocaler().getBundleText("PopupUnknownServer");
				error = true;
			}
			catch(IOException ex)
			{
				msg = mainApp.getLocaler().getBundleText("PopupSocketError");
				error = true;
			}
		}

		if(error)
		{
			title = mainApp.getLocaler().getBundleText("PopupErrorTitle");
			JOptionPane.showMessageDialog(null, msg, title, JOptionPane.ERROR_MESSAGE);
			return false;
		}
		else if(showSuccess)
		{
			msg = mainApp.getLocaler().getBundleText("PopupServerConnectionOK");
			JOptionPane.showMessageDialog(null, msg, "", JOptionPane.INFORMATION_MESSAGE);
		}

		return true;
	}
	
	/**
	 * This method receives a var that contains a number of bytes.
	 * It then reformats this number to a human readable format.
	 * 
	 * @param bytes The number of bytes
	 * @return The pretty printed string
	 */
	public static String prettyPrintFilesize(long bytes)
	{
		String prettyString = "";
		
		if(bytes >= 1000000000)
		{
			float tmp = (float) bytes / 1000000000;
			prettyString  = String.format("%.2f", tmp);
			prettyString += " GB";
		}
		else if(bytes >= 1000000)
		{
			float tmp = (float) bytes / 1000000;
			prettyString  = String.format("%.2f", tmp);
			prettyString += " MB";
		}
		else if(bytes >= 1000)
		{
			float tmp = (float) bytes / 1000;
			prettyString  = String.format("%.2f", tmp);
			prettyString += " KB";
		}
		else
		{
			prettyString  = String.valueOf(bytes);
			prettyString += " bytes";
		}
		
		return prettyString;
	}
	
	/**
	 * This method receives a var that contains a seconds.
	 * It then reformats this number to a human readable format.
	 * 
	 * @param secs The number of seconds
	 * @return The pretty printed string
	 */
	public static String prettyPrintSeconds(long secs)
	{
		String prettyString = "";
		
		long ss = secs % 60; secs -= ss; secs /= 60;
		long mm = secs % 60; secs -= mm; secs /= 60;
		long hh = secs;
		
		long days = 0;
		if(hh >= 24)
		{
			// days
			days = hh / 24;
			hh = hh % 24;
		}
		
		String hhTmp = String.valueOf(hh);
		String mmTmp = String.valueOf(mm);
		String ssTmp = String.valueOf(ss);
		
		if(hh < 10)
			hhTmp = "0" + hhTmp;
		if(mm < 10)
			mmTmp = "0" + mmTmp;
		if(ss < 10)
			ssTmp = "0" + ssTmp;
		
		prettyString = hhTmp + ":" + mmTmp + ":" + ssTmp;
		if(days > 0)
			prettyString = String.valueOf(days) + ":" + prettyString;
		
		return prettyString;
	}
	
	/**
	 * This method receives the value of "bytes per second".
	 * It returns this value pretty printed (kbps, mbps).
	 *  
	 * @param bps The cound of bytes per second
	 * @return The pretty printed string
	 */
	public static String prettyPrintBps(long bps)
	{
		String prettyString = "";
		
		if(bps >= 1000000000)
		{
			float tmp = (float) bps / 1000000000;
			prettyString  = String.format("%.2f", tmp);
			prettyString += " GB/s";
		}
		else if(bps >= 1000000)
		{
			float tmp = (float) bps / 1000000;
			prettyString  = String.format("%.2f", tmp);
			prettyString += " MB/s";
		}
		else if(bps >= 1000)
		{
			float tmp = (float) bps / 1000;
			prettyString  = String.format("%.2f", tmp);
			prettyString += " KB/s";
		}
		else
		{
			prettyString  = String.valueOf(bps);
			prettyString += " B/s";
		}
		
		return prettyString;
	}
	
	/**
	 * This method receives an array of short that contains downloaded data
	 * in raw bytes format. It converts the characters found from beginning
	 * until the first new line to a String object.
	 * 
	 * @param data The short array (data)
	 * @return The first line found within the data converted to a String
	 */
	public static String firstLineFromByteData(byte [] data, int linecount)
	{
		Charset csets = Charset.forName("US-ASCII");
		boolean fin = false;
		int currChar = 0;
		String retString = "";
		
		
		if(data.length == 0)
			return "";
		
		// remove any CR and/or LF characters at the beginning of the article data
		for(int line = 0; line < linecount; line++)
		{
			fin = false;
			
			while(!fin)
			{
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
			
			// extract first line (all chars until CR and/or LF
			fin = false;
			for(int i = 0; i < data.length && !fin; i++, currChar++)
			{
				byte in = data[currChar];
				ByteBuffer bb = ByteBuffer.wrap(new byte[] { (byte) in });
				CharBuffer cb = csets.decode(bb);
				char c = cb.charAt(0);
				
				if(c == '\n' || c == '\r')
					fin = true;
				else
					retString += c;
			}
			retString += "\r\n";
		}

		return retString;
	}
	
	/**
	 * Write the given String to the memory mapped file.
	 * 
	 * @param nzb The string to write
	 */
	public static void writeToMappedBuffer(String str)
	{
		try
		{
			String tempDir = System.getProperty("java.io.tmpdir");
			String mapFile = tempDir + "HelloNzb-memMap";
			
			RandomAccessFile raf = new RandomAccessFile(mapFile, "rw");
			FileChannel fc = raf.getChannel();
			MappedByteBuffer mbb = fc.map(FileChannel.MapMode.READ_WRITE, 0,
					HelloNzbCradle.MEM_MAP_BUFFER_SIZE);
			mbb.clear();
			mbb.put(new String("NZB " + str).getBytes());
			mbb.put((byte) 0);
			
	        fc.close();
	        raf.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Parse the passed String array for a valid nzb file location.
	 * 
	 * @param args The String array to parse
	 * @return The location of the nzb file, if valid args, null otherwise
	 */
	public static String parseCmdLineArgs(String [] args)
	{
		if(args.length > 1)
		{
			System.err.println("Only one argument (absolute path to nzb file) is allowed!");
			System.exit(9);
		}
		else if(args.length > 0)
		{
			try
			{
				File file = new File(args[0]);
				if(!file.isFile())
				{
					System.err.println("'" + args[0] + "' is not a valid file!");
					System.exit(8);
				}
				if(!file.canRead())
				{
					System.err.println("Could not read file '" + args[0] + "'!");
					System.exit(8);
				}
				return args[0];
			}
			catch(Exception e)
			{
				e.printStackTrace();
				System.exit(8);
			}
		}
		
		return null;
	}
	
	/**
	 * Initialize the GUI look&feel.
	 */
	public static void initializeLookAndFeel()
	{
		Font f = UIManager.getFont("ProgressBar.font");
		UIManager.put("ProgressBar.font", f.deriveFont(Font.PLAIN));
		UIManager.put("ProgressBar.selectionBackground", new ColorUIResource(Color.BLACK));
		UIManager.put("ProgressBar.selectionForeground", new ColorUIResource(Color.BLACK));

/*		try 
		{
			String osName = System.getProperty("os.name");
			if(osName.startsWith("Windows")) 
			{
				UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
			}
			else if(osName.startsWith("Mac")) 
			{
				// do nothing, use the Mac Aqua L&f
            } 
			else 
			{
				UIManager.setLookAndFeel("com.jgoodies.looks.plastic.PlasticXPLookAndFeel");
            }
        } 
		catch(Exception e) 
		{
			// Likely the Looks library is not in the class path; ignore.
			System.err.println("Can't initialize specified look&feel");
		}
*/	}

	
	/**
	 * Check for new HelloNzb program version. To do so fetch the version-file
	 * from HelloNzb project web site and parse it for the latest version.
	 * 
	 * @param current The current version of the program
	 * @return True if there is an update available, false otherwise
	 */
	public static boolean isUpdateAvailable(String current)
	{
		// get latest program version string from project web site
		String latest = getLatestVersion();
		if(latest == null)
			return false; // exception occured during method execution
		
		// compare the fetched version with the current program version
		return isLatestNewer(current, latest);
	}
	
	/**
	 * Get the latest version of HelloNzb from project web site.
	 * 
	 * @return The string containing the new version
	 */
	public static String getLatestVersion()
	{
		String line = null;

		try
		{
			// create URL object and try to fetch program version file
			URL url = new URL(PROG_VERSION_FILE);
			URLConnection urlConn = url.openConnection();
			urlConn.setDoInput(true);
			urlConn.setUseCaches(false);
			urlConn.setConnectTimeout(HelloNzb.DEFAULT_TIMEOUT);
			urlConn.setReadTimeout(HelloNzb.DEFAULT_TIMEOUT);
			
			// create new data input stream from online connection
			DataInputStream stream = new DataInputStream(urlConn.getInputStream());
			BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

			line = reader.readLine();
			
			reader.close();
			stream.close(); 
		}
		catch(MalformedURLException e)
		{
			e.printStackTrace();
		}
		catch(IOException e)
		{
			line = "0.0.0.0";
		}
		
		return line;
	}
	
	private static boolean isLatestNewer(String current, String l)
	{
		current.trim();
		l.trim();
		
		String latest = "";
		for(int i = 0; i < l.length() && l.charAt(i) != '\n' && l.charAt(i) != '\r'; i++)
			latest += l.charAt(i);
		
		int [] cParts = getVersionParts(current);
		int [] lParts = getVersionParts(latest);
		
		boolean newer = false;
		for(int i = 0; i < 4 && newer == false; i++)
		{
			if(lParts[i] > cParts[i])
				newer = true;
			else if(lParts[i] < cParts[i])
				break;
		}
		
		return newer;
	}
	
	private static int [] getVersionParts(String ver)
	{
		String [] tmp = ver.split("\\.");
		int [] parts = new int[] { 0, 0, 0, 0 };

		if(tmp.length > 0) parts[0] = Integer.valueOf(tmp[0]);
		if(tmp.length > 1) parts[1] = Integer.valueOf(tmp[1]);
		if(tmp.length > 2) parts[2] = Integer.valueOf(tmp[2]);
		if(tmp.length > 3) parts[3] = Integer.valueOf(tmp[3]);
		
		return parts;
	}
}


















