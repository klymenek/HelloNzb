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

import java.util.*;
import javax.swing.table.*;
import at.lame.hellonzb.*;
import at.lame.hellonzb.util.StringLocaler;


public class ThreadViewTableModel extends AbstractTableModel
{
	private static final long serialVersionUID = 1L;
	
	/** Column names vecotr */
	private Vector<String> columnNames;
	
	/** Column data vector<vector> */
	private Vector<Vector<Object>> tableData;
	
	/** String localer */
	private StringLocaler localer;
	
	
	public ThreadViewTableModel(StringLocaler localer)
	{
		this.localer = localer;
		
		this.columnNames = new Vector<String>();
		this.tableData = new Vector<Vector<Object>>();
		
		// set column headers
		this.columnNames.add(localer.getBundleText("ThreadViewColumn1Header"));
		this.columnNames.add(localer.getBundleText("ThreadViewColumn2Header"));
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
	
	public void setValueAt(Object value, int row, int col)
	{
		tableData.get(row).set(col, value);
		fireTableCellUpdated(row, col);
	}
	
	public void setRowCount(int count)
	{
		if(count < 0)
			return;
		
		synchronized(this.tableData)
		{
			// first remove all existing table rows
			tableData.clear();
	
			// then create new data
			for(int i = 0; i < count; i++)
			{
				Vector<Object> row = new Vector<Object>();
				row.add(localer.getBundleText("ThreadViewConnection") + " " + (int) (i+1));
				row.add(localer.getBundleText("ThreadViewStatusIdle"));
				tableData.add(row);
			}
		}
			
		this.fireTableDataChanged();	
	}
}






















