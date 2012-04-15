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
import at.lame.hellonzb.util.*;

import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;
import javax.swing.*;

import com.sun.jndi.toolkit.url.*;


public class ReportBugAction extends AbstractAction 
{
	private static final String BUG_REPORT_URL = "https://sourceforge.net/tracker/?group_id=255360&atid=1431000";
	
	/** The parent (host) application object */
	private final HelloNzb mainApp;
	
	/** central logger object */
	private MyLogger logger;
	
	
	public ReportBugAction(HelloNzbCradle f, Icon icon, String name)
	{
		this.mainApp = (HelloNzb) f;
		this.logger = mainApp.getLogger();
		putValue(Action.LARGE_ICON_KEY, icon);
		putValue(Action.NAME,  mainApp.getLocaler().getBundleText(name));
		putValue(Action.SHORT_DESCRIPTION, mainApp.getLocaler().getBundleText(name));
	}
	
	public void actionPerformed(ActionEvent e) 
	{
		try 
		{
			Desktop desktop = Desktop.getDesktop();
			URI uri = new URI(BUG_REPORT_URL);
			
			if(BUG_REPORT_URL.contains("mailto"))
				desktop.mail(uri);
			else
				desktop.browse(uri);
		} 
		catch(IOException ex)
		{
			logger.printStackTrace(ex);
		}
		catch(URISyntaxException ex)
		{
			logger.printStackTrace(ex);
		}
	}
}
