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


public abstract class MyHttpHandler implements HttpHandler
{
	public abstract void handle(HttpExchange exchange) throws IOException;
	
	protected void setDefaultResponseHeader(HttpExchange exchange, String contentType) throws IOException
	{
		Headers responseHeaders = exchange.getResponseHeaders();
		responseHeaders.set("Content-Type", contentType + "; charset=us-ascii");
		responseHeaders.set("Pragma", "no-cache");
		exchange.sendResponseHeaders(200, 0);
	}
	
	protected void invalidRequestMethod(HttpExchange exchange, String allowedMethod) throws IOException
	{
		// invalid request method detected!
		Headers responseHeaders = exchange.getResponseHeaders();
		responseHeaders.set("Content-Type", "text/plain; charset=us-ascii");
		responseHeaders.set("Pragma", "no-cache");
		responseHeaders.set("Allow", allowedMethod);
		exchange.sendResponseHeaders(405, 0);

		// send error message
		OutputStream responseBody = exchange.getResponseBody();
		responseBody.write("405 Method not allowed".getBytes());
		
		// close HTTP connection
		exchange.close();
	}
}














































