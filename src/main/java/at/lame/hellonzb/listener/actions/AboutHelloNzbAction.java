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
import at.lame.hellonzb.util.MyLogger;

import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;
import javax.swing.*;

import java.io.IOException;
import java.net.URISyntaxException;



public class AboutHelloNzbAction extends AbstractAction 
{
	/** The parent (host) application object */
	private final HelloNzb mainApp;
	
	/** central logger object */
	private MyLogger logger;
	
	
	public AboutHelloNzbAction(HelloNzbCradle f, Icon icon, String name)
	{
		this.mainApp = (HelloNzb) f;
		this.logger = mainApp.getLogger();
		putValue(Action.LARGE_ICON_KEY, icon);
		putValue(Action.NAME,  mainApp.getLocaler().getBundleText(name));
		putValue(Action.SHORT_DESCRIPTION, mainApp.getLocaler().getBundleText(name));
	}
	
	public void actionPerformed(ActionEvent e) 
	{
		String msg = mainApp.getLocaler().getBundleText("MenuHelpAboutText"); 
		String title = mainApp.getLocaler().getBundleText("MenuHelpAbout");
		JEditorPane pane = new JEditorPane();
		
		pane.setEditable(false);
		pane.setContentType("text/html");
		pane.setBackground(new Color(0xEEEEEE));
		pane.addHyperlinkListener(new MyHyperlinkListener());
		pane.setText(msg);

		JOptionPane.showMessageDialog(mainApp.getJFrame(), pane, title, JOptionPane.PLAIN_MESSAGE);
	}
	
	
	class MyHyperlinkListener implements HyperlinkListener 
	{
		public void hyperlinkUpdate(HyperlinkEvent evt) 
		{
			if(evt.getEventType() == HyperlinkEvent.EventType.ACTIVATED) 
			{
				try 
				{
					Desktop desktop = Desktop.getDesktop();
					String url = evt.getURL().toString();
					
					if(url.contains("mailto"))
						desktop.mail(evt.getURL().toURI());
					else
						desktop.browse(evt.getURL().toURI());
				} 
				catch(IOException e)
				{
					logger.printStackTrace(e);
				}
				catch(URISyntaxException e)
				{
					logger.printStackTrace(e);
				}
            }
        }
	}
}
