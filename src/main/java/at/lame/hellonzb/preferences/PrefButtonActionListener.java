/*******************************************************************************
 * HelloNzb -- The Binary Usenet Tool
 * Copyright (C) 2010-2011 Matthias F. Brandstetter
 * https://sourceforge.net/projects/hellonzb/
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
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import javax.swing.filechooser.*;


/**
 * This class handles all button actions on the preferences dialog.
 */
public class PrefButtonActionListener implements ActionListener
{
	/** main preferences object */
	private HelloNzbPreferences prefMain;
	
	
	public PrefButtonActionListener(HelloNzbPreferences pref)
	{
		this.prefMain = pref;
	}
	
	public void actionPerformed(ActionEvent e)
	{
		// save settings
		if(e.getSource().equals(prefMain.saveButton))
			handleSaveButton();
		
		// quit and close without saving
		else if(e.getSource().equals(prefMain.cancelButton))
		{
			prefMain.loadPreferences();
			prefMain.prefDialog.setVisible(false);
		}
		
		// choose download directory
		else if(e.getSource().equals(prefMain.downloadDirChooseButton))
			handleDlDirButton();
		
		// choose auto-load directory
		else if(e.getSource().equals(prefMain.autoLoadDirChooseButton))
			handleAutoLoadButton();
		
		// choose path to par2 command line tool
		else if(e.getSource().equals(prefMain.par2CmdLineAppButton))
			handlePar2UnrarButton("DownloadSettingsPar2ExeLocation");
		
		// choose path to unrar command line tool
		else if(e.getSource().equals(prefMain.unrarCmdLineAppButton))
			handlePar2UnrarButton("DownloadSettingsUnrarExeLocation");
		
		// reset all messages
		else if(e.getSource().equals(prefMain.resetAllMsg))
		{
			for(String s : HelloNzbPreferences.hiddenMsg)
			{
				prefMain.myPreferences.put(s, String.valueOf(false));
				prefMain.prefMap.put(s, String.valueOf(false));
			}
		}
	}
	
	private void handleSaveButton()
	{
		prefMain.savePreferences();

		// par2 check checkbox set?
		String pref = prefMain.prefMap.get("DownloadSettingsPar2Check");
		if(pref.equals("true"))
		{
			// then check whether or not the location textfield is filled
			String title = prefMain.mainApp.getLocaler().getBundleText("PopupErrorTitle");
			String loc = prefMain.prefMap.get("DownloadSettingsPar2ExeLocation");

			if(loc.isEmpty())
			{
				// not filled!
				String msg = prefMain.mainApp.getLocaler().getBundleText("PopupPar2LocationNotSet");
				JOptionPane.showMessageDialog(null, msg, title, JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			// also check whether or not the selected file is an executable program file
			File file = new File(loc);
			if(!file.exists() || file.isDirectory() || !file.canExecute())
			{
				// no executable file!
				String msg = prefMain.mainApp.getLocaler().getBundleText("PopupPar2IsNotExecutable");
				JOptionPane.showMessageDialog(null, msg, title, JOptionPane.ERROR_MESSAGE);
				return;
			}
		}
		
		prefMain.prefDialog.setVisible(false);
		prefMain.mainApp.resetThreadView();
		prefMain.mainApp.setConnSpeedLimit();
		
		// system tray icon
		pref = prefMain.prefMap.get("GeneralSettingsShowTrayIcon");
		prefMain.mainApp.setTrayIcon(Boolean.valueOf(pref));
		
		// language setting changed?
		if(!prefMain.tmpLang.equals(prefMain.prefMap.get("ExtendedSettingsChooseLanguage")))
		{
			String title = prefMain.mainApp.getLocaler().getBundleText("PopupInfoTitle");
			String msg   = prefMain.mainApp.getLocaler().getBundleText("PopupRestartForNewLocale");
			JOptionPane.showMessageDialog(null, msg, title, JOptionPane.INFORMATION_MESSAGE);
		}
		
		// usage statistics
		prefMain.mainApp.activateUsageStats();
	}
	
	private void handleDlDirButton()
	{
		JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		
		// set file chooser to current directory
		String currPath = prefMain.prefMap.get("GeneralSettingsDownloadDir");
		if(currPath.length() > 0)
		{
			File currDir = new File(currPath);
			fc.setCurrentDirectory(currDir);
		}
		
		int returnVal = fc.showOpenDialog(prefMain.prefDialog);
		
		if(returnVal == JFileChooser.APPROVE_OPTION) 
		{
			File downloadDir = fc.getSelectedFile();
			String dirName = downloadDir.getPath();

			// save selected dir path to global app. settings map
			prefMain.prefMap.put("GeneralSettingsDownloadDir", dirName);
			
			// also set this as new text for the dir input field
			setText(dirName, prefMain.generalSettingsPanel.getJPanel(), "GeneralSettingsDownloadDir");
		}
	}

	private void handleAutoLoadButton()
	{
		JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		
		int returnVal = fc.showOpenDialog(prefMain.prefDialog);
		
		if(returnVal == JFileChooser.APPROVE_OPTION) 
		{
			File autoLoadDir = fc.getSelectedFile();
			String dirName = autoLoadDir.getPath();

			// save selected dir path to global app. settings map
			prefMain.prefMap.put("GeneralSettingsAutoLoadDir", dirName);
			
			// also set this as new text for the dir input field
			setText(dirName, prefMain.generalSettingsPanel.getJPanel(), "GeneralSettingsAutoLoadDir");
		}
	}

	private void handlePar2UnrarButton(String propString)
	{
		JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		
		// set file chooser to current directory
		String currPar2Loc = prefMain.prefMap.get(propString);
		if(currPar2Loc.length() > 0)
		{
			int idx = currPar2Loc.lastIndexOf(System.getProperty("file.separator"));
			String path = currPar2Loc.substring(0, idx);
			File currDir = new File(path);
			fc.setCurrentDirectory(currDir);
		}
		
		if(System.getProperty("os.name").contains("Windows"))
		{
			// set ".exe" file extension filter on Windows systems
			FileNameExtensionFilter filter = new FileNameExtensionFilter("Executables", "exe");
			fc.setFileFilter(filter);
		}
		int returnVal = fc.showOpenDialog(prefMain.prefDialog);
		
		if(returnVal == JFileChooser.APPROVE_OPTION) 
		{
			File app = fc.getSelectedFile();
			String dirName = app.getPath();

			// save selected dir path to global app. settings map
			prefMain.prefMap.put(propString, dirName);
			
			// also set this as new text for the dir input field
			setText(dirName, prefMain.downloadSettingsPanel.getJPanel(), propString);
		}
	}
	
	private void setText(String text, JPanel panel, String compName)
	{
		for(Component comp : panel.getComponents())
		{
			String name = comp.getName();
			if(name != null && name.equals(compName))
			{
				((JTextField)comp).setText(text);
				break;
			}
		}
	}
}







































