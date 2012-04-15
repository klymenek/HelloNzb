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
import at.lame.hellonzb.util.MyLogger;
import at.lame.hellonzb.util.MyFuture;
import at.lame.hellonzb.util.StringLocaler;

import java.awt.event.*;
import java.net.URI;
import javax.swing.*;


public class CheckForUpdateAction extends AbstractAction 
{
	/** The parent (host) application object */
	private final HelloNzb mainApp;
	
	/** central logger object */
	private MyLogger logger;
	
	
	public CheckForUpdateAction(HelloNzbCradle f, Icon icon, String name)
	{
		this.mainApp = (HelloNzb) f;
		this.logger = mainApp.getLogger();
		putValue(Action.LARGE_ICON_KEY, icon);
		putValue(Action.NAME,  mainApp.getLocaler().getBundleText(name));
		putValue(Action.SHORT_DESCRIPTION, mainApp.getLocaler().getBundleText(name));
	}
	
	public void actionPerformed(ActionEvent e) 
	{
		StringLocaler localer = mainApp.getLocaler();
		
		// set background progress bar
		mainApp.getTaskManager().updateCheck(true);
		
		boolean updAvail = HelloNzbToolkit.isUpdateAvailable(HelloNzb.VERSION); 
		
		// unset background progress bar
		mainApp.getTaskManager().updateCheck(false);

		if(!updAvail)
		{
			// no updates available
			String msg = mainApp.getLocaler().getBundleText("PopupNoNewVersionAvailable");
			String title = mainApp.getLocaler().getBundleText("PopupInfoTitle");
			JOptionPane.showMessageDialog(mainApp.getJFrame(), msg, title, JOptionPane.INFORMATION_MESSAGE);		
		}
		else
		{
			// a newer version of HelloNzb is available for download!
			MyFuture<Boolean> future = new MyFuture<Boolean>(false);
			
			HelloNzbUpdateNotifier notifier = new HelloNzbUpdateNotifier(mainApp.getJFrame(), localer, logger);
			notifier.show(future);

			if(future.getPayload())
			{
				// yes, download new version --> open browser
				try
				{
					java.awt.Desktop.getDesktop().browse(new URI(HelloNzb.HELLONZB_WEBSITE));
				}
				catch(Exception ex)
				{
					// error, could not open browser
					String msg = localer.getBundleText("PopupErrorCouldNotOpenBrowser");
					String title = localer.getBundleText("PopupErrorTitle");
					JOptionPane.showMessageDialog(mainApp.getJFrame(), msg, title, JOptionPane.ERROR_MESSAGE);
				} 
			}
		}
	}
}
