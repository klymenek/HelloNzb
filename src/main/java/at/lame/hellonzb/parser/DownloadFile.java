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
package at.lame.hellonzb.parser;

import java.util.*;
import org.apache.commons.lang.*;


/**
 * This class represents a file to be downloaded by the application.
 * It contains all attributes found in the NZB source file and a list
 * of all segements, also found in the NZB file.
 * 
 * @author Matthias F. Brandstetter
 */
public class DownloadFile
{
	/** The poster of this file */
	private String poster;
	
	/** The creation date of this file */
	private String creationDate;
	
	/** The subject of the file's posting */
	private String subject;
	
	/** This is the (guessed) name of this file */
	private String filename;
	
	/** The total file size in bytes */
	private long fileSize;
	
	/** The name of the nzb file this file is part of */
	private String nzbFile;
	
	/** The groups where this file can be found */
	private Vector<String> groups; 
	
	/** A list of segments this file contains of */
	private TreeMap<Integer,DownloadFileSegment> segments;
	
	/** The original list of segments this file contains of */
	private TreeMap<Integer,DownloadFileSegment> origSegments;
	
	/** The current count of segments (i.e. max key value of treemap) */
	private int segCount;
		
	/** Article-not-found flag */
	private boolean downloadErrorFlag;
	
	
	/**
	 * Class constructor.
	 * 
	 * @param p The poster of the file
	 * @param cd The creation date of the file
	 * @param s The subject of the file
	 * @param nzb The nzb file name
	 */
	public DownloadFile(String p, String cd, String s, String nzb)
	{
		poster = StringEscapeUtils.unescapeHtml(p);
		creationDate = StringEscapeUtils.unescapeHtml(cd);
		subject = StringEscapeUtils.unescapeHtml(s);
		filename = StringEscapeUtils.unescapeHtml(StringUtils.getFilenameFromSubject(subject));
		fileSize = 0;
		nzbFile = nzb;
		
		groups = new Vector<String>();
		segments = new TreeMap<Integer,DownloadFileSegment>();
		origSegments = new TreeMap<Integer,DownloadFileSegment>();
		segCount = 0;
		
		downloadErrorFlag = false;
	}
	
	/**
	 * Add a group to the list of this file's groups.
	 * 
	 * @param group The group name to add
	 */
	public void addGroup(String group)
	{
		groups.add(group);
	}
	
	/**
	 * Add a segment to the list of this file's segments.
	 * 
	 * @param seg The segment to add (posting ID)
	 */
	public void addSegment(DownloadFileSegment seg)
	{
		int index = seg.getIndex();

		if(segments.put(index, seg) == null)
			segCount++;
		origSegments.put(index, seg);
		
		fileSize += seg.getSize();
	}
	
	/**
	 * Return the next element found in the list of segments to download.
	 * 
	 * @return The next element or null if none is left in queue
	 */
	public DownloadFileSegment nextSegment()
	{
		if(segments.isEmpty())
			return null;
		
		int idx = segments.firstKey();
		DownloadFileSegment seg = segments.get(idx);
		segments.remove(idx);
		
		return seg;
	}
	
	/**
	 * Remove the segment identified by the segment index number.
	 * 
	 * @param key The key/index number to search for in tree
	 * @return True if the segment was found and removed, false otherwise
	 */
	public boolean removeSegment(int key)
	{
		if(segments.containsKey(key))
		{
			segments.remove(key);
			return true;
		}
		else
			return false;
		
	}
	
	/**
	 * Return a vector of all remaining file segments.
	 * 
	 * @return The data vector
	 */
	public Vector<DownloadFileSegment> getAllSegments()
	{
		Vector<DownloadFileSegment> vec = new Vector<DownloadFileSegment>();
		
		for(int i = segments.firstKey(); i <= segCount; i++)
			vec.add(segments.get(i));
		
		return vec;
	}
	
	/**
	 * Return a vector of all original file segments.
	 * 
	 * @return The data vector
	 */
	public Vector<DownloadFileSegment> getAllOriginalSegments()
	{
		Vector<DownloadFileSegment> vec = new Vector<DownloadFileSegment>();
		
		for(int i = origSegments.firstKey(); i <= segCount; i++)
			vec.add(origSegments.get(i));
		
		return vec;
	}
	
	/**
	 * Reset the queue (vector) of segments to its original state.
	 */
	@SuppressWarnings("unchecked")
	public void resetSegments()
	{
		segments = (TreeMap<Integer,DownloadFileSegment>) origSegments.clone();
	}
	
	/**
	 * Return whether or not there are more segments to download.
	 * 
	 * @return True if there are more segments, false if not
	 */
	public boolean hasMoreSegments()
	{
		return ! segments.isEmpty();
	}
	
	/**
	 * Return the total file size (i.e. the sum of all segment sizes).
	 * 
	 * @return The total file size in bytes
	 */
	public long getTotalFileSize()
	{
		return fileSize;
	}

	/**
	 * @return the poster
	 */
	public String getPoster() 
	{
		return poster;
	}

	/**
	 * @return the creationDate
	 */
	public String getCreationDate() 
	{
		return creationDate;
	}

	/**
	 * @return the subject
	 */
	public String getSubject() 
	{
		return subject;
	}
	
	/**
	 * @return the filename
	 */
	public String getFilename() 
	{
		return filename;
	}
	
	/**
	 * @return nzb file name
	 */
	public String getNzbFile()
	{
		return nzbFile;
	}
	
	/**
	 * @return the newsgroups
	 */
	public Vector<String> getGroups()
	{
		return groups;
	}
	
	/**
	 * @return the seg count
	 */
	public int getSegCount()
	{
		return segCount;
	}

	public boolean downloadError()
	{
		return downloadErrorFlag;
	}

	public void setDownloadError(boolean flag)
	{
		this.downloadErrorFlag = flag;
	}
}
