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

import com.sun.net.httpserver.*;


public class HttpNzbFileReceiver extends MyHttpHandler
{
	private final String homeUrl;
	private WebUploadParser parser;
	
	
	public HttpNzbFileReceiver(String home)
	{
		this.homeUrl = home;
		this.parser = new WebUploadParser();
	}
	
	@Override
	public void handle(HttpExchange exchange) throws IOException
	{
		final String allowedMethod = "POST";
		
		// evaluate request method (GET, HEAD, POST)
		String requestMethod = exchange.getRequestMethod();
		if(!requestMethod.equalsIgnoreCase(allowedMethod))
		{
			invalidRequestMethod(exchange, allowedMethod);
			return;
		}

		// POST -- ok, read input data
		InputStreamReader input = new InputStreamReader(exchange.getRequestBody());
		BufferedReader reader = new BufferedReader(input);
		String line = reader.readLine();
		while(line != null)
		{
			parser.addLine(line);
			line = reader.readLine();
		}
		reader.close();
		input.close();
		
		// set standard response header
		setDefaultResponseHeader(exchange, "text/plain");
		
		// parse uploaded data
		try
		{
			parser.parse();

			// output response
			OutputStream responseBody = exchange.getResponseBody();
			responseBody.write("ok".getBytes());
		}
		catch(InvalidDataFormatException ex)
		{
			ex.printStackTrace();

			// output response
			OutputStream responseBody = exchange.getResponseBody();
			responseBody.write("invalid data".getBytes());
		}
		
		// close HTTP connection
		exchange.close();
	}
}














































