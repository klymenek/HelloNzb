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
package at.lame.hellonzb.tablemodels;

import at.lame.hellonzb.HelloNzbToolkit;
import at.lame.hellonzb.listener.actions.NzbFileListPopupMoveRowAction.MoveDirection;
import at.lame.hellonzb.parser.*;
import at.lame.hellonzb.util.StringLocaler;

import java.awt.Color;
import java.util.*;

import javax.swing.JProgressBar;
import javax.swing.table.AbstractTableModel;


public class NzbFileQueueTableModel extends AbstractTableModel implements Reorderable
{
	private static final long serialVersionUID = 1L;

	/** The nzb files in the queue */
	protected Vector<NzbParser> nzbFileQueue;
	
	/** Column names vecotr */
	private Vector<String> columnNames;
	
	/** Column data vector<vector> */
	private Vector<Vector<Object>> tableData;
	
	/** String localer */
	private StringLocaler localer;
	
	
	public NzbFileQueueTableModel(StringLocaler localer)
	{
		this.localer = localer;
		
		this.nzbFileQueue = new Vector<NzbParser>();
		this.columnNames = new Vector<String>();
		this.tableData = new Vector<Vector<Object>>();
		
		// set column headers
		this.columnNames.add(localer.getBundleText("NzbQueueHeader"));
	}
	
	public int getColumnCount() 
	{
		return columnNames.size();
	}
	
	public String getColumnName(int columnIndex)
	{
		return columnNames.elementAt(columnIndex);
	}

	public int getRowCount() 
	{
		return tableData.size();
	}

	public Object getValueAt(int row, int col) 
	{
		return tableData.get(row).get(col);
	}
	
	public Class<?> getColumnClass(int c) 
	{
		return getValueAt(0, c).getClass();
	}
	
	@Override
	public void reorder(int [] fromIndices, int toIndex)
	{
		int fromIdxCnt = fromIndices.length;
		int firstFrom = fromIndices[0];
		int lastFrom = fromIndices[fromIdxCnt - 1];
		
		// anything to move?
		if(		fromIdxCnt == 0 || (firstFrom <= toIndex && toIndex <= lastFrom) ||
				firstFrom == toIndex - 1 || firstFrom == toIndex || 
				lastFrom == toIndex - 1 || lastFrom == toIndex ||
				firstFrom < 0 || toIndex < 0 || 
				lastFrom > tableData.size() || toIndex > tableData.size())
		{
			return;
		}
		
		// determine which table rows to move
		Vector<Vector<Object>> dataToMove = new Vector<Vector<Object>>();
		Vector<NzbParser> parserToMove = new Vector<NzbParser>();
		for(int idx = 0; idx < fromIdxCnt; idx++)
		{
			dataToMove.add(tableData.get(fromIndices[idx]));
			parserToMove.add(nzbFileQueue.get(fromIndices[idx]));
		}

		if(toIndex < firstFrom)
		{
			// drag upwards
			for(int i = 0; i < fromIdxCnt; i++)
			{
				tableData.remove(firstFrom);
				nzbFileQueue.remove(firstFrom);
			}
			int offset = 0;
			for(int i = 0; i < fromIdxCnt; i++)
			{
				tableData.insertElementAt(dataToMove.get(i), toIndex + offset);
				nzbFileQueue.insertElementAt(parserToMove.get(i), toIndex + offset);
				offset++;
			}
		}
		else
		{
			// drag downwards
			int offset = 0;
			for(int i = 0; i < fromIdxCnt; i++)
			{
				tableData.insertElementAt(dataToMove.get(i), toIndex + offset);
				nzbFileQueue.insertElementAt(parserToMove.get(i), toIndex + offset);
				offset++;
			}
			for(int i = 0; i < fromIdxCnt; i++)
			{
				tableData.remove(firstFrom);
				nzbFileQueue.remove(firstFrom);
			}
		}
		
		// to finish update the table's display
		fireTableRowsUpdated(0, tableData.size() - 1);
	}
	
	public void moveRows(int [] selectedRows, MoveDirection direction)
	{
		JProgressBar bar = null;
		
		// first evaluate which rows we want to move (by name,
		// because row indices will change during this operation
		Vector<String> rows = new Vector<String>();
		for(int row : selectedRows)
		{
			bar = (JProgressBar) tableData.get(row).get(0);
			rows.add(bar.getName());
		}
		
		// move all these rows into the given direction
		int oldRow = -1;
		int newRow = -1;
		int chgRowIdx = 0;
		for(String name : rows)
		{
			// check for valid movement direction
			oldRow = getRowByFilename(name);
			if(oldRow == 0 && (direction == MoveDirection.TOP || direction == MoveDirection.UP))
				continue;
			if(oldRow == (tableData.size() - 1) && (direction == MoveDirection.DOWN || direction == MoveDirection.BOTTOM))
				continue;
			
			// swap row indices
			switch(direction)
			{
				case TOP:		newRow = chgRowIdx; 						break;
				case UP:		newRow = oldRow - 1; 						break;
				case DOWN:		newRow = oldRow + 1 + selectedRows.length; 	break;
				case BOTTOM:	newRow = tableData.size(); 					break;
			}
			
			// move table data (JProgressBar object)
			Vector<Object> data = tableData.get(oldRow);
			tableData.insertElementAt(data, newRow);
			switch(direction)
			{
				case TOP:		tableData.remove(oldRow + 1);	break;
				case UP:		tableData.remove(oldRow + 1);	break;
				case DOWN:		tableData.remove(oldRow);		break;
				case BOTTOM:	tableData.remove(oldRow);		break;
			}
			
			// move content (NzbParser)
			NzbParser parser = nzbFileQueue.get(oldRow);
			nzbFileQueue.insertElementAt(parser, newRow);
			switch(direction)
			{
				case TOP:		nzbFileQueue.remove(oldRow + 1);	break;
				case UP:		nzbFileQueue.remove(oldRow + 1);	break;
				case DOWN:		nzbFileQueue.remove(oldRow);		break;
				case BOTTOM:	nzbFileQueue.remove(oldRow);		break;
			}
			
			chgRowIdx++;
		}
		
		// to finish update the table's display
		fireTableRowsUpdated(0, tableData.size() - 1);
	}
	
	public void setValueAt(Object value, int row, int col)
	{
		JProgressBar progBar = (JProgressBar) tableData.get(row).get(col);
		progBar.setValue((Integer) value);
		fireTableCellUpdated(row, col);
	}
	
	/**
	 * Increase the progress bar on the first line by one.
	 * 
	 * @param progress The new progress value to set
	 */
	public void setRowProgress(NzbParser p, int progress)
	{
		for(int i = 0; i < nzbFileQueue.size(); i++)
		{
			NzbParser parser = nzbFileQueue.get(i);
			if(p == parser)
			{
				JProgressBar progBar = (JProgressBar) tableData.get(i).get(0);
				progBar.setValue(progress);
				fireTableRowsUpdated(i, i);
				break;
			}
		}
	}
	
	/**
	 * Check whether or not the nzb file queue already contains
	 * another nzb file with the same name as the passed parser.
	 * 
	 * @param parser The parser to check
	 * @return true if the queue already contains a nzb file with that name
	 */
	public boolean containsNzb(NzbParser parser)
	{
		String filename = HelloNzbToolkit.getLastFilename(parser.getName());
		for(int i = 0; i < nzbFileQueue.size(); i++)
		{
			String tmp = HelloNzbToolkit.getLastFilename(nzbFileQueue.get(i).getName());
			if(tmp.equals(filename))
				return true;
		}
			
		return false;
	}
	
	/**
	 * This method is called in order to add a new row to the end of the
	 * table models data. The data to display for this new row is extracted
	 * from a download file container.
	 * 
	 * @param parser The NzbParser object to add as new row
	 */
	public void addRow(NzbParser parser)
	{
		Vector<Object> innerVector = new Vector<Object>();
		
		String filename = HelloNzbToolkit.getLastFilename(parser.getName());
		//filename += " (" + HelloNzbToolkit.prettyPrintFilesize(parser.getTotalSize()) + ")";

		int max = 100; // 100 %
		JProgressBar bar = new JProgressBar(0, max);
		bar.setBackground(Color.white);
		bar.setName(filename);
		bar.setString(filename);
		bar.setValue(0);
		innerVector.add(bar);

		// add this new row to the table data
		nzbFileQueue.add(parser);
		tableData.add(innerVector);
		fireTableRowsInserted(0, tableData.size());
	}
	
	/**
	 * This method removes the specified row in the table model.
	 * 
	 * @param parser The parser object to remove
	 */
	public void removeRow(NzbParser parser)
	{
		for(int i = 0; i < nzbFileQueue.size(); i++)
			if(parser == nzbFileQueue.get(i))
			{
				removeRow(i);
				break;
			}
	}
	
	/**
	 * This method removes a row from the table model.
	 * 
	 * @param The row to remove identified by its filename value
	 */
	public void removeRow(String filename)
	{
		int row = getRowByFilename(filename);
		
		if(row >= 0)
			removeRow(row);
	}
	
	/**
	 * This method removes a row from the table model.
	 * 
	 * @param The row to remove identified by its index number
	 */
	public void removeRow(int index)
	{
		if(index >= 0 && index < tableData.size())
		{
			nzbFileQueue.remove(index);
			tableData.remove(index);
			fireTableRowsDeleted(0, tableData.size());
		}
	}
	
	/**
	 * Return the NzbParser object at the given index.
	 * 
	 * @param idx The index of the item to retreive
	 * @return The according NzbParser object, null if none found
	 */
	public NzbParser getNzbParser(int idx) throws IllegalArgumentException
	{
		if(idx < 0 || idx > nzbFileQueue.size())
			throw new IllegalArgumentException();
		
		return nzbFileQueue.get(idx);
	}
	
	/**
	 * Return the whole vector of nzb parsers.
	 * 
	 * @return A vector/copy containing all NzbParser objects
	 */
	@SuppressWarnings("unchecked")
	public Vector<NzbParser> copyQueue()
	{
		return (Vector<NzbParser>) nzbFileQueue.clone();
	}
	
	/**
	 * Update the progress bar in the specified row.
	 * 
	 * @param filename The row to update is identified by this parameter
	 */
	public void fireTableRowUpdated(String filename)
	{
		int row = getRowByFilename(filename);
		if(row > -1)
			fireTableRowsUpdated(row, row);
	}
	
	/**
	 * Receives the filename of a row and returns the row number within
	 * the data vector. If the filename is not found in this vecotr, the
	 * method returns -1.
	 * 
	 * @param filename The filename to search for
	 * @return The zero-based row number if row was found, -1 otherwise
	 */
	private int getRowByFilename(String filename)
	{
		int row = 0;
		for(; row < tableData.size(); row++)
		{
			JProgressBar progBar = (JProgressBar) tableData.get(row).get(0);
			String cellText = progBar.getString();
			if(cellText.equals(filename))
				break;
		}
		
		if(row == tableData.size())
			return -1;
		else
			return row; 
	}
}


























