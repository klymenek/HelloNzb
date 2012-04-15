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

package at.lame.hellonzb.httpserver;

import java.util.*;


public class WebUploadParser
{
	private enum State { INIT, FILENAME, TYPE, DATA };
	private State state;
	
	private Vector<String> buffer;
	private String currId;
	private String currFilename;
	private String currData;
	
	
	public WebUploadParser()
	{
		buffer = new Vector<String>();
	}
	
	public void addLine(String line)
	{
		buffer.add(line);
	}
	
	public void parse() throws InvalidDataFormatException
	{
		reset();
		
		// parse input data line by line
		for(String line : buffer)
		{
			switch(state)
			{
				case INIT:		stateInit(line); break;
				case FILENAME:	stateFilename(line); break;
				case TYPE:		stateType(line); break;
				case DATA:		stateData(line); break;
			}
		}
	}

	private void reset()
	{
		state = State.INIT;
		currId = "";
		currFilename = "";
		currData = "";
	}
	
	private void stateInit(String line) throws InvalidDataFormatException
	{
		StringBuffer id = new StringBuffer();
		
		int i = 0;
		for(; i < line.length() && line.charAt(i) == '-'; i++);
		for(; i < line.length(); i++)
			id.append(line.charAt(i));
		
		currId = id.toString();
		state = State.FILENAME;
	}
	
	private void stateFilename(String line) throws InvalidDataFormatException
	{
		final String prefix = "Content-Disposition: form-data; name=\"filename\"; filename=\"";
		StringBuffer filename = new StringBuffer();
		
		if(!line.startsWith(prefix))
			throw new InvalidDataFormatException("no filename header line found in data (id " + currId + ")");
		
		int i = prefix.length();
		for(; i < line.length() && line.charAt(i) != '"'; i++)
			filename.append(line.charAt(i));
			
		currFilename = filename.toString();
		state = State.TYPE;
	}
	
	private void stateType(String line) throws InvalidDataFormatException
	{
		final String prefix = "Content-Type: application/x-nzb";
		
		if(!line.startsWith(prefix))
			throw new InvalidDataFormatException("no content-type header line found in data (id " + currId + ")");

		state = State.DATA;
	}
	
	private void stateData(String line) throws InvalidDataFormatException
	{
		StringBuffer id = new StringBuffer();
		final String endPrefix = "-----";
		
		if(line.startsWith(endPrefix))
		{
			// end of data
			int i = 0;
			for(; i < line.length() && line.charAt(i) == '-'; i++);
			for(; i < line.length() && line.charAt(i) != '-'; i++)
				id.append(line.charAt(i));
			
			if(!currId.equals(id.toString()))
				throw new InvalidDataFormatException("corrupt or missing data (id missmatch " + currId + " != " + id.toString() + ")");
			
			// data ok, create new NzbParser and handle to main app
			System.out.println("New NZB file data: " + currFilename);
			System.out.println(currData);
			reset();
		}
		else
		{
			// new data line
			currData += line;
			currData += "\n";
		}
	}
}














































