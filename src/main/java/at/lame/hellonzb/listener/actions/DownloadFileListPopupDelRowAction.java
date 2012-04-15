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


public class DownloadFileListPopupDelRowAction extends AbstractAction 
{	
	/** main application object */
	private final HelloNzb mainApp;
	
	/** central logger object */
	private MyLogger logger;

	/** selected rows (zero-based) */
	String [] selectedRows;
	
	
	public DownloadFileListPopupDelRowAction(String name, HelloNzb f, int [] selectedRows)
	{
		this.mainApp = f;
		this.logger = mainApp.getLogger();
		
		// create list of row names to remove
		int i = 0;
		this.selectedRows = new String[selectedRows.length];
		for(int index : selectedRows)
			this.selectedRows[i++] = mainApp.getDownloadFileName(index);
		
		putValue(Action.NAME, mainApp.getLocaler().getBundleText(name));
	}
	
	public void actionPerformed(ActionEvent e) 
	{
		boolean stopped = mainApp.isDownloadActive();
		
		if(stopped)
			mainApp.startDownload();
		
		for(String row : selectedRows)
			mainApp.removeDownloadFileQueueRow(row);
		
		if(stopped)
			mainApp.startDownload();
	}
}
