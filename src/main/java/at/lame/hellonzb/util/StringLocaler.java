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
package at.lame.hellonzb.util;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;


/**
 * This class is used to retreive a locale string for the users language.
 * If the user has set another language for him, then this setting is used.
 * Hence this is the only class that reads a program config parameter w/o
 * the Preferences class.
 * 
 * @author Matthias F. Brandstetter
 */
public class StringLocaler
{
	private Locale locale;
	

	/** Class constructor. */
	public StringLocaler()
	{
		String defLang = Locale.getDefault().getLanguage();
		
		Preferences myPreferences = Preferences.userNodeForPackage(at.lame.hellonzb.HelloNzb.class);
		String val = myPreferences.get("ExtendedSettingsChooseLanguage", "");
		
		// has the user set the language?
		if(val.equals("English"))
			defLang = "en";
		else if(val.equals("German"))
			defLang = "de";
		else if(val.equals("Dutch"))
			defLang = "nl";
		else if(val.equals("Turkish"))
			defLang = "tr";
		
		// locale supported?
		String lang = "en";
		if(defLang.equals("de")) // German
			lang = "de";
		else if(defLang.equals("nl")) // Dutch
			lang = "nl";
		else if(defLang.equals("tr")) // Turkish
			lang = "tr";
		
		// set language to use
		this.locale = new Locale(lang);
	}
		
	/**
	 * Returns the Locale object used for this String localer.
	 * 
	 * @return The localer used (is also the JVM default localer now)
	 */
	public Locale getLocale()
	{
		return locale;
	}
	
	/**
	 * Read and return the text value of a bundle element.
	 * These elements are used as a convenient way to support
	 * internationalization of the application.
	 * 
	 * @param elem The element's name to select the according text value
	 * @return The text value of the element passed to this method
	 */
	public String getBundleText(String elem)
	{
		if(elem == null || elem.isEmpty())
			return "[unspecified]";
		
		String baseName = "resources.HelloNzb";
		String elemText = "";
		
		// load resource
		try
		{
			ResourceBundle bundle = ResourceBundle.getBundle(baseName, locale);
			elemText = bundle.getString(elem);
		}
		catch(MissingResourceException ex)
		{
			// element not found
			elemText = "[" + elem + " not defined]";
		}
		
		return elemText;
	}
}
