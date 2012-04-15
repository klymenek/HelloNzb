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

import java.util.*;
import javax.swing.*;
import at.lame.hellonzb.*;
import at.lame.hellonzb.parser.*;


/**
 * This class acts as a central task manager background thread.
 * It can handle one or more of the following, "concurrent" events:
 *   - check for updates
 *   - load last session
 *   - open NZB file
 *   - check via PAR2
 *   - extract RAR archive
 *   
 * The TaskManager class is responsible for controlling the background
 * task progress bar (in the lower right corner of the application window).
 * 
 * @author Matthias F. Brandstetter
 */
public class TaskManager extends Thread
{
	/** the main application object */
	private HelloNzb mainApp;
	
	/** central logging object */
	private MyLogger logger;
	
	/** whether or not a the program is currently checking for an update */
	private boolean activeUpdateCheck;
	
	/** whether or not the last session is currently being loaded */
	private boolean activeLoadSession;
	
	/** whether or not a NZB file is currently being loaded */
	private boolean activeNzbLoading;
	
	/** all parser objects that are currently PAR2 checked */
	private Vector<NzbParser> activePar2;
	
	/** all parser objects that are currently RAR extracted */
	private Vector<NzbParser> activeRar;
	
	/** the background task progress bar on the main application window */
	private JProgressBar progressBar;
	
	/** global shutdown flag for this thread */
	private boolean shutdown;
	
	/** flag tells whether or not a task is currently running */
	private boolean activeTask;
	
	
	/**
	 * Class constructor.
	 * 
	 * @param mainApp The HelloNzb main application object
	 */
	public TaskManager(HelloNzb mainApp)
	{
		this.mainApp = mainApp;
		this.progressBar = null;
		this.logger = mainApp.getLogger();
		
		activeUpdateCheck = false;
		activeLoadSession = false;
		activeNzbLoading = false;
		activePar2 = new Vector<NzbParser>();
		activeRar = new Vector<NzbParser>();
		
		shutdown = false;
		activeTask = false;
	}
	
	/**
	 * Returns the progress bar object of this task manager.
	 * Create if necessary.
	 * 
	 * @return The (newly create) JProgressBar object
	 */
	public JProgressBar getProgressBar()
	{
		if(progressBar == null)
		{
			// initialise progress bar object if necessary
			progressBar = new JProgressBar();
			progressBar.setStringPainted(true);
			progressBar.setString("");
			progressBar.setIndeterminate(false);
		}
		
		return progressBar;
	}
	
	/**
	 * Called via thread.start()
	 */
	public void run()
	{
		int currState = -1; // mini state machine with the following values:
				// 0 ... no active task, deactivate progress bar
				// 1 ... update check
				// 2 ... load last session
				// 3 ... NZB file loading
				// 4 ... PAR2 check
				// 5 ... RAR extract
		
		
		// run until we are asked to shutdown this thread
		while(!shutdown)
		{
			if(!activeRar.isEmpty())
			{
				activeTask = true;

				// RAR extract
				if(currState != 5)
				{	
					changeProgBar(mainApp.getLocaler().getBundleText("StatusBarArcExtracting"), true);
					currState = 5;
				}
			}
			else if(!activePar2.isEmpty())
			{
				activeTask = true;

				// PAR2 check
				if(currState != 4)
				{	
					changeProgBar(mainApp.getLocaler().getBundleText("StatusBarPar2Check"), true);
					currState = 4;
				}
			}
			else if(activeNzbLoading)
			{
				activeTask = true;

				// loading a NZB file
				if(currState != 3)
				{	
					changeProgBar(mainApp.getLocaler().getBundleText("StatusBarOpeningNzbFile"), true);
					currState = 3;
				}
			}
			else if(activeLoadSession)
			{
				activeTask = true;

				// load last session
				if(currState != 2)
				{	
					changeProgBar(mainApp.getLocaler().getBundleText("StatusBarLoadingLastSession"), true);
					currState = 2;
				}
			}
			else if(activeUpdateCheck)
			{
				activeTask = true;

				// check for program updates
				if(currState != 1)
				{	
					changeProgBar(mainApp.getLocaler().getBundleText("StatusBarUpdateCheck"), true);
					currState = 1;
				}
			}
			else
			{
				activeTask = false;
				
				// deactivate progress bar
				if(currState != 0)
				{
					changeProgBar("", false);
					currState = 0;
					
					// shutdown?
					if(mainApp.shouldShutdown())
						mainApp.shutdownNow();
				}
			}
			
			// let the thread sleep a bit
			try
			{
				Thread.sleep(100);
			}
			catch(InterruptedException ex) {}
		}
	}
	
	/**
	 * Update the progress bar on the main window via the SwingUtilities class.
	 * 
	 * @param str The string to set on the progress bar (or "")
	 * @param act either true or false to (de-)activate the progress bar
	 */
	private void changeProgBar(final String str, final boolean act)
	{
		if(progressBar == null)
			return;
		
		final JProgressBar progBar = progressBar;

		// update progress bar on main window
        SwingUtilities.invokeLater(new Runnable() 
        { 
        	public void run()
        	{
        		progBar.setString(str);
        		progBar.setIndeterminate(act);
        	}
        } );
	}
	
	/**
	 * Tells whether or not an active task is running.
	 * 
	 * @return either true or false
	 */
	public boolean activeTask()
	{
		return activeTask;
	}
	
	/**
	 * Call when the last session is being loaded.
	 * 
	 * @param val either true or false
	 */
	public void loadSession(boolean val)
	{
		activeLoadSession = val;
	}
	
	/**
	 * Call when a NZB file is being processed or stopped processing.
	 * 
	 * @param val either true or false
	 */
	public void loadNzb(boolean val)
	{
		activeNzbLoading = val;
	}
	
	/**
	 * Call when the program (stops to) check(s) for an update.
	 * 
	 * @param val either true or false
	 */
	public void updateCheck(boolean val)
	{
		activeUpdateCheck = val;
	}
	
	/**
	 * Call when a parser has to be PAR2 checked.
	 * 
	 * @param parser The NzbParser object to process
	 */
	public void par2Check(NzbParser parser)
	{
		synchronized(activePar2)
		{
			activePar2.add(parser);
		}
	}
	
	/**
	 * Call when a PAR2 check is done.
	 * 
	 * @param parser The NzbParser object that is done
	 */
	public void par2Done(NzbParser parser)
	{
		synchronized(activePar2)
		{
			activePar2.remove(parser);
		}
	}
	
	/**
	 * Call when a RAR archive has to be extracted.
	 * 
	 * @param parser The NzbParser object to process
	 */
	public void rarExtract(NzbParser parser)
	{
		synchronized(activeRar)
		{
			activeRar.add(parser);
		}
	}
	
	/**
	 * Call when RAR extract is done.
	 * 
	 * @param parser The NzbParser object that is done
	 */
	public void rarDone(NzbParser parser)
	{
		synchronized(activeRar)
		{
			activeRar.remove(parser);
		}
	}
	
	/**
	 * Call this method to shutdown this thread.
	 */
	public void shutdown()
	{
		shutdown = true;
	}
}







































