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
package at.lame.hellonzb.listener;

import at.lame.hellonzb.*;
import at.lame.hellonzb.listener.actions.QuitAction;
import at.lame.hellonzb.listener.adapter.MyWindowAdapter;

import java.awt.event.*;


public class MyWindowListener extends MyWindowAdapter
{
	private final HelloNzb mainApp;
	private QuitAction quitAction;
	
	
	public MyWindowListener(HelloNzb f)
	{
		mainApp = f;
	}

	public void setQuitAction(QuitAction qa)
	{
		quitAction = qa;
	}
	
	public void windowClosing(WindowEvent e)
	{
		if(quitAction != null)
			quitAction.actionPerformed(null);
	}
	
	public void windowDeiconified(WindowEvent e)
	{
		mainApp.getJFrame().setVisible(true);
	}

	public void windowIconified(WindowEvent e)
	{
		if(mainApp.hasTrayIcon())
			mainApp.getJFrame().setVisible(false);
	}
}
