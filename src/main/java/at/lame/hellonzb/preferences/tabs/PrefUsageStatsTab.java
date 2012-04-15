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

import java.awt.Font;
import java.util.*;
import javax.swing.*;

import at.lame.hellonzb.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.layout.*;



public class PrefUsageStatsTab extends PrefTabPanel
{
	protected static final String [] keys = new String [] { "UsageStatsSettingsActivate" };
	
	public static String [] getKeys() { return keys; }
	
	protected JCheckBox activateUsageStats;
	
	
	public PrefUsageStatsTab(HelloNzb h, HashMap<String,String> prefMap)
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
                "p, 3dlu, p, 9dlu, p"); // rows
        
        // create builder
        PanelBuilder builder = new PanelBuilder(layout, panel);
        builder.setDefaultDialogBorder();
        CellConstraints cc = new CellConstraints();
        
        // fill the grid with components

        //////////////////////////////////////////////////////////////////////
        // group "usage statistics"
        sepString = localer.getBundleText("PrefDialogUsageStatsSettingsPane");
        builder.addSeparator(sepString, cc.xyw(1, 1, 3));

        // actiave usage statistics?
        createCheckbox(compc, "UsageStatsSettingsActivate", true);
        activateUsageStats = ((JCheckBox)compc.comp);
        builder.add(compc.label, cc.xy(1, 3));
        builder.add(compc.comp,  cc.xy(3, 3));
        
        // info label
		JLabel infotxt = new JLabel(localer.getBundleText("UsageStatsSettingsInfoTxt"));
		infotxt.setName("Label-UsageStatsSettingsInfoTxt");
		infotxt.setFont(infotxt.getFont().deriveFont(Font.PLAIN));
		builder.add(infotxt, cc.xy(3, 5));
	}
	
	@Override
	public String [] keys()
	{
		return getKeys();
	}
}














































