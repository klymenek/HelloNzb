/*******************************************************************************
j * HelloNzb -- The Binary Usenet Tool
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

import java.io.*;
import java.net.*;
import java.math.*;
import java.util.*;
import java.security.*;
import java.util.concurrent.*;

import javax.xml.datatype.*;
import javax.xml.namespace.*;

import at.lame.hellonzb.*;
import at.lame.hellonzb.statistics.WebServiceCall.*;


/**
 * This class is used to send anonymous HelloNzb usage statistics to the HelloNzb
 * project web. No information about any user or computer ID is gathered this way,
 * we instead look for HOW the software is being used. This (statically used) class
 * recives the statistics from the running application and calls the according
 * web services on the HelloNzb project web site.
 * 
 * @author Matthias F. Brandstetter
 */
public final class HelloNzbUsageStats
{
	public static HelloNzb main;
	private static final ExecutorService threadPool = Executors.newCachedThreadPool();
	
	// web service attributes and objects#
	private static final QName USAGE_STATS_SERVICE = new QName("urn:hellonzb", "HelloNzb");
	private static final URL wsdlURL = HelloNzbService.WSDL_LOCATION;
	private static final HelloNzbService ss = new HelloNzbService(wsdlURL, USAGE_STATS_SERVICE);
	private static final HelloNzbPortType port = ss.getHelloNzbPort();  
	
	// service parameters
	private static final String wsPass = "SoEinPass123!";
	private static final String uuid = createUUID();
	private static final String session = createSessionID(uuid);
	
	// runtime flags
	private static boolean active = false;

	
	/** Private constructor, no instance needed. */
	private HelloNzbUsageStats() { }
		
	/**
	 * Register a new HelloNzb program instance startup.
	 * 
	 * @param version Program version being used
	 * @param language GUI language set
	 */
	public static void registerStartup(String version, String language)
	{
/*		if(!active)
			return;
		
		XMLGregorianCalendar cal = getCal();
		
		StartupRequestType req = new StartupRequestType();
		req.setId(wsPass);
		req.setUuid(uuid);
		req.setSession(session);
		req.setDate(cal);
		req.setTime(cal);
		req.setVersion(version);
		req.setLanguage(language);

		threadPool.execute(new WebServiceCall(Endpoint.STARTUP, port, req, main));
*/	}
	
	/**
	 * Register a new download started at HelloNzb client.
	 * 
	 * @param bytes Total amount of bytes (size of this download)
	 * @param connections Number of connections to Usenet server
	 * @param ssl Whether or not connection to Usenet server is SSL encrypted
	 * @param speedlimit Whether or not a speed limit is set
	 * @param par2 Whether or not automatic PAR2 check is set
	 * @param unrar Whether or not automatic RAR extraction is set
	 */
	public static void registerDownload(long bytes, int connections, 
			boolean ssl, boolean speedlimit, boolean par2, boolean unrar)
	{
/*		if(!active)
			return;
		
		XMLGregorianCalendar cal = getCal();
		
		DownloadRequestType req = new DownloadRequestType();
		req.setId(wsPass);
		req.setUuid(uuid);
		req.setSession(session);
		req.setDate(cal);
		req.setTime(cal);
		req.setBytes(new BigInteger(String.valueOf(bytes)));
		req.setConnections(new BigInteger(String.valueOf(connections)));
		req.setSsl(ssl);
		req.setSpeedlimit(speedlimit);
		req.setPar2(par2);
		req.setUnrar(unrar);
		
		threadPool.execute(new WebServiceCall(Endpoint.DOWNLOAD, port, req, main));
*/	}

	/**
	 * (De-)activate this usage statistics manager.
	 * 
	 * @param a Whether or not to activate
	 */
	public static void setActive(boolean a)
	{
		active = a;
	}
	
	// current date and time
	private static XMLGregorianCalendar getCal()
	{
		XMLGregorianCalendar xmlCal = null;
		
		try
		{
			GregorianCalendar c = new GregorianCalendar();
			c.setTime(new Date());
			xmlCal = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
		}
		catch(DatatypeConfigurationException e)
		{
			if(main != null)
				main.getLogger().printStackTrace(e);
			else
				e.printStackTrace();
		}
		
		return xmlCal;
	}
	
	// read system's MAC address and create MD5 hash of it
	private static String createUUID()
	{
		String uuid = null;
		String macAddress = getMacAddress();

		try
		{
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(macAddress.getBytes(), 0, macAddress.length());
			uuid = new BigInteger(1, md.digest()).toString(16);
		}
		catch(NoSuchAlgorithmException e) 
		{ 
			if(main != null)
				main.getLogger().printStackTrace(e);
			else
				e.printStackTrace();

			uuid = macAddress; 
		}
		
		return uuid;
	}
	
	// create unique session ID
	private static String createSessionID(String uuid)
	{
		String session = null;
		String tmp = uuid + System.currentTimeMillis();
		
		try
		{
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(tmp.getBytes(), 0, tmp.length());
			session = new BigInteger(1, md.digest()).toString(16);
		}
		catch(NoSuchAlgorithmException e) 
		{ 
			if(main != null)
				main.getLogger().printStackTrace(e);
			else
				e.printStackTrace();

			session = uuid + System.currentTimeMillis(); 
		}
		
		return session;
	}
	
	// determine system's MAC address
	private static String getMacAddress()
	{
		Process p = null;
		BufferedReader in = null;
		String macAddress = null;
        
		try 
		{
			String osname = System.getProperty("os.name", "");

			if(osname.startsWith("Windows"))
				p = Runtime.getRuntime().exec(new String[] { "ipconfig", "/all" }, null);

			// Solaris code must appear before the generic code
			else if(osname.startsWith("Solaris") || osname.startsWith("SunOS")) 
			{
				String hostName = getFirstLineOfCommand("uname", "-n" );
				if(hostName != null) 
					p = Runtime.getRuntime().exec(new String[] { "/usr/sbin/arp", hostName }, null);
			}
			
			else if(new File("/usr/sbin/lanscan").exists())
                p = Runtime.getRuntime().exec(new String[] { "/usr/sbin/lanscan" }, null);

            else if(new File("/sbin/ifconfig").exists())
            	p = Runtime.getRuntime().exec(new String[] { "/sbin/ifconfig", "-a" }, null);

			if(p != null) 
			{
				in = new BufferedReader(new InputStreamReader(p.getInputStream()), 128);
				String l = null;
				while((l = in.readLine()) != null) 
				{
					macAddress = MACAddressParser.parse(l);
					if(macAddress != null && parseShort(macAddress) != 0xff)
						break;
				}
			}
		}
		catch(SecurityException ex) { }
		catch(IOException ex) { }
		finally 
		{
			if(p != null) 
			{
				if(in != null) 
				{
					try 
					{
						in.close();
					}
					catch(IOException ex) { }
				}
				
				try 
				{
					p.getErrorStream().close();
				}
				catch(IOException ex) { }
				
				try 
				{
					p.getOutputStream().close();
				}
                catch(IOException ex) { }

				p.destroy();
			}
		}
        
		return macAddress;
	}
	
	private static String getFirstLineOfCommand(String... commands) throws IOException
	{
		Process p = null;
		BufferedReader reader = null;

		try 
		{
			p = Runtime.getRuntime().exec(commands);
			reader = new BufferedReader(new InputStreamReader(p.getInputStream()), 128);

			return reader.readLine();
		}
		finally 
		{
			if(p != null) 
			{
				if(reader != null) 
				{
					try 
					{
						reader.close();
					}
					catch (IOException ex) { }
				}
                
				try 
				{
					p.getErrorStream().close();
				}
				catch(IOException ex) { }
	
				try 
				{
					p.getOutputStream().close();
				}
				catch(IOException ex) { }
	
				p.destroy();
			}
		}
	}
	
	private static short parseShort(String s) 
	{
		short out = 0;
		byte shifts = 0;
		char c;
		
		for(int i = 0; i < s.length() && shifts < 4; i++) 
		{
			c = s.charAt(i);
			if((c > 47) && (c < 58)) 
			{
				++shifts;
				out <<= 4;
				out |= c - 48;
			}
			else if((c > 64) && (c < 71)) 
			{
				++shifts;
				out <<= 4;
				out |= c - 55;
			}
            else if((c > 96) && (c < 103)) 
            {
            	++shifts;
            	out <<= 4;
            	out |= c - 87;
            }
		}
		
		return out;
	}
	
	
	//////////////////////////////////////////////////////////////////////////////////
	private static class MACAddressParser 
	{
		private static String parse(String in) 
		{
			String out = in;

			// lanscan
			int hexStart = out.indexOf("0x");
			if(hexStart != -1 && out.indexOf("ETHER") != -1) 
			{
				int hexEnd = out.indexOf(' ', hexStart);
				if(hexEnd > hexStart + 2)
					out = out.substring(hexStart, hexEnd);
			}
			else 
			{
				int octets = 0;
				int lastIndex, old, end;

				if(out.indexOf('-') > -1)
					out = out.replace('-', ':');

				lastIndex = out.lastIndexOf(':');

				if(lastIndex > out.length() - 2)
					out = null;
				else
				{
					end = Math.min(out.length(), lastIndex + 3);
					++octets;
					old = lastIndex;
	                
					while(octets != 5 && lastIndex != -1 && lastIndex > 1) 
					{
						lastIndex = out.lastIndexOf(':', --lastIndex);
						if(old - lastIndex == 3 || old - lastIndex == 2) 
						{
							++octets;
							old = lastIndex;
						}
					}

					if(octets == 5 && lastIndex > 1)
						out = out.substring(lastIndex - 2, end).trim();
					else
						out = null;
				}
			}

			if(out != null && out.startsWith("0x"))
				out = out.substring(2);

			return out;
		}
	}
}















































