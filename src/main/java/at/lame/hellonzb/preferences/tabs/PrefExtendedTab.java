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

package at.lame.hellonzb.preferences.tabs;

import java.util.*;
import javax.swing.*;

import at.lame.hellonzb.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.layout.*;



public class PrefExtendedTab extends PrefTabPanel
{
	protected static final String [] keys = new String [] { "ExtendedSettingsChooseLanguage",
															"ExtendedSettingsConsoleOutput" };
	
	public static String [] getKeys() { return keys; }
	
	/** "reset all messages" button on extended settings tab */
	protected JButton resetAllMsg;
	
	
	public PrefExtendedTab(HelloNzb h, HashMap<String,String> prefMap)
	{
		super(h, prefMap);
		createPanel();
	}
	
	private void createPanel()
	{
		CompContainer compc = new CompContainer(null, null);
		String sepString = null;

		
		// create layout for this tab/panel
        FormLayout layout = new FormLayout(
                "right:pref, 3dlu, pref:grow", 	 // cols
                "p, 3dlu, p, 9dlu, p, 3dlu, p, 9dlu, p, 9dlu, p"); // rows
        
        // create builder
        PanelBuilder builder = new PanelBuilder(layout, panel);
        builder.setDefaultDialogBorder();
        CellConstraints cc = new CellConstraints();
        
        // fill the grid with components
        
        //////////////////////////////////////////////////////////////////////
        // group "locale settings"
        sepString = localer.getBundleText("ExtendedSettingsGroupLocaleSettings");
        builder.addSeparator(sepString, cc.xyw(1, 1, 3));

        // choose language
        String [] boxvalues = new String[] { "-default-", "English", "German", "Dutch", "Turkish" };
        createCombobox(compc, "ExtendedSettingsChooseLanguage", boxvalues, 0);
        builder.add(compc.label, cc.xy(1, 3));
        builder.add(compc.comp,  cc.xy(3, 3));

        //////////////////////////////////////////////////////////////////////
        // group "console output"
        sepString = localer.getBundleText("ExtendedSettingsGroupConsole");
        builder.addSeparator(sepString, cc.xyw(1, 5, 3));

        // activate console output 
		createCheckbox(compc, "ExtendedSettingsConsoleOutput", true);
        builder.add(compc.label, cc.xy(1, 7));
        builder.add(compc.comp,  cc.xy(3, 7));
        
        //////////////////////////////////////////////////////////////////////
        // group "program settings"
        sepString = localer.getBundleText("GeneralSettingsGroupProgramSettings");
        builder.addSeparator(sepString, cc.xyw(1, 9, 3));
        
        // reset all hidden messages
        resetAllMsg = new JButton(localer.getBundleText("ExtendedSettingsResetMessages"));
        builder.add(resetAllMsg, cc.xyw(1, 11, 3));
	}
	
	@Override
	public String [] keys()
	{
		return getKeys();
	}
	
	public JButton getResetButton()
	{
		return resetAllMsg;
	}
}














































