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
import java.net.*;
import java.util.concurrent.*;

import com.sun.net.httpserver.*;


public class HttpServerDemo
{
	private static final String HOME_URL = "http://localhost:8080";
	
	
	public static void main(String[] args) throws IOException
	{
		InetSocketAddress addr = new InetSocketAddress(8080);
		HttpServer server = HttpServer.create(addr, 0);

		server.createContext("/", new HttpGetHandler(HOME_URL, HttpGetHandler.ResourceType.HTML, "home.html"));
		server.createContext("/upload", new HttpGetHandler(HOME_URL, HttpGetHandler.ResourceType.HTML, "upload.html"));
		server.createContext("/postnzb", new HttpNzbFileReceiver(HOME_URL));
		server.setExecutor(Executors.newCachedThreadPool());
		server.start();
		
		System.out.println("Server is listening on port 8080\n---\n");
	}
}














































