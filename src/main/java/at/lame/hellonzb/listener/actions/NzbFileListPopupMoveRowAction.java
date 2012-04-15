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


public class NzbFileListPopupMoveRowAction extends AbstractAction 
{	
	public enum MoveDirection { TOP, UP, DOWN, BOTTOM };
	
	/** main application object */
	private final HelloNzb mainApp;
	
	/** central logger object */
	private MyLogger logger;

	/** selected rows (zero-based) */
	private int [] selectedRows;
	
	/** move into which direction, up or down? */
	private MoveDirection direction; 
	
	
	public NzbFileListPopupMoveRowAction(String name, HelloNzb f, int [] selectedRows, MoveDirection dir)
	{
		this.mainApp = f;
		this.logger = mainApp.getLogger();
		this.selectedRows = selectedRows;
		this.direction = dir;
		
		putValue(Action.NAME, mainApp.getLocaler().getBundleText(name));
	}
	
	public void actionPerformed(ActionEvent e) 
	{
		boolean stopped = mainApp.isDownloadActive();
		
		if(stopped)
			mainApp.startDownload();
		
		mainApp.moveRowsInNzbQueue(selectedRows, direction);
		mainApp.clearNzbQueueSelection();
		mainApp.saveOpenParserData(false);
		
		if(stopped)
			mainApp.startDownload();
	}
}
















































