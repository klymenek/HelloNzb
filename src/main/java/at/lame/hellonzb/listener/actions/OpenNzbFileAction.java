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
package at.lame.hellonzb.listener.actions;

import at.lame.hellonzb.*;
import at.lame.hellonzb.parser.*;
import at.lame.hellonzb.util.MyLogger;

import java.awt.event.*;
import java.io.File;
import java.io.IOException;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.stream.XMLStreamException;


public class OpenNzbFileAction extends AbstractAction 
{
	/** The parent (host) application object */
	private final HelloNzb mainApp;
	
	/** central logger object */
	private MyLogger logger;

	/** The path of the last chosen nzb file */
	private String lastNzbPath;
	
	
	public OpenNzbFileAction(HelloNzbCradle f, Icon icon, String name)
	{
		this.mainApp = (HelloNzb) f;
		this.logger = mainApp.getLogger();
		this.lastNzbPath = null;
		
		putValue(Action.LARGE_ICON_KEY, icon);
		putValue(Action.NAME, mainApp.getLocaler().getBundleText(name));
		putValue(Action.SHORT_DESCRIPTION, mainApp.getLocaler().getBundleText(name));
	}
	
	public void actionPerformed(ActionEvent e)
	{
		String msg;
		String title;
		String filename = "";
		
		
		// first check if preferences are complete
		String hostname = mainApp.getPrefValue("ServerSettingsHost");
		String port     = mainApp.getPrefValue("ServerSettingsPort");
		
		if(hostname.length() == 0 || port.length() == 0)
		{
			msg = mainApp.getLocaler().getBundleText("PopupServerNotSet");
			title = mainApp.getLocaler().getBundleText("PopupErrorTitle");
			JOptionPane.showMessageDialog(null, msg, title, JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		FileNameExtensionFilter filter = new FileNameExtensionFilter("NZB files", "nzb");
		JFileChooser fc = new JFileChooser();
		fc.setFileFilter(filter);
		
		// set file chooser to current directory
		lastNzbPath = mainApp.getPrefValue("AutoSettingsLastNzbFilePath");
		if(lastNzbPath != "")
		{
			File lastNzb = new File(lastNzbPath);
			File lastDir = new File(lastNzb.getParent());
			fc.setCurrentDirectory(lastDir);
		}
		
		int returnVal = fc.showOpenDialog(mainApp.getJFrame());
		
		if(returnVal == JFileChooser.APPROVE_OPTION) 
		{
			File nzbFile = fc.getSelectedFile();
			try
			{
				filename = nzbFile.getCanonicalPath();
				NzbParserCreator pc = new NzbParserCreator(mainApp, filename);
				pc.start();
				mainApp.clearNzbQueueSelection();
			}
			catch(IOException ex)
			{
				msg = mainApp.getLocaler().getBundleText("PopupCannotOpenNzb"); 
				title = mainApp.getLocaler().getBundleText("PopupErrorTitle");
				JOptionPane.showMessageDialog(null, msg, title, JOptionPane.ERROR_MESSAGE);
				logger.printStackTrace(ex);
			}
			
			// save path for later use
			lastNzbPath = nzbFile.getAbsolutePath();
			mainApp.getPrefContainer().setLastNzbFilePath(lastNzbPath);
		}
	}
}

















