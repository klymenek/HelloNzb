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

import java.awt.Toolkit;

import javax.swing.JTextField;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;


/**
 * This class is used as an text input field that permits
 * only numerical input.
 * 
 * @author unknown
 * @see http://www.javaswing.net/numeric-number-only-jtextfield-in-swing-ready-made-code.html
 */
public class NumericTextField extends JTextField
{	 
	// Add other constructors as required. If you do,
	// be sure to call the "addFilter" method
	public NumericTextField(String text)
	{
		super(text);
		addFilter();
	}
 
	//Add an instance of NumericDocumentFilter as a 
	//document filter to the current text field
	private void addFilter()
	{
		((AbstractDocument)this.getDocument()).
				setDocumentFilter(new NumericDocumentFilter());
	}
  
	class NumericDocumentFilter extends DocumentFilter
	{
		public void insertString(FilterBypass fb, 
				int offset, String string, AttributeSet attr) 
				throws BadLocationException 
		{
			if(string == null)
				return;
			if(isStringNumeric(string))
				super.insertString(fb, offset, string, attr);
			else
				Toolkit.getDefaultToolkit().beep();
		}
 
		public void replace(FilterBypass fb, int offset, 
				int length, String text, AttributeSet attrs) 
				throws BadLocationException
		{
			if(text == null)
				return;
			if(isStringNumeric(text))
				super.replace(fb, offset, length, text, attrs);
			else
				Toolkit.getDefaultToolkit().beep();
		}
 
		private boolean isStringNumeric(String string)
		{
			char[] characters = string.toCharArray();
     
			for(char c: characters)
			{
				if(!Character.isDigit(c))
					return false;
			}
     
			return true;
		}
	}
}
