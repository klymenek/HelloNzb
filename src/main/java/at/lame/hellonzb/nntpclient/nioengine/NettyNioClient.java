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

import at.lame.hellonzb.*;
import at.lame.hellonzb.parser.*;
import at.lame.hellonzb.util.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import javax.swing.*;

import org.jboss.netty.bootstrap.*;
import org.jboss.netty.buffer.*;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.group.*;
import org.jboss.netty.channel.socket.nio.*;


/**
 * This is a non-blocking implementation of an NNTP client.
 * It is designed as a finite state machine -- see enum
 * Channel status and NettyNioClientHandler.java for more
 * info.
 * 
 * @author Matthias F. Brandstetter
 */
public class NettyNioClient implements Runnable
{
	private static final int WAIT_AFTER_ERROR = 3;
	private static final int MIN_SLEEP_TIME = 3;

	/** thread's gobal run-flag */
	private boolean runFlag;
	
	/** command this thread to shut down cleanly */
	private boolean shutdown;
	
	/** command this thread to shut down immedeately */
	private boolean shutdownNow;
	
	/** main application object */
	private HelloNzb mainApp;
	
	/** central logger object */
	private MyLogger logger;
	
	/** The host:port combination to connect to */
	private InetAddress serverAddress;
	private int port;
	
	/** "use SSL" flag */
	private boolean useSSL;
	
	/** Username and password */
	private String username;
	private String password;
	
	/** The max. amount of simultaneous connections (download threads) */
	private int threadcount;
	
	/** Authentication test flag */
	private boolean testAuthFlag;
	
	/** Segment availability test flag */
	private boolean testSegAvailability;
	
	/** set as soon as first group cmd was successful */
	private boolean groupCmdSuccessful;
	
	/** The number of threads (connections, sockets) currently in idle status */
	private Integer idleSocketCount;
	
	/** This object is used to handle all SocketChannels and relevant meta information */
	private NettyChannelManager ncMgr;
	
	/** a collection of all open Jetty Channel objects */
	private ChannelGroup channelGroup;
	
	/** the client socket channel factory to use */
	private NioClientSocketChannelFactory clSockChannelFactory;
	
	/** the channel pipeline factory to use */
	private NettyNioClientPipelineFactory channelPipelineFactory;
	
	/** a list of new tasks for this NIO client */
	private Vector<NewTask> newTasks;
			
	/** the last amount of ms to wait for bandwith throttling */
	private Vector<Integer> lastToWaitValues;
	
	/** the amount of bytes downloaded so far */
	private Long downloadedBytes;

	

	/**
	 * Class constructor. Makes no connection.
	 * 
	 * @param mainApp Main application object
	 * @throws IOException
	 * @throws IllegalArgumentException
	 */
	public NettyNioClient(HelloNzb mainApp) 
			throws IOException, IllegalArgumentException, UnknownHostException
	{
		// some default values
		this.mainApp = mainApp;
		this.logger = mainApp.getLogger();
		this.runFlag = true;
		this.shutdown = false;
		this.shutdownNow = false;
		this.testAuthFlag = false;
		this.testSegAvailability = false;
		this.groupCmdSuccessful = true;
		
		// usenet server connection settings
		this.useSSL = mainApp.getBooleanPrefValue("ServerSettingsUseSSL"); 
		this.serverAddress = InetAddress.getByName(mainApp.getPrefValue("ServerSettingsHost"));
		if(this.useSSL)
			this.port = Integer.valueOf(mainApp.getPrefValue("ServerSettingsSSLPort"));
		else
			this.port = Integer.valueOf(mainApp.getPrefValue("ServerSettingsPort"));
		this.username = mainApp.getPrefValue("ServerSettingsUsername");
		this.password = mainApp.getPrefValue("ServerSettingsPassword");
		this.threadcount = Integer.parseInt(mainApp.getPrefValue("ServerSettingsThreadCount"));
		
		this.ncMgr = new NettyChannelManager(mainApp.getPrefContainer());
		this.channelGroup = new DefaultChannelGroup("HelloNzb-Channels");
		this.newTasks = new Vector<NewTask>();

		// Netty factories
		long speedLimit = getSpeedLimit();
		if(speedLimit < 0)
			speedLimit = 0;
		clSockChannelFactory = new NioClientSocketChannelFactory(
				Executors.newCachedThreadPool(), Executors.newCachedThreadPool());
		channelPipelineFactory = new NettyNioClientPipelineFactory(
				this, mainApp.getLocaler(), logger, ncMgr, username, speedLimit, useSSL); 
		
		// more default values
		this.idleSocketCount = 0;
		this.lastToWaitValues = new Vector<Integer>();
		this.lastToWaitValues.add(MIN_SLEEP_TIME);
		this.downloadedBytes = 0L;		
	}

	/**
	 * Run this thread.
	 */
	public void run()
	{
		long currTime = 0;
		long lastTime = 0;


		// init. socket channel connections
		try
		{
			for(int i = 0; i < threadcount; i++)
			{
				// create new channel
				Channel channel = createNewChannel(10000); 
				ncMgr.addChannelAndInit(channel);
				newIdleChannel();
				
				printDebugMsg("> Connecting to server '" + this.serverAddress + "' on port " + 
						this.port + "\r\n", channel);
				
				// update thread view
				updThreadView(channel, mainApp.getLocaler().getBundleText("ThreadViewStatusConnecting"));
			}
		}
		catch(IOException e)
		{
			logger.printStackTrace(e);
			runFlag = false;
		}

		// main loop
		while(runFlag)
		{
			try
			{
				checkShutdown();
				checkTimeout();
				
				// process all open (connected) channels
				for(Channel channel : channelGroup)
					write(channel);
				
				// let the thread sleep a bit
				Thread.sleep(1);
			} 
			catch(IOException e) 
			{
				logger.printStackTrace(e);
				runFlag = false;
			}
			catch(InterruptedException e)
			{
				runFlag = false;
			}
			
			// calculate bytes/sec
            currTime = System.nanoTime();
            if((currTime - lastTime) >= HelloNzbCradle.SEC_MODIFIER)
            {
                // update status bar (count of active threads/connections)
            	final int threads = this.threadcount - this.idleSocketCount;
                SwingUtilities.invokeLater(new Runnable() 
                { 
                	public void run()
                	{
                		// update status bar on main app window
                		mainApp.updStatusBar(threads);
                	} 
                } ); 
            	
                lastTime = currTime;
            }
		}
		
		ChannelGroupFuture groupFuture = channelGroup.close();
		groupFuture.awaitUninterruptibly();
		clSockChannelFactory.releaseExternalResources();
		channelPipelineFactory.releaseExternalResources();
		
		logger.msg("NettyNioClient stopped", MyLogger.SEV_DEBUG);
	} 
	
	/**
	 * Call this method to close all connections.
	 * 
	 * @param block Block until all connections have been closed
	 * @param max Max. value of System.nanoTime() until to wait, -1 for no max.
	 */
	public void shutdown(boolean block, long max)
	{
		logger.msg("NioClient should shutdown now", MyLogger.SEV_DEBUG);
		
		this.shutdown = true;
		
		if(block)
		{
			try
			{
				long curr = System.nanoTime();
				if(max == -1)
					curr = -2;
					
				while(runFlag && curr < max)
				{
					Thread.sleep(10);
					if(max > 0)
						curr = System.nanoTime();
				}
			}
			catch(InterruptedException e)
			{
				// do nothing
			}
		}
	}

	/**
	 * Test the specified authentication details (username and password)
	 * for correctness.
	 * 
	 * @param handler The RspHandler object to use
	 */
	public void testAuth(RspHandler handler)
	{
		// register the response handler
		synchronized(this.newTasks)
		{
			NewTask nt = new NewTask(handler, null, null, null);
			this.newTasks.add(nt);
		}

		// set parameters 
		this.testAuthFlag = true;
	}
	
	/**
	 * Test whether all segments of the specified DownloadFile exist on server.
	 * 
	 * @param dlFile The DownloadFile to use
	 * @param handler The RspHandler to use
	 */
	public void testSegAvailability(DownloadFile dlFile, RspHandler handler)
	{
		// register the response handler
		synchronized(this.newTasks)
		{
			NewTask nt = new NewTask(handler, null, null, dlFile);
			this.newTasks.add(nt);
		}
		
		// set parameters 
		this.testSegAvailability = true;
	}
	
	/**
	 * Returns true if at least one socket (download slot) is available.
	 * 
	 * @return true/false
	 */
	public boolean hasFreeSlot()
	{
		synchronized(this.idleSocketCount)
		{
			if(this.idleSocketCount < 1)
				return false;
			else
				return true;
		}
	}
	
	/**
	 * Fetch the specified usenet article from NNTP server.
	 * 
	 * @param artID The article to fetch, identified by its ID
	 * @return true if article could be added to download queue, false if no slots were free
	 */
	public boolean fetchArticleData(String group, String artID, RspHandler handler)
	{
		String groupCmd   = "GROUP " + group + "\r\n";
		String articleCmd = "BODY <" + artID + ">\r\n";
		
		synchronized(this.idleSocketCount)
		{
			if(this.idleSocketCount < 1)
				return false;
			else
			{
				this.idleSocketCount--;
				sendNntpCmd(articleCmd, groupCmd, handler);
				
				return true;
			}
		}
	}

	/**
	 * Send an array of bytes (a NNTP command) via a SocketChannel.
	 * 
	 * @param cmd The NNTP command to send (in byte[] format)
	 * @param group The GROUP command to send (in byte[] format)
	 * @param handler The RspHandler object to handle response
	 */
	private void sendNntpCmd(String cmd, String group, RspHandler handler)
	{
		// queue the data we want written
		synchronized(this.newTasks)
		{
			NewTask nt = new NewTask(handler, cmd, group, null);
			this.newTasks.add(nt);
		}
	}
	
	/**
	 * Check if shutdown flag is set. If so, then command all
	 * connections (socket channels) to send the QUIT command
	 * next. If the flag is set and all connections have been
	 * closed, then set the runFlag to false.
	 */
	private void checkShutdown()
	{
		if(shutdown)
		{
			int counter = 0;
			for(Channel channel : channelGroup)
			{
				// let all active channels disconnect
				ChannelStatus status = ncMgr.getNCStatus(channel);
				
				// if shutdownNow flag is true, then shutdown in any socket status
				if(shutdownNow && 	status != ChannelStatus.TO_QUIT &&
									status != ChannelStatus.FINISHED)
				{
					ncMgr.setNCStatus(channel, ChannelStatus.TO_QUIT);
				}
				
				// if not, then wait until connection is idle (download finished)
				else if(!shutdownNow && status == ChannelStatus.IDLE)
				{
					ncMgr.setNCStatus(channel, ChannelStatus.TO_QUIT);
				}

				counter++;
			}
			
			if(counter == 0)
				runFlag = false; // no active channels left
		}
	}
	
	/**
	 * Check if one or more socket channel(s) have not changed
	 * its/their status for x seconds (where x is defined by
	 * the user via application settings). If a timeout is
	 * identified, then the according socket channel is closed
	 * and reconnected again.
	 */
	private void checkTimeout()
	{
		if(shutdown)
			return;
		
		// loop through all socket channels
		for(int i = 0; i < ncMgr.size(); i++)
		{
			Channel channel = ncMgr.getNC(i);
			long currTime = System.nanoTime();
			long timeout = ncMgr.getNCTimeout(channel);
			
			// downloader paused?
			if(mainApp.isDownloadPaused())
			{
				ncMgr.resetTimeout(channel);
				continue;
			}
			
			// timeout time reached for this socket channel?
			if(currTime > timeout || !channel.isOpen())
			{
				try
				{
					// timeout, so create a new channel...
					// first update thread view
					String msg = mainApp.getLocaler().getBundleText("ThreadViewTimeoutReconnecting");
					updThreadView(channel, msg);

					// close the old channel
					if(channel.isConnected())
					{
						ChannelBuffer buffer = ChannelBuffers.wrappedBuffer("QUIT\r\n".getBytes());
						channel.write(buffer);
					}
					channel.close().awaitUninterruptibly();
					
					// then create new bootstrap (channel helper object)
					Channel newChannel = createNewChannel(5000); 
					ncMgr.exchangeNC(channel, newChannel, true);
					ncMgr.setNCStatus(newChannel, ChannelStatus.INIT);
					ncMgr.resetTimeout(newChannel);
				}
				catch(Exception e)
				{
					logger.printStackTrace(e);
				}
			}
		}
	}
	
	/**
	 * Write a string to the channel, depending on its current status.
	 * 
	 * @param channel The Netty Channel object to use
	 * @throws IOException
	 */
	private void write(Channel channel) throws IOException 
	{
		// send NNTP command according to current status of this channel
		ChannelStatus status = (ChannelStatus) ncMgr.getNCStatus(channel);
		
		if(status == null)
			return;
		
		if((this.username == null || this.username.length() == 0) && status == ChannelStatus.CONNECTED)
			status = ChannelStatus.IDLE;
		
		switch(status)
		{
			case CONNECTED:
			case W_AUTH_USER:
				// channel has just connected, so send first AUTHINFO command
				ncMgr.setNCStatus(channel, ChannelStatus.R_AUTH_USER);
				writeStatusAuthinfo("AUTHINFO USER " + this.username + "\r\n", channel);
				break;
				
			case W_AUTH_PASS:
				// first AUTHINFO command sent, now send the second part
				ncMgr.setNCStatus(channel, ChannelStatus.R_AUTH_PASS);
				writeStatusAuthinfo("AUTHINFO PASS " + this.password + "\r\n", channel);
				break;
				
			case IDLE:
				handleIdleState(channel);
				break;
		
			case START_FETCH:
				// authentication successful, after IDLE state, now do something
				if(!this.groupCmdSuccessful)
				{
					ncMgr.setNCStatus(channel, ChannelStatus.GROUP_SENT);
					writeStatusCmds(channel, false);
				}
				else
				{
					ncMgr.setNCStatus(channel, ChannelStatus.READY);
				}
				break;
		
			case READY:
				// GROUP command successful, so send article fetch command now
				ncMgr.setNCStatus(channel, ChannelStatus.START_RECEIVE);
				writeStatusCmds(channel, true);
				break;

			case SERVER_ERROR:
				handleServerErrorState(channel);
				break;

			case TO_QUIT:
				// article has been fetched, so send QUIT command
				ncMgr.setNCStatus(channel, ChannelStatus.FINISHED);
				writeStatusArticleFetched(channel);
				break;
		}
	}
	
	/**
	 * Handle state "SERVER_ERROR".
	 * 
	 * @param channel The channel to use
	 */
	private void handleServerErrorState(Channel channel)
	{
		RspHandler handler = null;
		
		if(this.testSegAvailability)
		{
			// segment availability check mode
			this.shutdown = true;
			handler = ncMgr.getRspHandler(channel);
			if(handler != null)
				handler.setFinished();
		}
		
		if(shutdown)
		{
			ncMgr.setNCStatus(channel, ChannelStatus.TO_QUIT);
			return;
		}
		
		// 481 exceeded maximum number of connections per user
		if(!ncMgr.containsErrorWait(channel))
		{
			// calculate time until socket has to wait for (10 sec)
			long fin = System.nanoTime() + (HelloNzbCradle.SEC_MODIFIER * WAIT_AFTER_ERROR);
			ncMgr.setErrorWait(channel, fin);
		}
		else
		{
			// waiting done?
			long fin = ncMgr.getErrorWait(channel);
			if(System.nanoTime() >= fin)
			{
				ncMgr.removeErrorWait(channel);
				ncMgr.setNCStatus(channel, ChannelStatus.INIT);
			}
		}
	}

	/**
	 * Handle state "IDLE".
	 * 
	 * @param channel The channel to use
	 */
	private void handleIdleState(Channel channel)
	{
		boolean empty = true;
		RspHandler handler = null;
		
		
		// first check if there is already a RspHandler object registered
		// for this socket channel. If so then it's likely that we have
		// reconnected after a timeout
		ArticleMetadata md = ncMgr.getMD(channel);
		handler = ncMgr.getRspHandler(channel);
		if(md != null && handler != null)
		{
			String cmd = md.cmd;
			String group = md.group;
			NewTask nt = new NewTask(handler, cmd, group, null);
		
			synchronized(this.newTasks)
			{
				newTasks.add(0, nt);
			}
		}
		
		// new tasks to process?
		synchronized(this.newTasks)
		{
			if(!newTasks.isEmpty())
				empty = false;
		}

		if(this.testAuthFlag)
		{
			if(!empty)
			{
				// authentication check mode, channel already authenticated and waiting
				handler = null;
				synchronized(this.newTasks)
				{
					handler = newTasks.firstElement().handler;
					newTasks.remove(0);
				}
				handler.setError(RspHandler.ERR_NONE, ""); // authentication test succeeded
				handler.setFinished();
				ncMgr.setNCStatus(channel, ChannelStatus.TO_QUIT);
				shutdown = true;
			}
		}
		else if(this.testSegAvailability)
		{
			if(!empty)
			{
				// test segment availability mode, channel already authenticated and waiting
				handler = null;
				DownloadFile dl = null;
				synchronized(this.newTasks)
				{
					handler = newTasks.firstElement().handler;
					dl = newTasks.firstElement().dl;
					newTasks.remove(0);
				}
				
				ncMgr.setRspHandler(channel, handler);
				ncMgr.setDLFile(channel, dl);
				ncMgr.setDLFileSeg(channel, dl.getAllOriginalSegments());
				ncMgr.setNCStatus(channel, ChannelStatus.START_FETCH);
			}
		}
		else
		{
			// check if new tasks are available
			if(!empty)
			{
				NewTask task = null;
				synchronized(this.newTasks)
				{
					// yes, so get the first task in queue...
					task = newTasks.firstElement();
					newTasks.remove(0);
				}

				// ...and prepare it for further processing
				ArticleMetadata articleData = new ArticleMetadata(task.cmd, task.group);
				ncMgr.setMD(channel, articleData);
				ncMgr.setRspHandler(channel, task.handler);
				ncMgr.setNCStatus(channel, ChannelStatus.START_FETCH);
			}
		}
	}
	
	/**
	 * Helper method for the write() method.
	 * 
	 * @param cmd The command to send
	 * @param socketChannel The socket channel to use
	 * @throws IOException
	 */
	private void writeStatusAuthinfo(String cmd, Channel channel) throws IOException
	{
		printDebugMsg("> " + cmd, channel);
		
		ChannelBuffer buffer = ChannelBuffers.wrappedBuffer(cmd.getBytes());
		channel.write(buffer);
	}
	
	/**
	 * Helper method for the write() method.
	 * 
	 * @param socketChannel The socket channel to use
	 * @param cmdFlag True when the fetch command should be send, false for the GROUP cmd
	 * @return Whether or not the thread should be switched to READ mode
	 * @throws IOException
	 */
	private boolean writeStatusCmds(Channel channel, boolean cmdFlag) throws IOException
	{
		ArticleMetadata metadata = (ArticleMetadata) ncMgr.getMD(channel);
		String string = null;
		
		if(this.testSegAvailability == false)
		{
			// normal download activity
			if(cmdFlag == true)
			{
				// send fetch command
				string = metadata.cmd;
				
				// update thread view
				String cmd = new String(metadata.cmd);
				String msg = mainApp.getLocaler().getBundleText("ThreadViewStatusFetchArticle") +
						" " + cmd.substring(5, cmd.length() - 2);
				updThreadView(channel, msg);
			}
			else
				string = metadata.group; // send GROUP command
		}
		else
		{
			// test download file segment availability
			if(cmdFlag == true)
			{
				Vector<DownloadFileSegment> segs = ncMgr.getDLFileSeg(channel);
				
				if(segs.isEmpty())
				{
					// no more segments left to test
					RspHandler handler = (RspHandler) ncMgr.getRspHandler(channel);
					handler.setFinished();
					ncMgr.setNCStatus(channel, ChannelStatus.IDLE);
					ncMgr.removeRspHandler(channel);
					ncMgr.removeDLFile(channel);
					ncMgr.removeDLFileSeg(channel);
					
					// update thread view
					updThreadView(channel, 
							mainApp.getLocaler().getBundleText("ThreadViewStatusConnected"));
					
					return false;
				}
				else
				{
					// get next article to test
					String article = segs.firstElement().getArticleId();
					String cmd = "HEAD <" + article + ">\r\n";
					string = cmd;
					segs.remove(0);
					ncMgr.setDLFileSeg(channel, segs);
					
					// update thread view
					String msg = mainApp.getLocaler().getBundleText("ThreadViewStatusCheckArticle") +
							" <" + article + ">";
					updThreadView(channel, msg);
				}
			}
			else
			{
				DownloadFile dl = ncMgr.getDLFile(channel);
				String group = "GROUP " + dl.getGroups().firstElement() + "\r\n";
				string = group;
			}
		}
			
		printDebugMsg("> " + string, channel);

		// write out data to channel
		ChannelBuffer buffer = ChannelBuffers.wrappedBuffer(string.getBytes());
		channel.write(buffer);
		
		return true;
	}
	
	/**
	 * Helper method for the write() method.
	 * 
	 * @param socketChannel The socket channel to use
	 * @throws IOException
	 */
	private void writeStatusArticleFetched(Channel channel) throws IOException
	{
		String cmd = "QUIT\r\n";

		// update thread view
		updThreadView(channel, 
				mainApp.getLocaler().getBundleText("ThreadViewStatusDisconnecting"));

		printDebugMsg("> " + cmd, channel);

		// send QUIT command to remote server
		ChannelBuffer buffer = ChannelBuffers.wrappedBuffer(cmd.getBytes());
		channel.write(buffer);
	}

	/**
	 * Create a new Jetty-based Channel. The parameter specifies how long
	 * to wait during connection attempt before timeout.
	 * 
	 * @param timeoutMillis The amount of time to wait in milli seconds
	 * @return The newly created Channel object
	 * @throws IOException If no connection could be established
	 */
	private Channel createNewChannel(long timeoutMillis) throws IOException
	{
		// configure the Netty client
		ClientBootstrap bootstrap = new ClientBootstrap(clSockChannelFactory);
		
		// configure the pipeline factory
		bootstrap.setPipelineFactory(channelPipelineFactory);
		bootstrap.setOption("tcpNoDelay", true);
		bootstrap.setOption("keepAlive", true);
		bootstrap.setOption("child.receiveBufferSizePredictorFactory", 
				new AdaptiveReceiveBufferSizePredictorFactory());

		// start the connection attempt
		ChannelFuture future = bootstrap.connect(new InetSocketAddress(serverAddress, port));
		Channel channel = null;
		if(future.awaitUninterruptibly(timeoutMillis))
			channel = future.getChannel();
		else
			throw new IOException("Could not connect to remote server");
		
		return channel;
	}
	
	/**
	 * Handle a new idle socket channel.
	 */
	protected void newIdleChannel()
	{
		synchronized(idleSocketCount)
		{
			idleSocketCount++;
		}
	}
	
	/**
	 * Called from the NettyNioClientHandler when the auth test was unsuccessful.
	 */
	protected void authTestError()
	{
		// authentication check mode, channel already authenticated and waiting
		RspHandler handler = null;
		synchronized(this.newTasks)
		{
			handler = newTasks.firstElement().handler;
			newTasks.remove(0);
		}

		handler.setError(RspHandler.ERR_AUTH, "could not authenticate"); // authentication test failed
		handler.setFinished();
	}
	
	/**
	 * Add an amount of bytes to the count of totally downloaded bytes.
	 * 
	 * @param bytes The value to add
	 */
	protected void addToDownloadedBytes(long bytes)
	{
		synchronized(downloadedBytes)
		{
			downloadedBytes += bytes;
		}
	}
	
	/**
	 * Prints a debug message to standard output.
	 * Prints the thread/connection number in front of every line.
	 * If null passed as socket channel object, then no thread nr. is printed.
	 * 
	 * @param msg The message to print
	 * @param channel The socket channel / thread / connection number (counted from 0)
	 */
	protected void printDebugMsg(String msg, Channel channel)
	{
		// message has more than one line
		if(msg.indexOf("\r\n") != msg.lastIndexOf("\r\n"))
			msg = "[" + msg.length() + " bytes received from server]\r\n";
		
		if(channel != null)
			msg = (ncMgr.indexOfNC(channel) + 1) + "| " + msg;
		else
			msg = "?| " + msg;
		
		logger.msg(msg, MyLogger.SEV_DEBUG);
	}

	/**
	 * Helper method to update thread view in main window.
	 * 
	 * @param i Thread number to update
	 * @param t New text to display
	 */
	protected void updThreadView(Channel channel, String t)
	{
		if(channel == null)
			return;
		
		final int tnum = ncMgr.indexOfNC(channel);
		final String text = t;
		
		if(tnum < 0)
			return;
	
        SwingUtilities.invokeLater(new Runnable() 
        { 
        	public void run()
        	{
        		// update thread view table
        		mainApp.updThreadView(tnum, text);
        	} 
        } ); 
	}

	/**
	 * Get the download speed limit set by the user.
	 * 
	 * @return The speed limit in KB/s, or -1 if not set
	 */
	protected long getSpeedLimit()
	{
		// get speed limit value from preferences
		long limit = 0;
		try
		{
			limit = Integer.valueOf(mainApp.getPrefValue("DownloadSettingsMaxConnectionSpeed"));
		}
		catch(NumberFormatException e)
		{
			return -1L;
		}
		
		return limit;
	}
	
	/**
	 * Set the global download speed limit.
	 * 
	 * @param limit The limit to set (in KB/s)
	 */
	public void setSpeedLimit(long limit)
	{
		channelPipelineFactory.setDlSpeedLimit(limit);
	}
		
	/**
	 * Returns the current number of read bytes since the last check interval
	 * (check interval = 1000ms per default).
	 * 
	 * @return The number of read bytes
	 */
	public long getDlTraffic()
	{
		return channelPipelineFactory.getDlTraffic();
	}
	
	protected void setGroupCmdSuccessful(boolean value)
	{
		groupCmdSuccessful = value;
	}
	
	protected boolean isGroupCmdSuccessful()
	{
		return groupCmdSuccessful;
	}
	
	protected boolean isTestAuthFlag()
	{
		return testAuthFlag;
	}

	protected boolean isTestSegAvailability()
	{
		return testSegAvailability;
	}

	protected ChannelGroup getChannelGroup()
	{
		return channelGroup;
	}
	
	
	class NewTask
	{
		public RspHandler handler;
		public String cmd;
		public String group;
		public DownloadFile dl;
		
		public NewTask(RspHandler h, String c, String g, DownloadFile dl)
		{
			this.handler = h;
			this.cmd = c;
			this.group = g;
			this.dl = dl;
		}
	}
}


































