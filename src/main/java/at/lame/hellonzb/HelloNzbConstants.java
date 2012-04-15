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


public interface HelloNzbConstants
{
	/** The socket number to use for HelloNzb instance communication */
	public static final int SERVER_SOCKET = 23472;
	
	/** From nano to secs */
	public static final long SEC_MODIFIER = 1000000000;
	
	/** size of the memory map buffer (for HelloNzb instance communication) */
	public static final int MEM_MAP_BUFFER_SIZE = 1024;
	
	/** debug mode */
	public boolean DEBUG = false;
	
	/** allow only one running instance -- deactivate for debugging in Eclipse! */
	public static boolean ONLY_ONE_INSTANCE = true;
	
	/** version number */
	public static final String VERSION = "1.0.6.1";
	
	/** The main window's width */
	public static final int MAINWIN_WIDTH = 1000;
	
	/** The main window's height */
	public static final int MAINWIN_HEIGHT = 700;
	
	/** homepage of HelloNzb */
	public static final String HELLONZB_WEBSITE = "https://sourceforge.net/projects/hellonzb/";
	
	/** Default timeout value (e.g. for update check and such) */
	public static final int DEFAULT_TIMEOUT = 10000; // 10 sec.
}



































