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

package at.lame.hellonzb.unrar;

import java.io.*;
import java.util.Vector;
import at.lame.hellonzb.*;
import at.lame.hellonzb.parser.*;
import at.lame.hellonzb.util.*;

/**
 * This class is used as background thread to extract a RAR archive.
 * 
 * @author Matthias F. Brandstetter
 */
public class RarExtractor implements Runnable
{
	private final String UNRAR_DIR = "RAR_EXTRACT";

	/** main application object */
	private HelloNzb mainApp;
	
	/** parser object to process */
	private NzbParser parser;
	
	/** central logger object */
	private MyLogger logger;
	
	/** download directory from application preferences */
	private File dlDir;
	
	
	public RarExtractor(HelloNzb mainApp, NzbParser parser) throws RuntimeException
	{
		this.mainApp = mainApp;
		this.logger = mainApp.getLogger();
		this.parser = parser;

		// download directory
		String rootDir = mainApp.getPrefValue("GeneralSettingsDownloadDir");
		String nzbFilename = parser.getName();
		nzbFilename = HelloNzbToolkit.getLastFilename(nzbFilename);
		dlDir = new File(rootDir + File.separator + nzbFilename);
		if(!dlDir.isDirectory())
			throw new RuntimeException("Invalid directory for RAR extraction");
	}
	
	public void run()
	{
		String rarFilename = "";
		
		// search all files within destination directory
		String filenames[] = dlDir.list();
		if(filenames == null)
			return;
		else
			java.util.Arrays.sort(filenames);
		
		for(String filename : filenames)
		{
			if(filename.toLowerCase().endsWith(".rar"))
			{
				rarFilename = filename;
				break;
			}
		}
		
		// valid rar filename found?
		try
		{
			if(!rarFilename.isEmpty())
			{
				logger.msg("Start to extract RAR archive to " + dlDir, MyLogger.SEV_INFO);
				
				// use the following line for the Unrar command line utility
				callUnrarCmdLine(rarFilename, dlDir);
			}
			else
				logger.msg("No .rar file for extraction found", MyLogger.SEV_INFO);
		}
		catch(Exception ex)
		{
			logger.printStackTrace(ex);
		}
		finally
		{
			mainApp.rarExtractDone(parser);
		}
	}
	
	private void callUnrarCmdLine(String rarFilename, File destination) 
			throws IOException, InterruptedException
	{
		Vector<String> cmd = new Vector<String>();
		String finalDestDir = destination + File.separator + UNRAR_DIR;
		File finalDest = new File(finalDestDir);
	
		// check if destination directory exists
		if(!finalDest.exists())
			finalDest.mkdirs();
		if(!finalDest.exists())
		{
			logger.msg("could not create target archive for RAR extraction", MyLogger.SEV_ERROR);
			return;
		}
		
		// yes, do the unrar extract!
		String execLocation = mainApp.getPrefValue("DownloadSettingsUnrarExeLocation");
		
		// create command to execute
		cmd.add(execLocation);
		cmd.add("x");
		cmd.add("-p-");
		cmd.add("-y");
		cmd.add("-r");
		cmd.add("-idp");
		cmd.add(rarFilename);
		cmd.add(UNRAR_DIR);
		
		logger.msg("Starting Unrar extraction: " + cmd, MyLogger.SEV_INFO);
		
		// execute command
		String [] cmdArray = cmd.toArray(new String[] {});
		Runtime rt = Runtime.getRuntime();
		Process proc = rt.exec(cmdArray, null, destination);
		StreamGobbler errGobbler = new StreamGobbler(
				logger, proc.getErrorStream(), "ERR");
		StreamGobbler outGobbler = new StreamGobbler(
				logger, proc.getInputStream(), "OUT");
		
		// fetch command's STDOUT and STDERR
		errGobbler.start();
		outGobbler.start();
		
		// wait until program has finished
		int exitVal = proc.waitFor();
		logger.msg("Unrar command exit value: " + exitVal, MyLogger.SEV_INFO);
	}
}









































