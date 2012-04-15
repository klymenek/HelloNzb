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
package at.lame.hellonzb.renderer;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;


public class ProgressRenderer extends JProgressBar implements TableCellRenderer
{
	private boolean showTooltip;
	
	
	public ProgressRenderer(boolean showTooltip)
	{
		super(SwingConstants.HORIZONTAL);
		setBorderPainted(false);
		setStringPainted(true);
		
		this.showTooltip = showTooltip;
	}
	
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) 
	{
		if(value != null)
		{
			// set current values of progress bar
			setString(((JProgressBar) value).getString());
			setBackground(((JProgressBar) value).getBackground());
			
			// indeterminate progress bar?
			if(((JProgressBar) value).isIndeterminate())
				setIndeterminate(true);
			else
			{
				setIndeterminate(false);
				setMinimum(((JProgressBar) value).getMinimum());
				setMaximum(((JProgressBar) value).getMaximum());
				setValue(((JProgressBar) value).getValue());
				
				// tooltip
				if(showTooltip)
					setToolTipText(((JProgressBar) value).getString());
			}
			
			// row selected?
			if(isSelected)
				setBackground(table.getSelectionBackground());
		}
			
		return this;
	}
	
	public boolean isDisplayable() 
	{
		return true;
	}
}

	




















