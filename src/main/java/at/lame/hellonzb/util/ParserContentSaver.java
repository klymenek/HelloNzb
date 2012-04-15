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

package at.lame.hellonzb.util;

import java.io.*;
import java.util.Vector;
import javax.swing.*;

import at.lame.hellonzb.*;
import at.lame.hellonzb.parser.*;


public class ParserContentSaver extends Thread
{
	private HelloNzbCradle mainApp;
	private Vector<NzbParser> parserList;
	private Vector<DownloadFile> first;
	
	
	public ParserContentSaver(HelloNzbCradle mainApp, Vector<NzbParser> parserList, Vector<DownloadFile> first)
	{
		this.mainApp = mainApp;
		this.parserList = parserList;
		this.first = first;
	}
	
	public void run()
	{
		boolean success = false; 

		HelloNzbToolkit.emptyDir(new File(System.getProperty("user.home") + "/.HelloNzb/"));

		for(int i = 0; i < parserList.size(); i++)
		{
			NzbParser nzbFile = parserList.elementAt(i);
			String nzbFileName = HelloNzbToolkit.getLastFilename(nzbFile.getName());
			
			if(i == 0)
				success = NzbParser.saveParserData(mainApp.getLogger(), i, nzbFileName, first);
			else
				success = NzbParser.saveParserData(mainApp.getLogger(), i, nzbFileName, nzbFile.getFiles());

/*			if(success == false)
			{
				String msg = localer.getBundleText("PopupCantSaveNzbFile"); 
				String title = localer.getBundleText("PopupErrorTitle");
				JOptionPane.showMessageDialog(mainApp.getJFrame(), msg, title, JOptionPane.ERROR_MESSAGE);
				
				return;
			}
*/		}
		
		mainApp.contentSaverDone();
	}
}









































