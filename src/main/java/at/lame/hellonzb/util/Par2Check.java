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

import java.io.*;
import java.util.*;

import at.lame.hellonzb.HelloNzb;
import at.lame.hellonzb.HelloNzbToolkit;
import at.lame.hellonzb.parser.*;
import at.lame.hellonzb.preferences.HelloNzbPreferences;


/**
 * Instanciate this class for a background thread that has
 * to par2 check the specified NzbParser.
 * 
 * @author Matthias F. Brandstetter
 */
public class Par2Check implements Runnable
{
	/** The main application object */
	private HelloNzb mainApp;
	
	/** central logger object */
	private MyLogger logger;
	
	/** The nzb parser object to check */
	private NzbParser parser;
	
	
	public Par2Check(HelloNzb mainApp, NzbParser parser)
	{
		this.mainApp = mainApp;
		this.logger = mainApp.getLogger();
		this.parser = parser;
	}
	
	public void run()
	{
		String par2Filename = "";
		Vector<String> cmd = new Vector<String>();
		
		
		// yes, do the par2 check!
		String execLocation = mainApp.getPrefValue("DownloadSettingsPar2ExeLocation");
		
		// get par2 filename
		String rootDir = mainApp.getPrefValue("GeneralSettingsDownloadDir");
		rootDir += File.separator + HelloNzbToolkit.getLastFilename(parser.getName());
		File nzbDir = new File(rootDir); 
				
		String filenames[] = nzbDir.list();
		for(String filename : filenames)
		{
			if(filename.toLowerCase().endsWith(".par2"))
			{
				par2Filename = filename;
				break;
			}
		}
		
		// valid par2 file found?
		if(!par2Filename.isEmpty())
		{
			// create command to execute
			cmd.add(execLocation);
			cmd.add("repair");
			cmd.add("-q");
			cmd.add(par2Filename);
			
			logger.msg("Starting PAR2 check: " + cmd, MyLogger.SEV_INFO);
			
			try
			{
				String [] cmdArray = cmd.toArray(new String[] {});
				
				// execute command
				Runtime rt = Runtime.getRuntime();
				Process proc = rt.exec(cmdArray, null, nzbDir);
				StreamGobbler errGobbler = new StreamGobbler(
						logger, proc.getErrorStream(), "ERR");
				StreamGobbler outGobbler = new StreamGobbler(
						logger, proc.getInputStream(), "OUT");
				
				// fetch command's STDOUT and STDERR
				errGobbler.start();
				outGobbler.start();
				
				// wait until program has finished
				int exitVal = proc.waitFor();
				logger.msg("PAR2 command exit value: " + exitVal, MyLogger.SEV_INFO);
			}
			catch(IOException e)
			{
				logger.printStackTrace(e);
			}
			catch(InterruptedException e)
			{
				logger.printStackTrace(e);
			}
			finally
			{
				done();
			}
		}
		else
		{
			logger.msg("No .par2 file found", MyLogger.SEV_INFO);
			done();
		}
	}
	
	private void done()
	{
		mainApp.par2CheckDone(parser);
	}
}





































