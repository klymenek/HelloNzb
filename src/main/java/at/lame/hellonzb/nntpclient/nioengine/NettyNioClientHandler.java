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

import java.io.*;

import org.jboss.netty.buffer.*;
import org.jboss.netty.channel.*;
import org.jboss.netty.handler.ssl.*;

import at.lame.hellonzb.util.*;


public class NettyNioClientHandler extends SimpleChannelUpstreamHandler
{
	/** The Netty NIO client object */
	private NettyNioClient nettyNioClient;
		
	/** The StringLocaler object of the main application */
	private StringLocaler localer;

	/** central logger object */
	private MyLogger logger;
	
	/** This object is used to handle all SocketChannels and relevant meta information */
	private NettyChannelManager ncMgr;
	
	/** Usenet server username */
	private String username;
		
	/** "use SSL" flag */
	private boolean useSSL;
	
	
	/**
	 * Class constructor.
	 * 
	 * @param client The NettyNioClient object (parent of this client handler)
	 * @param loc The StringLocaler object to use
	 * @param ncMgr The NettyChannelManager object to use
	 * @param user The username to use for NNTP authentication
	 */
	public NettyNioClientHandler(NettyNioClient client, StringLocaler loc, MyLogger logger,
			NettyChannelManager ncMgr, String user, boolean useSSL)
	{
		this.nettyNioClient = client;
		this.localer = loc;
		this.logger = logger;
		this.ncMgr = ncMgr;
		this.username = user;
		this.useSSL = useSSL;
	}
	
	@Override
	public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception
	{
		// handle upstream event
		super.handleUpstream(ctx, e);
	}
	
	@Override
	public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e)
	{
		// add the newly opened channel to the global channel group
		nettyNioClient.getChannelGroup().add(e.getChannel());
	}
	
	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception 
	{
		if(useSSL)
		{
			// get SSL handler from pipeline
			SslHandler sslHandler = ctx.getPipeline().get(SslHandler.class);
			
			// begin handshake
			ChannelFuture future = sslHandler.handshake();
			future.awaitUninterruptibly();
			if(!future.isSuccess())
			{
				logger.msg("SSL handshake error", MyLogger.SEV_FATAL);
				ctx.getChannel().close().awaitUninterruptibly();
			}
		}
	}
	
	@Override
	public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception 
	{
		// get SSL handler from pipeline and close it
		if(useSSL)
		{
			SslHandler sslHandler = ctx.getPipeline().get(SslHandler.class);
			sslHandler.close().awaitUninterruptibly();
		}
	}
	
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) 
	{
		ChannelBuffer buf = (ChannelBuffer) e.getMessage();
		int numBytes = buf.readableBytes();
		if(numBytes > 0)
		{
			// inform client about amount of loaded bytes
			nettyNioClient.addToDownloadedBytes(numBytes);

			// process fetched data
			byte [] data = buf.array();
			handleResponse(e.getChannel(), new String(data), data);
		}
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) 
	{
		logger.printStackTrace(e.getCause());
		// TODO: handle exception (??)
	}
	
	/**
	 * Handle a response from a socket channel.
	 * 
	 * @param channel The Netty channel which received the data
	 * @param data The data to process as String object
	 * @param rspData The data to process as byte array
	 * @throws IOException
	 */
	private void handleResponse(Channel channel, String response, byte [] rspData) 
	{
		// process server response depending on current channel status
		ChannelStatus status = (ChannelStatus) ncMgr.getNCStatus(channel);
		
		if(status == null)
			return;
		
		switch(status)
		{
			case INIT:
				// initial connection reply
				if(this.username == null || this.username.length() == 0)
				{
					if(procReply(channel, response, "200 |201 ", ChannelStatus.IDLE, RspHandler.ERR_CONN))
					{
						nettyNioClient.updThreadView(channel, 
								localer.getBundleText("ThreadViewStatusConnected"));
					}
	
				}
				else
					procReply(channel, response, "200 |201 ", ChannelStatus.W_AUTH_USER, RspHandler.ERR_CONN);
					
				nettyNioClient.printDebugMsg(response, channel);
				break;
				
			case R_AUTH_USER:
				// reply from "AUTHINFO USER" command
				procReply(channel, response, "381 |502 ", ChannelStatus.W_AUTH_PASS, RspHandler.ERR_AUTH);
				nettyNioClient.printDebugMsg(response, channel);
				break;
				
			case R_AUTH_PASS:
				// reply from "AUTHINFO PASS" command
				if(procReply(channel, response, "281 |502 ", ChannelStatus.IDLE, RspHandler.ERR_AUTH))
				{
					nettyNioClient.updThreadView(channel, 
							localer.getBundleText("ThreadViewStatusConnected"));
				}
					
				nettyNioClient.printDebugMsg(response, channel);
				break;
		
			case GROUP_SENT:
				// reply from "GROUP" command
				if(procReply(channel, response, "211 ", ChannelStatus.READY, RspHandler.ERR_GROUP))
				{
					nettyNioClient.setGroupCmdSuccessful(true);
				}
				
				nettyNioClient.printDebugMsg(response, channel);
				break;
				
			case START_RECEIVE:
			case RECEIVING_DATA:
				nettyNioClient.printDebugMsg(response, channel);
				
				// TODO: RspHandler data reset (auf 0 setzen) und download neu starten,
				// wenn wir nach einem verbindungsabbruch waehrend RECEIVING_DATA nicht
				// mehr wirklich weitere daten eines artikels bekommen (weil stattdessen
				// neu authentifiziert werden muesste, und stattdessen eine fehlermeldung
				// an diesen punkt kommt)
				
				if(!nettyNioClient.isTestSegAvailability())
				{
					boolean err = false;
					if(status == ChannelStatus.START_RECEIVE)
					{
						if(!procReply(channel, response, "22", ChannelStatus.RECEIVING_DATA, RspHandler.ERR_FETCH))
							err = true;
					}
					
					if(!err)
					{
						// Look up the handler for this channel
						RspHandler handler = (RspHandler) ncMgr.getRspHandler(channel);
	
						// send new data package to the handler object
						handler.handleResponse(rspData);
						
						// article finished?
						if(articleFinished(rspData, channel))
						{
							// article data complete
							ncMgr.removeMD(channel);
							ncMgr.removeRspHandler(channel);
	
							// update thread view
							nettyNioClient.updThreadView(channel, 
									localer.getBundleText("ThreadViewStatusConnected"));
	
							handler.setFinished();
							ncMgr.setNCStatus(channel, ChannelStatus.IDLE);
							nettyNioClient.newIdleChannel();
						}
						else
							// more data to come
							ncMgr.setNCStatus(channel, ChannelStatus.RECEIVING_DATA);
					}
				}
				else
				{
					// segment availability check
					if(status == ChannelStatus.START_RECEIVE)
					{
						boolean good = procReply(
								channel, response, "221 ", ChannelStatus.READY, RspHandler.ERR_FETCH);
						
						if(good && !response.endsWith(".\r\n"))
							// more data to come (from HEAD command)
							ncMgr.setNCStatus(channel, ChannelStatus.RECEIVING_DATA);
					}
					else if(response.endsWith(".\r\n"))
						ncMgr.setNCStatus(channel, ChannelStatus.READY);
				}
				break;

			case FINISHED:
				// after QUIT command
				nettyNioClient.printDebugMsg(response, channel);
			
				// update thread view
				nettyNioClient.updThreadView(channel, localer.getBundleText("ThreadViewStatusIdle"));
						
				// remove this channel from all global lists
				ncMgr.cleanup(channel, true);
						
				if(nettyNioClient.isTestSegAvailability())
					nettyNioClient.shutdown(false, 0);
				
				break;
		}
	}

	/**
	 * Process the server reply.
	 * Helper method for the handleResponse() method.
	 * 
	 * @param response The server response to parse
	 * @param startWithString The string that the response should start with
	 * @param status The status to set this channel if response was good
	 * @param handlerErrorCode The error code to set in case of non-good server reply
	 * @return true if the response was good, false otherwise
	 */
	private boolean procReply(Channel channel, String response, String startWithString, 
			ChannelStatus status, int handlerErrorCode)
	{
		Boolean good = null;
		
		if(startWithString.contains("|"))
		{
			int offset = startWithString.indexOf('|');
			String start1 = startWithString.substring(0, offset);
			String start2 = startWithString.substring(offset + 1, startWithString.length());
			if(response.startsWith(start1) || response.startsWith(start2))
				good = true;
			else
				good = false;
		}
		else
		{
			if(response.startsWith(startWithString))
				good = true;
			else
				good = false;
		}
		
		if(good)
			// response = good
			ncMgr.setNCStatus(channel, status);
		
		else
		{
			// response = bad
			
			// special case: 430 no such article
			if(handle430Error(channel, response))
				return good;
			
			// all other errors
			ncMgr.setNCStatus(channel, ChannelStatus.SERVER_ERROR);
			nettyNioClient.updThreadView(channel, response);
			RspHandler handler = ncMgr.getRspHandler(channel);
			if(handler != null)
			{
				handler.setError(handlerErrorCode, response);
				handler.setFinished();
			}
			else if(nettyNioClient.isTestAuthFlag())
			{
				nettyNioClient.authTestError();
				ncMgr.setNCStatus(channel, ChannelStatus.TO_QUIT);
			}
			else
				logger.msg("no RspHandler found!", MyLogger.SEV_DEBUG);
		}

		return good;
	}
	
	/**
	 * Handle a "430 no such article" error.
	 * 
	 * @param channel The current channel
	 * @return True if an 430 error was encountered, false otherwise
	 */
	private boolean handle430Error(Channel channel, String response)
	{
		if(!response.startsWith("430 "))
			return false;
		
		RspHandler handler = ncMgr.getRspHandler(channel);
		
		ncMgr.removeMD(channel);
		ncMgr.removeRspHandler(channel);

		// update thread view
		nettyNioClient.updThreadView(channel, 
				localer.getBundleText("ThreadViewStatusConnected"));

		handler.setFinished();
		ncMgr.setNCStatus(channel, ChannelStatus.IDLE);
		nettyNioClient.newIdleChannel();
		
		return true;
	}

	/**
	 * Check if the data received ended with a line containing only a single dot.
	 * 
	 * @param data The data to parse
	 * @return whether or not the data ended with the single dot
	 */
	private boolean articleFinished(byte[] data, Channel channel)
	{
		int l = data.length;
		byte [] toCheck = null;
		byte [] l4b = ncMgr.getLast4Bytes(channel);
		
		// enough bytes to process in this method?
		if(l >= 5)
		{
			// yes, so save the last 4 byte for the next iteration
			l4b[0] = data[l-4];
			l4b[1] = data[l-3];
			l4b[2] = data[l-2];
			l4b[3] = data[l-1];
			ncMgr.setLast4Bytes(channel, l4b);
			
			toCheck = new byte[] { data[l-5], data[l-4], data[l-3], data[l-2], data[l-1] }; 
		}
		else
		{
			// not enough bytes to process, so concatenate the last 4 plus the few from now
			toCheck = new byte[l + 4];
			for(int i = 0; i < 4; i++)
				toCheck[i] = l4b[i];
			for(int i = 0; i < l; i++)
				toCheck[i+4] = data[i];
			
			// also save last 4 bytes again
			l = toCheck.length;
			l4b[0] = toCheck[l-4];
			l4b[1] = toCheck[l-3];
			l4b[2] = toCheck[l-2];
			l4b[3] = toCheck[l-1];
			ncMgr.setLast4Bytes(channel, l4b);
		}
					
		// CR LF . CR LF (checked in reverse order)
		l = toCheck.length;
		if(	toCheck[l-1] == 10 && toCheck[l-2] == 13 && toCheck[l-3] == 46 && 
			toCheck[l-4] == 10 && toCheck[l-5] == 13)
			return true;
		else
			return false;
	}
}


































