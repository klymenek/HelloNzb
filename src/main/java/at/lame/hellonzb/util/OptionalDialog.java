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

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import at.lame.hellonzb.*;


/**
 * This class acts as a special popup window based on JDialog.
 * The dialog will display a line of text, a specified title,
 * two buttons (yes and no) and a checkbox to disable this
 * message in the future.
 * 
 * @author Matthias F. Brandstetter
 */
public class OptionalDialog extends JDialog
{
	/** main application object */
	private HelloNzb mainApp;
	
	/** Stores the "result" of the user action, i.e. what he has chosen */
	private MyFuture<Boolean> future;
	
	/** yes button */
	private JButton yesBtn;
	
	/** no button */
	private JButton noBtn;
	
	/** the "hide message in future" checkbox */
	private JCheckBox checkbox;
	
	/** the name of this message */
	private String hideMsg;
	
	
	/**
	 * Class constructor.
	 * 
	 * @param mainApp The HelloNzb main application object
	 * @param hideMsg The name of the message (to hide it in future)
	 * @param title Title string to set
	 * @param msg Message string to show
	 * @param yesBtnTxt Yes button text
	 * @param noBtnTxt No button text
	 * @param cbText Checkbox text
	 */
	public OptionalDialog(HelloNzb mainApp, String hideMsg, String title,
			String msg, String yesBtnTxt, String noBtnTxt, String cbText)
	{
		super(mainApp.getJFrame(), true);
		
		this.mainApp = mainApp;
		this.hideMsg = hideMsg;
		
		setTitle(title);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setLocation(mainApp.getJFrame().getX() + 200, mainApp.getJFrame().getY() + 200);
		setResizable(false);
		
		// create content pane
		setContentPane(msg, yesBtnTxt, noBtnTxt, cbText);
		
		setVisible(false);
		pack();
	}
	
	/**
	 * Create and set the content pane of this dialog.
	 * 
	 * @param msg Message string to show
	 * @param yesBtnTxt Yes button text
	 * @param noBtnTxt No button text
	 * @param cbText Checkbox text
	 */
	private void setContentPane(String msg, String yesBtnTxt, String noBtnTxt, String cbText)
	{
		checkbox = new JCheckBox(cbText);
		yesBtn = new JButton(yesBtnTxt);
		noBtn = new JButton(noBtnTxt);
		
		checkbox.addActionListener(new HiddenButtonActionListener());
		yesBtn.addActionListener(new HiddenButtonActionListener());
		noBtn.addActionListener(new HiddenButtonActionListener());

		// request focus for yes button
		addWindowFocusListener(new WindowAdapter() 
		{
		    public void windowGainedFocus(WindowEvent e) 
		    {
		    	yesBtn.requestFocusInWindow();
		    }
		});
		
		checkbox.setFont(getFont().deriveFont(Font.PLAIN));
		
		// Lay out message label
		JPanel mainPane = new JPanel();
		mainPane.setLayout(new BoxLayout(mainPane, BoxLayout.PAGE_AXIS));
		JLabel label = new JLabel(msg);
		mainPane.add(label);
		mainPane.add(Box.createRigidArea(new Dimension(0, 5)));
		mainPane.add(checkbox);
		mainPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		// Lay out the buttons from left to right.
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
		buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		buttonPane.add(Box.createHorizontalGlue());
		buttonPane.add(yesBtn);
		buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
		buttonPane.add(noBtn);
		buttonPane.add(Box.createHorizontalGlue());

		// Put everything together, using the content pane's BorderLayout.
		Container contentPane = getContentPane();
		contentPane.add(mainPane, BorderLayout.CENTER);
		contentPane.add(buttonPane, BorderLayout.PAGE_END);
	}
	
	/**
	 * Show this dialog window (set visible).
	 * 
	 * @param future Where to store the user action result to
	 */
	public void show(MyFuture<Boolean> future)
	{
		this.future = future;
		setVisible(true);
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
			
			// checkbox (de)activated
			if(e.getSource().equals(checkbox))
				mainApp.getPrefContainer().hideMessage(hideMsg, checkbox.isSelected()); 
			
			// save settings
			else if(e.getSource().equals(yesBtn))
			{
				future.setPayload(true);
				future.setDone();
				dispose();
			}
			
			// quit and close without saving
			else if(e.getSource().equals(noBtn))
			{
				future.setPayload(false);
				future.setDone();
				dispose();
			}
		}
	}
}








































