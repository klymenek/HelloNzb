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
import at.lame.hellonzb.nntpclient.nioengine.NettyNioClient;
import at.lame.hellonzb.util.MyLogger;

import java.awt.event.*;
import javax.swing.*;


public class SpeedLimitAction extends AbstractAction 
{
	/** The parent (host) application object */
	private final HelloNzb mainApp;
	
	/** central logger object */
	private MyLogger logger;

	
	public SpeedLimitAction(HelloNzbCradle f, Icon icon, String name)
	{
		this.mainApp = (HelloNzb) f;
		this.logger = mainApp.getLogger();
		putValue(Action.LARGE_ICON_KEY, icon);
		putValue(Action.NAME, mainApp.getLocaler().getBundleText(name));
		putValue(Action.SHORT_DESCRIPTION, mainApp.getLocaler().getBundleText(name));
	}
	
	public void actionPerformed(ActionEvent e) 
	{
		// shop input dialog
		String currLimit = mainApp.getPrefValue("DownloadSettingsMaxConnectionSpeed");
		String msg = mainApp.getLocaler().getBundleText("PopupEnterSpeedLimit");
		String title = mainApp.getLocaler().getBundleText("MenuHelloNzbSpeedLimit");
		String reply = (String) JOptionPane.showInputDialog(mainApp.getJFrame(), msg, title, 
				JOptionPane.QUESTION_MESSAGE, null, null, currLimit);

		if(reply == null)
			return;
		
		if(reply.isEmpty())
			reply = "0";
		
		// process user input (save to preferences)
		long limit = 0;
		try
		{
			limit = Long.valueOf(reply);
			mainApp.getPrefContainer().setSpeedLimit(reply);
			mainApp.setConnSpeedLimit();
			NettyNioClient nio = mainApp.getCurrNioClient();
			if(nio != null)
				nio.setSpeedLimit(limit);
		}
		catch(NumberFormatException ex) { }
	}
}
