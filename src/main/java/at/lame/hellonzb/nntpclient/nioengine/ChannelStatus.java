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

public enum ChannelStatus
{
	INIT,
	CONNECTED,
	W_AUTH_USER,
	R_AUTH_USER,
	W_AUTH_PASS,
	R_AUTH_PASS,
	START_FETCH,
	GROUP_SENT,
	IDLE,
	READY,
	START_RECEIVE,
	RECEIVING_DATA,
	TO_QUIT,
	FINISHED,
	SERVER_ERROR
	
	
	/*
	 * Netty NIO Client State Machine Design.
	 * 
	 * 
	 * INIT								(CONNECT: establish connection to remote server, expect "200") 
	 *	|
	 *  V
	 * CONNECTED						(READ: Connection established, "200" expected)
	 *	|
	 *  |----------.					(only send "AUTHINFO" commands if username is set in preferences)
	 *  |          |
	 *  |          V
	 *  |     W_AUTH_USER				(WRITE: send "AUTHINFO USER" command) 
	 *  |          |
	 *  |          V
	 *  |     R_AUTH_USER				(READ: read resonse from "AUTHINFO USER" command, "381"/"502" expected)
	 *  |          |
	 *  |          V
	 *  |     W_AUTH_PASS				(WRITE: send "AUTHINFO PASS" command)
	 *  |          |
	 *  |          V
	 *  |     R_AUTH_PASS				(READ: read response from "AUTHINFO PASS" command, "281"/"502" expected)
	 *  |          |
	 *  |<---------+
	 *  |
	 *  V
	 * IDLE<-------------------------.	(WRITE: wait until we have new work to do, or if shutdown flag is true)
	 *  |                            |
	 *  |----------.				 |	(if testAuthFlag is set, then we don't want to do anything else)
	 *  |          |                 |
	 *  |          V                 |
	 *  |     START_FETCH			 |	(WRITE: we have work now, so start fetching data from server)
	 *  |          |                 |
	 *  |          |----------.		 |	(only do the extra steps if we need to send the "GROUP" command)
	 *  |          |          |      |
	 *  |          |          V      |
	 *  |          |     GROUP_SENT	 |	(READ: "GROUP" command has been sent, wait for reply, "211" expected)
	 *  |          |          |      |
	 *  |          |----------+      |
	 *  |          |                 |
	 *  |          V                 |
	 *  |  ,---->READY				 |	(WRITE: send "HEAD" or "BODY" command, depending on testSegAvailability)
	 *  |  |       |                 |
	 *  |  |       V                 |
	 *  |  |  START_RECEIVE			 |	(READ: read response after "HEAD" or "BODY" command, expect "22x")
	 *  |  |       |                 |
	 *  |  `-------|				 |	(if testSegAvailability == true, then go back to READY status)
	 *  |          |                 |
	 *  |          V                 |
	 *  |  ,->RECEIVING_DATA		 |	(READ: read more response data from the last "BODY" command)
	 *  |  |       |                 |
	 *  |  `-------|	             |	(more data to come, so stay in RECEIVING_DATA status)
	 *  |          |                 |
	 *  |          `-----------------+	(if all data is received (articleFinished() == true) go back to IDLE)
	 *  |
	 *  V
	 * TO_QUIT							(WRITE: send "QUIT" command to remote server)
	 *  |
	 *  V
	 * FINISHED							(READ: read last response from server, then close connection)
	 * 
	 * 
	 * *) note that if a READ state does not reads the expected value, the status is set
	 *    to SERVER_ERROR -- in this status the write() method waits for some time and then
	 *    sets the status of the channel to CONNECTED again
	 * 
	 */
}



































