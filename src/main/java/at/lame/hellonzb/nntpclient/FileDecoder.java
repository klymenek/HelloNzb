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

package at.lame.hellonzb.nntpclient;

import at.lame.hellonzb.*;
import at.lame.hellonzb.helloyenc.*;
import at.lame.hellonzb.parser.*;
import at.lame.hellonzb.util.*;

import java.io.*;
import java.util.*;
import javax.swing.*;
import com.sun.mail.util.*;


/**
 * This class is called by the class NntpFileDownload as a thread's runnable
 * object when a file has been finished downloading and should now be decoded.
 * 
 * @author Matthias F. Brandstetter
 */
public class FileDecoder implements Runnable, HelloYencRunnable
{
	/** The main HelloNzb application object */
	private final HelloNzb mainApp;

	/** central logger object */
	private MyLogger logger;

	/** download file to decode */
	private DownloadFile dlFile;

	/** the data to decode */
	private Vector<byte[]> articleData;

	/** data encoding */
	private String encoding;

	/** The download directory on local disk */
	private File dlDir;

	public FileDecoder(HelloNzb mainApp, File dlDir, DownloadFile dlFile,
			Vector<byte[]> data, String encoding)
	{
		this.mainApp = mainApp;
		this.logger = mainApp.getLogger();
		this.dlDir = dlDir;
		this.dlFile = dlFile;
		this.articleData = data;
		this.encoding = encoding;
		this.dlDir = dlDir;
	}

	public void run()
	{
		Vector<byte[]> outVector = new Vector<byte[]>();
		HelloYenc yencDecoder = null;
		UUDecoderStream uuDecoder = null;
		FileOutputStream fileOutStream = null;
		int uuLength = 0;

		// prepare suitable decoder
		if(encoding.equals("yenc"))
			yencDecoder = new HelloYenc(logger);
		else if(encoding.equals("uu"))
			uuDecoder = null;
		else
		{
			logger.msg("Could not find a suitable decoder!", MyLogger.SEV_ERROR);
			return;
		}

		try
		{
			int i = 0;
			while(articleData.size() > 0)
			{
				// check for corrupt download
				if(articleData.get(0) == null || articleData.get(0).length == 0)
				{
					logger.msg("FileDecoder: Corrupt data found", MyLogger.SEV_WARNING);
					articleData.remove(0);
					continue;
				}

				// set data input stream of the yenc decoder object
				if(encoding.equals("yenc"))
				{
					yencDecoder.setInputData(articleData.get(0));
					yencDecoder.setPartNum(i + 1);
					yencDecoder.setRunnable(this);
				}

				// set data for the UUDecoder object
				else if(encoding.equals("uu"))
				{
					byte[] src = articleData.get(0);
					uuLength = src.length;
					ByteArrayInputStream inStream = new ByteArrayInputStream(src);
					uuDecoder = new UUDecoderStream(inStream);
				}

				// do we have the first (yenc) part loaded (then get filename)?
				if(i == 0)
				{
					File resultFile = createOutFile(yencDecoder, uuDecoder);
					fileOutStream = new FileOutputStream(resultFile);
				}

				// now decode the current article data block
				if(articleData.get(0).length > 0)
				{
					if(encoding.equals("yenc"))
						outVector.add(yencDecoder.decode());

					else if(encoding.equals("uu"))
					{
						byte[] bytes = new byte[uuLength];
						int b;

						b = uuDecoder.read();
						int outBufCounter = 0;
						for(; b != -1 && outBufCounter < uuLength; outBufCounter++)
						{
							bytes[outBufCounter] = (byte) b;
							b = uuDecoder.read();
						}

						byte[] newOutBuf = new byte[outBufCounter];
						System.arraycopy(bytes, 0, newOutBuf, 0, outBufCounter);
						outVector.add(newOutBuf);
					}
				}

				// update progress bar in main window
				final int j = i + 1;
				SwingUtilities.invokeLater(new Runnable()
				{
					public void run()
					{
						mainApp.updateDownloadQueue(dlFile.getFilename(), j);
					}
				});

				// remove processed element
				articleData.remove(0);
				i++;
			}

			// notify main application that writing of file started
			SwingUtilities.invokeLater(new Runnable()
			{
				public void run()
				{
					mainApp.fileWritingStarted(dlFile.getFilename());
					mainApp.updateDownloadQueue(dlFile.getFilename(), 0);
				}
			});

			// write data to output file and close it afterwards
			writeData(outVector, fileOutStream);
			fileOutStream.close();
		}
		catch(Exception e)
		{
			logger.printStackTrace(e);
		}

		// update main application window that file decoding is finished now
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				// notify main application that decoding of this file has
				// finished
				mainApp.fileDecodingFinished(dlFile.getFilename());
			}
		});
	}

	// create output file
	private File createOutFile(HelloYenc yencDecoder, UUDecoderStream uuDecoder)
			throws IOException
	{
		File resultFile = null;
		File resultDir = null;
		String filename = "";

		// get filename
		if(encoding.equals("yenc"))
			filename = yencDecoder.getFileName();
		else if(encoding.equals("uu"))
			filename = uuDecoder.getName();

		if(filename.equals(""))
			filename = dlFile.getFilename();

		int pos = 0;
		if((pos = filename.indexOf("name=")) != -1)
			filename = filename.substring(pos + 5, filename.length());

		if(filename.charAt(0) == '[')
			filename = filename.substring(1);
		if(filename.charAt(filename.length() - 1) == ']')
			filename = filename.substring(0, filename.length() - 1);

		try
		{
			resultFile = new File(dlDir.getAbsolutePath().trim() + File.separator + filename);
			resultDir = new File(dlDir.getAbsolutePath().trim());

			// create directory
			resultDir.mkdirs();

			if(resultFile.exists())
				resultFile = createNewFilename(resultFile);

			resultFile.createNewFile();
		}
		catch(IOException ex) // invalid filename in yenc header, so use name from article subject
		{
			filename = dlFile.getFilename();
			resultFile = new File(dlDir.getAbsolutePath().trim() + File.separator + filename);
			resultDir = new File(dlDir.getAbsolutePath().trim());

			// create directory
			resultDir.mkdirs();

			if(resultFile.exists())
				resultFile = createNewFilename(resultFile);

			resultFile.createNewFile();
		}

		return resultFile;
	}

	// if the given file already exists, create another file instead
	private File createNewFilename(File resultFile)
	{
		@SuppressWarnings("serial")
		File fileRename = new File(resultFile.getPath())
		{
			public File unique()
			{
				String sExtension;
				String sFileWithoutExt = getPath();
				int iIndexDot = sFileWithoutExt.lastIndexOf(".");
				if(iIndexDot < 0)
					sExtension = "";
				else
				{
					sExtension = sFileWithoutExt.substring(iIndexDot, sFileWithoutExt.length());
					sFileWithoutExt = sFileWithoutExt.substring(0, iIndexDot + 1);
				}
				int iT1 = 1;
				while(true)
				{
					File fileHelp = new File(sFileWithoutExt + Integer.toString(iT1) + sExtension);
					if(!fileHelp.exists())
						return fileHelp;
					iT1++;
				}
			}
		}.unique();

		return fileRename;
	}

	// write data to given output file stream
	private void writeData(Vector<byte[]> outVector, FileOutputStream fileOutStream) throws IOException
	{
		int i = 0;

		while(outVector.size() > 0)
		{
			byte[] buffer = outVector.get(0);
			fileOutStream.write(buffer);
			fileOutStream.flush();

			// update progress bar in main window
			final int j = i + 1;
			SwingUtilities.invokeLater(new Runnable()
			{
				public void run()
				{
					mainApp.updateDownloadQueue(dlFile.getFilename(), j);
				}
			});

			outVector.remove(0);
			i++;
		}
	}

	public void crc32Error()
	{
		// TODO Auto-generated method stub
	}
}
