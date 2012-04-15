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

import java.awt.*;
import java.util.*;
import javax.swing.*;
import at.lame.hellonzb.HelloNzbToolkit;


/**
 * Implements a child class of JPanel to use for the graphical download speed history
 * display. Overrides the paint() method.
 * 
 * @author Matthias F. Brandstetter
 */
public class SpeedGraphPanel extends JPanel
{
	private static final long serialVersionUID = 1L;
	private static final Color BACK_COLOR = new Color(238, 238, 238);
	private static final Color BORDER_COLOR = new Color(122, 138, 153); // from table.getGridColor()
	private static final Color LINE_COLOR = new Color(60, 114, 171);
	private static final Color FILL_COLOR = new Color(169, 217, 229);
	private static final Color LIGHT_COLOR = new Color(172, 182, 191);
	private static final Color VERY_LIGHT_COLOR = new Color(209, 215, 220);
	
	/** the panel's width */
	private int panelWidth;
	
	/** the panel's height */
	private int panelHeight;
	
	/** graph markers/points */
	private FixedSizeQueue<Long> queue;
	
	
	/**
	 * Class constructor.
	 * Initializes the panel's height to 50.
	 * 
	 * @param w The width of the new panel
	 */
	public SpeedGraphPanel(int w)
	{
		super();
		
		// set default size
		panelWidth = w;
		panelHeight = 50;
		
		// initialize queue collection
		queue = new FixedSizeQueue<Long>(panelWidth);
	}

	/**
	 * Set the size of this speed graph panel.
	 * 
	 * @param h The height of the new panel
	 */
	public void setSize(int h)
	{
		panelHeight = h;
		
		Dimension dim = new Dimension(panelWidth, panelHeight);
		super.setSize(dim);
		super.setMinimumSize(dim); 
		super.setMaximumSize(dim);
		super.setPreferredSize(dim);
	}
	
	/**
	 * Override paintComponent() method of parent JPanel class.
	 * @param g The Graphics object to use
	 */
	@Override
    public void paintComponent(Graphics g) 
	{
        super.paintComponent(g);   

        if(panelWidth == 0 || panelHeight == 0)
			return;
        
        // draw graph
        long max = drawGraph(g);

        // draw max value
        if(max > 0)
        	drawMaxVal(g, max);
        
        // draw border
        g.setColor(BORDER_COLOR);
        g.drawRect(0, 0, panelWidth - 1, panelHeight - 1);
	}
	
	/**
	 * Add a new data object to the queue (speed history graph).
	 * @param bps The bps data value to add
	 */
	public void add(long bps)
	{
		queue.add(bps);
	}
	
	/**
	 * Draw the graph onto the panel.
	 * @param _g The Graphics object to paint with
	 * @return The maximum value in the graph
	 */
	private long drawGraph(Graphics _g)
	{
		Graphics2D g = (Graphics2D) _g;
	    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		// draw background
        g.setColor(BACK_COLOR);
        g.fillRect(1, 1, panelWidth - 2, panelHeight - 2);

        // draw horizontal lines
        g.setColor(LIGHT_COLOR); 
        int y = (int) (panelHeight / 2);
        g.drawLine(0, y, panelWidth - 1, y);
        g.setColor(VERY_LIGHT_COLOR);
        y = (int) (panelHeight / 4);
        g.drawLine(0, y, panelWidth - 1, y);
        y = (int) (panelHeight / 4) * 3;
        g.drawLine(0, y, panelWidth - 1, y);
        
		if(queue == null)
			return 0L;
		
		Vector<Long> vec = queue.getAll();
		int offset = panelWidth - 2;
		long max = 0;
		
		// get highest value off all vector elements
		for(long l : vec)
			if(l > max)
				max = l;
		
		// calc variables for loop below
		Polygon polygon = new Polygon();
		polygon.addPoint(offset + 1, panelHeight);
		float tmp = (float) ((float) (panelHeight - 2) / (float) 100);

		// now draw every pixel in vector
		for(long l : vec)
		{
			// calculate y-coordinate of current point
			int percent = 0;
			if(max > 0)    
				percent = (int) (l * 100 / max);
			y = (int) (tmp * percent);
			y = panelHeight - y;
			
			// add new point to polygon
			polygon.addPoint(offset + 1, y + 1);
			offset--;
		}
		
		// draw graph
		polygon.addPoint(offset + 1, panelHeight);
		g.setColor(FILL_COLOR);
		g.fillPolygon(polygon);
		g.setColor(LINE_COLOR);
		g.drawPolygon(polygon);
		
		return max;
	}
	
	/**
	 * Draws the max. value into the left-upper corner of the graph panel.
	 * 
	 * @param _g The Graphics object to paint with
	 * @param max The max. value to print
	 */
	private void drawMaxVal(Graphics _g, long max)
	{
		Graphics2D g = (Graphics2D) _g;
	    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
	    // get pretty printed version of the max. bps value
	    String pretty = "max. " + HelloNzbToolkit.prettyPrintBps(max);
	    Font font = new Font("Dialog", Font.BOLD, 10);
	    FontMetrics fm = this.getFontMetrics(font);
	    int stringWidth = fm.stringWidth(pretty);

	    // draw white background
	    g.setColor(BORDER_COLOR);
	    g.drawRect(0, panelHeight - 1 - 18, stringWidth + 8, 18);
	    g.setColor(Color.WHITE);
	    g.fillRect(1, panelHeight - 18, stringWidth + 7, 17);
	    
	    // draw string
	    g.setColor(LINE_COLOR);
	    g.setFont(font);
	    g.drawString(pretty, 4, panelHeight - 6);
	}
}






































