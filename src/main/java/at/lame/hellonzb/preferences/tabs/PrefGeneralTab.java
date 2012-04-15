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


public class PrefGeneralTab extends PrefTabPanel
{
	protected static final String [] keys = new String [] { "GeneralSettingsDownloadDir",
															"GeneralSettingsAutoLoadDir",
															"GeneralSettingsDelNzbAfterLoading",
															"GeneralSettingsCheckSegments",
															"GeneralSettingsShowTrayIcon",
															"GeneralSettingsCheckForUpdates" };
	
	public static String [] getKeys() { return keys; }
	
	/** "choose dir" button on general settings tab (download dir) */
	private JButton downloadDirChooseButton;
	
	/** "choose dir" button on general settings tab (auto-load dir) */
	private JButton autoLoadDirChooseButton;
	
	
	public PrefGeneralTab(HelloNzb h, HashMap<String,String> prefMap)
	{
		super(h, prefMap);
		createPanel();
	}
	
	private void createPanel()
	{
        String sepString = null;
		CompContainer compc = new CompContainer(null, null);
		downloadDirChooseButton = new JButton(localer.getBundleText("GeneralSettingsChooseDownloadDir"));
		downloadDirChooseButton.setName("GeneralSettingsChooseDownloadDir-Button");
		autoLoadDirChooseButton = new JButton(localer.getBundleText("GeneralSettingsChooseAutoLoadDir"));
		autoLoadDirChooseButton.setName("GeneralSettingsChooseAutoLoadDir-Button");
		
		
		// create layout for this tab/panel
        FormLayout layout = new FormLayout(
                "right:pref, 3dlu, [200dlu,pref]:grow, 3dlu, pref", // cols
        		"p, 3dlu, p, 3dlu, p, 9dlu, p, 3dlu, p, 9dlu, p, 3dlu, p, 3dlu, p"); // rows
        
        // create builder
        PanelBuilder builder = new PanelBuilder(layout, panel);
        builder.setDefaultDialogBorder();
        CellConstraints cc = new CellConstraints();
        
        // fill the grid with components
        
        //////////////////////////////////////////////////////////////////////
        // group "general settings"
        sepString = localer.getBundleText("GeneralSettingsGroupGeneralSettings");
        builder.addSeparator(sepString, cc.xyw(1, 1, 5));

        // download directory
        createTextfield(compc, "GeneralSettingsDownloadDir", "JTextField");
        builder.add(compc.label, 			 cc.xy(1, 3));
        builder.add(compc.comp,  			 cc.xy(3, 3));
        builder.add(downloadDirChooseButton, cc.xy(5, 3));
        
        // auto-load directory
        createTextfield(compc, "GeneralSettingsAutoLoadDir", "JTextField");
        builder.add(compc.label, 			 cc.xy(1, 5));
        builder.add(compc.comp,  			 cc.xy(3, 5));
        builder.add(autoLoadDirChooseButton, cc.xy(5, 5));
        
        //////////////////////////////////////////////////////////////////////
        // group "nzb file loading"
        sepString = localer.getBundleText("GeneralSettingsGroupNzbLoadBehaviour");
        builder.addSeparator(sepString, cc.xyw(1, 7, 5));

		// delete nzb file after loading
		createCheckbox(compc, "GeneralSettingsDelNzbAfterLoading", true);
        builder.add(compc.label, cc.xy(1, 9));
        builder.add(compc.comp,  cc.xy(3, 9));
		
        //////////////////////////////////////////////////////////////////////
        // group "program settings"
        sepString = localer.getBundleText("GeneralSettingsGroupProgramSettings");
        builder.addSeparator(sepString, cc.xyw(1, 11, 5));
		
        // show system tray icon
		createCheckbox(compc, "GeneralSettingsShowTrayIcon", false);
        builder.add(compc.label, cc.xy(1, 13));
        builder.add(compc.comp,  cc.xy(3, 13));
        
		// check for new progarm version
		createCheckbox(compc, "GeneralSettingsCheckForUpdates", true);
        builder.add(compc.label, cc.xy(1, 15));
        builder.add(compc.comp,  cc.xy(3, 15));
	}
	
	@Override
	public String [] keys()
	{
		return getKeys();
	}
	
	public JButton getDlDirButton()
	{
		return downloadDirChooseButton;
	}
	
	public JButton getAutoLoadButton()
	{
		return autoLoadDirChooseButton;
	}
}















































