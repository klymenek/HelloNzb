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
import java.util.concurrent.*;
import javax.net.ssl.*;

import org.jboss.netty.channel.*;
import org.jboss.netty.handler.execution.*;
import org.jboss.netty.handler.ssl.*;
import org.jboss.netty.handler.traffic.*;
import static org.jboss.netty.channel.Channels.*;

import at.lame.hellonzb.util.*;


/**
 * This is the pipeline factory class for the Netty NIO framework.
 * It creates the following event pipelines for the channels (upstream):
 *   1. traffic shaping
 *   2. memory executor
 *   3. business logic
 * 
 * @author Matthias F. Brandstetter
 *
 */
public class NettyNioClientPipelineFactory implements ChannelPipelineFactory
{
	/** The Netty NIO client object */
	private final NettyNioClient nettyNioClient;
		
	/** The StringLocaler object of the main application */
	private final StringLocaler localer;

	/** central logger object */
	private MyLogger logger;
	
	/** This object is used to handle all SocketChannels and relevant meta information */
	private final NettyChannelManager ncMgr;
	
	/** Usenet server username */
	private final String username;
	
	/** "use SSL" flag */
	private boolean useSSL;
	
	/** global traffic shaping handler */
	private final GlobalTrafficShapingHandler trafficHandler;
	
	/** memory executor */
	private final ExecutionHandler execHandler;
	
	
	/**
	 * Class constructor. Initializes traffic shaping and memory executor objects.
	 * 
	 * @param client The NettyNioClient object (= parent of this class)
	 * @param loc The StringLocaler object to use
	 * @param ncMgr The NettyChannelManager object to use
	 * @param user The username to use for NNTP authentication
	 * @param limit The download speed limit to set (in KB/s) or 0 for no limit
	 */
	public NettyNioClientPipelineFactory(NettyNioClient client, 
			StringLocaler loc, MyLogger logger, NettyChannelManager ncMgr, 
			String user, long limit, boolean useSSL)
	{
		this.nettyNioClient = client;
		this.localer = loc;
		this.logger = logger;
		this.ncMgr = ncMgr;
		this.username = user;
		this.useSSL = useSSL;
		
		// prepare traffic shaping objects
		Executor executor = Executors.newCachedThreadPool();
		trafficHandler = new GlobalTrafficShapingHandler(executor, 0, limit * 1000L, 1000);
	
		// prepare memory executor object
		execHandler = new ExecutionHandler(new OrderedMemoryAwareThreadPoolExecutor(20, 0, 0));
	}	
	
	/**
	 * Create a new channel pipeline.
	 */
	public ChannelPipeline getPipeline() throws Exception
	{
		// create default pipeline
		ChannelPipeline pipeline = pipeline(); 
		
		// traffic shaping handler
		pipeline.addLast("global-traffic-shaping", trafficHandler);
		
		// SSL support
		if(useSSL)
		{
			SSLEngine sslEngine = createSSLEngine();
			pipeline.addLast("ssl", new SslHandler(sslEngine));
		}
		
		// memory executor
		pipeline.addLast("memory-executor", execHandler);
		
		// business logic
		pipeline.addLast("data-processing", 
				new NettyNioClientHandler(nettyNioClient, localer, logger, ncMgr, username, useSSL));
		
		return pipeline;
	}
	
	// create SSL engine (and avoid Diffie-Hellman there)
	// http://stackoverflow.com/questions/10036174/how-to-avoid-diffie-hellman-for-ssl-connections-with-java-netty
	private SSLEngine createSSLEngine()
	{
		SSLEngine sslEngine = SSLContextFactory.getClientContext().createSSLEngine();
		sslEngine.setUseClientMode(true);
		
		// Java doesn't like DH with prime size > 1024 bits, so avoid DH completely
		List<String> limited = new LinkedList<String>();
		for(String suite : sslEngine.getEnabledCipherSuites())
			if(!suite.contains("_DHE_"))
				limited.add(suite);
		
		sslEngine.setEnabledCipherSuites(limited.toArray(new String[limited.size()]));		
		return sslEngine;
	}
	
	/**
	 * Set the global download speed limit for all connections/channels.
	 * A parameter value of 0 disables the download speed limit. 
	 *  
	 * @param limit The speed limit to set (in KB/s)
	 */
	public void setDlSpeedLimit(long limit)
	{
		if(limit < 0)
			return;
		
		trafficHandler.configure(0, limit * 1000L);
	}
	
	/**
	 * Returns the current number of read bytes since the last check interval
	 * (check interval = 1000ms per default).
	 * 
	 * @return The number of read bytes
	 */
	public long getDlTraffic()
	{
		return trafficHandler.getTrafficCounter().getLastReadThroughput();
	}
	
	/**
	 * Release all external resources (from executor services).
	 */
	public void releaseExternalResources()
	{
		trafficHandler.releaseExternalResources();
		execHandler.releaseExternalResources();
	}
}




































