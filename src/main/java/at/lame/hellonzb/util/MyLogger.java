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

import java.util.*;
import javax.swing.*;
import at.lame.hellonzb.*;


/**
 * Implementation of a Logger class. It handles all console and logging
 * output, based on messages and their severity. It also stores logging
 * messages for later review (or display in a logging window for example).
 * It also handles output of exception stack traces.
 * 
 * @author Matthias F. Brandstetter
 */
public class MyLogger
{
	// logging severity constants (increasing severity)
	public final static int SEV_DEBUG = 10;
	public final static int SEV_INFO = 20;
	public final static int SEV_WARNING = 30;
	public final static int SEV_ERROR = 50;
	public final static int SEV_FATAL = 60;

	/** Logger singelton instance */
	private static MyLogger instance = null;
	
	/** the JTextArea to display loggnig messages */
	private JTextArea logTextArea;
	
	/** main app object */
	private HelloNzbCradle mainApp;
	
	/** severity names */
	private HashMap<Integer,String> sevNames;
	
	/** flag defines whether or not to print exception stack traces */
	private boolean printStackTraces;
	
	/** all messages with severity higher than this level will be printed */
	private int printLevel;
	
	
	/**
	 * Class constructor.
	 * Sets printLevel value to SEV_ERROR.
	 */
	private MyLogger(HelloNzbCradle mainApp, JTextArea logTextArea)
	{
		this.mainApp = mainApp;
		this.logTextArea = logTextArea;
		
		// default values
		this.printStackTraces = true;
		this.printLevel = SEV_ERROR;
		
		// init
		this.sevNames = new HashMap<Integer,String>();
		sevNames.put(SEV_DEBUG, "DEBUG");
		sevNames.put(SEV_INFO, "INFO");
		sevNames.put(SEV_WARNING, "WARNING");
		sevNames.put(SEV_ERROR, "ERROR");
		sevNames.put(SEV_FATAL, "FATAL");
	}
	
	/**
	 * Create the instance if necessary and return it.
	 */
	public static MyLogger getInstance(HelloNzbCradle mainApp, JTextArea logTextArea)
	{
		if(instance == null)
			instance = new MyLogger(mainApp, logTextArea);
		
		return instance;
	}
	
	/**
	 * (Un)set the flag whether or not to print exception stack traces.
	 * 
	 * @param flag true or false
	 */
	public void printStackTraces(boolean flag)
	{
		printStackTraces = flag;
	}
	
	/**
	 * Print throwable stack trace.
	 * 
	 * @param t The throwable to use
	 * @param source Where this exception comes frome
	 */
	public void printStackTrace(Throwable t)
	{
		if(printStackTraces)
		{
			// print stack trace to error stream on console
			System.err.println("\nJAVA EXCEPTION OCCURED IN CLASS '" + 
					t.getStackTrace()[0].getClassName() + "':");
			t.printStackTrace();
			System.err.println("");
			
			// update text area in logging window
			updateTextArea("\n" + t.toString() + "\n");
			StackTraceElement[] stack = t.getStackTrace();
			for(StackTraceElement e : stack)
			{
				updateTextArea(e.toString() + "\n");
			}
			updateTextArea("\n");
		}
	}
	
	/**
	 * Set the print level value. All messages with severity >= this level
	 * will be printed by the logger.
	 * 
	 * @param level The value to set
	 */
	public void setPrintLevel(int level)
	{
		printLevel = level;
	}
	
	/**
	 * Print out and optionally add a message (string) to the log buffer.
	 * The message is only printed to stdout if its severity >= printLevel.
	 * The message is logged into the buffer if its severity > SEV_DEBUG.
	 * 
	 * @param msg The message to print/log
	 * @param sev The message's severity
	 */
	public void msg(String msg, int sev)
	{
		String prefix = sevNames.get(sev);
		if(prefix != null && prefix.length() > 0)
			msg = prefix + ": " + msg;
		
		if(sev >= printLevel)
		{
			if(msg.endsWith("\n") || msg.endsWith("\r"))
			{
				System.out.print(msg);
				updateTextArea(msg);
			}
			else
			{
				System.out.println(msg);
				updateTextArea(msg + "\n");
			}
		}
	}
	
	/**
	 * Append given string to the logging text area.
	 * 
	 * @param text The text to append
	 */
	private void updateTextArea(final String text)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				logTextArea.append(text);
			}
		});
	}
}




































