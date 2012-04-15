/*******************************************************************************
u * HelloNzb -- The Binary Usenet Tool
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
package at.lame.hellonzb.preferences;

import java.awt.*;
import javax.swing.*;
import java.util.*;
import java.util.prefs.*;

import at.lame.hellonzb.*;
import at.lame.hellonzb.util.*; 
import at.lame.hellonzb.preferences.tabs.*;


/**
 * This class is used as container for all global settings of the
 * JavaNzb application, such as usenet server address and so on.
 * It can save all currently loaded preferencse to a file or load
 * them back again. Additionally this class provides the JDialog
 * object that is drawn onto the screen to let the user change the
 * application's preferences.
 * 
 * @author Matthias F. Brandstetter
 * @see net.java.dev.designgridlayout.DesignGridLayout
 */
public class HelloNzbPreferences
{
	/** all preferences */
	protected static final Vector<String> keys = new Vector<String>();
	protected static final String [] autoKeys = new String [] {	"AutoSettingsWindowWidth",
																"AutoSettingsWindowHeight",
																"AutoSettingsLrDivider",
																"AutoSettingsUdDivider",
																"AutoSettingsLastNzbFilePath" };	
	static
	{
		keys.addAll(Arrays.asList(autoKeys));
		keys.addAll(Arrays.asList(PrefGeneralTab.getKeys()));
		keys.addAll(Arrays.asList(PrefServerTab.getKeys()));
		keys.addAll(Arrays.asList(PrefDownloadTab.getKeys()));
		keys.addAll(Arrays.asList(PrefExtendedTab.getKeys()));
		keys.addAll(Arrays.asList(PrefUsageStatsTab.getKeys()));
	}

	/** all messages that can be suppressed */
	protected static final String [] hiddenMsg = new String [] {	"QuitApplication" };
	
	/** The dialog window object */
	protected JDialog prefDialog;
	
	/** The parent frame of this dialog */
	protected HelloNzb mainApp;
	
	/** String localer object */
	protected StringLocaler localer;
	
	/** central logger object */
	protected MyLogger logger;
	
	/** Preferences node (where to save application settings) */
	protected Preferences myPreferences;
	
	/** Contains the application settings as key/value pairs */
	protected HashMap<String,String> prefMap;
	
	/** The JPanel of the general settings tab */
	protected PrefGeneralTab generalSettingsPanel;
	
	/** The JPanel of the server settings tab */
	protected PrefServerTab serverSettingsPanel;
	
	/** The JPanel of the download settings tab */
	protected PrefDownloadTab downloadSettingsPanel;
	
	/** The JPanel of the extended settings tab */
	protected PrefExtendedTab extendedSettingsPanel;
	
	/** The JPanel of the usage stats settings tab */
//	protected PrefUsageStatsTab usageStatsSettingsPanel;
	
	/** "save" button on main panel */
	protected JButton saveButton;

	/** "cancel" button on main panel */
	protected JButton cancelButton;
	
	/** "choose dir" button on general settings tab */
	protected JButton downloadDirChooseButton;
	
	/** "choose dir" button on general settings tab (auto-load dir) */
	protected JButton autoLoadDirChooseButton;
	
	/** "choose par2 exec" button on download settings tab */
	protected JButton par2CmdLineAppButton;
	
	/** "choose unrar exec" button on download settings tab */
	protected JButton unrarCmdLineAppButton;
	
	/** "reset all messages" button on extended settings tab */
	protected JButton resetAllMsg;

	/** standard server port text field */
	protected NumericTextField stdServerPort;
	
	/** SSL server port text field */
	protected NumericTextField sslServerPort;
	
	/** path to par2 exec */
	protected JTextField par2LocationTextField;
	
	/** path to unrar exec */
	protected JTextField unrarLocationTextField;
	
	/** auto PAR2 archive verification checkbox */
	protected JCheckBox par2ExtractCheckbox;
	
	/** auto RAR archive extraction checkbox */
	protected JCheckBox rarExtractCheckbox;
	
	/** last openend nzb file location */
	protected String lastNzbFilePath;
	
	/** the language setting when the dialog was called */
	protected String tmpLang;
		
	
	/**
	 * This is the constructor of the class.
	 */
	public HelloNzbPreferences(HelloNzb h)
	{
		this.mainApp = h;
		this.localer = h.getLocaler();
		this.logger = mainApp.getLogger();
		this.prefDialog = new JDialog(mainApp.getJFrame(), true);
		
		// create key array
		
		// get preferences object and the preferences map, load settings
		this.myPreferences = Preferences.userNodeForPackage(at.lame.hellonzb.HelloNzb.class);
		this.prefMap = new HashMap<String,String>();
		loadPreferences();
		
		// create the panel for the dialog
		addContentPane();
	
		// dialog settings 
		this.prefDialog.setTitle(mainApp.getLocaler().getBundleText("MenuHelloNzbPref"));
		this.prefDialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
		this.prefDialog.setLocation(mainApp.getJFrame().getX(), mainApp.getJFrame().getY());
		this.prefDialog.pack();
		this.prefDialog.setMinimumSize(new Dimension(600, 1));
		this.prefDialog.setVisible(false);
		
		// default values
		this.lastNzbFilePath = "";
		
		this.tmpLang = "";
	}
	
	/**
	 * This method is called by the constructor to load the user preferences.
	 */
	protected void loadPreferences()
	{
		String val = "";

		try
		{
			// load preferences
			for(String s : keys)
			{
				val = myPreferences.get(s, "");
				prefMap.put(s, val);
				
				if(s.equals("AutoSettingsLastNzbFilePath"))
					lastNzbFilePath = val;
				else if(s.equals("DownloadSettingsIgnoreCrc32Error"))
					prefMap.put(s, "true");
				else if(s.equals("GeneralSettingsCheckSegments"))
					prefMap.put(s, "false");
			}
			
			// check what messages should be suppressed
			for(String s : hiddenMsg)
			{
				val = myPreferences.get(s, "false");
				prefMap.put(s, val);
			}
		}
		catch(Exception e)
		{
			logger.printStackTrace(e);
		}
	}
	
	/**
	 * This method is called by the constructor to save the user preferences.
	 */
	protected void savePreferences()
	{
		Vector<PrefTabPanel> tabs = new Vector<PrefTabPanel>();
		tabs.add(generalSettingsPanel);
		tabs.add(serverSettingsPanel);
		tabs.add(downloadSettingsPanel);
		tabs.add(extendedSettingsPanel);
//		tabs.add(usageStatsSettingsPanel);
		
		for(PrefTabPanel tab : tabs)
		{
			JPanel panel = tab.getJPanel();
			String [] keys = tab.keys();
			for(String key : keys)
				savePrefKeyVal(panel, key);
		}
	}
	
	/**
	 * This method is called at normal application shutdown in order to
	 * save some automatic values (like window size).
	 */
	public void saveAutoValues()
	{
		JFrame frame = mainApp.getJFrame();
		String width = String.valueOf(frame.getWidth());
		String height = String.valueOf(frame.getHeight());
		
		String lrDivider = String.valueOf(mainApp.getLrSplitPane().getDividerLocation());
		String udDivider = String.valueOf(mainApp.getUdSplitPane().getDividerLocation());
		
		
		// window width
		myPreferences.put("AutoSettingsWindowWidth", width);
		prefMap.put("AutoSettingsWindowWidth", width);

		// window height
		myPreferences.put("AutoSettingsWindowHeight", height);
		prefMap.put("AutoSettingsWindowHeight", height);
		
		// left/right divider
		myPreferences.put("AutoSettingsLrDivider", lrDivider);
		prefMap.put("AutoSettingsLrDivider", lrDivider);
		
		// up/down divider
		myPreferences.put("AutoSettingsUdDivider", udDivider);
		prefMap.put("AutoSettingsUdDivider", udDivider);
		
		// path/location of last loaded nzb file
		if(!lastNzbFilePath.isEmpty())
		{
			myPreferences.put("AutoSettingsLastNzbFilePath", lastNzbFilePath);
			prefMap.put("AutoSettingsLastNzbFilePath", lastNzbFilePath);
		}
	}

	/**
	 * Sets whether or not so hide the specified message in future.
	 * 
	 * @param msg The message to set
	 * @param toHide True if the message should be suppressed
	 */
	public void hideMessage(String msg, boolean toHide)
	{
		// save which messages should be suppressed
		for(String s : hiddenMsg)
		{
			if(s.equals(msg))
			{
				// ok, message found, so set its value
				myPreferences.put(msg, String.valueOf(toHide));
				prefMap.put(msg, String.valueOf(toHide));
			}
		}
	}
	
	/**
	 * This helper method is called by the savePreferences() method to
	 * save one preferences key/value pair. This method automatically
	 * chooses the correct method to call the text value of the selected
	 * component. The component is passed to this method as index of the
	 * passend panel's component. 
	 * 
	 * @param p The JPanel were the component was added to
	 * @param idx The index of the desired component of the panel
	 * @param key The key of the key/value pair to save
	 * @param t The type of the component to choose
	 */
	private void savePrefKeyVal(JPanel p, String key)
	{
		String val = "";
		
		for(Component comp : p.getComponents())
		{
			String name = comp.getName();
			if(name != null && name.equals(key))
			{
				if(comp instanceof JTextField)
					val = ((JTextField)comp).getText().trim();
				else if(comp instanceof JSlider)
					val = String.valueOf(((JSlider)comp).getValue());
				else if(comp instanceof JCheckBox)
					val = String.valueOf(((JCheckBox)comp).isSelected());
				else if(comp instanceof JComboBox)
					val = String.valueOf(((JComboBox)comp).getSelectedItem());
				
				break;
			}
		}
		
		// (de)activate debug mode
		if(key.equals("ExtendedSettingsConsoleOutput") && !HelloNzb.DEBUG)
		{
			if(Boolean.parseBoolean(val))
				logger.setPrintLevel(MyLogger.SEV_INFO);
			else
				logger.setPrintLevel(MyLogger.SEV_ERROR);
		}
		
		myPreferences.put(key, val);
		prefMap.put(key, val);
	}
	
	/**
	 * This method is used by the constructor to create the content on
	 * the content pane of the dialog.
	 */
	private void addContentPane()
	{
		JPanel mainPanel = new JPanel();
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		JTabbedPane contentTabs = new JTabbedPane();
		

		// create dialog buttons
		saveButton = new JButton(mainApp.getLocaler().getBundleText("PrefDialogSaveButton"));
		cancelButton = new JButton(mainApp.getLocaler().getBundleText("PrefDialogCancelButton"));
		saveButton.addActionListener(new PrefButtonActionListener(this));
		cancelButton.addActionListener(new PrefButtonActionListener(this));

		// create spacer panels
		mainPanel.setLayout(new BorderLayout());
		mainPanel.add(new JPanel(), BorderLayout.NORTH);
		mainPanel.add(new JPanel(), BorderLayout.WEST);
		mainPanel.add(new JPanel(), BorderLayout.EAST);
		
		// create tabbed content panes
		mainPanel.add(contentTabs, BorderLayout.CENTER);
		
		// general settings
		generalSettingsPanel = new PrefGeneralTab(mainApp, prefMap);
		downloadDirChooseButton = generalSettingsPanel.getDlDirButton();
		downloadDirChooseButton.addActionListener(new PrefButtonActionListener(this));
		autoLoadDirChooseButton = generalSettingsPanel.getAutoLoadButton();
		autoLoadDirChooseButton.addActionListener(new PrefButtonActionListener(this));
		contentTabs.addTab(localer.getBundleText("PrefDialogGeneralSettingsPane"), generalSettingsPanel.getJPanel());
		
		// server settings
		serverSettingsPanel = new PrefServerTab(mainApp, prefMap);
		serverSettingsPanel.getStdSslCB().addActionListener(new SslCBActionListener(this));
		stdServerPort = serverSettingsPanel.getStdServerPort();
		sslServerPort = serverSettingsPanel.getSslServerPort();
		contentTabs.addTab(localer.getBundleText("PrefDialogServerSettingsPane"), serverSettingsPanel.getJPanel());
		
		// download settings
		downloadSettingsPanel = new PrefDownloadTab(mainApp, prefMap);
		par2CmdLineAppButton = downloadSettingsPanel.getPar2Button();
		unrarCmdLineAppButton = downloadSettingsPanel.getUnrarButton();
		par2LocationTextField = downloadSettingsPanel.getPar2LocationTextField();
		unrarLocationTextField = downloadSettingsPanel.getUnrarLocationTextField();
		par2ExtractCheckbox = downloadSettingsPanel.getPar2ExtractCheckbox();
		rarExtractCheckbox = downloadSettingsPanel.getRarExtractCheckbox();
		par2CmdLineAppButton.addActionListener(new PrefButtonActionListener(this));
		unrarCmdLineAppButton.addActionListener(new PrefButtonActionListener(this));
        par2ExtractCheckbox.addActionListener(new Par2CBActionListener(this));
		rarExtractCheckbox.addActionListener(new UnrarCBActionListener(this));
		contentTabs.addTab(localer.getBundleText("PrefDialogDownloadSettingsPane"), downloadSettingsPanel.getJPanel());
		
		// extended settings
		extendedSettingsPanel = new PrefExtendedTab(mainApp, prefMap);
		resetAllMsg = extendedSettingsPanel.getResetButton();
        resetAllMsg.addActionListener(new PrefButtonActionListener(this));
		contentTabs.addTab(localer.getBundleText("PrefDialogExtendedSettingsPane"), extendedSettingsPanel.getJPanel());
		
		// usage statistics settings
//		usageStatsSettingsPanel = new PrefUsageStatsTab(mainApp, prefMap);
//		contentTabs.addTab(localer.getBundleText("PrefDialogUsageStatsSettingsPane"), usageStatsSettingsPanel.getJPanel());

		// add dialog buttons to panel
		buttonPanel.add(saveButton);
		buttonPanel.add(cancelButton);
		mainPanel.add(buttonPanel, BorderLayout.SOUTH);
		
		prefDialog.setContentPane(mainPanel);
	}
	
	/**
	 * Get the JDialog object of this class, i.e. the window to draw onto the screen
	 * 
	 * @return The JDialog object
	 */
	public JDialog getPrefDialog()
	{
		return prefDialog;
	}
	
	/**
	 * Return the value of the given key.
	 * 
	 * @param key Key the value should be returned for
	 * @return The value for the given key
	 */
	public String getPrefValue(String key)
	{
		if(prefMap.containsKey(key))
			return prefMap.get(key);
		else
			return "";
	}
	
	/**
	 * Return the boolean value of the given key.
	 * 
	 * @param key Key the boolean-typed value should be returned for
	 * @return The boolean-typed value for the given key
	 */
	public boolean getBooleanPrefValue(String key)
	{
		return Boolean.parseBoolean(getPrefValue(key));
	}
	
	/**
	 * Returns the status of a message, i.e. whether or not to hide it.
	 * 
	 * @param msg The message to query
	 * @return "true" if the message should be suppressed
	 */
	public String getMessageStatus(String msg)
	{
		if(prefMap.containsKey(msg))
			return prefMap.get(msg);
		else
			return "false";
	}
	
	/**
	 * Set the path of the last loaded nzb file.
	 *  
	 * @param path The path to set
	 */
	public void setLastNzbFilePath(String path)
	{
		myPreferences.put("AutoSettingsLastNzbFilePath", path);
		prefMap.put("AutoSettingsLastNzbFilePath", path);
		lastNzbFilePath = path;
	}
	
	/**
	 * Set the download connection speed limit.
	 * 
	 * @param limit The lmit to set
	 */
	public void setSpeedLimit(String limit)
	{
		myPreferences.put("DownloadSettingsMaxConnectionSpeed", limit);
		prefMap.put("DownloadSettingsMaxConnectionSpeed", limit);
	}
	
	/**
	 * This method should be called before this preferences dialog
	 * is called (i.e. set to visible). It initialises some default
	 * values and "counters".
	 */
	public void dialogCalled()
	{
		this.tmpLang = prefMap.get("ExtendedSettingsChooseLanguage");
	}
}






















