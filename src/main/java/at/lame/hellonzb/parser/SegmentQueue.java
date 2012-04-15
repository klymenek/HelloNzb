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
import at.lame.hellonzb.tablemodels.FilesToDownloadTableModel;


/**
 * This class acts as a queue for all download file segments.
 * Used by NntpFileDownloader class.
 * 
 * @author Matthias F. Brandstetter
 */
public class SegmentQueue
{
	Vector<DownloadFileSegment> segQueue;
	

	/** 
	 * Class constructor.
	 * 
	 * @param tabModel The FilesToDownloadTableModel object to use
	 */
	public SegmentQueue(FilesToDownloadTableModel tabModel)
	{
		// initialize segment list
		segQueue = new Vector<DownloadFileSegment>();
		for(int i = 0; i < tabModel.getRowCount(); i++)
		{
			DownloadFile dlFile = tabModel.getDownloadFile(i);
			segQueue.addAll(dlFile.getAllSegments());
		}
	}
	
	/**
	 * Remove all segments the belong to the given filename.
	 * 
	 * @param filename The name of the download file to remove
	 */
	public void removeSegments(String filename)
	{
		Vector<Integer> removeIndices = new Vector<Integer>();
		
		synchronized(segQueue)
		{
			for(int i = 0; i < segQueue.size(); i++)
			{
				DownloadFile dlFile = segQueue.get(i).getDlFile();
				if(dlFile.getFilename().equals(filename))
					removeIndices.add(i);
			}
			
			for(int i = 0; i < removeIndices.size(); i++)
				segQueue.remove(removeIndices.get(i));
		}
	}
	
	/**
	 * Calls removeSegments() for all filenames in the given vector.
	 * 
	 * @param filenames All filenames to process
	 */
	public void removeSegments(Vector<String> filenames)
	{
		for(int i = 0; i < filenames.size(); i++)
			removeSegments(filenames.get(i));
	}
	
	/**
	 * Return whether or not there are more segments in the queue.
	 * 
	 * @return True if there are more segments, false if not
	 */
	public boolean hasMoreSegments()
	{
		synchronized(segQueue)
		{
			return ! segQueue.isEmpty();
		}
	}	
	
	/**
	 * Return the next element found in the list of segments to download.
	 * 
	 * @return The next element or null if none is left in queue
	 */
	public DownloadFileSegment nextSegment()
	{
		synchronized(segQueue)
		{
			if(segQueue.isEmpty())
				return null;
			
			DownloadFileSegment seg = segQueue.get(0);
			segQueue.remove(0);
			
			return seg;
		}
	}
	
	/**
	 * Calculate the remaining amount of bytes left in this segment queue.
	 */
	public long remainingBytes()
	{
		long size = 0;
		
		synchronized(segQueue)
		{
			for(DownloadFileSegment seg : segQueue)
				size += seg.getSize();
		}
		
		return size;
	}
}
































