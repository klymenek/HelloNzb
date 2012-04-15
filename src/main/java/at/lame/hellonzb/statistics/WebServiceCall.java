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

package at.lame.hellonzb.statistics;

import javax.xml.ws.soap.*;

import at.lame.hellonzb.*;
import at.lame.hellonzb.util.*;


public class WebServiceCall implements Runnable
{
	protected enum Endpoint { STARTUP, DOWNLOAD };
	
	private final Endpoint endpoint;
	private final HelloNzbPortType port;
	private final ServiceRequestType req;
	private final HelloNzb main;
	
	
	public WebServiceCall(Endpoint endpoint, HelloNzbPortType port, ServiceRequestType req, HelloNzb main)
	{
		this.endpoint = endpoint;
		this.port = port;
		this.req = req;
		this.main = main;
	}
	
	public void run()
	{
		try
		{
			@SuppressWarnings("unused")
			ServiceResponseType res = null;
			
			switch(endpoint)
			{
				case STARTUP: res = port.startup((StartupRequestType) req); break;
				case DOWNLOAD: res = port.download((DownloadRequestType) req); break;
			}
		}
		catch(SOAPFaultException ex)
		{
			String err = "SOAP Exception (" + endpoint + "): " + ex.getLocalizedMessage();
			if(main != null)
				main.getLogger().msg(err, MyLogger.SEV_WARNING);
			else
				System.err.println(err);
		}
		catch(Exception ex)
		{
			String err = "Exception at WS call (" + endpoint + "): " + ex.getLocalizedMessage();
			if(main != null)
				main.getLogger().msg(err, MyLogger.SEV_WARNING);
			else
				System.err.println(err);
		}
	}
}
















































