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

package at.lame.hellonzb.preferences;

import java.awt.event.*;
import javax.swing.*;


/**
 * This class handles the PAR2 checkbox action on the preferences dialog.
 */
public class Par2CBActionListener implements ActionListener
{
	/** main preferences object */
	private HelloNzbPreferences prefMain;
	
	
	public Par2CBActionListener(HelloNzbPreferences pref)
	{
		this.prefMain = pref;
	}
	
	public void actionPerformed(ActionEvent e)
	{
		JCheckBox cb = (JCheckBox) e.getSource();
		if(!cb.isSelected())
		{
			// par2 exe location
			prefMain.par2LocationTextField.setEnabled(false);
			prefMain.par2CmdLineAppButton.setEnabled(false);
			
			// rar extraction checkbox
			prefMain.rarExtractCheckbox.setEnabled(false);
			prefMain.rarExtractCheckbox.setSelected(false);
			
			// unrar command line program location
			prefMain.unrarLocationTextField.setEnabled(false);
			prefMain.unrarCmdLineAppButton.setEnabled(false);
		}
		else
		{
			// par2 exe location
			prefMain.par2LocationTextField.setEnabled(true);
			prefMain.par2CmdLineAppButton.setEnabled(true);

			// rar extraction checkbox
			prefMain.rarExtractCheckbox.setEnabled(true);
			
			// unrar command line program location
			if(prefMain.rarExtractCheckbox.isSelected())
			{
				prefMain.unrarLocationTextField.setEnabled(true);
				prefMain.unrarCmdLineAppButton.setEnabled(true);
			}
		}
	}
}
