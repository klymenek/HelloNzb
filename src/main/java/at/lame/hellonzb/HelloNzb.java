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

import at.lame.hellonzb.listener.*;
import at.lame.hellonzb.listener.actions.*;
import at.lame.hellonzb.nntpclient.*;
import at.lame.hellonzb.nntpclient.nioengine.*;
import at.lame.hellonzb.parser.*;
import at.lame.hellonzb.preferences.HelloNzbPreferences;
import at.lame.hellonzb.statistics.*;
import at.lame.hellonzb.unrar.RarExtractor;
import at.lame.hellonzb.util.*;

import java.awt.TrayIcon.MessageType;
import java.awt.dnd.*;
import java.io.*;
import java.net.*;
import java.util.Vector;
import javax.swing.*;


/**
 * This is the main class of the JavaNzb application.
 * 
 * @author Matthias F. Brandstetter
 */
public class HelloNzb extends HelloNzbCradle
{
	/** Default window title string */
	private String winTitle;
		
	/** vars for one instance check */
	private static ServerSocket oneInstanceSocket;
	
	/** set to true if the user has stopped download before it was finished */
	private boolean downloadStopped;
	
	
	/**
	 * This is the main method of the HelloNzb application.
	 * 
	 * @param args String array of command line arguments
	 */
	public static void main(String [] args)
	{
		String nzbFileToLoad = HelloNzbToolkit.parseCmdLineArgs(args);
		
		// only one instance of the application is allowed to run simultaneously
		if(ONLY_ONE_INSTANCE)
		{
			// try to open the socket
			try
			{
				oneInstanceSocket = new ServerSocket(SERVER_SOCKET, 0, InetAddress.getByAddress(new byte[] {127,0,0,1}));
			}
			catch(IOException e)
			{
				String newNzbFile = HelloNzbToolkit.parseCmdLineArgs(args);
				if(newNzbFile != null)
					HelloNzbToolkit.writeToMappedBuffer(newNzbFile);
				else
					System.out.println("Only one instance of HelloNzb allowed!");

				System.exit(1);
			}
			catch(Exception e)
			{
				e.printStackTrace();
				System.exit(1);
			}
			
			// create runtime shutdown hook
			ShutdownHook shutdownHook = new ShutdownHook();
			Runtime.getRuntime().addShutdownHook(shutdownHook);
		}
		
		// init GUI look&feel
		HelloNzbToolkit.initializeLookAndFeel();
		
		// start application
		JFrame frame = new JFrame();
		new HelloNzb(frame, "HelloNzb - The Binary Usenet Tool", nzbFileToLoad);
	}
		
	/**
	 * This is the constructor of the class.
	 * 
	 * @param frame The JFrame object to use as window
	 * @param title The string used as window title
	 */
	public HelloNzb(JFrame frame, String title, String nzbFileToLoad)
	{
		super(frame, title);
		
		winTitle = title + " - v" + VERSION;
		jframe.setTitle(winTitle);
		
		// create new object of the preferences class
		prefContainer = new HelloNzbPreferences(this);
		
		// HelloNzb usage statistics
		HelloNzbUsageStats.main = this;
		activateUsageStats();
		HelloNzbUsageStats.registerStartup(VERSION, localer.getLocale().getDisplayLanguage());
		
		// set console output mode
		if(!DEBUG && prefContainer.getBooleanPrefValue("ExtendedSettingsConsoleOutput"))
			logger.setPrintLevel(MyLogger.SEV_INFO);

		// restore window size
		String savedWinWidth = this.prefContainer.getPrefValue("AutoSettingsWindowWidth");
		String savedWinHeight = this.prefContainer.getPrefValue("AutoSettingsWindowHeight");
		if(!savedWinWidth.isEmpty() && !savedWinHeight.isEmpty())
		{
			jframe.setSize(Integer.parseInt(savedWinWidth), Integer.parseInt(savedWinHeight));
			jframe.setLocationRelativeTo(null);
		}
		
		// create a shared memory mapping so that a second instance of this
		// program can later on pass new nzb files to this first instance here
		createMemoryMapping();
		
		// create the background task manager
		taskMgr = new TaskManager(this);
		JProgressBar progBar = taskMgr.getProgressBar();
		taskMgr.start();
		
		// add content panes to the main window
		QuitAction quitAction = addContentPanes(progBar);
		
		// initialise system tray icon
		initSystemTray(quitAction);
		
		// add window listener
		MyWindowListener wListener = new MyWindowListener(this);
		jframe.addWindowListener(wListener);
		wListener.setQuitAction(quitAction);

		// drag'n'drop support
		DropTarget target = new DropTarget(this.jframe, new MyDropTargetAdapter(this));
		this.jframe.setDropTarget(target);
		
		// repaint window
		this.jframe.setVisible(true);
		this.jframe.validate();
		this.jframe.repaint();

		// update speed graph panel
		this.speedGraph.setSize(toolBar.getHeight());
		this.jframe.validate();
		this.jframe.repaint();
		
		// load nzb file from command line
		loadFileFromCmdLine(nzbFileToLoad);
		
		// load last session (NZB files that were open)
		loadLastSession();
		
		// check for new program version
		if(prefContainer.getPrefValue("GeneralSettingsCheckForUpdates").equals("true"))
			checkProgramUpdate();
		
		// preferences set?
		String hostname = prefContainer.getPrefValue("ServerSettingsHost");
		if(hostname.isEmpty())
			showPreferences();
		else
			saveOpenParserData(false);
		
		this.downloadStopped = false;
	}

	/**
	 * Check online whether a new HelloNzb version is available.
	 * Called from constructor.
	 */
	private void checkProgramUpdate()
	{
		// set background status bar
		taskMgr.updateCheck(true);
		
		if(HelloNzbToolkit.isUpdateAvailable(VERSION))
		{
			MyFuture<Boolean> future = new MyFuture<Boolean>(false);
			HelloNzbUpdateNotifier notifier = new HelloNzbUpdateNotifier(jframe, localer, logger);
			notifier.show(future);

			if(future.getPayload())
			{
				// yes, download new version --> open browser
				try
				{
					java.awt.Desktop.getDesktop().browse(new URI(HELLONZB_WEBSITE));
				}
				catch(Exception e)
				{
					// error, could not open browser
					String msg = localer.getBundleText("PopupErrorCouldNotOpenBrowser");
					String title = localer.getBundleText("PopupErrorTitle");
					JOptionPane.showMessageDialog(jframe, msg, title, JOptionPane.ERROR_MESSAGE);
				} 
			}
		}

		// unset background status bar
		taskMgr.updateCheck(false);
	}
	
	/**
	 * Called at app shutdown to delete lock file
	 */
	private static void unlockFile() 
	{
		// unbind server socket
		try
		{
			oneInstanceSocket.close();
		}
		catch(IOException e) 
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Open the preferences window.
	 */
	public void showPreferences()
	{
		// create new object of the preferences class
		prefContainer = new HelloNzbPreferences(this);
		prefContainer.dialogCalled();
		prefContainer.getPrefDialog().setVisible(true);
	}
	
	/**
	 * This method starts to download the first file in the queue.
	 */
	public void startDownload()
	{
		String rootDir = prefContainer.getPrefValue("GeneralSettingsDownloadDir");
		String msg, title;
		
		
		// check if we are running already
		if(currentFileDownloader != null)
		{
			stopCurrDownload(true);
			return;
		}
		
		// check if the root download directory is set
		if(rootDir.equals(""))
		{
			msg = localer.getBundleText("PopupCreateDirError"); 
			title = localer.getBundleText("PopupErrorTitle");
			JOptionPane.showMessageDialog(jframe, msg, title, JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		// create download directory if necessary
		String nzbFilename = nzbFileQueueTabModel.getNzbParser(0).getName();
		nzbFilename = HelloNzbToolkit.getLastFilename(nzbFilename);
		File downloadDir = new File(rootDir + File.separator + nzbFilename);
		if(!downloadDir.isDirectory())
		{
			if(!downloadDir.mkdirs())
			{
				msg = localer.getBundleText("PopupCreateDirError"); 
				title = localer.getBundleText("PopupErrorTitle");
				JOptionPane.showMessageDialog(jframe, msg, title, JOptionPane.ERROR_MESSAGE);
				return;
			}
		}
	
		// (try to) start NIO client, if not already started
		if(!startNioClient())
			return;
		
		// start file downloader thread
		SegmentQueue segs = new SegmentQueue(filesToDownloadTabModel);
		NntpFileDownloader downloader = new NntpFileDownloader(nioClient, segs, downloadDir, this);
		downloader.setPaused(pauseToggleButton.isSelected());
		Thread thread = new Thread(downloader);
		thread.setDaemon(true);
		thread.start();
		
		// send usage stats
		statsNewDownload(segs.remainingBytes());
		
		// enable/disable toolbar and menu action
		AbstractAction action = actions.get("MenuServerStartDownload");
		action.setEnabled(true);
		action = actions.get("MenuServerPauseDownload");
		action.setEnabled(true);
		startToggleButton.setSelected(true);
		pauseToggleButton.setSelected(false);
		
		currentFileDownloader = downloader;
		
		// reset data counter?
		if(downloadStopped)
			downloadStopped = false;
		else
			totalBytesLoaded = currentNzbParser.getDownloadedBytes();
	}

	// immediately stop currently active download
	private void stopCurrDownload(boolean wait)
	{
		if(currentFileDownloader == null)
			return;
		
		// disconnect
		globalDisconnect(wait);
		filesToDownloadTabModel.resetAllSegCounts(false);
		Vector<DownloadFile> dlFiles = currentNzbParser.getFiles();
		for(DownloadFile file : dlFiles)
			file.resetSegments();
		dlFiles = filesToDownloadTabModel.getDownloadFileVector();
		for(DownloadFile file : dlFiles)
			updateDownloadQueue(file.getFilename(), 0);

		// and reset actions (menus and actions)
		updStatusBar(0);
		startToggleButton.setSelected(false);
		pauseToggleButton.setSelected(false);
		AbstractAction action = actions.get("MenuServerPauseDownload");
		action.setEnabled(false);
		
		downloadStopped = true;			
	}
	
	// (try to) start NIO client, if not already started
	private boolean startNioClient()
	{
		try
		{
			// start NIO client
			if(nioClient == null)
			{
				nioClient = new NettyNioClient(this);
				
				// start daemon thread
				Thread t = new Thread(nioClient);
				t.setDaemon(true);
				t.start();
			}
			return true;
		}
		catch(UnknownHostException e)
		{
			String msg = localer.getBundleText("PopupUnknownServer"); 
			String title = localer.getBundleText("PopupErrorTitle");
			JOptionPane.showMessageDialog(jframe, msg, title, JOptionPane.ERROR_MESSAGE);
			startToggleButton.setSelected(false);
			pauseToggleButton.setSelected(false);
			return false;
		}
		catch(IOException e)
		{
			logger.printStackTrace(e);
			startToggleButton.setSelected(false);
			pauseToggleButton.setSelected(false);
			return false;
		}
	}
	
	// register new download in usage statistics (call web service)
	private void statsNewDownload(long bytes)
	{
		int threadcount = Integer.valueOf(prefContainer.getPrefValue("ServerSettingsThreadCount"));
		boolean ssl = prefContainer.getBooleanPrefValue("ServerSettingsUseSSL");
		String speedPref = prefContainer.getPrefValue("DownloadSettingsMaxConnectionSpeed");
		boolean speed = false;
		if(!speedPref.equals("0")) speed = true;
		boolean par2 = prefContainer.getBooleanPrefValue("DownloadSettingsPar2Check");
		boolean unrar = prefContainer.getBooleanPrefValue("DownloadSettingsExtractRARArchives");
		
		HelloNzbUsageStats.registerDownload(bytes, threadcount, ssl, speed, par2, unrar);
	}
	
	/**
	 * Called from action listener when download should be paused.
	 */
	public void pauseDownload()
	{
		if(currentFileDownloader == null)
			return;
		
		// check if we are currently in paused state
		if(!currentFileDownloader.isPaused())
		{
			pauseToggleButton.setSelected(true);
			currentFileDownloader.setPaused(true);
	        SwingUtilities.invokeLater(new Runnable() 
	        { 
	        	public void run()
	        	{
	        		statusBarText.setText(localer.getBundleText("StatusBarDownloadPaused"));
				} 
			} ); 
		}
		else
		{
			pauseToggleButton.setSelected(false);
			currentFileDownloader.setPaused(false);
		}
	}
	
	/**
	 * Returns whether or not the download is paused.
	 * 
	 * @return true/false
	 */
	public boolean isDownloadPaused()
	{
		return pauseToggleButton.isSelected();
	}
	
	/**
	 * This method is called by a NntpFileDownloader object when it has finished
	 * downloading the file's data. 
	 */
	public void fileDownloadFinished(String filename)
	{
		// update total progress bar in the nzb file queue table, also the application window title
		totalBytesLoaded += filesToDownloadTabModel.getDownloadFile(filename).getTotalFileSize();
		currentNzbParser.setDownloadedBytes(totalBytesLoaded);
		int p = (int) (totalBytesLoaded * 100 / currentNzbParser.getOrigTotalSize());
		jframe.setTitle("(" + p + "%) " + winTitle);
		synchronized(nzbFileQueueTabModel)
		{
			nzbFileQueueTabModel.setRowProgress(currentNzbParser, p);
		}
	}
	
	/**
	 * This method is called by a NntpFileDownloader object when it has finished
	 * decoding the file's data. It the updates the download queues and menu
	 * items if necessary.
	 */
	public void fileDecodingFinished(String filename)
	{
		// remove according row from file download queue table
		synchronized(filesToDownloadTabModel)
		{
			for(int i = 0; i < filesToDownloadTabModel.getRowCount(); i++)
			{
				DownloadFile file = filesToDownloadTabModel.getDownloadFile(i);

				// check if an error occurred during file download
				// remove line from table if no error occurred
				if(file.getFilename().equals(filename) && !file.downloadError())
				{
					// remove from table
					disposeRightPopup();
					filesToDownloadTabModel.removeRow(i);
					currentNzbParser.removeFileAt(i);
					break;
				}
			}
		}
		
		synchronized(nzbFileQueueTabModel)
		{
			// remove first entry from nzb file list, if last seg. was downloaded
			if(filesToDownloadTabModel.getRowCount() == 0)
			{
				// par2 check?
				String pref = prefContainer.getPrefValue("DownloadSettingsPar2Check");
				if(pref.equals("true"))
					par2Check(currentNzbParser);
				else if(trayIcon != null)
				{
					// show tray icon message
					String msg = localer.getBundleText("SystemTrayMsgDownloadFinished");
					String name = "\"" + HelloNzbToolkit.getLastFilename(currentNzbParser.getName()) + "\"";
					msg = msg.replaceAll("_", name);
					trayIcon.displayMessage(null, msg, MessageType.INFO);
				}					
	
				// begin next download or disconnect globally
				disposeLeftPopup();
				nzbFileQueueTabModel.removeRow(currentNzbParser);
				startNextNzbDownload();
			}
			
			// shutdown computer after all downloads have finished
			if(shutdownToggleButton.isSelected() && nzbFileQueueTabModel.getRowCount() == 0)
			{
				if(!taskMgr.activeTask())
					shutdownNow();
			}
		}
		
		saveOpenParserData(false);
	}
	
	/**
	 * Called from background thread when par2 check is done.
	 * 
	 * @param parser The parser object that is finished being checked
	 */
	public void par2CheckDone(NzbParser parser)
	{
		// update task manager
		taskMgr.par2Done(parser);
		
		// get RAR extraction setting
		String pref = prefContainer.getPrefValue("DownloadSettingsExtractRARArchives");
		if(pref.equals("true"))
			rarExtract(parser); // start RAR archive extraction
		else if(trayIcon != null)
		{
			// show tray icon message
			String msg = localer.getBundleText("SystemTrayMsgDownloadFinished");
			String name = "\"" + HelloNzbToolkit.getLastFilename(parser.getName()) + "\"";
			msg = msg.replaceAll("_", name);
			trayIcon.displayMessage(null, msg, MessageType.INFO);
		}
	}
	
	/**
	 * Called from background thread when RAR extract is done.
	 * 
	 * @param parser The parser object that is finished being checked
	 */
	public void rarExtractDone(NzbParser parser)
	{
		// update task manager
		taskMgr.rarDone(parser);
		
		if(trayIcon != null)
		{
			// show tray icon message
			String msg = localer.getBundleText("SystemTrayMsgDownloadFinished");
			String name = "\"" + HelloNzbToolkit.getLastFilename(parser.getName()) + "\"";
			msg = msg.replaceAll("_", name);
			trayIcon.displayMessage(null, msg, MessageType.INFO);
		}
	}
	
	/**
	 * Check whether or not we should shutdown the computer.
	 * Depends on the toggle button and an empty NZB file queue.
	 * 
	 * @return either true or false
	 */
	public boolean shouldShutdown()
	{
		if(shutdownToggleButton == null || nzbFileQueueTabModel == null)
			return false;
		
		return (shutdownToggleButton.isSelected() && nzbFileQueueTabModel.getRowCount() == 0);
	}
	
	/**
	 * Called when the next nzb file in queue should be downloaded.
	 */
	public void startNextNzbDownload()
	{
		if(filesToDownloadTabModel.getRowCount() > 0)
			return;
		
		// is there at least one more nzb file to download?
		currentFileDownloader = null;
		if(nzbFileQueueTabModel.getRowCount() > 0)
		{
			loadNextNzbFile();
			startDownload();
		}
		else
		{
			globalDisconnect(false);
			updStatusBar(0);

			AbstractAction action = actions.get("MenuServerStartDownload");
			action.setEnabled(false);
			action = actions.get("MenuServerPauseDownload");
			action.setEnabled(false);
			startToggleButton.setSelected(false);
			pauseToggleButton.setSelected(false);
			jframe.setTitle(winTitle);
			
			// call garbage collector
			Runtime.getRuntime().gc();
		}
	}

	/**
	 * Remove the specified row from the nzb queue.
	 * 
	 * @param parser The parser object of the row to remove
	 */
	public void removeRowFromNzbQueue(NzbParser parser)
	{
		nzbFileQueueTabModel.removeRow(parser);
		saveOpenParserData(true);
	}
	
	/**
	 * This method is called when a background file downloader thread
	 * has finished file decoding and started writing file data to hard
	 * disk. 
	 */
	public void fileWritingStarted(String filename)
	{
		synchronized(filesToDownloadTabModel)
		{
			filesToDownloadTabModel.setRowToWriting(filename);
		}
	}
	
	/**
	 * Update the download file  (for progress bar redrawing).
	 * 
	 * @param filename The name of the download file row to update
	 * @param value The absolute value of the progress bar to set
	 */
	public void updateDownloadQueue(String filename, int value)
	{
		// update download file progress bar
		synchronized(filesToDownloadTabModel)
		{
			filesToDownloadTabModel.setValueAt(value, filename, 3);
		}
	}
	
	/**
	 * Decrease the segment counter of this file by one.
	 * 
	 * @param filename The name of the download file row to update
	 */
	public void decrSegCount(String filename)
	{
		synchronized(filesToDownloadTabModel)
		{
			filesToDownloadTabModel.decrSegCount(filename);
		}
	}
	
	/**
	 * This method sets the progress bar of the row identified by its filename
	 * to the "decoding" status, including the maximum value of the progress bar.
	 * 
	 * @param filename The name of the download file row to update
	 * @param value The new maximum value of the progress bar
	 */
	public void setProgBarToDecoding(String filename, int value)
	{
		synchronized(filesToDownloadTabModel)
		{
			filesToDownloadTabModel.setRowToDecoding(filename, value);
		}
	}
	
	/**
	 * Called when a NNTP client thread catches an IO exception.
	 */
	public void nntpConnectIoException()
	{
		// show error popup
		String msg = localer.getBundleText("PopupSocketError"); 
		String title = localer.getBundleText("PopupErrorTitle");
		JOptionPane.showMessageDialog(jframe, msg, title, JOptionPane.ERROR_MESSAGE);
	}
	
	/**
	 * Called when a NNTP client thread could not find the article to download.
	 * 
	 * @param filename The filename of the file missing the article
	 */
	public void articleNotFound(String filename)
	{
		synchronized(filesToDownloadTabModel)
		{
			// set the according row to "article not found" state
			filesToDownloadTabModel.setArticleNotFound(filename);
		}
	}
	
	/**
	 * Called when a NNTP client thread receives a malformed server reply.
	 * 
	 * @param filename The filename of the file missing the article
	 */
	public void malformedServerReply(String filename)
	{
		synchronized(filesToDownloadTabModel)
		{
			// set the according row to "malformed server reply" state
			filesToDownloadTabModel.setMalformedServerReply(filename);
		}
	}
	
	/**
	 * Called when a NNTP client thread encounters a crc32 error during data decoding.
	 * 
	 * @param filename The filename of the file encountering the crc32 error
	 */
	public void crc32Error(String filename)
	{
		synchronized(filesToDownloadTabModel)
		{
			// set the according row to "crc32 error" state
			filesToDownloadTabModel.setCrc32Error(filename);
		}
	}
	
	/**
	 * Called when a NNTP client thread has not found any suitable decoder..
	 * 
	 * @param filename The filename of the file encountering the error
	 */
	public void noDecoderFound(String filename)
	{
		synchronized(filesToDownloadTabModel)
		{
			// set the according row to "no decoder found" state
			filesToDownloadTabModel.setNoDecoderFound(filename);
		}
	}
		
	/**
	 * Called when a file that has been downloaded is corrupted.
	 */
	public void downloadDataCorrupted()
	{
		// show error popup
		String msg = localer.getBundleText("PopupDownloadDataCorrupted"); 
		String title = localer.getBundleText("PopupErrorTitle");
		JOptionPane.showMessageDialog(jframe, msg, title, JOptionPane.ERROR_MESSAGE);
	}
	
	/**
	 * Clear all selections on the NZB file queue table.
	 */
	public void clearNzbQueueSelection()
	{
		nzbListTab.clearSelection();
	}
	
	/**
	 * This method is called when the user wants to move one or more row(s)
	 * in the NZB file queue table up or down, via the according context menu.
	 * 
	 * @param selectedRows The row(s) to move
	 * @param direction The direction to move the row(s), up or down
	 */
	public void moveRowsInNzbQueue(int [] selectedRows, NzbFileListPopupMoveRowAction.MoveDirection direction)
	{
		if(selectedRows.length == 0)
			return;
		
		// now move row(s) and compare first row before and after operation
		NzbParser pBefore = nzbFileQueueTabModel.getNzbParser(0);
		nzbFileQueueTabModel.moveRows(selectedRows, direction);
		NzbParser pAfter = nzbFileQueueTabModel.getNzbParser(0);
		if(pBefore != pAfter)
			nzbQueueReordered(pAfter);
	}
	
	/**
	 * (Re-)start downloading with the given parser object.
	 */
	public void nzbQueueReordered(NzbParser parser)
	{
		filesToDownloadTabModel.clearTableData();
		filesToDownloadTabModel.addNzbParserContents(parser);
		currentNzbParser = parser;
		totalBytesLoaded = currentNzbParser.getDownloadedBytes();
		super.jframe.setTitle(winTitle);
	}
	
	/**
	 * Called when the user has clicked on the "remove row" menu item
	 * in context popup menu of the nzb files table.
	 * 
	 * @param row The row number to delete (zero-based)
	 * @param delLocalData When true then also delete local data on disk
	 */
	public void removeNzbFileQueueRow(int row, boolean delLocalData)
	{
		if(row < 0 || row > (nzbFileQueueTabModel.getRowCount() - 1))
			return;

		if(row == 0)
			jframe.setTitle(winTitle);
		
		String dirname = nzbFileQueueTabModel.getNzbParser(row).getName();
		synchronized(filesToDownloadTabModel)
		{
			// remove row from nzb file queue table
			nzbFileQueueTabModel.removeRow(row);
			if(row == 0)
			{
				// also remove according download files from right table
				filesToDownloadTabModel.clearTableData();
				loadNextNzbFile();
			}
		}
		
		// delete local content
		if(delLocalData)
		{
			String rootDir = prefContainer.getPrefValue("GeneralSettingsDownloadDir");
			dirname = HelloNzbToolkit.getLastFilename(dirname);
			File downloadDir = new File(rootDir + File.separator + dirname);
			if(downloadDir.isDirectory())
				HelloNzbToolkit.deleteNonEmptyDir(downloadDir);
		}
		
		// disable toolbar and menu action
		if(nzbFileQueueTabModel.getRowCount() == 0)
		{
			AbstractAction action = actions.get("MenuServerStartDownload");
			action.setEnabled(false);
			action = actions.get("MenuServerPauseDownload");
			action.setEnabled(false);
		}
		else
			saveOpenParserData(true);
	}
	
	/**
	 * Called when the user has clicked on the "remove row" menu item
	 * in context popup menu of the files to download table.
	 * 
	 * @param row The row number to delete (zero-based)
	 */
	public void removeDownloadFileQueueRow(String name)
	{
		synchronized(filesToDownloadTabModel)
		{
			int row = -1;
			
			if(!filesToDownloadTabModel.containsFilename(name))
				return;
			else
				row = filesToDownloadTabModel.getRowByFilename(name);
			
			// subtract the amount of bytes of the removed file from 
			// the total nzb file size progress bar on the left side
			totalBytesLoaded += filesToDownloadTabModel.getDownloadFile(row).getTotalFileSize();
			int p = (int) (totalBytesLoaded * 100 / currentNzbParser.getOrigTotalSize());
			nzbFileQueueTabModel.setRowProgress(currentNzbParser, p);
		
			// remove item from download file queue
			filesToDownloadTabModel.removeRow(row);
			currentNzbParser.removeFileAt(row);
			
			// if this was the last item in download queue, then load the next nzb file 
			if(filesToDownloadTabModel.getRowCount() == 0)
				removeNzbFileQueueRow(0, false);
		}
	}

	/**
	 * Returns the file name of the given row in the download file queue.
	 * 
	 * @param row Get the name of this row (zero-based)
	 * @return The file name of the given row, or null if this row was not found
	 */
	public String getDownloadFileName(int row)
	{
		String name = null;
		
		synchronized(filesToDownloadTabModel)
		{
			if(row < 0 || row >= filesToDownloadTabModel.getRowCount())
				return null;
			
			name = filesToDownloadTabModel.getDownloadFile(row).getFilename();
		}
		
		return name;
	}
	
	/**
	 * Returns the error status of a download file in a specific row.
	 * 
	 * @param row The row in the table
	 * @return error yes/no
	 */
	public boolean errorAtDownloadFile(int row)
	{
		return filesToDownloadTabModel.getDownloadFile(row).downloadError();
	}
		
	/**
	 * If set via program config, do a par2 check and automatically
	 * repair if necessary.
	 * 
	 * @param parser The parser object to check
	 */
	private void par2Check(NzbParser parser)
	{
		// wait until all download files are finished
		if(filesToDownloadTabModel.getRowCount() > 1)
			return;
		
		// do par2 check
		taskMgr.par2Check(parser);
		Par2Check par2 = new Par2Check(this, parser);
		Thread t = new Thread(par2);
		t.start();
	}
	
	/**
	 * If set via program config, do a RAR archive extraction after download.
	 * 
	 * @param parser The parser object to use
	 */
	private void rarExtract(NzbParser parser)
	{
		// do rar archive extracr
		taskMgr.rarExtract(parser);
		RarExtractor unrar = new RarExtractor(this, parser);
		Thread t = new Thread(unrar);
		t.start();
	}

	/**
	 * Reset the thread view and the status bar.
	 */
	public void resetThreadView()
	{
		super.resetThreadView();
		updStatusBar(0);
	}
	
	/**
	 * Returns the currently set bps value (on status bar).
	 * 
	 * @return Last bps value
	 */
	public long lastBpsValue()
	{
		if(nioClient == null)
			return 0;
		else
			return nioClient.getDlTraffic();
	}
	
	public void activateUsageStats()
	{
		boolean flag = prefContainer.getBooleanPrefValue("UsageStatsSettingsActivate");
		HelloNzbUsageStats.setActive(flag);
	}
	
	
	// used at application shutdown
	static class ShutdownHook extends Thread 
	{
		public void run() 
		{
			unlockFile();
		}
	}
}

































