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
import at.lame.hellonzb.listener.actions.*;

import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;


public class DownloadFileListPopupListener extends MouseAdapter implements ListSelectionListener
{
	/** The main application object */
	private final HelloNzb mainApp;
	
	/** The table this listener is used for */
	private final JTable table;
	

	public DownloadFileListPopupListener(HelloNzbCradle f, JTable tab)
	{
		this.mainApp = (HelloNzb) f;
		this.table = tab;
	}

	public void mouseClicked(MouseEvent me)
	{
		final int row = table.rowAtPoint(me.getPoint());
		final int col = table.columnAtPoint(me.getPoint());
		
		if(row < 0 || col < 0)
			return;

		// right-click?
		if((me.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK)
		{
			// was the right-clicked row already selected while right-clicked?
			if(!table.isCellSelected(row, col))
				table.setRowSelectionInterval(row, row);
			
			popup(me, row, col, table.getSelectedRows());
		}
	}
	
	public void valueChanged(ListSelectionEvent e)
	{/*
		int [] selectedRows = table.getSelectedRows();
		for(int row : selectedRows)
			table.removeRowSelectionInterval(row, row);
		
		if(e.getSource() == table.getSelectionModel() && table.getRowSelectionAllowed()) 
		{ 
			int first = e.getFirstIndex(); 
			int last = e.getLastIndex();
		}*/
	}
	
	private void popup(MouseEvent me, int row, int col, int [] selectedRows)
	{
		// generate popup menu
		JPopupMenu popupMenu = new JPopupMenu();
		popupMenu.add(new DownloadFileListPopupDelRowAction("ContextMenuRemoveFromList", mainApp, selectedRows));
		
		// show popup menu
		popupMenu.show(table, me.getX(), me.getY()); 
		mainApp.registerRightPopup(popupMenu);
	}
}
