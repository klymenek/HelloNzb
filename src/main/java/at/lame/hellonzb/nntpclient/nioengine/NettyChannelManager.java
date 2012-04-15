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
package at.lame.hellonzb.nntpclient.nioengine;

import java.util.*;

import org.jboss.netty.channel.*;

import at.lame.hellonzb.parser.*;
import at.lame.hellonzb.preferences.HelloNzbPreferences;


/**
 * This class is used to manage a set of Channel instances
 * and the according meta information.
 * 
 * @author Matthias F. Brandstetter
 */
public class NettyChannelManager
{
	private static long NANO_MODIFIER = 1000000000;
	
	/** The main application's preferences container */
	private HelloNzbPreferences prefs;
	
	/** A list of socket channels, like download threads (connections) */
	private Vector<Channel> nettyChannels;

	/** Maps a Channel to a list of ByteBuffer instances */
	private Map<Channel,ArticleMetadata> pendingData;
	
	/** Maps a Channel to a RspHandler */
	private Map<Channel,RspHandler> rspHandlers;
	
	/** Maps a Channel to it's according status */
	private Map<Channel,ChannelStatus> channelStatus;
	
	/** the last 4 bytes of the buffer from last iteration (for CRLF.CRLF check) */
	private Map<Channel,byte[]> last4bytes;
		
	/** if a 481 error was encountered we want to wait this channel for some secs */
	private Map<Channel,Long> waitAfterError;

	/** The DownloadFile to test */
	private Map<Channel,DownloadFile> dlFiles;
	
	/** The DownloadFileSegment objects to test */
	private Map<Channel,Vector<DownloadFileSegment>> dlFileSegments;
	
	/** The nano sec time when a SC should raise a timeout event */
	private Map<Channel,Long> ncTimeouts;


	public NettyChannelManager(HelloNzbPreferences p)
	{
		this.prefs = p;
		
		this.nettyChannels = new Vector<Channel>();	

		this.dlFiles = new HashMap<Channel,DownloadFile>();
		this.dlFileSegments = new HashMap<Channel,Vector<DownloadFileSegment>>();
		this.pendingData = new HashMap<Channel,ArticleMetadata>();
		this.rspHandlers = Collections.synchronizedMap(new HashMap<Channel,RspHandler>());
		this.channelStatus = new HashMap<Channel,ChannelStatus>();
		this.waitAfterError = new HashMap<Channel,Long>();
		this.last4bytes = new HashMap<Channel,byte[]>();
		this.ncTimeouts = new HashMap<Channel,Long>();
	}
	
	public synchronized int size()
	{
		return nettyChannels.size();
	}

	public synchronized int addChannelAndInit(Channel nc)
	{
		nettyChannels.add(nc);
		
		last4bytes.put(nc, new byte[4]);
		setNCStatus(nc, ChannelStatus.INIT);
		
		return nettyChannels.size();
	}
	
	public synchronized Channel getNC(int i)
	{
		return nettyChannels.get(i);
	}
	
	public synchronized int indexOfNC(Channel nc)
	{
		return nettyChannels.indexOf(nc);
	}
	
	public synchronized ChannelStatus getNCStatus(Channel nc)
	{
		return channelStatus.get(nc);
	}
	
	public synchronized void setNCStatus(Channel nc, ChannelStatus status)
	{
		// set the new status for the according socket channel
		channelStatus.put(nc, status);
				
		// also reset the timeout counter for this socket channel
		resetTimeout(nc);
	}
	
	public synchronized void resetTimeout(Channel nc)
	{
		String tmp = prefs.getPrefValue("ServerSettingsTimeout");
		long timeout = System.nanoTime() + (Integer.valueOf(tmp) * NANO_MODIFIER);
		ncTimeouts.put(nc, timeout);
	}
	
	public synchronized long getNCTimeout(Channel nc)
	{
		return ncTimeouts.get(nc);
	}
	
	public synchronized ChannelStatus removeNCStatus(Channel nc)
	{
		return channelStatus.remove(nc);
	}
	
	public synchronized RspHandler getRspHandler(Channel nc)
	{
		return rspHandlers.get(nc);
	}
	
	public synchronized void setRspHandler(Channel nc, RspHandler handler)
	{
		rspHandlers.put(nc, handler);
	}
	
	public synchronized RspHandler removeRspHandler(Channel nc)
	{
		return rspHandlers.remove(nc);
	}
	
	public synchronized byte[] getLast4Bytes(Channel nc)
	{
		return last4bytes.get(nc);
	}
	
	public synchronized void setLast4Bytes(Channel nc, byte [] bytes)
	{
		last4bytes.put(nc, bytes);
	}
	
	public synchronized byte[] removeLast4Bytes(Channel nc)
	{
		return last4bytes.remove(nc);
	}

	public synchronized DownloadFile getDLFile(Channel nc)
	{
		return dlFiles.get(nc);
	}
	
	public synchronized void setDLFile(Channel nc, DownloadFile dlFile)
	{
		dlFiles.put(nc, dlFile);
	}
	
	public synchronized DownloadFile removeDLFile(Channel nc)
	{
		return dlFiles.remove(nc);
	}
	
	public synchronized Vector<DownloadFileSegment> getDLFileSeg(Channel nc)
	{
		return dlFileSegments.get(nc);
	}
	
	public synchronized void setDLFileSeg(Channel nc, Vector<DownloadFileSegment> dlFileSegs)
	{
		dlFileSegments.put(nc, dlFileSegs);
	}
	
	public synchronized Vector<DownloadFileSegment> removeDLFileSeg(Channel nc)
	{
		return dlFileSegments.remove(nc);
	}
	
	public synchronized ArticleMetadata getMD(Channel nc)
	{
		return pendingData.get(nc);
	}
	
	public synchronized void setMD(Channel nc, ArticleMetadata umd)
	{
		pendingData.put(nc, umd);
	}
	
	public synchronized ArticleMetadata removeMD(Channel nc)
	{
		return pendingData.remove(nc);
	}

	public synchronized boolean containsErrorWait(Channel nc)
	{
		return waitAfterError.containsKey(nc);
	}
	
	public synchronized long getErrorWait(Channel nc)
	{
		return waitAfterError.get(nc);
	}
	
	public synchronized void setErrorWait(Channel nc, long wait)
	{
		waitAfterError.put(nc, wait);
	}
	
	public synchronized long removeErrorWait(Channel nc)
	{
		return waitAfterError.remove(nc);
	}
	
	public synchronized void cleanup(Channel nc, boolean removeChannel)
	{
		int idx = indexOfNC(nc);
		if(idx < 0)
			return;

		removeMD(nc);
		removeRspHandler(nc);
		removeNCStatus(nc);
		removeLast4Bytes(nc);
		
		if(removeChannel)
			nettyChannels.set(idx, null);
	}
	
	public synchronized void exchangeNC(Channel oldNC, Channel newNC, boolean resetFlag)
	{
		ArticleMetadata md = pendingData.get(oldNC);
		if(md != null)
		{
			pendingData.remove(oldNC);
			pendingData.put(newNC, md);
		}

		RspHandler handler = rspHandlers.get(oldNC);
		if(handler != null)
		{
			rspHandlers.remove(oldNC);
			rspHandlers.put(newNC, handler);
			
			if(resetFlag)
				handler.reset();
		}

		ChannelStatus status = channelStatus.get(oldNC);
		if(status != null)
		{
			channelStatus.remove(oldNC);
			channelStatus.put(newNC, status);
		}

		byte [] bytes = last4bytes.get(oldNC);
		if(bytes != null)
		{
			last4bytes.remove(oldNC);
		}
		last4bytes.put(newNC, new byte[4]);
		
		int index = nettyChannels.indexOf(oldNC);
		nettyChannels.remove(index);
		nettyChannels.add(index, newNC);
	}
}





























