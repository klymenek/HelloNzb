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

import java.io.*;
import java.util.*;

import com.sun.net.httpserver.*;


public class HttpGetHandler extends MyHttpHandler
{
	public enum ResourceType 
	{ 
		HTML ("web/html/"), 
		CSS ("web/css/");
		
		private String loc;
		
		ResourceType(String loc)
		{
			this.loc = loc;
		}
	};

	private final String homeUrl;
	private Vector<String> resData;
	
	
	public HttpGetHandler(String home, ResourceType resourceType, String resourceFile)
	{
		if(resourceFile == null || resourceFile.isEmpty())
			throw new IllegalArgumentException("resourceFile must not be null nor empty");
		
		this.homeUrl = home;
		String filename = resourceType.loc + resourceFile;
		
		try
		{
			resData = readWebResource(filename);
			if(resData == null)
				throw new RuntimeException("Could not open resource file '" + filename + "'");
		}
		catch(IOException ex)
		{
			// TODO
			ex.printStackTrace();
		}
	}
	
	@Override
	public void handle(HttpExchange exchange) throws IOException
	{
		final String allowedMethod = "GET";
		
		// evaluate request method (GET, HEAD, POST)
		String requestMethod = exchange.getRequestMethod();
		if(!requestMethod.equalsIgnoreCase(allowedMethod))
		{
			invalidRequestMethod(exchange, allowedMethod);
			return;
		}

		// GET -- ok, set standard response header
		setDefaultResponseHeader(exchange, "text/html");

		// output specified HTML page
		OutputStream responseBody = exchange.getResponseBody();
		for(String line : resData)
		{
			line = line.replaceAll("_HOME_URL_", homeUrl);
			responseBody.write(line.getBytes());
		}
		
		// close HTTP connection
		exchange.close();
	}
	
	private Vector<String> readWebResource(String filename) throws IOException
	{
		String line = "";
		Vector<String> data = new Vector<String>();

		File file = new File(filename);
		if(!file.exists() || !file.canRead())
			return null;		
		
		FileReader fr = new FileReader(file);
		BufferedReader input = new BufferedReader(fr);
		
		do
		{
			line = input.readLine();
			if(line != null)
				data.add(line + "\n");
		}
		while(line != null);
			
		input.close();
		fr.close();
		
		return data;
	}
}

















































