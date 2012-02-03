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
package com.emc.atmos.sync.plugins;

import java.io.InputStream;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import com.emc.atmos.sync.util.AtmosMetadata;

public abstract class SyncObject {
	private URI sourceURI;
	private URI destURI;
	private boolean directory;
	private Set<ObjectAnnotation> annotations;
	private long size;
	private AtmosMetadata metadata;
	
	public SyncObject() {
		annotations = new HashSet<ObjectAnnotation>();
		metadata = new AtmosMetadata();
	}
	
	public abstract InputStream getInputStream();
	
	public URI getSourceURI() {
		return sourceURI;
	}

	public void setSourceURI(URI sourceURI) {
		this.sourceURI = sourceURI;
	}

	public URI getDestURI() {
		return destURI;
	}

	public void setDestURI(URI destURI) {
		this.destURI = destURI;
	}

	public boolean isDirectory() {
		return directory;
	}

	public void setDirectory(boolean directory) {
		this.directory = directory;
	}
	
	public void addAnnotation(ObjectAnnotation annotation) {
		annotations.add(annotation);
	}
	
	public Set<ObjectAnnotation> getAnnotations() {
		return annotations;
	}
	
	public Set<ObjectAnnotation> getAnnotations(Class<? extends ObjectAnnotation> clazz) {
		Set<ObjectAnnotation> subset = new HashSet<ObjectAnnotation>();
		for(ObjectAnnotation ann : annotations) {
			if(ann.getClass().isAssignableFrom(clazz)) {
				subset.add(ann);
			}
		}
		return subset;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	/**
	 * Gets the relative path for the object.  If the destination is a
	 * namespace destination, this path will be used when computing the 
	 * absolute path in the destination, relative to the destination root.
	 */
	public abstract String getRelativePath();

	/**
	 * @return the atmosMetadata
	 */
	public AtmosMetadata getMetadata() {
		return metadata;
	}

	/**
	 * @param atmosMetadata the atmosMetadata to set
	 */
	public void setMetadata(AtmosMetadata atmosMetadata) {
		this.metadata = atmosMetadata;
	}

	public abstract long getBytesRead();
}
