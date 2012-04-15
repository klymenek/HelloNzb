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
import at.lame.hellonzb.parser.*;
import at.lame.hellonzb.util.StringLocaler;

import java.awt.Color;
import java.awt.Font;
import java.util.*;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;


public class FilesToDownloadTableModel extends AbstractTableModel 
{
	private static final long serialVersionUID = 1L;

	/** index of segment count column in this table model */ 
	private final static int SEG_COUNT_IDX = 2;
	
	/** index of progress bar column in this table model */ 
	private final static int PROG_BAR_IDX = 3;

	/** The files in the download queue */
	protected Vector<DownloadFile> fileDownloadQueue;

	/** The column names vector */
	private Vector<String> columnNames;
	
	/** The column data vector<vector> */
	private Vector<Vector<Object>> tableData;
	
	/** The list of original segments count */
	private Vector<Integer> origSegCount;
	
	/** Error color */
	private static Color errorColor = new Color(255, 222, 150);
	
	/** String localer */
	private StringLocaler localer;
	
	
	public FilesToDownloadTableModel(StringLocaler localer)
	{
		this.localer = localer;
		
		this.fileDownloadQueue = new Vector<DownloadFile>();
		this.columnNames = new Vector<String>();
		this.tableData = new Vector<Vector<Object>>();
		this.origSegCount = new Vector<Integer>();
		
		// set column headers
		this.columnNames.add(localer.getBundleText("FileDownloadHeaderFilename"));
		this.columnNames.add(localer.getBundleText("FileDownloadHeaderSize"));
		this.columnNames.add(localer.getBundleText("FileDownloadHeaderSegments"));
		this.columnNames.add(localer.getBundleText("FileDownloadHeaderProgress"));
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
	
	public Class<?> getColumnClass(int c) 
	{
		return getValueAt(0, c).getClass();
	}

	public Object getValueAt(int row, int col) 
	{
		if(row < tableData.size())
			return tableData.get(row).get(col);
		else
			return null;
	}
	
	public void setValueAt(Object value, int row, int col)
	{
		if(col == PROG_BAR_IDX)
		{
			JProgressBar progBar = (JProgressBar) tableData.get(row).get(col);
			progBar.setValue((Integer) value);
		}
		else
		{
			JLabel label = (JLabel) tableData.get(row).get(col);
			label.setText((String) value);
		}
		
		fireTableCellUpdated(row, col);
	}
	
	public void setValueAt(Object value, String filename, int col)
	{
		int row = getRowByFilename(filename);
		
		if(row >= 0 && row < tableData.size())
		{
			if(col == PROG_BAR_IDX)
			{
				JProgressBar progBar = (JProgressBar) tableData.get(row).get(col);
				progBar.setValue((Integer) value);
			}
			else
			{
				JLabel label = (JLabel) tableData.get(row).get(col);
				label.setText((String) value);
			}
			
			fireTableCellUpdated(row, col);
		}
	}
	
	public void resetSegCount(String filename)
	{
		int row = getRowByFilename(filename);
		
		if(row >= 0 && row < tableData.size())
		{
			JLabel label = (JLabel) tableData.get(row).get(SEG_COUNT_IDX);
			int val = origSegCount.get(row);
			label.setText(String.valueOf(val));
			
			fireTableCellUpdated(row, SEG_COUNT_IDX);
		}
	}
	
	public void resetAllSegCounts(boolean resetFinished)
	{
		for(int row = 0; row < tableData.size(); row++)
		{
			JLabel label = (JLabel) tableData.get(row).get(SEG_COUNT_IDX);
			if(label.getText().equals("0") && !resetFinished)
				continue;
				
			int val = origSegCount.get(row);
			label.setText(String.valueOf(val));
			
			fireTableCellUpdated(row, SEG_COUNT_IDX);
		}
	}
	
	public void decrSegCount(String filename)
	{
		int row = getRowByFilename(filename);
		
		if(row >= 0 && row < tableData.size())
		{
			JLabel label = (JLabel) tableData.get(row).get(SEG_COUNT_IDX);
			int val = Integer.valueOf(label.getText());
			val--;
			label.setText(String.valueOf(val));
			
			fireTableCellUpdated(row, SEG_COUNT_IDX);
		}
	}
	
	/**
	 * Adds the contents of an nzb parser file to this table model.
	 * 
	 * @param parser The NzbParser object to process
	 */
	public void addNzbParserContents(NzbParser parser)
	{
		Vector<DownloadFile> files = parser.getFiles();
		
		for(int i = 0; i < files.size(); i++)
		{
			DownloadFile file = files.get(i);
			addRow(file);
		}
	}

	public DownloadFile getDownloadFile(int index) throws IllegalArgumentException
	{
		if(index < 0 || index > tableData.size())
			throw new IllegalArgumentException();
		
		return fileDownloadQueue.get(index);
	}
	
	public DownloadFile getDownloadFile(String filename)
	{
		int row = getRowByFilename(filename);
		if(row == -1)
			return null;
		else
			return fileDownloadQueue.get(row);
	}
	
	public Vector<DownloadFile> getDownloadFileVector()
	{
		return fileDownloadQueue;
	}
	
	/**
	 * Set the row identified by its filename value to the "decoding" state.
	 * 
	 * @param filename The filename of the row
	 * @param maxVal The maximum value of the progress bar
	 */
	public void setRowToDecoding(String filename, int maxVal)
	{
		int row = getRowByFilename(filename);
		
		if(row >= 0 && row < tableData.size())
		{
			JProgressBar progBar = (JProgressBar) tableData.get(row).get(PROG_BAR_IDX);
			progBar.setValue(0);
			progBar.setMinimum(0);
			progBar.setMaximum(maxVal);
			progBar.setString(localer.getBundleText("ProgressBarDecoding"));
			
			fireTableCellUpdated(row, PROG_BAR_IDX);
		}
	}
	
	/**
	 * Set the row identified by its filename value to "writing to disk" state.
	 * 
	 * @param filename The filename of the row
	 */
	public void setRowToWriting(String filename)
	{
		int row = getRowByFilename(filename);
		
		if(row >= 0 && row < tableData.size())
		{
			JProgressBar progBar = (JProgressBar) tableData.get(row).get(PROG_BAR_IDX);
			progBar.setValue(0);
			progBar.setString(localer.getBundleText("ProgressBarWriting"));
			
			fireTableCellUpdated(row, PROG_BAR_IDX);
		}
	}
	
	/**
	 * Set the row identified by its filename value to the "article not found" state.
	 * 
	 * @param filename The filename of the row
	 */
	public void setArticleNotFound(String filename)
	{
		int row = getRowByFilename(filename);
		String message = localer.getBundleText("ProgressBarArtNotFound");
		
		if(row >= 0 && row < tableData.size())
			setRowToErrorState(row, errorColor, message);
	}
	
	/**
	 * Set the row identified by its filename value to the "malformed server reply" state.
	 * 
	 * @param filename The filename of the row
	 */
	public void setMalformedServerReply(String filename)
	{
		int row = getRowByFilename(filename);
		String message = localer.getBundleText("ProgressBarBadServerReply");
		
		if(row >= 0 && row < tableData.size())
			setRowToErrorState(row, errorColor, message);
	}
	
	/**
	 * Set the row identified by its filename value to the "crc32" state.
	 * 
	 * @param filename The filename of the row
	 */
	public void setCrc32Error(String filename)
	{
		int row = getRowByFilename(filename);
		String message = localer.getBundleText("ProgressBarCrc32Error");
		
		if(row >= 0 && row < tableData.size())
			setRowToErrorState(row, errorColor, message);
	}
	
	/**
	 * Set the row identified by its filename value to the "no decoder found" state.
	 * 
	 * @param filename The filename of the row
	 */
	public void setNoDecoderFound(String filename)
	{
		int row = getRowByFilename(filename);
		String message = localer.getBundleText("ProgressBarNoDecoderError");
		
		if(row >= 0 && row < tableData.size())
			setRowToErrorState(row, errorColor, message);
	}
	
	/**
	 * This method is called in order to add a new row to the end of the
	 * table models data. The data to display for this new row is extracted
	 * from a download file container.
	 * 
	 * @param file The DownloadFile object to add as new row
	 */
	public void addRow(DownloadFile file)
	{
		Vector<Object> innerVector = new Vector<Object>();
		
		// filename column
		JLabel label = new JLabel(file.getFilename());
		label.setBackground(Color.white);
		label.setFont(label.getFont().deriveFont(Font.PLAIN));
		innerVector.add(label);
		
		// filesize column
		label = new JLabel(HelloNzbToolkit.prettyPrintFilesize(file.getTotalFileSize()));
		label.setBackground(Color.white);
		label.setFont(label.getFont().deriveFont(Font.PLAIN));
		innerVector.add(label);
		
		// segments column
		label = new JLabel(String.valueOf(file.getSegCount()));
		label.setBackground(Color.white);
		label.setFont(label.getFont().deriveFont(Font.PLAIN));
		innerVector.add(label);
		
		// progress bar column
		JProgressBar bar = new JProgressBar(0, (int) file.getTotalFileSize());
		bar.setBackground(Color.white);
		innerVector.add(bar);

		// add this new row to the table data
		tableData.add(innerVector);
		fireTableRowsInserted(tableData.size(), tableData.size());
		
		fileDownloadQueue.add(file);
		origSegCount.add(file.getSegCount());
	}
	
	/**
	 * This method removes a row from the table model.
	 * 
	 * @param The row to remove identified by its filename value
	 */
	public void removeRow(String filename)
	{
		int row = getRowByFilename(filename);
		removeRow(row);
	}
	
	/**
	 * This method removes a row from the table model.
	 * 
	 * @param row The row to remove identified by the row number
	 */
	public void removeRow(int index)
	{
		if(index >= 0 && index < tableData.size())
		{
			tableData.remove(index);
			fileDownloadQueue.remove(index);
			origSegCount.remove(index);
			fireTableRowsDeleted(0, tableData.size());
		}
	}
		
	/**
	 * Clear/reset the whole table, discard all data.
	 */
	public void clearTableData()
	{
		int size = tableData.size();
			
		tableData.clear();
		fileDownloadQueue.clear();
		origSegCount.clear();
		fireTableRowsDeleted(0, size);
	}
	
	/**
	 * Returns whether or not this table model contains a download file
	 * with the specified filename.
	 * 
	 * @param filename The filename to check
	 * @return True if the table model contains this filename, false otherwise
	 */
	public boolean containsFilename(String filename)
	{
		if(getRowByFilename(filename) == -1)
			return false;
		else
			return true;
	}
	
	/**
	 * Set the according row to a new (error) state.
	 * 
	 * @param color The new background color of the row
	 * @param message The error message to display in the line
	 */
	private void setRowToErrorState(int row, Color color, String message)
	{		
		// filename column
		JLabel label = (JLabel) tableData.get(row).get(0);
		label.setBackground(color);
		label.setFont(label.getFont().deriveFont(Font.BOLD));

		// filesize column
		label = (JLabel) tableData.get(row).get(1);
		label.setBackground(color);
		label.setFont(label.getFont().deriveFont(Font.BOLD));
			
		// segments column
		label = (JLabel) tableData.get(row).get(SEG_COUNT_IDX);
		label.setBackground(color);
		label.setFont(label.getFont().deriveFont(Font.BOLD));
			
		// set progress bar column
		JProgressBar progBar = (JProgressBar) tableData.get(row).get(PROG_BAR_IDX);
		progBar.setValue(0);
		progBar.setBackground(color);
		progBar.setString(message);
			
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
	public int getRowByFilename(String filename)
	{
		int row = 0;
		for(; row < tableData.size(); row++)
		{
			String cellText = ((JLabel) tableData.get(row).get(0)).getText();
			if(cellText.equals(filename))
				break;
		}
		
		if(row == tableData.size())
			return -1;
		else
			return row; 
	}
}


























