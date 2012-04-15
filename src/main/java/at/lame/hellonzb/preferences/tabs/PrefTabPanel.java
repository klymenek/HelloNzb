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
import java.util.HashMap;
import at.lame.hellonzb.*;
import at.lame.hellonzb.util.*;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


public abstract class PrefTabPanel
{
	protected final JPanel panel;
	protected final HelloNzb mainApp;
	protected final StringLocaler localer;
	protected final HashMap<String,String> prefMap;
	
	
	public abstract String [] keys();
	
	public PrefTabPanel(HelloNzb h, HashMap<String,String> prefMap)
	{
		this.panel = new JPanel();
		this.mainApp = h;
		this.localer = h.getLocaler();
		this.prefMap = prefMap;
	}
	
	public JPanel getJPanel()
	{
		return panel;
	}
	
	/**
	 * Create a new checkbox with the specified values.
	 *
	 * @param cc The component container object contains the label and component to use (re-set)
	 * @param name The name of the new checkbox
	 * @param defaultValue The default value of the new checkbox
	 */
	protected void createCheckbox(CompContainer cc, String name, boolean defaultValue)
	{
		// create label
		cc.label = new JLabel(localer.getBundleText(name));
		cc.label.setName("Label-" + name);
		cc.label.setFont(cc.label.getFont().deriveFont(Font.PLAIN));
		
		// create component
		boolean value = defaultValue;
		String pref = prefMap.get(name);
		if(pref == null || pref.length() == 0)
			value = defaultValue;
		else if(!prefMap.get(name).equals(String.valueOf(defaultValue)))
			value = !defaultValue;
		cc.comp = new JCheckBox("", value);
		cc.comp.setName(name);
	}
	
	/**
	 * Create a new combobox with the specified values.
	 * 
	 * @param cc The component container object contains the label and component to use (re-set)
	 * @param name The name of the new checkbox
	 * @param values The values to fill the combobox with
	 * @param defaultValue The default value of the new checkbox
	 */
	protected void createCombobox(CompContainer cc, String name, String [] values, int defaultValue)
	{
		// create label
		cc.label = new JLabel(localer.getBundleText(name));
		cc.label.setName("Label-" + name);
		cc.label.setFont(cc.label.getFont().deriveFont(Font.PLAIN));
		
		// create component
		cc.comp = new JComboBox(values);
		cc.comp.setName(name);

		// set value in combobox
		((JComboBox)cc.comp).setSelectedIndex(defaultValue);
		String pref = prefMap.get(name);
		if(pref != null && pref.length() > 0)
		{
			for(int i = 0; i < values.length; i++)
				if(values[i].equals(prefMap.get(name)))
				{
					((JComboBox)cc.comp).setSelectedIndex(i);
					break;
				}
		}
	}
	
	/**
	 * Create a new textfield with the specified values.
	 * 
	 * @param cc The component container object contains the label and component to use (re-set)
	 * @param name The name of the new textfield
	 * @param type The type of the new textfield
	 */
	protected void createTextfield(CompContainer cc, String name, String type)
	{
		// create label
		cc.label = new JLabel(localer.getBundleText(name));
		cc.label.setName("Label-" + name);
		cc.label.setFont(cc.label.getFont().deriveFont(Font.PLAIN));
		
		// create component
		if(type.equals("JTextField"))
			cc.comp = new JTextField(prefMap.get(name));
		else if(type.equals("JPasswordField"))
			cc.comp = new JPasswordField(prefMap.get(name));
		else if(type.equals("NumericTextField"))
			cc.comp = new NumericTextField(prefMap.get(name));
		cc.comp.setName(name);
	}
	
	/**
	 * Create a new JSlider object.
	 * 
	 * @param cc The ComponentContainer object to use
	 * @param name The name of the new slider
	 * @param min The minimum value of the new slider
	 * @param max The maximum value of the new slider
	 * @param t1 Minor tick spacing
	 * @param t2 Major tick spacing
	 * @param pointer Default value (between min and max)
	 * @param suffix The suffix to add for the extra label
	 */
	protected void createSlider(CompContainer cc, String name, int min, int max, 
			int t1, int t2, int pointer, final String suffix)
	{
		cc.label = new JLabel(localer.getBundleText(name));
		cc.label.setName("Label-" + name);
		cc.label.setFont(cc.label.getFont().deriveFont(Font.PLAIN));

		cc.extraLabel = new JLabel("", JLabel.CENTER);
		cc.extraLabel.setName("ChgValue-" + name);
		cc.extraLabel.setFont(cc.extraLabel.getFont().deriveFont(Font.PLAIN));
		final JLabel extra = cc.extraLabel;
		
		String preValue = prefMap.get(name);
		if(preValue == null || preValue.equals("")) // if setting is found in prefs
			cc.extraLabel.setText(Integer.toString(pointer) + suffix);
		else
		{
			pointer = Integer.parseInt(preValue);
			cc.extraLabel.setText(preValue + suffix);
		}
		
		cc.comp = new JSlider(min, max, pointer);
		cc.comp.setName(name);
		
		((JSlider)cc.comp).setPaintTicks(true);
		((JSlider)cc.comp).setSnapToTicks(true);
		((JSlider)cc.comp).setMinorTickSpacing(t1);
		((JSlider)cc.comp).setMajorTickSpacing(t2);
		((JSlider)cc.comp).addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent e)
			{
				extra.setText(Integer.toString(
						((JSlider)e.getSource()).getValue()) + suffix); 
			} 
		} ); 
	}
	
	
	///////////////////////////////////////////////////////////////////////////////////////////////
	protected class CompContainer
	{
		public JLabel label;
		public JComponent comp;
		public JLabel extraLabel;
		
		public CompContainer(JLabel label, JComponent comp)
		{
			this.label = label;
			this.comp = comp;
			this.extraLabel = null;
		}
		
		public CompContainer(JLabel label, JComponent comp, JLabel extra)
		{
			this.label = label;
			this.comp = comp;
			this.extraLabel = extra;
		}
	}
}


















































