/*******************************************************************************
 * HelloNzb -- The Binary Usenet Tool
 * Copyright (C) 2010-2011 Matthias F. Brandstetter
 * https://sourceforge.net/projects/hellonzb/
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

package at.lame.hellonzb.tablemodels;

import at.lame.hellonzb.*;
import at.lame.hellonzb.parser.*;

import javax.swing.*;
import javax.activation.*;

import java.awt.*;
import java.awt.dnd.*;
import java.awt.datatransfer.*;


/**
 * Handles drag & drop row reordering.
 * http://stackoverflow.com/questions/638807/how-do-i-drag-and-drop-a-row-in-a-jtable
 */
@SuppressWarnings("serial")
public class TableRowTransferHandler extends TransferHandler
{
	private final DataFlavor localObjectFlavor = new ActivationDataFlavor(int[].class, "Table Row Indices");
	private JTable table = null;
	private HelloNzb mainApp;
	private NzbParser firstParser;
	

	/**
	 * Class constructor. If mainApp parameter is not null, then this handler
	 * re-starts downloading with the new NZB file list.
	 */
	public TableRowTransferHandler(JTable table, HelloNzbCradle mainApp)
	{
		this.table = table;
		this.mainApp = (HelloNzb) mainApp;
		this.firstParser = null;
	}

	@Override
	protected Transferable createTransferable(JComponent c)
	{
		if(mainApp != null)
			firstParser = mainApp.getNzbQueue().firstElement();
		
		return new DataHandler(table.getSelectedRows(), localObjectFlavor.getMimeType());
	}

	@Override
	public boolean canImport(TransferHandler.TransferSupport info)
	{
		boolean b = info.getComponent() == table && info.isDrop() && info.isDataFlavorSupported(localObjectFlavor);
		table.setCursor(b ? DragSource.DefaultMoveDrop : DragSource.DefaultMoveNoDrop);
		
		return b;
	}

	@Override
	public int getSourceActions(JComponent c)
	{
		return TransferHandler.COPY_OR_MOVE;
	}

	@Override
	public boolean importData(TransferHandler.TransferSupport info)
	{
		JTable target = (JTable) info.getComponent();
		JTable.DropLocation dl = (JTable.DropLocation) info.getDropLocation();
		int index = dl.getRow();
		int max = table.getModel().getRowCount();
		
		if(index < 0 || index > max)
			index = max;
		
		target.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		try
		{
			Transferable trans = info.getTransferable();
			int [] rows = (int[]) trans.getTransferData(localObjectFlavor);
			if(rows.length > 0)
				((Reorderable) table.getModel()).reorder(rows, index);
			return true;
		}
		catch(Exception ex)
		{
			if(mainApp != null)
				mainApp.getLogger().printStackTrace(ex);
			else
				ex.printStackTrace();
		}
		
		return false;
	}

	@Override
	protected void exportDone(JComponent c, Transferable t, int act)
	{
		table.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		firstParser = null;

		if(act == TransferHandler.MOVE)
			notifyMainApp();
		
		if(mainApp != null)
		{
			mainApp.saveOpenParserData(false);
			mainApp.clearNzbQueueSelection();
		}
	}
	
	private void notifyMainApp()
	{
		if(mainApp == null)
			return;
		
		boolean stopped = mainApp.isDownloadActive();
		
		if(stopped)
			mainApp.startDownload();
		
		// new first parser in queue?
		NzbParser newFirstParser = mainApp.getNzbQueue().firstElement();
		if(newFirstParser != firstParser)
			mainApp.nzbQueueReordered(newFirstParser);
			
		if(stopped)
			mainApp.startDownload();
	}
}














































