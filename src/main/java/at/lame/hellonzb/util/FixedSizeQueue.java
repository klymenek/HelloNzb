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
package at.lame.hellonzb.util;

import java.util.*;


/**
 * Implementation of a fixed size (fifo) queue. New elements are always
 * added to the beginning of the queue. If an element is added and then
 * the total size of elements is now > max size, then the last element
 * in the queue is removed.
 * 
 * @author Matthias F. Brandstetter
 * @param <T>
 */
public class FixedSizeQueue<T>
{
	/** The max. size of the queue (set at construction) */
	private int maxSize;
	
	/** The current amount of elements in the queue */
	private int size;
	
	/** Pointer to the first element in the queue */
	private Element<T> first;
	
	/** Pointer to the last element in the queue */
	private Element<T> last;
	
	
	/**
	 * Class constructor, initialises the size of the queue.
	 * 
	 * @param maxSize The max. size of the queue
	 * @throws IllegalArgumentException if not 0 < size <= Integer.MAX_SIZE
	 */
	public FixedSizeQueue(int maxSize) throws IllegalArgumentException
	{
		if(maxSize < 1 || maxSize > Integer.MAX_VALUE)
			throw new IllegalArgumentException("invalid size (must be 0 < maxSize <= Integer.MAX_SIZE)");
		
		this.maxSize = maxSize;
		this.size = 0;
		this.first = null;
		this.last = null;
	}
	
	/**
	 * Add an element to the queue.
	 * 
	 * @param data The element to add
	 */
	public synchronized void add(T data)
	{
		// add element to the queue
		Element<T> newE = new Element<T>(data);
		newE.setNext(first);
		first = newE;
		if(first.getNext() != null)
			first.getNext().setPrev(first);
		
		// increase size
		size++;
		
		if(size == 1)
			last = first;
		
		// check for max. size
		if(size > maxSize)
		{
			last = last.getPrev();
			last.setNext(null);
			size--;
		}
	}
	
	/**
	 * Get the element specified by the zero-based index.
	 * Returns null if no element is found at this position.
	 * 
	 * @param index The element at this position (zero-based)
	 * @return The element at the given position or null
	 */
	public synchronized T get(int index)
	{
		if(index < 0 || index > (size - 1))
			return null;
		
		Element<T> e = first;
		for(int i = 0; i < index && e != null; i++)
			e = e.getNext(); 
		
		if(e != null)
			return e.getData();
		else
			return null;
	}
	
	/**
	 * Return all elements of the queue.
	 * @return The vector of elements
	 */
	public synchronized Vector<T> getAll()
	{
		Vector<T> vec = new Vector<T>();
		
		Element<T> e = first;
		while(e != null)
		{
			vec.add(e.getData());
			e = e.getNext();
		}
		
		return vec;
	}
	
	/**
	 * Returns the current size of the queue.
	 * @return The amount of elements in the queue
	 */
	public synchronized int size()
	{
		return size;
	}
	
	/**
	 * Returns the maximum size of the queue.
	 * @return The max. value
	 */
	public int maxSize()
	{
		return maxSize;
	}
	
	
	/////////////////////////////////////////////////////////////////////
	class Element<T2>
	{
		private Element<T2> prev;
		private Element<T2> next;
		
		private T2 data;
		
		public Element(T2 data)
		{
			this.prev = null;
			this.next = null;
			this.data = data;
		}
		
		public void setPrev(Element<T2> p)
		{
			prev = p;
		}
		
		public Element<T2> getPrev()
		{
			return prev;
		}
		
		public void setNext(Element<T2> n)
		{
			next = n;
		}
		
		public Element<T2> getNext()
		{
			return next;
		}
		
		public T2 getData()
		{
			return data;
		}
	}
}
