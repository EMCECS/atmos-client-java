// Copyright (c) 2012, EMC Corporation.
// Redistribution and use in source and binary forms, with or without modification, 
// are permitted provided that the following conditions are met:
//
//     + Redistributions of source code must retain the above copyright notice, 
//       this list of conditions and the following disclaimer.
//     + Redistributions in binary form must reproduce the above copyright 
//       notice, this list of conditions and the following disclaimer in the 
//       documentation and/or other materials provided with the distribution.
//     + The name of EMC Corporation may not be used to endorse or promote 
//       products derived from this software without specific prior written 
//       permission.
//
//      THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
//      "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED 
//      TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR 
//      PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS 
//      BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
//      CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
//      SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
//      INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
//      CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
//      ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
//      POSSIBILITY OF SUCH DAMAGE.
package com.emc.esu.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * @author cwikj
 *
 */
public class MultiExtent extends Extent implements List<Extent> {
	private List<Extent> extents;
	
	public MultiExtent() {
		super(0,0);
		extents = new ArrayList<Extent>();
	}
	
	@Override
	public long getOffset() {
		if(extents.isEmpty()) {
			return -1;
		}
		long start = Long.MAX_VALUE;
		for(Extent e : extents) {
			if(e.getOffset() < start) {
				start = e.getOffset();
			}
		}
		
		return start;
	}
	
	@Override
	public long getSize() {
		long size = 0;
		for(Extent e : extents) {
			size += e.getSize();
		}
		
		return size;
	}
	
	@Override
	public String toString() {
		if(extents.isEmpty()) {
			throw new IllegalArgumentException("Must have at least one extent");
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("bytes=");
		boolean first = true;
		for(Extent e : extents) {
			if(!first) {
				sb.append(",");
			} else {
				first = false;
			}
			
	        long end = e.getOffset() + (e.getSize()-1);
	        sb.append(e.getOffset() + "-" + end);
		}
		
		return sb.toString();
	}

	@Override
	public boolean add(Extent e) {
		return extents.add(e);
	}

	@Override
	public void add(int i, Extent e) {
		extents.add(i, e);
	}

	@Override
	public boolean addAll(Collection<? extends Extent> c) {
		return extents.addAll(c);
	}

	@Override
	public boolean addAll(int i, Collection<? extends Extent> c) {
		return extents.addAll(i, c);
	}

	@Override
	public void clear() {
		extents.clear();
	}

	@Override
	public boolean contains(Object e) {
		return extents.contains(e);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return extents.containsAll(c);
	}

	@Override
	public Extent get(int i) {
		return extents.get(i);
	}

	@Override
	public int indexOf(Object e) {
		return extents.indexOf(e);
	}

	@Override
	public boolean isEmpty() {
		return extents.isEmpty();
	}

	@Override
	public Iterator<Extent> iterator() {
		return extents.iterator();
	}

	@Override
	public int lastIndexOf(Object e) {
		return extents.lastIndexOf(e);
	}

	@Override
	public ListIterator<Extent> listIterator() {
		return extents.listIterator();
	}

	@Override
	public ListIterator<Extent> listIterator(int i) {
		return extents.listIterator(i);
	}

	@Override
	public boolean remove(Object e) {
		return extents.remove(e);
	}

	@Override
	public Extent remove(int i) {
		return extents.remove(i);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return extents.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return extents.retainAll(c);
	}

	@Override
	public Extent set(int i, Extent e) {
		return extents.set(i, e);
	}

	@Override
	public int size() {
		return extents.size();
	}

	@Override
	public List<Extent> subList(int i, int j) {
		return extents.subList(i, j);
	}

	@Override
	public Object[] toArray() {
		return extents.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return extents.toArray(a);
	}
	
	

}
