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



public class PrefDownloadTab extends PrefTabPanel
{
	protected static final String [] keys = new String [] { "DownloadSettingsMaxConnectionSpeed",
															"DownloadSettingsIgnoreCrc32Error",
															"DownloadSettingsExtractRARArchives",
															"DownloadSettingsPar2Check",
															"DownloadSettingsPar2ExeLocation",
															"DownloadSettingsExtractRARArchives",
															"DownloadSettingsUnrarExeLocation" };
	
	public static String [] getKeys() { return keys; }
	
	/** "choose par2 exec" button on download settings tab */
	protected JButton par2CmdLineAppButton;
	
	/** "choose unrar exec" button on download settings tab */
	protected JButton unrarCmdLineAppButton;
	
	/** path to par2 exec */
	protected JTextField par2LocationTextField;
	
	/** path to unrar exec */
	protected JTextField unrarLocationTextField;
	
	/** auto PAR2 archive verification checkbox */
	protected JCheckBox par2ExtractCheckbox;
	
	/** auto RAR archive extraction checkbox */
	protected JCheckBox rarExtractCheckbox;
	
	
	public PrefDownloadTab(HelloNzb h, HashMap<String,String> prefMap)
	{
		super(h, prefMap);
		createPanel();
	}
	
	private void createPanel()
	{
		String sepString = null;
		CompContainer compc = new CompContainer(null, null);

		
		par2CmdLineAppButton = new JButton(localer.getBundleText("DownloadSettingsPar2ChooseButton"));
		par2CmdLineAppButton.setName("DownloadSettingsPar2ExeLocation-Button");
		unrarCmdLineAppButton = new JButton(localer.getBundleText("DownloadSettingsPar2ChooseButton"));
		unrarCmdLineAppButton.setName("DownloadSettingsUnrarExeLocation-Button");		
		
		// create layout for this tab/panel
        FormLayout layout = new FormLayout(
                "right:pref, 3dlu, [100dlu,pref]:grow, 3dlu, pref", // cols
				"p, 3dlu, p, 9dlu, p, 3dlu, p, 3dlu, p, 9dlu, p, 3dlu, p, 3dlu, p");	// rows
        
        // create builder
        PanelBuilder builder = new PanelBuilder(layout, panel);
        builder.setDefaultDialogBorder();
        CellConstraints cc = new CellConstraints();
        
        // fill the grid with components
        
        //////////////////////////////////////////////////////////////////////
        // group "connection"
        sepString = localer.getBundleText("DownloadSettingsGroupConnection");
        builder.addSeparator(sepString, cc.xyw(1, 1, 5));
        
        // max connection speed
		createTextfield(compc, "DownloadSettingsMaxConnectionSpeed", "NumericTextField");
		compc.label.setText(compc.label.getText() + " (KB/s)");
        builder.add(compc.label, cc.xy(1, 3));
        builder.add(compc.comp,  cc.xy(3, 3));

        //////////////////////////////////////////////////////////////////////
        // group "par2 check"
        sepString = localer.getBundleText("DownloadSettingsGroupPar2Check");
        builder.addSeparator(sepString, cc.xyw(1, 5, 5));

        // par2 check after download
		createCheckbox(compc, "DownloadSettingsPar2Check", false);
        par2ExtractCheckbox = (JCheckBox) compc.comp;
        builder.add(compc.label, cc.xy(1, 7));
        builder.add(compc.comp,  cc.xy(3, 7));
		
		// path to par2 command line tool
		createTextfield(compc, "DownloadSettingsPar2ExeLocation", "JTextField");
		par2LocationTextField = (JTextField) compc.comp;
        builder.add(compc.label, 		  cc.xy(1, 9));
        builder.add(compc.comp,  		  cc.xy(3, 9));
        builder.add(par2CmdLineAppButton, cc.xy(5, 9));
        
		if(!par2ExtractCheckbox.isSelected())
		{
			par2LocationTextField.setEnabled(false);
			par2CmdLineAppButton.setEnabled(false);
		}
		
        //////////////////////////////////////////////////////////////////////
        // group "RAR archives"
        sepString = localer.getBundleText("DownloadSettingsGroupArcExtract");
        builder.addSeparator(sepString, cc.xyw(1, 11, 5));

		// automatically extract (RAR) archive files
		createCheckbox(compc, "DownloadSettingsExtractRARArchives", false);
		rarExtractCheckbox = (JCheckBox) compc.comp;
        builder.add(compc.label, cc.xy(1, 13));
        builder.add(compc.comp,  cc.xy(3, 13));

		// path to unrar command line tool
		createTextfield(compc, "DownloadSettingsUnrarExeLocation", "JTextField");
		unrarLocationTextField = (JTextField) compc.comp;
        builder.add(compc.label, 		   cc.xy(1, 15));
        builder.add(compc.comp,  		   cc.xy(3, 15));
        builder.add(unrarCmdLineAppButton, cc.xy(5, 15));
        
		if(!rarExtractCheckbox.isSelected())
		{
			unrarLocationTextField.setEnabled(false);
			unrarCmdLineAppButton.setEnabled(false);
		}
	}
	
	@Override
	public String [] keys()
	{
		return getKeys();
	}

	public JButton getPar2Button()
	{
		return par2CmdLineAppButton;
	}
	
	public JButton getUnrarButton()
	{
		return unrarCmdLineAppButton;
	}

	public JTextField getPar2LocationTextField()
	{
		return par2LocationTextField;
	}

	public JTextField getUnrarLocationTextField()
	{
		return unrarLocationTextField;
	}

	public JCheckBox getPar2ExtractCheckbox()
	{
		return par2ExtractCheckbox;
	}

	public JCheckBox getRarExtractCheckbox()
	{
		return rarExtractCheckbox;
	}
}















































