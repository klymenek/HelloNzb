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
package at.lame.hellonzb.helloyenc;


import java.io.*;
import java.util.zip.CRC32;
import java.security.InvalidParameterException;

import at.lame.hellonzb.helloyenc.YencException;
import at.lame.hellonzb.util.MyLogger;


/**
 * This class is used to decode yEnc-encoded files to raw binary data.
 *  
 * @author Matthias F. Brandstetter
 */
public class HelloYenc 
{
	/** central logger instance */
	private MyLogger logger;
	
	/** The data input vector to decode */
	private byte [] inputData;

	/** The number of yenc parts this file consists of */
	private int partNum;
	
	/** The size of an input line */
	private int lineSize;
	
	/** The size of the output file */
	private int fileSize;
	
	/** The name of the output file */
	private String fileName;
	
	/** The offset within the input vector where the encoded data begins */
	private int dataOffset;
	
	/** This flag shows whether the data input vector has been set or not */
	private boolean initialized;
	
	/** This flag shows whether the yenc footer was found or not */
	private boolean footerFound;
	
	/** Used to calculate the CRC32 checksum of the bytes decoded */
	private CRC32 crc32Obj;
	
	/** The Runnable object that uses this HelloYenc instance */
	private HelloYencRunnable runnable;
	
	
	/**
	 * This is the constructor of the class.
	 * 
	 * @param logger The central logger object
	 */
	public HelloYenc(MyLogger logger)
	{
		this.logger = logger;
		
		this.inputData = null;
		this.runnable = null;
		
		this.partNum = 0;
		this.lineSize = 0;
		this.fileSize = 0;
		this.fileName = "";
		this.dataOffset = -1;
		
		this.initialized = false;
		this.footerFound = false; 

		this.crc32Obj = new CRC32();		
	}
	
	/**
	 * This method should be called after the decoder object has been created,
	 * but before its decode() method is called. It sets the data input vector
	 * and parses its header line(s).
	 * 
	 * @param in The data input vector to decode
	 */
	public void setInputData(byte [] in) throws YencException
	{
		inputData = in;
		initialized = true;
		footerFound = false;
		partNum = 0;
		crc32Obj.reset();
		
		dataOffset = parseHeader(inputData);
	}
	
	/**
	 * Use this method to decode the data input vecotr.
	 */
	public byte [] decode() throws IOException, YencException
	{
		int inSize = inputData.length;
		int byteCount = 0;
		int outBufCounter = 0;
		byte [] outBuf = new byte [inSize];
		
		
		// was this decoder already initialized?
		if(! initialized)
			throw new YencException("No data input vector set");
		
		// step through the input data
		for(int j = dataOffset; j < inSize; j++, byteCount++)
		{
			// read input character
			int i = (int) inputData[j];

			// skip end of line
			if(i == 10 || i == 13)
				continue;
				
			// check for escape character
			if(i == '=' && (j + 1) < inSize)
			{
				// parse footer
				try
				{
					if(inputData[j + 1] == 'y' && checkFooter(inputData, j))
						break;
				}
				catch(YencException ex)
				{
					runnable.crc32Error();
				}

				// decode a non-yenc-meta-character
				j++;
				i = (int) inputData[j];
				i = (i - 64) % 256;
			}
				
			// subtract the 42 offset and send the result to the output
			i = (i - 42) % 256;

			outBuf[outBufCounter++] = (byte) i;
			crc32Obj.update(i);
		}
		
		// create new output buffer as the first one will be too long,
		// because of the CR/LF chars in the original data
		byte [] newOutBuf = new byte[outBufCounter];
		System.arraycopy(outBuf, 0, newOutBuf, 0, outBufCounter);
		
		initialized = false;
		
		if(!footerFound)
			throw new YencException("No yenc footer found");
		else
			footerFound = false;
		
		return newOutBuf;
	}
	
	/**
	 * Parse the header line of the input vector.
	 * 
	 * @param in The Vector<Inteter> to parse
	 * @return The offset where the header line ends
	 */
	private int parseHeader(byte [] in) throws YencException
	{
		String ybegin = "=ybegin ";
		int inputSize = in.length;
		int idx = 0;
		String tmp = "";
		
		
		if(inputSize <= ybegin.length())
			throw new YencException("invalid header format");
		
		// check for existing and correctly formatted header line
		for(; idx < ybegin.length() && (in[idx] == 10 || in[idx] == 13); idx++);
		for(int i = 0; idx < ybegin.length(); idx++, i++)
		{
			if(in[idx] != ybegin.charAt(i))
				throw new YencException("invalid header format");
		}
		
		if(in[idx + 2] != 10 && in[idx + 2] != 13)
			idx = idx + 2;
		
		while(idx < inputSize && in[idx] != 10 && in[idx] != 13)
		{
			// "part" token
			tmp = "";
			if(in[idx] == 'p')
			{
				while(in[idx++] != '=');
				while(in[idx] != ' ' && in[idx] != 10 && in[idx] != 13)
				{
					byte b = in[idx];
					tmp += (char) b;
					idx++;
				}
				partNum = Integer.parseInt(tmp);
			}
			
			// "line" token
			tmp = "";
			if(in[idx] == 'l')
			{
				while(in[idx++] != '=');
				while(in[idx] != ' ' && in[idx] != 10 && in[idx] != 13)
				{
					byte b = in[idx];
					tmp += (char) b;
					idx++;
				}
				lineSize = Integer.parseInt(tmp);
			}
			
			// "size" token
			tmp = "";
			if(in[idx] == 's')
			{
				while(in[idx++] != '=');
				while(in[idx] != ' ' && in[idx] != 10 && in[idx] != 13)
				{
					byte b = in[idx];
					tmp += (char) b;
					idx++;
				}
				fileSize = Integer.parseInt(tmp);
			}
			
			// "name" token
			tmp = "";
			if(in[idx] == 'n')
			{
				while(in[idx++] != '=');
				while(in[idx] != 10 && in[idx] != 13)
				{
					byte b = in[idx];
					tmp += (char) b;
					idx++;
				}
				fileName = tmp.trim();
			}
			
			idx++;
		}

		for(; in[idx] == 10 || in[idx] == 13; idx++);
		idx = parsePartHeader(in, idx); 
		
		return idx;
	}
	
	/**
	 * This method is called after the yenc header line was parsed.
	 * It checks if there is another yenc header line, for a new
	 * yenc part.
	 * 
	 * @param in The Vector<Integer> of input bytes
	 * @param idx The current position within the input vector
	 * @return The offset where the header line ended within the input vector
	 */
	private int parsePartHeader(byte [] in, int index)
	{
		int idx = index;
		
		
		if(in[idx] == '=' && in[idx + 1] == 'y')
		{
			while(in[idx] != 10 && in[idx] != 13)
				idx++;

			idx++;
		}
		
		return idx;
	}
	
	/**
	 * This method is called every time an escape character ('=') is found within
	 * the input stream (vector). It checks if this is the beginning of the yenc
	 * "=yend" meta line (yenc footer).
	 * 
	 * @param in The Vector<Integer> of input bytes
	 * @param idx The current position within the input vector
	 * @return True if the yenc footer line was found, false otherwise
	 */
	private boolean checkFooter(byte [] in, int idx) throws YencException
	{
		String yend = "=yend ";
		int inputSize = in.length;
		
		
		if((idx + yend.length()) >= inputSize)
			return false;
	
		// check for existing and correctly formatted footer line
		for(int i = 0; i < yend.length() && idx < in.length; idx++, i++)
		{
			if(in[idx] != yend.charAt(i))
				return false;
		}

		footerFound = true;
		
		// check for the "[p]crc32" token
		while(idx < (in.length - 1))
		{
			if(	(in[idx] == 'p' && in[idx+1] == 'c') ||
				(in[idx] == 'c' && in[idx+1] == 'r') ||
				 in[idx] == 10  || in[idx] == 13)
			{
				break;
			}
			else
				idx++;
		}

		if(in[idx] != 'p' && in[idx] != 'c')
		{
			// footer found, but no CRC32 value found in it
			logger.msg("no CRC32 value found in yenc footer", MyLogger.SEV_WARNING);
			
			return true; 
		}
		
		while(in[idx++] != '=');
		
		// parse the "[p]crc32" token
		String crc32 = "";
		while(in[idx] != ' ' && in[idx] != 10 && in[idx] != 13)
		{
			byte b = in[idx];
			crc32 += (char) b;
			idx++;
		}
		
		// check the crc32 checksum
		if(crc32Obj.getValue() != crc32ToLong(crc32))
		{
			logger.msg("CRC32 check failed", MyLogger.SEV_WARNING);
			throw new YencException("CRC32 error in yenc part " + partNum);
		}	
		else logger.msg("yenc CRC32 check ok", MyLogger.SEV_DEBUG);
		
		return true; // footer line found
	}
	
	/**
	 * This method calculates the crc32 value in a string into the corresponding
	 * Long value.
	 * 
	 * @param val The crc32 value as a String
	 * @return The calculated Long value
	 * @throws InvalidParameterException
	 */
	private static long crc32ToLong(String val) throws InvalidParameterException 
	{
		int radix = 16;
		
		try 
		{
			return Long.valueOf(val, radix);
		}
		catch(NumberFormatException ex) 
		{
			//throw new InvalidParameterException("value " + val + " is not a number");
			return 0L;
		}
	}

	/**
	 * @return the fileSize
	 */
	public int getFileSize() 
	{
		return fileSize;
	}

	/**
	 * @return the fileName
	 */
	public String getFileName() 
	{
		return fileName;
	}
	
	public void setPartNum(int num)
	{
		partNum = num;
	}
	
	public void setRunnable(HelloYencRunnable r)
	{
		runnable = r;
	}
}






















