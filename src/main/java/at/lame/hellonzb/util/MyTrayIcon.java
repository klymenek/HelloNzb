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

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

import at.lame.hellonzb.*;


public class MyTrayIcon extends TrayIcon
{
	/** The parent frame of this dialog */
	private HelloNzbCradle mainApp;
		
	
	/**
	 * Class constructor, calls super constructor with image.
	 * Initialises the tray icon with the context menu.
	 * 
	 * @param mainApp The parent HelloNzbCradle object
	 * @param image The Image object to set for the tray icon
	 * @param tooltip The default tooltip to set
	 * @param actions A map of Action objects
	 */
	public MyTrayIcon(HelloNzbCradle mainApp, Image image, String tooltip, 
			HashMap<String,AbstractAction> actions)
	{
		super(image);
		super.addMouseListener(new MyMouseActionListener());
		
		this.mainApp = mainApp;
		
		setToolTip(tooltip);
		setImageAutoSize(true);
		setPopupMenu(createPopupMenu(actions));
		
	}
	
	/**
	 * Create the popup menu (called from constructor).
	 * 
	 * @param actions A map of Action objects
	 * @return The newly created PopupMenu object
	 */
	private PopupMenu createPopupMenu(HashMap<String,AbstractAction> actions)
	{
		MySystemTrayActionListener listener = new MySystemTrayActionListener();
		StringLocaler loc = mainApp.getLocaler();
		
		// create new popup menu
		PopupMenu popup = new PopupMenu();

		// show program window
		MenuItem item = new MenuItem(loc.getBundleText("SystemTrayShowWindow"));
		item.addActionListener(listener);
		popup.add(item);
		popup.addSeparator();
		
		// open NZB file
		item = new MenuItem(loc.getBundleText("MenuHelloNzbOpenNzbFile"));
		item.addActionListener(actions.get("MenuHelloNzbOpenNzbFile"));
		popup.add(item);

		// pause download
		item = new MenuItem(loc.getBundleText("MenuServerPauseDownload"));
		item.addActionListener(actions.get("MenuServerPauseDownload"));
		popup.add(item);
		
		// speed limit
		item = new MenuItem(loc.getBundleText("DownloadSettingsMaxConnectionSpeed"));
		item.addActionListener(actions.get("MenuHelloNzbSpeedLimit"));
		popup.add(item);
		popup.addSeparator();
		
		// quit application
		item = new MenuItem(loc.getBundleText("MenuHelloNzbQuit"));
		item.addActionListener(actions.get("MenuHelloNzbQuit"));
		popup.add(item);
		
		return popup;
	}
	

	////////////////////////////////////////////////////////////////////////////////
	class MyMouseActionListener extends MouseAdapter
	{
		public void mouseClicked(MouseEvent me)
		{
			// left click? (right click = context menu as std. action)
			if((me.getModifiers() & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK)
			{
				// show program window
				mainApp.getJFrame().setVisible(true);
				mainApp.getJFrame().setState(Frame.NORMAL);
				mainApp.getJFrame().requestFocus();
			}
		}
	}
	
	////////////////////////////////////////////////////////////////////////////////
	class MySystemTrayActionListener implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			String cmd = e.getActionCommand();
			StringLocaler loc = mainApp.getLocaler();
			
			if(cmd.equals(loc.getBundleText("SystemTrayShowWindow")))
			{
				// show program window
				mainApp.getJFrame().setVisible(true);
				mainApp.getJFrame().setState(Frame.NORMAL);
				mainApp.getJFrame().requestFocus();
			}
		}
	}
}







































