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

package at.lame.hellonzb;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import at.lame.hellonzb.util.*;


/**
 * This class shows a message to the user that a HelloNzb update is availabe.
 * The window shows the new program version and the changelog.
 * Both is downloaded from the project's web site.
 * 
 * @author Matthias F. Brandstetter
 */
public class HelloNzbUpdateNotifier
{
	private static String PROG_CHANELOG_FILE = "http://hellonzb.sourceforge.net/changelog.txt";
	
	/** The dialog window object */
	private JDialog notifDialog;
	
	/** "yes" button on main panel */
	private JButton yesButton;

	/** "no" button on main panel */
	private JButton noButton;
	
	/** String localer object */
	private StringLocaler localer;
	
	/** This textare displays the changelog */
	private JTextArea changelog;
	
	/** central loggin object */
	private MyLogger logger;
	
	/** Stores the "result" of the user action, i.e. what he has chosen */
	private MyFuture<Boolean> future;
	
	
	/**
	 * This is the constructor of the class.
	 */
	public HelloNzbUpdateNotifier(JFrame jframe, StringLocaler loc, MyLogger log)
	{
		notifDialog = new JDialog(jframe, true);
		localer = loc;
		logger = log;
		
		// create the panel for the dialog
		setContentPane();
	
		// dialog settings 
		notifDialog.setTitle(localer.getBundleText("PopupInfoTitle"));
		notifDialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
		notifDialog.setLocation(jframe.getX() + 100, jframe.getY() + 100);
		notifDialog.pack();
		notifDialog.setMinimumSize(new Dimension(800, 400));
		notifDialog.setVisible(false);
	}

	/**
	 * Load the changelog from the project server and display popup window.
	 * Only show log down to the current version.
	 * 
	 * @param future Where to store the user action result to
	 */
	public void show(MyFuture<Boolean> future)
	{
		StringBuilder sb = new StringBuilder();
		
		this.future = future;
		
		try
		{
			BufferedReader reader = null;
			DataInputStream stream = null;
		
			// create URL object and try to fetch program version file
			URL url = new URL(PROG_CHANELOG_FILE);
			URLConnection urlConn = url.openConnection();
			urlConn.setDoInput(true);
			urlConn.setUseCaches(false);
			urlConn.setConnectTimeout(HelloNzb.DEFAULT_TIMEOUT);
			urlConn.setReadTimeout(HelloNzb.DEFAULT_TIMEOUT);
			
			// create new data input stream from online connection
			stream = new DataInputStream(urlConn.getInputStream());
			reader = new BufferedReader(new InputStreamReader(stream));

			String line = "";
			while(line != null)
			{
				line = reader.readLine();
				if(line == null)
					break;
				else if(line.endsWith(HelloNzb.VERSION))
					break;
				else
					sb.append(line + "\n");
			}
			
			reader.close();
			stream.close(); 
		}
		catch(Exception ex)
		{
			logger.printStackTrace(ex);
		}
		finally
		{
			// show dialog
			changelog.setText(sb.toString());
			notifDialog.setVisible(true);
		}
	}
	
	/**
	 * This method is used by the constructor to create the content on
	 * the content pane of the dialog.
	 */
	private void setContentPane()
	{
		JPanel mainPanel = new JPanel();
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		JLabel jlabel;
		

		// create dialog buttons
		yesButton = new JButton(localer.getBundleText("PopupOptionOpenBrowserForUpdateYes"));
		noButton = new JButton(localer.getBundleText("PopupOptionOpenBrowserForUpdateNo"));
		yesButton.addActionListener(new HiddenButtonActionListener());
		noButton.addActionListener(new HiddenButtonActionListener());

		// create spacer panels
		mainPanel.setLayout(new BorderLayout());
		mainPanel.add(new JPanel(), BorderLayout.NORTH);
		mainPanel.add(new JPanel(), BorderLayout.WEST);
		mainPanel.add(new JPanel(), BorderLayout.EAST);
		
		// create changelog textarea
		changelog = new JTextArea(10, 30);
		changelog.setFont(new Font("Courier New", Font.PLAIN, 11));
		changelog.setEditable(false);
		JScrollPane scrollPane = new JScrollPane(changelog);
		scrollPane.setViewportBorder(new EmptyBorder(2, 2, 2, 2));
		
		// create main panel
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		jlabel = new JLabel(localer.getBundleText("PopupNewVersionAvailable1") + " (" + HelloNzbToolkit.getLatestVersion() + ")!");
		jlabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel.add(jlabel);
		jlabel = new JLabel(localer.getBundleText("PopupNewVersionAvailable2"));
		jlabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel.add(jlabel);
		panel.add(Box.createRigidArea(new Dimension(10, 10)));
		panel.add(scrollPane);
		mainPanel.add(panel, BorderLayout.CENTER);

		// add dialog buttons to panel
		buttonPanel.add(yesButton);
		buttonPanel.add(noButton);
		mainPanel.add(buttonPanel, BorderLayout.SOUTH);
		
		notifDialog.setContentPane(mainPanel);
	}

	/**
	 * Inner class that handles all button actions on the preferences dialog.
	 */
	class HiddenButtonActionListener implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			if(future == null)
				return;
			
			// save settings
			if(e.getSource().equals(yesButton))
			{
				notifDialog.setVisible(false);
				future.setPayload(true);
				future.setDone();
			}
			
			// quit and close without saving
			else if(e.getSource().equals(noButton))
			{
				notifDialog.setVisible(false);
				future.setPayload(false);
				future.setDone();
			}
		}
	}
}











































