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
package at.lame.hellonzb.parser;

import java.util.*;


/**
 * This class is used by the NZB parser for misc. string manipulations.
 * 
 * @author Matthias F. Brandstetter
 * @author R�al Gagnon
 */
public class StringUtils 
{
	private static HashMap<String,String> htmlEntities;
	
	static 
	{
	    htmlEntities = new HashMap<String,String>();
	    htmlEntities.put("&lt;","\u003c")    ; htmlEntities.put("&gt;","\u003e");
	    htmlEntities.put("&amp;","\u0026")   ; htmlEntities.put("&quot;","\"");
	    htmlEntities.put("&agrave;","\u00e0"); htmlEntities.put("&Agrave;","\u00c0");
	    htmlEntities.put("&acirc;","\u00e2") ; htmlEntities.put("&auml;","\u00e4");
	    htmlEntities.put("&Auml;","\u00c4")  ; htmlEntities.put("&Acirc;","\u00c2");
	    htmlEntities.put("&aring;","\u00e5") ; htmlEntities.put("&Aring;","\u00c5");
	    htmlEntities.put("&aelig;","\u00e6") ; htmlEntities.put("&AElig;","\u00c6" );
	    htmlEntities.put("&ccedil;","\u00e7"); htmlEntities.put("&Ccedil;","\u00c7");
	    htmlEntities.put("&eacute;","\u00e9"); htmlEntities.put("&Eacute;","\u00c9" );
	    htmlEntities.put("&egrave;","\u00e8"); htmlEntities.put("&Egrave;","\u00c8");
	    htmlEntities.put("&ecirc;","\u00ea") ; htmlEntities.put("&Ecirc;","\u00ca");
	    htmlEntities.put("&euml;","\u00eb")  ; htmlEntities.put("&Euml;","\u00cb");
	    htmlEntities.put("&iuml;","\u00ef")  ; htmlEntities.put("&Iuml;","\u00cF");
	    htmlEntities.put("&ocirc;","\u00f4") ; htmlEntities.put("&Ocirc;","\u00d4");
	    htmlEntities.put("&ouml;","\u00f6")  ; htmlEntities.put("&Ouml;","\u00d6");
	    htmlEntities.put("&oslash;","\u00f8"); htmlEntities.put("&Oslash;","\u00d8");
	    htmlEntities.put("&szlig;","\u00df") ; htmlEntities.put("&ugrave;","\u00f9");
	    htmlEntities.put("&Ugrave;","\u00d9"); htmlEntities.put("&ucirc;","\u00fb");
	    htmlEntities.put("&Ucirc;","\u00db") ; htmlEntities.put("&uuml;","\u00fc");
	    htmlEntities.put("&Uuml;","\u00dc")  ; htmlEntities.put("&nbsp;","\u00a0");
	    htmlEntities.put("&copy;","\u00a9"); htmlEntities.put("&reg;","\u00ae");
	    htmlEntities.put("&euro;","\u20a0");
	}

	/**
	 * This method is used to unescape a string (i.e. remove
	 * all HTML special characters).
	 * 
	 * @param source The string to unescape
	 * @param start The index within the string to start from
	 * @return The unescaped string
	 */
	public static final String unescapeHTML(String source, int start)
	{
		int i, j;

		i = source.indexOf("&", start);
		if(i > -1) 
		{
			j = source.indexOf(";", i);
			if (j > i) 
			{
				String entityToLookFor = source.substring(i, j + 1);
				String value = (String)htmlEntities.get(entityToLookFor);
				if(value != null) 
				{
					source = new StringBuffer().append(source.substring(0, i))
										.append(value)
										.append(source.substring(j + 1))
										.toString();
					return unescapeHTML(source, i + 1); // recursive call
				}
			}
		}
		
		return source;
	}
	
	/**
	 * This method is used to unescape a string (i.e. remove
	 * all HTML special characters). This version of the method
	 * unescapes the whole string.
	 * 
	 * @param source The string to unescape
	 * @return The unescaped string
	 */
	public static final String unescapeHTML(String source)
	{
		return unescapeHTML(source, 0);
	}
	
	/**
	 * This method tries to "guess" and extract the filename out of the subject
	 * of a usenet posting.
	 * 
	 * @param subject The subject line to parse
	 * @return The guessed filename
	 */
	public static final String getFilenameFromSubject(String subject)
	{
		String guessedName = subject;
		String [] fileExtList = new String [] { ".mp3",
												".zip",
												".rar",
												".txt",
												".png",
												".jpg" };
		
		// first check for quoted string ('..."filename.ext"...')
		if(guessedName.indexOf("\"") != -1)
		{
			guessedName = guessedName.substring(guessedName.indexOf("\"") + 1, guessedName.length());
			if(guessedName.indexOf("\"") != -1)
			{
				guessedName = guessedName.substring(0, guessedName.indexOf("\""));
			}
		}
		
		// check for known filename extension
		else
		{
			for(int i = 0; i < fileExtList.length; i++)
			{
				String ext = fileExtList[i];
				if(guessedName.indexOf(ext) != -1)
				{
					guessedName = guessedName.substring(0, guessedName.indexOf(ext) + 4);
					i = fileExtList.length;
				}
			}
		}
		
		if(guessedName.equals(""))
			guessedName = subject;
		
		return guessedName;
	}
}



















