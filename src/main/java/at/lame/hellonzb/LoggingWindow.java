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
import javax.swing.*;
import javax.swing.border.*;
import at.lame.hellonzb.util.*;


/**
 * This class creates a new window containing a JTextArea to display
 * logging information. It must not be closed during runtime!
 * 
 * @author Matthias F. Brandstetter
 */
public class LoggingWindow
{
	/** The dialog window object */
	private JDialog loggingDialog;
	
	/** "Clear" button on main panel */
	private JButton clearButton;
	
	/** "close" button on main panel */
	private JButton closeButton;

	/** This textare displays the changelog */
	private JTextArea logTextArea;


	/**
	 * This is the constructor of the class.
	 */
	public LoggingWindow(StringLocaler localer, JFrame jframe)
	{
		loggingDialog = new JDialog(jframe, false);
		
		// create the panel for the dialog
		setContentPane(localer);
	
		// dialog settings 
		loggingDialog.setTitle("HelloNzb Log");
		loggingDialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
		loggingDialog.setLocation(200, 200);
		loggingDialog.pack();
		loggingDialog.setMinimumSize(new Dimension(800, 400));
		loggingDialog.setVisible(false);
	}

	/**
	 * Get the logging text area of this window.
	 * 
	 * @return The JTextArea object
	 */
	public JTextArea getTextArea()
	{
		return logTextArea;
	}
	
	/**
	 * Show the logging window.
	 */
	public void show()
	{
		// show dialog
		loggingDialog.setVisible(true);
	}
	
	/**
	 * This method is used by the constructor to create the content on
	 * the content pane of the dialog.
	 */
	private void setContentPane(StringLocaler localer)
	{
		JPanel mainPanel = new JPanel();
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		

		// create dialog buttons
		clearButton = new JButton(localer.getBundleText("LoggingWindowClear"));
		clearButton.addActionListener(new HiddenButtonActionListener());
		closeButton = new JButton(localer.getBundleText("LoggingWindowClose"));
		closeButton.addActionListener(new HiddenButtonActionListener());

		// create spacer panels
		mainPanel.setLayout(new BorderLayout());
		mainPanel.add(new JPanel(), BorderLayout.NORTH);
		mainPanel.add(new JPanel(), BorderLayout.WEST);
		mainPanel.add(new JPanel(), BorderLayout.EAST);
		
		// create changelog textarea
		logTextArea = new JTextArea(40, 150);
		logTextArea.setFont(new Font("Courier New", Font.PLAIN, 11));
		logTextArea.setEditable(false);
		JScrollPane scrollPane = new JScrollPane(logTextArea);
		scrollPane.setViewportBorder(new EmptyBorder(2, 2, 2, 2));
		
		// create main panel
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.add(scrollPane);
		mainPanel.add(panel, BorderLayout.CENTER);

		// add dialog buttons to panel
		buttonPanel.add(clearButton);
		buttonPanel.add(closeButton);
		mainPanel.add(buttonPanel, BorderLayout.SOUTH);
		
		loggingDialog.setContentPane(mainPanel);
	}

	/**
	 * Inner class that handles all button actions on the preferences dialog.
	 */
	class HiddenButtonActionListener implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			if(e.getSource() == clearButton)
				logTextArea.setText("");
			else if(e.getSource() == closeButton)
				loggingDialog.setVisible(false);
		}
	}
}











































