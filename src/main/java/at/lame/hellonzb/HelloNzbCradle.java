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
import at.lame.hellonzb.renderer.*;
import at.lame.hellonzb.tablemodels.*;
import at.lame.hellonzb.util.*;

import java.awt.*;
import java.io.*;
import java.net.URL;
import java.util.*;

import javax.swing.*;
import javax.swing.table.*;
import javax.xml.stream.XMLStreamException;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.layout.*;


/**
 * This is the base class of the main application class HelloNzb.
 * It contains private/protected members and methods for this main
 * application class. These are mainly "background" and low-level
 * functionalities.
 * 
 * @author Matthias F. Brandstetter
 * @see at.lame.hellonzb.HelloNzb
 */
public abstract class HelloNzbCradle implements HelloNzbConstants
{
	/** The main JFrame object of the application window */
	protected JFrame jframe;
	
	/** The menu bar object */
	protected JMenuBar menuBar;
	
	/** The tool bar object */
	protected JToolBar toolBar;
	
	/** download speed history graph */
	protected SpeedGraphPanel speedGraph;
	
	/** "start" toggle button on toolbar */
	protected JToggleButton startToggleButton;
	
	/** "pause" toggle button on toolbar */
	protected JToggleButton pauseToggleButton;
	
	/** "shutdown" toggle button on toolbar */
	protected JToggleButton shutdownToggleButton;
	
	/** Menu and toolbar items (actions) */
	protected HashMap<String,AbstractAction> actions;
	
	/** A JPanel representing the status bar of the window */
	protected JPanel statusBarPanel;
	
	/** A text element for the status bar text */
	protected JLabel statusBarText;
	
	/** download speed label */
	protected JLabel currDlSpeed;
	
	/** limit download speed button */
	protected JButton limitDlSpeedButton;
		
	/** A text element for the ETA and total file size label on the status bar */
	protected JLabel etaAndTotalText;
	
	/** The list of nzb's to process on the left side of the main split pane */
	protected JTable nzbListTab;
	
	/** A list of all connections in main window */
	protected JTable threadViewTab;
	
	/** left/right split pane */
	protected JSplitPane lrSplitPane;
	
	/** up/down split pane */
	protected JSplitPane udSplitPane;
	
	/** The table model for the left JTable */
	protected NzbFileQueueTableModel nzbFileQueueTabModel;
	
	/** The list of files to download for the selected nzb in the left nzb list */
	protected JTable filesToDownloadTab;
	
	/** NIO client */
	protected NettyNioClient nioClient;
	
	/** A pointer to the file downloader currently active */
	protected NntpFileDownloader currentFileDownloader;
	
	/** A pointer to the nzb parser currently active */
	protected NzbParser currentNzbParser;
	
	/** the total amount of bytes loaded for the current file download */
	protected long totalBytesLoaded;
	
	/** The table model for the right JTable */
	protected FilesToDownloadTableModel filesToDownloadTabModel;
	
	/** Application preferences container (incl. preferences dialog) */
	protected HelloNzbPreferences prefContainer;
	
	/** background worker for misc. background tasks */
	protected BackgroundWorker bWorker;
	
	/** task manager responsible for controlling the background progress bar */
	protected TaskManager taskMgr;
	
	/** last known count of active threads */
	protected int lastActThreadCount;

	/** String localer */
	protected StringLocaler localer;
	
	/** cental logger object */
	protected MyLogger logger;
	
	/** logging window */
	protected LoggingWindow logWnd;
	
	/** the system tray icon object */
	protected TrayIcon trayIcon;
		
	/** if set to true then we can start another parser content saver */
	protected Boolean contentSaverDone;
	
	/** popup window of the NZB file queue */
	protected JPopupMenu leftPopup;
	
	/** popup window of the download file queue */
	protected JPopupMenu rightPopup;
	
	
	/** Class constructor. */
	public HelloNzbCradle(JFrame frame, String title)
	{
		this.jframe = frame;

		// String localer and JVM default locale
		this.localer = new StringLocaler();
		Locale.setDefault(localer.getLocale());
		JOptionPane.setDefaultLocale(localer.getLocale());
		JFileChooser.setDefaultLocale(localer.getLocale());
		
		// create logging window and logger
		logWnd = new LoggingWindow(localer, frame);
		this.logger = MyLogger.getInstance(this, logWnd.getTextArea());
		if(DEBUG)
			logger.setPrintLevel(MyLogger.SEV_DEBUG);
		
		// set application logo/icon
		URL url = ClassLoader.getSystemResource("resources/icons/HelloNzb_logo.png");
		Toolkit kit = Toolkit.getDefaultToolkit();
		Image img = kit.createImage(url);
		this.jframe.setIconImage(img);

		// set the (default) settings for the main JFrame
		this.jframe.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.jframe.setSize(MAINWIN_WIDTH, MAINWIN_HEIGHT);
		this.jframe.setLocationRelativeTo(null);
		this.jframe.setLayout(new BorderLayout());
		
		// create data for nzb file queue and file download queue
		this.nzbFileQueueTabModel = new NzbFileQueueTableModel(localer);
		this.filesToDownloadTabModel = new FilesToDownloadTableModel(localer);

		// dynamically set the widgets while window size is changed
		Toolkit.getDefaultToolkit().setDynamicLayout(true);
		
		// some default values
		this.currentFileDownloader = null;
		this.currentNzbParser = null;
		this.lastActThreadCount = 0;
		this.totalBytesLoaded = 0;
		this.contentSaverDone = true;
		this.leftPopup = null; 
		this.rightPopup = null; 
	}
	
	/**
	 * Initialise the system tray icon.
	 */
	protected void initSystemTray(AbstractAction quitAction)
	{
		// system tray activate in preferences?
		boolean isActive = prefContainer.getBooleanPrefValue("GeneralSettingsShowTrayIcon");
		if(!isActive)
		{
			trayIcon = null;
			return;
		}
		
		// system tray supported by OS?
		if(!SystemTray.isSupported())
		{
			trayIcon = null;
			return;
		}
		
		URL url = ClassLoader.getSystemResource("resources/icons/HelloNzb_logo.png");
		Image img = Toolkit.getDefaultToolkit().createImage(url);
		trayIcon = new MyTrayIcon(this, img, "HelloNzb -- The Binary Usenet Tool", actions);
		
		SystemTray tray = SystemTray.getSystemTray();
		try
		{
			tray.add(trayIcon);
		}
		catch(Exception ex) 
		{
			logger.printStackTrace(ex);
			trayIcon = null;
		}
	}
	
	/**
	 * Used by the constructor to create the content panes on the main window.
	 * 
	 * These are:
	 *   - headerPanel (NORTH)
	 *   - splitPane (CENTER, local object in this method)
	 *   - statusBarPanel (SOUTH)
	 *   
	 * The split pane in the CENTER of the window contains two scrollable panes:
	 *   - nzbList (LEFT, list of all nzb's, the queue of nzb's to download)
	 *   - filesToDownload (RIGHT, list of all files within the selected nzb)
	 *   
	 * @param progBar The previously create JProgressBar object
	 */
	protected QuitAction addContentPanes(JProgressBar progBar)
	{
		// create spacer panels
		jframe.add(new JPanel(), BorderLayout.WEST);
		jframe.add(new JPanel(), BorderLayout.EAST);
	
		// create menu and tool bars
		actions = new HashMap<String,AbstractAction>();
		QuitAction quitAction = createMenuAndToolBars();

		// header panel -- icons and speed graph
		JPanel headerPanel = new JPanel();
		headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.LINE_AXIS));
		headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 12));
		headerPanel.add(toolBar);
		headerPanel.add(Box.createHorizontalGlue());
		
		// text area and button left of the speed graph
		JPanel tmpPanel = new JPanel();
		tmpPanel.setLayout(new BoxLayout(tmpPanel, BoxLayout.PAGE_AXIS));
		limitDlSpeedButton = new JButton();
		limitDlSpeedButton.setAlignmentX(Component.RIGHT_ALIGNMENT);
		limitDlSpeedButton.setAction(actions.get("MenuHelloNzbSpeedLimit"));
		limitDlSpeedButton.setToolTipText(localer.getBundleText("MenuHelloNzbSpeedLimit"));
		setConnSpeedLimit();
		currDlSpeed = new JLabel(HelloNzbToolkit.prettyPrintBps(0));
		currDlSpeed.setAlignmentX(Component.RIGHT_ALIGNMENT);
		tmpPanel.add(currDlSpeed);
		tmpPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		tmpPanel.add(limitDlSpeedButton);
		tmpPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
		headerPanel.add(tmpPanel);
		
		// speed graph
		speedGraph = new SpeedGraphPanel(200);
		headerPanel.add(speedGraph);
		jframe.add(headerPanel, BorderLayout.NORTH);

		// create left and right tables
		createDataTables();

		// restore window size
		String savedLrDivider = this.prefContainer.getPrefValue("AutoSettingsLrDivider");
		String savedUdDivider = this.prefContainer.getPrefValue("AutoSettingsUdDivider");

		// create a split pane for the download queues (left/right)
		this.lrSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, 
				new JScrollPane(nzbListTab), new JScrollPane(filesToDownloadTab));
		lrSplitPane.setContinuousLayout(true);
		if(savedLrDivider.isEmpty())
			lrSplitPane.setDividerLocation((int) (jframe.getWidth() / 3));
		else
			lrSplitPane.setDividerLocation(Integer.valueOf(savedLrDivider));
		
		// create a scroll pane for the thread view
		JScrollPane threadView = createThreadViewPane();
		
		// create a split pane for the main split pane and the thread view (up/down)
		this.udSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, lrSplitPane, threadView);
		udSplitPane.setContinuousLayout(true);
		if(savedUdDivider.isEmpty())
			udSplitPane.setDividerLocation((int) (jframe.getHeight() * 0.5));
		else
			udSplitPane.setDividerLocation(Integer.valueOf(savedUdDivider));

		jframe.add(udSplitPane, BorderLayout.CENTER);
		
		// create status bar
		statusBarPanel = createStatusBar(progBar);
		jframe.add(statusBarPanel, BorderLayout.SOUTH);
		
		// return QuitAction object to use for the window listener
		return quitAction;
	}
	
	/**
	 * Create the status bar inclusive all contents.
	 * 
	 * @param progBar The previously create JProgressBar object
	 * @return The newly created JPanel object
	 */
	protected JPanel createStatusBar(JProgressBar progBar)
	{
		// create layout for this tab/panel
        FormLayout layout = new FormLayout(
                "pref:grow, 10dlu, pref, 10dlu, [100dlu,pref,150dlu]", // cols
				"p");	// rows
        
        // create builder
        PanelBuilder builder = new PanelBuilder(layout);
        builder.setDefaultDialogBorder();
        CellConstraints cc = new CellConstraints();		
		
        // status bar text field
		statusBarText = new JLabel(localer.getBundleText("StatusBarRunningThreads") + " 0");
		builder.add(statusBarText, cc.xy(1, 1));
		
		// ETA & total file size text field
		etaAndTotalText = new JLabel("");
		builder.add(etaAndTotalText, cc.xy(3, 1));
		etaAndTotalText.setToolTipText(
				localer.getBundleText("StatusBarEtaAndTotalTooltip"));

		// create background task progress bar in status bar
		builder.add(progBar, cc.xy(5, 1));
		
		return builder.getPanel();
	}
	
	/**
	 * Creates the table for the thread view and adds it to a scroll pane.
	 * 
	 * @return The newly created scroll pane
	 */
	protected JScrollPane createThreadViewPane()
	{
		// create table data and table object
		int threadcount = 1;
		String tmp = prefContainer.getPrefValue("ServerSettingsThreadCount");
		if(tmp.length() > 0)
			threadcount = Integer.valueOf(tmp);
		ThreadViewTableModel tm = new ThreadViewTableModel(localer);
		tm.setRowCount(threadcount);
		JTable table = new JTable(tm);		
		
		// set center alignment for the contents of the first colunn
		DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer();
		cellRenderer.setHorizontalAlignment(SwingConstants.CENTER);
		table.getColumnModel().getColumn(0).setCellRenderer(cellRenderer);
		
		// set header renderers
		table.getColumnModel().getColumn(0).setHeaderRenderer(
				new AlignedTableHeaderRenderer(SwingConstants.CENTER));
		table.getColumnModel().getColumn(1).setHeaderRenderer(
				new AlignedTableHeaderRenderer(SwingConstants.LEFT));

		// misc. table settings
		table.setRowSelectionAllowed(false);
		table.setColumnSelectionAllowed(false);
		table.setCellSelectionEnabled(false);
		table.setDragEnabled(false);
		table.getTableHeader().setReorderingAllowed(false);
		
		TableColumn column = table.getColumnModel().getColumn(1);
		column.setPreferredWidth((int) (jframe.getWidth() * 0.66));
		
		// set table header height
		table.getTableHeader().setPreferredSize(
				new Dimension(table.getColumnModel().getTotalColumnWidth(), 20));
		
		this.threadViewTab = table;
		
		return new JScrollPane(table);
	}
	
	/**
	 * Used by the constructor to create the menu bar for the main window.
	 */
	protected QuitAction createMenuAndToolBars()
	{
		menuBar = new JMenuBar();
		toolBar = new JToolBar();
		Icon icon = null;
		
		toolBar.setFloatable(false);
		
		/////////////////////////////////////////////////////////////////////
		// create "HelloNzb" menu (main menu, like "File")
		JMenu helloNzbMenu = new JMenu(localer.getBundleText("MenuHelloNzb"));
		menuBar.add(helloNzbMenu); 
		
		// open nzb file action
		icon = new ImageIcon(HelloNzb.class.getResource("/resources/icons/open_nzb.png"));
		OpenNzbFileAction openNzbFileAction = new OpenNzbFileAction(this, icon, "MenuHelloNzbOpenNzbFile");
		helloNzbMenu.add(openNzbFileAction);
		actions.put("MenuHelloNzbOpenNzbFile", openNzbFileAction);
		
		// preferences popup
		icon = new ImageIcon(HelloNzb.class.getResource("/resources/icons/preferences.png"));
		PrefAction prefAction = new PrefAction(this, icon, "MenuHelloNzbPref");
		helloNzbMenu.add(prefAction);
		actions.put("MenuHelloNzbPref", prefAction);
		
		helloNzbMenu.addSeparator();
		
		// shutdown computer after download has finished
		icon = new ImageIcon(HelloNzb.class.getResource("/resources/icons/shutdown.png"));
		ShutdownAction shutdownAction = new ShutdownAction(this, icon, "MenuHelloNzbShutdown");
		actions.put("MenuHelloNzbShutdown", shutdownAction);

		// check for program updates
		icon = null;
		CheckForUpdateAction updateAction = new CheckForUpdateAction(this, icon, "MenuHelloNzbUpdate");
		helloNzbMenu.add(updateAction);
		actions.put("MenuHelloNzbUpdate", updateAction);
		
		// show program log
		icon = null;
		ShowLoggingWindowAction showLogAction = new ShowLoggingWindowAction(this, icon, "MenuHelloNzbShowLog", logWnd);
		helloNzbMenu.add(showLogAction);
		actions.put("MenuHelloNzbShowLog", updateAction);
		
		helloNzbMenu.addSeparator();
		
		// quit application
		icon = new ImageIcon(HelloNzb.class.getResource("/resources/icons/quit.png"));
		QuitAction quitAction = new QuitAction(this, icon, "MenuHelloNzbQuit", bWorker, taskMgr);
		helloNzbMenu.add(quitAction);
		actions.put("MenuHelloNzbQuit", quitAction);
		
		/////////////////////////////////////////////////////////////////////
		// create "Server" menu (connect, disconnect, ...)
		JMenu serverMenu = new JMenu(localer.getBundleText("MenuServer"));
		menuBar.add(serverMenu);
		
		// start download
		icon = new ImageIcon(HelloNzb.class.getResource("/resources/icons/start_download.png"));
		StartDownloadAction startDownloadAction = 
			new StartDownloadAction(this, icon, "MenuServerStartDownload");
		actions.put("MenuServerStartDownload", startDownloadAction);
		
		// pause download
		icon = new ImageIcon(HelloNzb.class.getResource("/resources/icons/pause_download.png"));
		PauseDownloadAction pauseDownloadAction = 
			new PauseDownloadAction(this, icon, "MenuServerPauseDownload");
		actions.put("MenuServerPauseDownload", pauseDownloadAction);
		
		// test server connection
		icon = null;
		ConnectAction connectAction = new ConnectAction(this, icon, "MenuServerConnect");
		serverMenu.add(connectAction);
		actions.put("MenuServerConnect", connectAction);
		
		/////////////////////////////////////////////////////////////////////
		// create "Help" menu (help, about, ...)
		JMenu helpMenu = new JMenu(localer.getBundleText("MenuHelp"));
		menuBar.add(helpMenu);
		
		// report a bug (open browser window)s
		icon = null;
		ReportBugAction reportBugAction = new ReportBugAction(this, icon, "MenuHelpReportBug");
		helpMenu.add(reportBugAction);
		actions.put("MenuHelpReportBug", reportBugAction);
		
		// about HelloNzb
		icon = null;
		AboutHelloNzbAction aboutAction = new AboutHelloNzbAction(this, icon, "MenuHelpAbout");
		helpMenu.add(aboutAction);
		actions.put("MenuHelpAbout", aboutAction);
		
		// set the new menu bar to the main window
		jframe.setJMenuBar(menuBar);

		/////////////////////////////////////////////////////////////////////
		// create tool bar
		startToggleButton = new JToggleButton(startDownloadAction);
		startToggleButton.setText(null);
		pauseToggleButton = new JToggleButton(pauseDownloadAction);
		pauseToggleButton.setText(null);
		shutdownToggleButton = new JToggleButton(shutdownAction);
		shutdownToggleButton.setText(null);
		toolBar.setFocusable(false);
		toolBar.add(openNzbFileAction);
		toolBar.add(startToggleButton);
		toolBar.add(pauseToggleButton);
		toolBar.addSeparator();
		toolBar.add(prefAction);
		toolBar.addSeparator();
		toolBar.add(shutdownToggleButton);
		toolBar.addSeparator();
		toolBar.add(quitAction);

		// speed limit popup
		SpeedLimitAction speedLimitAction = new SpeedLimitAction(this, icon, "MenuHelloNzbSpeedLimit");
		actions.put("MenuHelloNzbSpeedLimit", speedLimitAction);
		
		startDownloadAction.setEnabled(false);
		pauseDownloadAction.setEnabled(false);
		
		// return QuitAction object to use for the window listener
		return quitAction;
	}
	
	/**
	 * This method creates the data vectors for the left and right tables
	 * (left = nzb files, right = download file queue).
	 */
	protected void createDataTables()
	{
		DownloadFileListPopupListener dlFileListener = null;
		
		// create left JTable (nzb files)
		nzbListTab = new JTable(nzbFileQueueTabModel);
		nzbListTab.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		nzbListTab.getTableHeader().setReorderingAllowed(false);
		nzbListTab.addMouseListener(new NzbFileListPopupListener(this, nzbListTab));
		nzbListTab.setDragEnabled(true);
		nzbListTab.setDropMode(DropMode.INSERT_ROWS);
		nzbListTab.setTransferHandler(new TableRowTransferHandler(nzbListTab, this));
		
		// set table cell renderers (left)
		ProgressRenderer cellRenderer = new ProgressRenderer(true);
		nzbListTab.getColumnModel().getColumn(0).setCellRenderer(cellRenderer);
		
		// set table header renderers (left)
		nzbListTab.getColumnModel().getColumn(0).setHeaderRenderer(
				new AlignedTableHeaderRenderer(SwingConstants.CENTER));
		
		// create right JTable (files to download)
		filesToDownloadTab = new JTable(filesToDownloadTabModel);
		filesToDownloadTab.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		dlFileListener = new DownloadFileListPopupListener(this, filesToDownloadTab);
		filesToDownloadTab.addMouseListener(dlFileListener);
		filesToDownloadTab.getSelectionModel().addListSelectionListener(dlFileListener);
		filesToDownloadTab.getColumnModel().getColumn(0).setMinWidth(333);
		filesToDownloadTab.getColumnModel().getColumn(1).setMinWidth(50);
		filesToDownloadTab.getColumnModel().getColumn(2).setMinWidth(50);
		filesToDownloadTab.getColumnModel().getColumn(3).setMinWidth(50);
		filesToDownloadTab.getTableHeader().setReorderingAllowed(false);

		// set table cell renderers (right)
		filesToDownloadTab.getColumnModel().getColumn(0).setCellRenderer(new FilenameRenderer());
		filesToDownloadTab.getColumnModel().getColumn(1).setCellRenderer(new FilesizeRenderer());
		filesToDownloadTab.getColumnModel().getColumn(2).setCellRenderer(new SegCountRenderer());
		filesToDownloadTab.getColumnModel().getColumn(3).setCellRenderer(new ProgressRenderer(false));
		
		// set table header renderers (right)
		filesToDownloadTab.getColumnModel().getColumn(0).setHeaderRenderer(
				new AlignedTableHeaderRenderer(SwingConstants.CENTER));
		filesToDownloadTab.getColumnModel().getColumn(1).setHeaderRenderer(
				new AlignedTableHeaderRenderer(SwingConstants.CENTER));
		filesToDownloadTab.getColumnModel().getColumn(2).setHeaderRenderer(
				new AlignedTableHeaderRenderer(SwingConstants.CENTER));
		filesToDownloadTab.getColumnModel().getColumn(3).setHeaderRenderer(
				new AlignedTableHeaderRenderer(SwingConstants.CENTER));

		// set table header height
		nzbListTab.getTableHeader().setPreferredSize(
				new Dimension(nzbListTab.getColumnModel().getTotalColumnWidth(), 20));
		filesToDownloadTab.getTableHeader().setPreferredSize(
				new Dimension(filesToDownloadTab.getColumnModel().getTotalColumnWidth(), 20));
	}

	/**
	 * Set the status bar to a "Running threads: <count>" message.
	 * 
	 * @param threads The count of running download threads
	 */
	public void updStatusBar(int threads)
	{
		if(pauseToggleButton.isSelected())
			return;
		if(!startToggleButton.isSelected() && threads >= lastActThreadCount)
			return;
		
		String text = localer.getBundleText("StatusBarRunningThreads") + " " + threads;
		String tc = prefContainer.getPrefValue("ServerSettingsThreadCount");

		final String statusText = text + "/" + tc;
		lastActThreadCount = threads;

        SwingUtilities.invokeLater(new Runnable() 
        { 
        	public void run()
        	{
        		statusBarText.setText(statusText);
			} 
		} );
	}

	/**
	 * This method is called if either
	 *   a) the last file in download queue has been downloaded, or
	 *   b) the user has manually removed the first entry in the nzb file queue.
	 *   
	 * It reads the next item in the nzb file queue, retrieves all items from
	 * it and updates the download file queue.
	 */
	protected void loadNextNzbFile()
	{
		if(nzbFileQueueTabModel.getRowCount() > 0)
		{
			// start to download first file of the next nzb file in queue
			NzbParser parser = nzbFileQueueTabModel.getNzbParser(0);
			if(parser != null)
			{
				filesToDownloadTabModel.addNzbParserContents(parser);
				currentNzbParser = parser;
			}
		}
	}
	
	/**
	 * This method adds the content of a nzb file (i.e. a NzbParser object)
	 * to the download queue. It first checks if another nzb file with that
	 * name already exists in the nzb queue.
	 * 
	 * @param parser The NzbParser object to add
	 */
	public void addNzbToQueue(NzbParser parser)
	{
		// check if an nzb file with this name already exists in queue
		if(nzbFileQueueTabModel.containsNzb(parser))
		{
			String title = localer.getBundleText("PopupErrorTitle");
			String msg = localer.getBundleText("PopupNzbFilenameAlreadyInQueue");
			JOptionPane.showMessageDialog(null, msg, title, JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		// no, so add the new nzb file to the queue
		nzbFileQueueTabModel.addRow(parser);
		
		if(filesToDownloadTabModel.getRowCount() == 0)
		{
			filesToDownloadTabModel.addNzbParserContents(parser);
			currentNzbParser = parser;
			totalBytesLoaded = currentNzbParser.getDownloadedBytes();
		}
		
		logger.msg("Added content of " + parser.getName() + ".nzb to queue", MyLogger.SEV_INFO);

		// enable toolbar and menu action
		AbstractAction action = actions.get("MenuServerStartDownload");
		action.setEnabled(true);
	}
	
	/**
	 * Load the last session (NZB files that were open).
	 */
	protected void loadLastSession()
	{
		// does data directory exists?
		String datadirPath = System.getProperty("user.home") + "/.HelloNzb/";
		File datadir = new File(datadirPath);
		
		if(!datadir.isDirectory() || !datadir.canRead() || !datadir.canExecute())
		{
			logger.msg("Could not load last session, specified folder is no directory or non-readable", MyLogger.SEV_WARNING);
			return;
		}
				
		// set background status bar
		taskMgr.loadSession(true);
				
		// directory exists, so load all nzb files from it
		File [] files = datadir.listFiles();
		File tmpFile = null;
		for(File file : files)
		{
			try
			{
				// directory
				if(file.isDirectory())
					continue;
				
				// nzb file?
				String filename = file.getCanonicalPath();
				if(!filename.substring(filename.length() - 4, filename.length()).equalsIgnoreCase(".nzb"))
				{
					if(file.canWrite())
					{
						logger.msg("Deleting non-NZB file " + file.getName(), MyLogger.SEV_INFO);
						file.delete();
					}
					continue;
				}
				
				if(!filename.contains("-"))
				{
					if(file.canWrite())
					{
						logger.msg("Deleting NZB file without '-' (" + file.getName() + ")", MyLogger.SEV_INFO);
						file.delete();
					}
					continue;
				}

				// yes, so remove index part of the filename (e.g. "1-filename.nzb")
				while(!filename.startsWith("-"))
					filename = filename.substring(1, filename.length());
				filename = filename.substring(1, filename.length());
				
				// tmp. rename original source file
				tmpFile = new File(datadirPath + filename);
				file.renameTo(tmpFile);
				
				addNzbToQueue(new NzbParser(this, tmpFile.getAbsolutePath()));
			}
			catch(Exception e)
			{
				logger.printStackTrace(e);
			}
			finally
			{
				if(tmpFile != null && tmpFile.canWrite())
					tmpFile.delete();
				if(file != null && file.canWrite())
					file.delete();
			}
		}
		
		// unset background status bar
		taskMgr.loadSession(false);
	}
	
	/**
	 * Update the status of one connection in the thread view table.
	 * 
	 * @param conn The connection number to set (counted from 0)
	 * @param text The new text to set
	 */
	public void updThreadView(int conn, String text)
	{
		synchronized(threadViewTab)
		{
			threadViewTab.setValueAt(text, conn, 1);
		}
	}
	
	/**
	 * Called to save the currently open and unused parser data.
	 * 
	 * @param wait If true then wait until saving to disk has finished.
	 */
	public void saveOpenParserData(boolean wait)
	{
		synchronized(contentSaverDone)
		{
			if(contentSaverDone)
			{
				contentSaverDone = false;			
				new ParserContentSaver(this, nzbFileQueueTabModel.copyQueue(), filesToDownloadTabModel.getDownloadFileVector()).start();
			}
		}
		
		if(wait)
		{
			while(!contentSaverDone)
			{
				try
				{
					Thread.sleep(100);
				}
				catch(InterruptedException ex) {}
			}
		}
	}

	/**
	 * Sets the contentSaverDone flag to true.
	 */
	public void contentSaverDone()
	{
		synchronized(contentSaverDone)
		{
			contentSaverDone = true;
		}
	}
	
	/**
	 * Return the current contentSaverDone value (either true or false).
	 */
	public boolean isContentSaverDone()
	{
		boolean ret;
		
		synchronized(contentSaverDone)
		{
			ret = contentSaverDone;
		}
		
		return ret;
	}
	
	/**
	 * Load the specified nzb file (passed via command line at application call).
	 * 
	 * @param filename The file to load, quit if null
	 */
	protected void loadFileFromCmdLine(String filename)
	{
		// filename passed at command line?
		if(filename == null)
			return;
		
		// set background status bar
		taskMgr.loadNzb(true);

		try
		{
			File file = new File(filename);
			addNzbToQueue(new NzbParser(this, filename));
			file.delete();
		}
		catch(IOException e)
		{
			System.err.println("Error while reading file '" + filename + "'!");
			System.exit(8);
		}
		catch(XMLStreamException e)
		{
			System.err.println("Given file '" + filename + "' is not a valid NZB file!");
			System.exit(8);
		}
		catch(java.text.ParseException e)
		{
			System.err.println(e.getMessage());
			System.exit(8);
		}
		
		// unset background status bar
		taskMgr.loadNzb(false);
	}
	
	/**
	 * Shutdown the computer now (after all downloads have been finished).
	 */
	public void shutdownNow()
	{
		String shutdownCmd = "";
		String shutdownDir = "";
		
		if(System.getProperty("os.name").contains("Windows"))
		{
			shutdownCmd = "shutdown.exe /s /f";
			shutdownDir = System.getenv("SystemRoot") + "/system32";
		}
		else if(System.getProperty("os.name").contains("Linux"))
		{
			shutdownCmd = "shutdown -h -q";
			shutdownDir = "/sbin";
		}
		
		File dir = new File(shutdownDir);
		
		// execute shutdown command
		try
		{
			taskMgr.shutdown();
			bWorker.shutdown();
			Runtime rt = Runtime.getRuntime();
			rt.exec(shutdownCmd, null, dir);
		}
		catch(IOException e)
		{
			logger.printStackTrace(e);
		}
		
		System.exit(0);
	}
	
	/**
	 * Create a share memory mapping so that a second instance of this
	 * program can later on pass new nzb files to this first instance here.
	 * This method also starts a background thread that "listens" to this
	 * shared memory for new data (nzb file locations) to read.
	 */
	protected void createMemoryMapping()
	{
		try
		{
			bWorker = new BackgroundWorker(this, MEM_MAP_BUFFER_SIZE);
			bWorker.start();
		}
		catch(IOException e)
		{
			logger.printStackTrace(e);
		}
	}

	/**
	 * Set the connection speed limit label on the status bar 
	 * according to value in preferences.
	 */
	public void setConnSpeedLimit()
	{
		String pref = prefContainer.getPrefValue("DownloadSettingsMaxConnectionSpeed");
		
		try
		{
			@SuppressWarnings("unused")
			long tmp = Long.parseLong(pref);
		}
		catch(Exception ex)
		{
			pref = "0";
			prefContainer.setSpeedLimit("0");
		}
		
		if(pref.length() == 0 || Long.valueOf(pref) == 0)
		{
			limitDlSpeedButton.setText(localer.getBundleText("DlSpeedLimitButton"));
		}		
		else	
		{
			limitDlSpeedButton.setText(
					localer.getBundleText("DlSpeedLimitButton2") + " " + pref + " KB/s");
		}			
	}
	
	/**
	 * Use this method to set the ETA and total file size label.
	 * Also set the tooltip of the system tray icon accordingly.
	 * 
	 * @param text The text to set on the label
	 */
	public void setEtaAndTotalLabel(final String text)
	{
		// set label in status bar
		if(etaAndTotalText != null)
		{
	        SwingUtilities.invokeLater(new Runnable() 
	        { 
	        	public void run()
	        	{
	        		etaAndTotalText.setText(text);
				} 
			} );
		}
		
		// set system tray icon
		if(trayIcon != null)
		{
			if(text == null || text.isEmpty())
				trayIcon.setToolTip("HelloNzb -- " + localer.getBundleText("SystemTrayTooltipNoFiles"));
			else
				trayIcon.setToolTip("HelloNzb -- " + text);
		}
	}
	
	/**
	 * Use this method to set the current download speed label.
	 * 
	 * @param bps The text to set on the label
	 */
	public void setCurrDlSpeedLabel(final long bps)
	{
		if(currDlSpeed != null)
		{
	        SwingUtilities.invokeLater(new Runnable() 
	        { 
	        	public void run()
	        	{
	        		currDlSpeed.setText(HelloNzbToolkit.prettyPrintBps(bps));
				} 
			} );
		}			
	}
	
	/**
	 * (Un)set the system tray icon.
	 * 
	 * @param flag Whether or not to set the icon
	 */
	public void setTrayIcon(boolean flag)
	{
		if(flag && trayIcon != null)
			return; // yes to set, but alreay active
		else if(!flag && trayIcon == null)
			return; // don't set, but already non-active
		else if(flag)
			initSystemTray(actions.get("MenuHelloNzbQuit")); // set it
		else
		{
			// unset it
			SystemTray tray = SystemTray.getSystemTray();
			tray.remove(trayIcon);
			trayIcon = null;
		}
	}

	/**
	 * Check whether or not the system tray icon is set for the running program.
	 * 
	 * @return true if we have set the tray icon, false otherwise
	 */
	public boolean hasTrayIcon()
	{
		return (trayIcon != null) ? true : false;  
	}
	
	/**
	 * Returns true if there is currently a download active.
	 * 
	 * @return true/false
	 */
	public boolean isDownloadActive()
	{
		return (currentFileDownloader == null ? false : true);
	}

	/**
	 * Get the JFrame object, i.e. the applications main window.
	 * 
	 * @return The JFrame object
	 */
	public JFrame getJFrame()
	{
		return jframe;
	}
	
	/**
	 * Get the specified preferences value.
	 */
	public String getPrefValue(String id)
	{
		return prefContainer.getPrefValue(id);
	}
	
	/**
	 * Get the specified boolean-type preferences value.
	 */
	public boolean getBooleanPrefValue(String id)
	{
		return prefContainer.getBooleanPrefValue(id);
	}
	
	/**
	 * Get the applications preferences container.
	 * 
	 * @return The HelloNzbPreferences object
	 */
	public HelloNzbPreferences getPrefContainer()
	{
		return prefContainer;
	}
	
	/**
	 * Get the menu bar object of the main application window.
	 * 
	 * @return The JMenuBar object
	 */
	public JMenuBar getMenu()
	{
		return menuBar;
	}
	
	/**
	 * Get the tool bar object of the main application window.
	 * 
	 * @return The JToolBar object
	 */
	public JToolBar getToolBar()
	{
		return toolBar;
	}
	
	/**
	 * Return the count of files in the download queue.
	 * 
	 * @return The count of files
	 */
	public int getDownloadFileCount()
	{
		return filesToDownloadTabModel.getRowCount();
	}
	
	/**
	 * Set the table in the thread view to the according number of lines.
	 */
	public void resetThreadView()
	{
		ThreadViewTableModel tm = (ThreadViewTableModel) threadViewTab.getModel();
		int threadcount = Integer.valueOf(prefContainer.getPrefValue("ServerSettingsThreadCount"));
		tm.setRowCount(threadcount);
	}
	
	/**
	 * Globally disconnect from server, if currently connected.
	 * 
	 * @param block Whether or not to wait until connection has closed
	 */
	public void globalDisconnect(boolean block)
	{
		if(currentFileDownloader != null)
		{
			currentFileDownloader.shutdown();
			currentFileDownloader = null;
		}
		
		if(nioClient != null)
		{
			nioClient.shutdown(false, System.nanoTime() + 10 * SEC_MODIFIER);
			nioClient = null;
		}
		
		resetThreadView();
	}
	
	/**
	 * Returns the lr split pane divider.
	 * 
	 * @return The current value
	 */
	public JSplitPane getLrSplitPane()
	{
		return lrSplitPane;
	}

	/**
	 * Returns the ud split pane divider.
	 * 
	 * @return The current value
	 */
	public JSplitPane getUdSplitPane()
	{
		return udSplitPane;
	}
	
	/**
	 * Returns the localer object.
	 * 
	 * @return The localer object
	 */
	public StringLocaler getLocaler()
	{
		return localer;
	}
	
	/**
	 * Returns the task manager object.
	 * 
	 * @return The task manager object
	 */
	public TaskManager getTaskManager()
	{
		return taskMgr;
	}
	
	/**
	 * Returns the current NettyNioClient object (or null)
	 * 
	 * @return The current NettyNioClient object (or null)
	 */
	public NettyNioClient getCurrNioClient()
	{
		return nioClient;
	}
	
	/**
	 * Returns the total amount of bytes loaded (counted for the currently
	 * downloaded NZB file and all its segments).
	 * 
	 * @return The total amount of loaded bytes
	 */
	public long getTotalBytesLoaded()
	{
		return totalBytesLoaded;
	}
	
	/**
	 * Returns all NzbParser objects currently in the NZB file queue.
	 * 
	 * @return All NzbParser objects in a vector
	 */
	public Vector<NzbParser> getNzbQueue()
	{
		Vector<NzbParser> vector = new Vector<NzbParser>();
		
		// get all NzbParser objects from table model
		try
		{
			int size = nzbFileQueueTabModel.getRowCount();
			for(int i = 0; i < size; i++)
				vector.add(nzbFileQueueTabModel.getNzbParser(i));
		}
		catch(Exception e) {}
		
		return vector;
	}
	
	/**
	 * Returns the central logger instance.
	 * @return The Logger object
	 */
	public MyLogger getLogger()
	{
		return logger;
	}
	
	/**
	 * Returns the speed history graph panel object.
	 * @return The SpeedGraphPanel object
	 */
	public SpeedGraphPanel getSpeedGraphPanel()
	{
		return speedGraph;
	}

	/**
	 * Register the popup menu object of the NZB file queue.
	 * @param popup
	 */
	public void registerLeftPopup(JPopupMenu popup)
	{
		leftPopup = popup;
	}

	/**
	 * Register the popup menu object of the download file queue.
	 * @param popup
	 */
	public void registerRightPopup(JPopupMenu popup)
	{
		rightPopup = popup;
	}
	
	/**
	 * Dispose left popup menu.
	 */
	public void disposeLeftPopup()
	{
		if(leftPopup != null)
		{
			leftPopup.setVisible(false);
			leftPopup.setEnabled(false);
			leftPopup = null;
		}
	}
	
	/**
	 * Dispose right popup menu.
	 */
	public void disposeRightPopup()
	{
		if(rightPopup != null)
		{
			rightPopup.setVisible(false);
			rightPopup.setEnabled(false);
			rightPopup = null;
		}
	}
}

























