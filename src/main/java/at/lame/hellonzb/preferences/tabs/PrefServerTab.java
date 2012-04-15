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
import at.lame.hellonzb.util.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.layout.*;



public class PrefServerTab extends PrefTabPanel
{
	protected static final String [] keys = new String [] { "ServerSettingsServerName",
															"ServerSettingsHost",
															"ServerSettingsPort",
															"ServerSettingsUseSSL",
															"ServerSettingsSSLPort",
															"ServerSettingsThreadCount",
															"ServerSettingsTimeout",
															"ServerSettingsUsername",
															"ServerSettingsPassword" };
	
	public static String [] getKeys() { return keys; }

	/** standard server port text field */
	protected NumericTextField stdServerPort;
	
	/** SSL server port text field */
	protected NumericTextField sslServerPort;
	
	/** Std/SSL selection check box */
	protected JCheckBox stdSslCB;
	
	
	public PrefServerTab(HelloNzb h, HashMap<String,String> prefMap)
	{
		super(h, prefMap);
		createPanel();
	}
	
	private void createPanel()
	{
        String sepString = null;
		CompContainer compc = new CompContainer(null, null);

		
		// create layout for this tab/panel
        FormLayout layout = new FormLayout(
                "right:pref, 3dlu, pref:grow, 3dlu, [20dlu,pref]",								// cols
        		"p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 9dlu, p, 3dlu, p, 3dlu, p");	// rows
        
        // create builder
        PanelBuilder builder = new PanelBuilder(layout, panel);
        builder.setDefaultDialogBorder();
        CellConstraints cc = new CellConstraints();
        
        // fill the grid with components
        
        //////////////////////////////////////////////////////////////////////
        // group "connection"
        sepString = localer.getBundleText("ServerSettingsGroupConnection");
        builder.addSeparator(sepString, cc.xyw(1, 1, 5));

		// server name
		createTextfield(compc, "ServerSettingsServerName", "JTextField");
        builder.add(compc.label, cc.xy(1, 3));
        builder.add(compc.comp,  cc.xy(3, 3));
		
		// host
		createTextfield(compc, "ServerSettingsHost", "JTextField");
        builder.add(compc.label, cc.xy(1, 5));
        builder.add(compc.comp,  cc.xy(3, 5));
		
		// port
		createTextfield(compc, "ServerSettingsPort", "NumericTextField");
		if(prefMap.get("ServerSettingsPort").equals(""))
			((JTextField)compc.comp).setText("119");
		stdServerPort = (NumericTextField) compc.comp;
        builder.add(compc.label, cc.xy(1, 7));
        builder.add(compc.comp,  cc.xy(3, 7));
        
        // "use SSL connections" checkbox
        createCheckbox(compc, "ServerSettingsUseSSL", false);
        stdSslCB = ((JCheckBox)compc.comp);
        builder.add(compc.label, cc.xy(1, 9));
        builder.add(compc.comp,  cc.xy(3, 9));
        
        // SSL server port number
		createTextfield(compc, "ServerSettingsSSLPort", "NumericTextField");
		if(prefMap.get("ServerSettingsSSLPort").equals(""))
			((JTextField)compc.comp).setText("563");
		sslServerPort = (NumericTextField) compc.comp;
        builder.add(compc.label, cc.xy(1, 11));
        builder.add(compc.comp,  cc.xy(3, 11));
		
		if(!stdSslCB.isSelected())
		{
			stdServerPort.setEnabled(true);
			sslServerPort.setEnabled(false);
		}
		else
		{
			stdServerPort.setEnabled(false);
			sslServerPort.setEnabled(true);
		}
        
		// create slider and add to layout (via new row)
		createSlider(compc, "ServerSettingsTimeout", 30, 310, 10, 40, 60, "s");
        builder.add(compc.label, 	  cc.xy(1, 13));
        builder.add(compc.comp,  	  cc.xy(3, 13));
        builder.add(compc.extraLabel, cc.xy(5, 13));
        
		// create slider and add to layout (via new row)
        createSlider(compc, "ServerSettingsThreadCount", 1, 51, 1, 5, 1, "");
        builder.add(compc.label, 	  cc.xy(1, 15));
        builder.add(compc.comp,  	  cc.xy(3, 15));
        builder.add(compc.extraLabel, cc.xy(5, 15));
	
        //////////////////////////////////////////////////////////////////////
        // group "authentication"
        sepString = localer.getBundleText("ServerSettingsGroupAuthentication");
        builder.addSeparator(sepString, cc.xyw(1, 17, 5));
        
		// username
		createTextfield(compc, "ServerSettingsUsername", "JTextField");
        builder.add(compc.label, cc.xy(1, 19));
        builder.add(compc.comp,  cc.xy(3, 19));
		
		// password
		createTextfield(compc, "ServerSettingsPassword", "JPasswordField");
        builder.add(compc.label, cc.xy(1, 21));
        builder.add(compc.comp,  cc.xy(3, 21));
	}
	
	@Override
	public String [] keys()
	{
		return getKeys();
	}

	public NumericTextField getStdServerPort()
	{
		return stdServerPort;
	}
	
	public NumericTextField getSslServerPort()
	{
		return sslServerPort;
	}
	
	public JCheckBox getStdSslCB()
	{
		return stdSslCB;
	}
}


















































