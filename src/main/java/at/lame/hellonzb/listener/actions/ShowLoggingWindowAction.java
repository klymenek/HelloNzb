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

import java.awt.event.*;
import javax.swing.*;


public class ShowLoggingWindowAction extends AbstractAction 
{
	private final HelloNzb mainApp;
	private final LoggingWindow logWnd;
	
	
	public ShowLoggingWindowAction(HelloNzbCradle f, Icon icon, String name, LoggingWindow logWnd)
	{
		this.mainApp = (HelloNzb) f;
		this.logWnd = logWnd;
		
		putValue(Action.LARGE_ICON_KEY, icon);
		putValue(Action.NAME,  mainApp.getLocaler().getBundleText(name));
		putValue(Action.SHORT_DESCRIPTION, mainApp.getLocaler().getBundleText(name));
	}
	
	public void actionPerformed(ActionEvent e) 
	{
		logWnd.show();
	}
}
