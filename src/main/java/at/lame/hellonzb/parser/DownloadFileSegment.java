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


/** 
 * This class represents a segment of a download file.
 * 
 * @author Matthias F. Brandstetter
 */
public class DownloadFileSegment 
{
	/** The DownloadFile object this segment belongs to */
	private DownloadFile dlFile;
	
	/** The size of this segment in bytes */
	private long size;

	/** The index number of this segment */
	private int index;
	
	/** The newsgroup(s) where to find this segment */
	private Vector<String> groups;
	
	/** The unique ID of this segment */
	private String articleId;
	
	
	/**
	 * This is the constructor of the class.
	 * It creates a new download file segment object.
	 * 
	 * @param index The index of the segment within the whole file
	 * @param size The size of this segment in bytes
	 */
	public DownloadFileSegment(DownloadFile dlFile, 
			long size, int index, Vector<String> groups)
	{
		this.dlFile = dlFile;
		this.size = size;
		this.index = index;
		this.groups = groups;
		this.articleId = "";
	}
	
	/**
	 * Returns the DownloadFile object this segment belongs to.
	 * 
	 * @return The DownloadFile object
	 */
	public DownloadFile getDlFile()
	{
		return dlFile;
	}
	
	/**
	 * This method sets the posting ID to the segment object.
	 * 
	 * @param pID The posting ID
	 */
	public void setArticleId(String aID)
	{
		articleId += aID;
	}
	
	/**
	 * Return the index value of this segment.
	 * 
	 * @return The index value of this segment
	 */
	public int getIndex()
	{
		return index;
	}
	
	/**
	 * Return the size of this segment.
	 * 
	 * @return The size of this segment
	 */
	public long getSize()
	{
		return size;
	}
	
	/**
	 * Return the article ID of this segment.
	 * 
	 * @return The article ID of this segment
	 */
	public String getArticleId()
	{
		return articleId;
	}
	
	/**
	 * Return the newsgroups where to find this segment.
	 * 
	 * @return The Vector<String> with the list of newsgroups
	 */
	public Vector<String> getGroups()
	{
		return groups;
	}
}
