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

import java.awt.event.*;
import javax.swing.*;


public class NzbFileListPopupDelRowAction extends AbstractAction 
{	
	/** main application object */
	private final HelloNzb mainApp;
	
	/** central logger object */ 
	private MyLogger logger;

	/** selected rows (zero-based) */
	int [] selectedRows;
	boolean delData; // delete data on disk as well
	
	
	public NzbFileListPopupDelRowAction(String name, HelloNzb f, int [] selectedRows, boolean del)
	{
		this.mainApp = f;
		this.logger = mainApp.getLogger();
		this.selectedRows = selectedRows;
		this.delData = del;
		
		putValue(Action.NAME, mainApp.getLocaler().getBundleText(name));
	}
	
	public void actionPerformed(ActionEvent e) 
	{
		int count = 0;
		boolean stopped = mainApp.isDownloadActive();
		
		if(stopped)
			mainApp.startDownload();
		
		for(int row : selectedRows)
		{
			mainApp.removeNzbFileQueueRow(row - count, delData);
			count++;
		}
		
		if(stopped && mainApp.getDownloadFileCount() > 0)
			mainApp.startDownload();
	}
}
