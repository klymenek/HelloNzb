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

package at.lame.hellonzb.unrar;

import java.io.*;
import at.lame.hellonzb.util.MyLogger;

//import de.innosystec.unrar.Archive;
//import de.innosystec.unrar.rarfile.FileHeader;


/**
 * This class can extract RAR archives. It is based on the junrar libs by
 * Edmund Wagner: https://github.com/edmund-wagner/junrar
 * 
 * @author Matthias F. Brandstetter
 */
public class Unrar
{
//	public static void extractArchive(MyLogger logger, String archive, String destination)
//	{
//		if(archive == null || destination == null)
//			throw new RuntimeException("archive and destination must me set");
//
//		File arch = new File(archive);
//		if(!arch.exists())
//			throw new RuntimeException("the archive does not exist: " + archive);
//
//		File dest = new File(destination);
//		if(!dest.exists() || !dest.isDirectory())
//			throw new RuntimeException("the destination must exist and point to a directory: " 
//					+ destination);
//
//		extractArchive(logger, arch, dest);
//	}
//
//	public static void extractArchive(MyLogger logger, File archive, File destination)
//	{
//		Archive arch = null;
//		try
//		{
//			arch = new Archive(archive);
//		}
//		catch(Exception ex)
//		{
//			logger.printStackTrace(ex);
//		}
//		
//		// check if destination directory exists
//		if(!destination.exists())
//			destination.mkdirs();
//		if(!destination.exists())
//		{
//			logger.msg("Could not create target archive for RAR extraction", MyLogger.SEV_ERROR);
//			return;
//		}
//		
//		if(arch != null)
//		{
//			if(arch.isEncrypted())
//			{
//				logger.msg("Archive is encrypted cannot extract", MyLogger.SEV_WARNING);
//				return;
//			}
//			
//			FileHeader fh = null;
//			while(true)
//			{
//				fh = arch.nextFileHeader();
//				if(fh == null)
//				{
//					break;
//				}
//				if(fh.isEncrypted())
//				{
//					logger.msg("File is encrypted cannot extract: " + fh.getFileNameString(), MyLogger.SEV_WARNING);
//					continue;
//				}
//				
//				logger.msg("Extracting: " + fh.getFileNameString(), MyLogger.SEV_INFO);
//				try
//				{
//					if(fh.isDirectory())
//						createDirectory(fh, destination);
//					else
//					{
//						File f = createFile(logger, fh, destination);
//						OutputStream stream = new FileOutputStream(f);
//						arch.extractFile(fh, stream);
//						stream.close();
//						stream.flush();
//					}
//				}
//				catch(Exception ex)
//				{
//					logger.printStackTrace(ex);
//				}
//			}
//			try
//			{
//				arch.close();
//				logger.msg("RAR extraction finished.", MyLogger.SEV_INFO);
//			}
//			catch(Exception ex)
//			{
//				logger.printStackTrace(ex);
//			}
//		}
//	}
//
//	private static File createFile(MyLogger logger, FileHeader fh, File destination)
//	{
//		File f = null;
//		String name = null;
//		if(fh.isFileHeader() && fh.isUnicode())
//		{
//			name = fh.getFileNameW();
//		}
//		else
//		{
//			name = fh.getFileNameString();
//		}
//		
//		f = new File(destination, name);
//		if(!f.exists())
//		{
//			try
//			{
//				f = makeFile(destination, name);
//			}
//			catch(IOException ex)
//			{
//				logger.printStackTrace(ex);
//			}
//		}
//		return f;
//	}
//
//	private static File makeFile(File destination, String name) throws IOException
//	{
//		String[] dirs = name.split("\\\\");
//		if(dirs == null)
//		{
//			return null;
//		}
//		
//		String path = "";
//		int size = dirs.length;
//		if(size == 1)
//		{
//			return new File(destination, name);
//		}
//		else if(size > 1)
//		{
//			for(int i = 0; i < dirs.length - 1; i++)
//			{
//				path = path + File.separator + dirs[i];
//				new File(destination, path).mkdir();
//			}
//			path = path + File.separator + dirs[dirs.length - 1];
//			File f = new File(destination, path);
//			f.createNewFile();
//			return f;
//		}
//		else
//		{
//			return null;
//		}
//	}
//
//	private static void createDirectory(FileHeader fh, File destination)
//	{
//		File f = null;
//		if(fh.isDirectory() && fh.isUnicode())
//		{
//			f = new File(destination, fh.getFileNameW());
//			if(!f.exists())
//			{
//				makeDirectory(destination, fh.getFileNameW());
//			}
//		}
//		else if(fh.isDirectory() && !fh.isUnicode())
//		{
//			f = new File(destination, fh.getFileNameString());
//			if(!f.exists())
//			{
//				makeDirectory(destination, fh.getFileNameString());
//			}
//		}
//	}
//
//	private static void makeDirectory(File destination, String fileName)
//	{
//		String[] dirs = fileName.split("\\\\");
//		if(dirs == null)
//		{
//			return;
//		}
//		String path = "";
//		for(String dir : dirs)
//		{
//			path = path + File.separator + dir;
//			new File(destination, path).mkdir();
//		}
//
//	}
}
