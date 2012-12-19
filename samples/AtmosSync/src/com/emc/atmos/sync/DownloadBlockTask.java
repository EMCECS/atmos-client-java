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
package com.emc.atmos.sync;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import org.apache.log4j.Logger;

import com.emc.esu.api.EsuApi;
import com.emc.esu.api.EsuException;
import com.emc.esu.api.Extent;
import com.emc.esu.api.ObjectPath;

public class DownloadBlockTask extends TaskNode {
	private static final Logger l4j = Logger.getLogger(DownloadBlockTask.class);

	private FileChannel channel;
	private EsuApi esu;
	private Extent extent;
	private DownloadCompleteTask listener;
	private ObjectPath path;
	private boolean aborted = false;

	@Override
	protected TaskResult execute() throws Exception {
		if( aborted ) {
			// Abort, abort!
			l4j.debug( this + ": aborted" );
			return new TaskResult(false);
		}
		try {
			byte[] data = null;
			while(true) {
				try {
					data = esu.readObject(path, extent, null);
					break;
				} catch(EsuException e) {
					System.err.println( "Read failed: " + e + "( " + e.getCause() + "), retrying" );
					try {
						Thread.sleep(500);
					} catch (InterruptedException e1) {
						l4j.debug( "Sleep interrupted", e1 );
					}
				}
			}

			channel.write(ByteBuffer.wrap(data), extent.getOffset());

			listener.complete(this);
		} catch (IOException e) {
			listener.error(this, e);
			return new TaskResult(false);
		}
		return new TaskResult(true);
	}

	public void setChannel(FileChannel channel) {
		this.channel = channel;
	}

	public void setEsu(EsuApi esu) {
		this.esu = esu;
	}

	public void setExtent(Extent extent) {
		this.extent = extent;
	}

	public void setListener(DownloadCompleteTask dct) {
		this.listener = dct;
	}

	public void setPath(ObjectPath path) {
		this.path = path;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((extent == null) ? 0 : extent.hashCode());
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DownloadBlockTask other = (DownloadBlockTask) obj;
		if (extent == null) {
			if (other.extent != null)
				return false;
		} else if (!extent.equals(other.extent))
			return false;
		if (path == null) {
			if (other.path != null)
				return false;
		} else if (!path.equals(other.path))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "DownloadBlockTask [extent=" + extent + ", path=" + path + "]";
	}

	public void abort() {
		// TODO Auto-generated method stub
		
	}

	
}
