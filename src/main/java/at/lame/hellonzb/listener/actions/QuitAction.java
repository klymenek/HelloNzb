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
package at.lame.hellonzb.listener.actions;

import at.lame.hellonzb.*;
import at.lame.hellonzb.util.*;

import java.awt.event.*;
import javax.swing.*;


public class QuitAction extends AbstractAction 
{
	/** The parent (host) application object */
	private final HelloNzb mainApp;
	
	/** central logger object */
	private MyLogger logger;

	/** background worker for misc. background tasks */
	private BackgroundWorker bWorker;
	
	/** background task manager */
	private TaskManager taskMgr;
	
	
	public QuitAction(HelloNzbCradle f, Icon icon, String name, 
			BackgroundWorker bWorker, TaskManager taskMgr)
	{
		this.mainApp = (HelloNzb) f;
		this.logger = mainApp.getLogger();
		this.bWorker = bWorker;
		this.taskMgr = taskMgr;
		putValue(Action.LARGE_ICON_KEY, icon);
		putValue(Action.NAME, mainApp.getLocaler().getBundleText(name));
		putValue(Action.SHORT_DESCRIPTION, mainApp.getLocaler().getBundleText(name));
	}
	
	public void actionPerformed(ActionEvent e) 
	{
		StringLocaler loc = mainApp.getLocaler(); 
		String msg = loc.getBundleText("PopupReallyQuit");
		String title = "Bye!";
		String yesBtn = loc.getBundleText("PopupOptionYes");
		String noBtn = loc.getBundleText("PopupOptionNo");
		String cbTxt = loc.getBundleText("PopupCheckboxDontShowAgain");
		
		boolean yesQuit = false;
		boolean toHide = Boolean.valueOf(mainApp.getPrefContainer().getMessageStatus("QuitApplication"));
		
		if(!toHide)
		{
			// show popup window (optionally)
			MyFuture<Boolean> future = new MyFuture<Boolean>(false);
			OptionalDialog dialog = new OptionalDialog(mainApp, "QuitApplication", title, msg, yesBtn, noBtn, cbTxt);
			dialog.show(future);
			yesQuit = future.getPayload();
		}
		else
			yesQuit = true; // don't show popup, quit in any case
		
		if(yesQuit)
		{
			// shutdown task manager
			if(taskMgr != null)
				taskMgr.shutdown();
			
			// shutdown memory mapper / background worker
			if(bWorker != null)
				bWorker.shutdown();
			
			// disconnect from server
			mainApp.globalDisconnect(true);
			
			// save any open nzb file
			mainApp.saveOpenParserData(true);
		
			// save auto settings
			mainApp.getPrefContainer().saveAutoValues();
			
			// close application windows
			//mainApp.getPrefContainer().getPrefDialog().dispose();
			//frame.dispose();

			System.exit(0);
		}
	}
}
