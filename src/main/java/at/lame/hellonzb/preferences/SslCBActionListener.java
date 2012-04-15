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
 * This class handles the SSL checkbox action on the preferences dialog.
 */
public class SslCBActionListener implements ActionListener
{
	/** main preferences object */
	private HelloNzbPreferences prefMain;
	
	
	public SslCBActionListener(HelloNzbPreferences pref)
	{
		this.prefMain = pref;
	}

	public void actionPerformed(ActionEvent e)
	{
		JCheckBox cb = (JCheckBox) e.getSource();
		if(!cb.isSelected())
		{
			prefMain.stdServerPort.setEnabled(true);
			prefMain.sslServerPort.setEnabled(false);
		}
		else
		{
			prefMain.stdServerPort.setEnabled(false);
			prefMain.sslServerPort.setEnabled(true);
		}
	}
}
