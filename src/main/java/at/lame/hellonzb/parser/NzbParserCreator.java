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

import java.io.*;
import javax.swing.JOptionPane;
import javax.xml.stream.XMLStreamException;
import at.lame.hellonzb.*;
import at.lame.hellonzb.util.MyLogger;


public class NzbParserCreator extends Thread
{
	private HelloNzb mainApp;
	private MyLogger logger;
	private String filename;
	
	
	public NzbParserCreator(HelloNzb mainApp, String filename)
	{
		this.mainApp = mainApp;
		this.logger = mainApp.getLogger();
		this.filename = filename;
	}
	
	public void run()
	{
		// set background progress bar
		mainApp.getTaskManager().loadNzb(true);

		try
		{
			NzbParser parser = new NzbParser(mainApp, filename);
			mainApp.addNzbToQueue(parser);
			
			// delete loaded nzb file?
			try
			{
				if(mainApp.getPrefValue("GeneralSettingsDelNzbAfterLoading").equals("true"))
				{
					File file = new File(filename);
					if(file.isFile())
						file.delete();
				}
			}
			catch(Exception e)
			{
				logger.printStackTrace(e);
			}
		}
		catch(IOException e)
		{
			String msg = mainApp.getLocaler().getBundleText("PopupCannotOpenNzb"); 
			String title = mainApp.getLocaler().getBundleText("PopupErrorTitle");
			JOptionPane.showMessageDialog(null, msg, title, JOptionPane.ERROR_MESSAGE);
			logger.printStackTrace(e);
		}
		catch(XMLStreamException e)
		{
			String msg = mainApp.getLocaler().getBundleText("PopupXMLParserError"); 
			String title = mainApp.getLocaler().getBundleText("PopupErrorTitle");
			JOptionPane.showMessageDialog(null, msg, title, JOptionPane.ERROR_MESSAGE);
			logger.printStackTrace(e);
		}
		catch(java.text.ParseException e)
		{
			String msg = e.getMessage(); 
			String title = mainApp.getLocaler().getBundleText("PopupErrorTitle");
			JOptionPane.showMessageDialog(null, msg, title, JOptionPane.ERROR_MESSAGE);
			logger.printStackTrace(e);
		}
		finally
		{
			// unset background progress bar
			mainApp.getTaskManager().loadNzb(false);
		}
	}
}
