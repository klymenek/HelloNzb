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
import at.lame.hellonzb.parser.*;


/**
 * Handle a response from a NIO channel.
 * 
 * @author Matthias F. Brandstetter
 */
public class RspHandler
{
	public final static int ERR_NONE = 0;
	public final static int ERR_AUTH = 1;
	public final static int ERR_CONN = 2;
	public final static int ERR_GROUP = 3;
	public final static int ERR_FETCH = 4;
	
	/** a sorted collection of byte arrays, i.e. the sorted data pieces */
	private Vector<byte[]> rspData;

	/** whether or not the handler has received all data */
	private boolean finished;
	
	/** the error status of the handler (can be ERR_NONE as well) */
	private Integer error;
	
	/** last error message */
	private String errorMsg;
	
	/** the amount of bytes stored to the tree */
	private Integer dataByteCount;
	
	/** the filename of the according downloaadFile */
	private DownloadFileSegment dlFileSeg;
	
	
	/**
	 * Class constructor.
	 */
	public RspHandler(DownloadFileSegment dlFileSeg)
	{
		this.dlFileSeg = dlFileSeg;
		
		this.rspData = new Vector<byte[]>();
		this.finished = false;
		this.error = ERR_NONE;
		this.errorMsg = "";
		this.dataByteCount = 0;
	}
	
	/**
	 * Store the response data array into the internal data vector of the handler.
	 * 
	 * @param rsp The byte[] array
	 */
	public void handleResponse(byte [] rsp)
	{
		synchronized(this.dataByteCount)
		{
			this.dataByteCount += rsp.length;
		}
		
		synchronized(this.rspData)
		{
			this.rspData.add(rsp);
		}
	}
		
	/**
	 * Returns the amount of bytes received sind the last call of this method.
	 * 
	 * @return The amount of bytes received
	 */
	public int newByteCount()
	{
		synchronized(this.dataByteCount)
		{
			int count = this.dataByteCount;
			this.dataByteCount = 0;
			
			return count;
		}
	}
	
	/**
	 * Set the error status of this handler object.
	 * 
	 * @param error The error status to set
	 */
	public void setError(int error, String errMsg)
	{
		synchronized(this.error)
		{
			if(this.error == ERR_NONE)
			{
				this.error = error;
				this.errorMsg = errMsg;
			}
		}
	}
	
	/**
	 * Return the error code of this handler object.
	 * 
	 * @return The error code to return
	 */
	public int getError()
	{
		synchronized(this.error)
		{
			return this.error;
		}
	}
	
	/**
	 * Return the error message of the last error.
	 * 
	 * @return The error message
	 */
	public String getErrorMsg()
	{
		synchronized(this.error)
		{
			return errorMsg.substring(0, errorMsg.length() - 2);
		}
	}
	
	/**
	 * Returns the data stored in this handler object (if any).
	 * 
	 * @param trim If set to true then the dot in the last line (if any) is removed
	 * @return The stored data array (byte[]), or null if handler is not finished yet
	 */
	public byte[] getData(boolean trim)
	{
		byte [] newData = null;

		// handler must be finished before data can be retreived
		if(!finished)
			return null;
		
		// any data here?
		if(rspData.size() == 0)
			return new byte[0];
		
		// concatenate all data arrays into one single byte[]
		// step 1: get total size of all arrays in the vector
		int totalSize = 0;
		for(int i = 0; i < rspData.size(); i++)
			totalSize += rspData.get(i).length;
		
		// concatenate all data arrays into one single byte[]
		// step 2: create a new data array to return
		newData = new byte[totalSize];
		
		// concatenate all data arrays into one single byte[]
		// step 3: concatenate all arrays into the new one
		int idx = 0;
		for(int i = 0; i < rspData.size(); i++)
		{
			byte [] arr = rspData.get(i);
			if(arr.length == 0)
				continue;
			System.arraycopy(arr, 0, newData, idx, arr.length);
			idx += arr.length;
		}
		rspData.clear();

		byte [] tmp = new byte[newData.length];
		int offset = 0;
		int counter = 0;
		
		// replace any double dot characters (int code 46) at the beginning
		// of a line with newData single dot character
		for(int i = 0; i < newData.length; i++, offset++)
		{
			if(i < 3)
				tmp[offset] = newData[i];
			else if(	newData[i-3] == '\r' && newData[i-2] == '\n' &&
						newData[i-1] == '.'  && newData[i]   == '.')
			{
				offset--;
				counter++;
				continue;
			}
			tmp[offset] = newData[i];
		}
		
		newData = new byte[tmp.length - counter];
		System.arraycopy(tmp, 0, newData, 0, newData.length);

		// remove the last line, if it contains only a single dot?
		if(trim)
		{
			int l = newData.length - 1;
			
			// . CR LF
			if(newData[l] == 10 && newData[l-1] == 13 && newData[l-2] == 46)
			{
				// trim array --> remove last three bytes
				byte [] newDataTrimmed = new byte[l-2];
				System.arraycopy(newData, 0, newDataTrimmed, 0, l - 2);
				return newDataTrimmed;
			}
		}
		
		return newData;
	}
	
	/**
	 * Reset this response handler.
	 */
	public void reset()
	{
		synchronized(this.rspData)
		{
			this.rspData.clear();
			this.finished = false;
		}
		
		synchronized(this.error)
		{
			this.error = ERR_NONE;
			this.errorMsg = "";
		}
		
		synchronized(this.dataByteCount)
		{
			this.dataByteCount = 0;
		}
	}
	
	/**
	 * Returns true if the Handler is set to "finished" state, or false othewise
	 * 
	 * @return true/false
	 */
	public boolean isFinished()
	{
		synchronized(this.rspData)
		{
			return this.finished;
		}
	}
	
	/**
	 * Sets the finished flag to true.
	 */
	public void setFinished()
	{
		synchronized(this.rspData)
		{
			this.finished = true;
		}
	}
	
	/**
	 * Returns the download file segment this response handler is associated with.
	 * 
	 * @return The DownloadFileSegment object
	 */
	public DownloadFileSegment dlFileSeg()
	{
		return dlFileSeg;
	}
}






















