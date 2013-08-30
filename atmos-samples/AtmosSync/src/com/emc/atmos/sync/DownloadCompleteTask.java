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

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import com.emc.esu.api.ObjectPath;

public class DownloadCompleteTask extends TaskNode {
	private static final Logger l4j = Logger.getLogger( DownloadCompleteTask.class );

	private FileChannel channel;
	private Set<DownloadBlockTask> blocks;
	private Set<DownloadBlockTask> completedBlocks;

	private ObjectPath path;
	private File file;
	private boolean successful = true;
	private Exception failure;
	private AtmosSync atmosSync;
	private long size;

	private Date mtime;
	
	public DownloadCompleteTask() {
		completedBlocks = Collections.synchronizedSet( 
				new HashSet<DownloadBlockTask>() );
	}


	@Override
	protected TaskResult execute() throws Exception {
		channel.close();
		
		if( successful ) {
			file.setLastModified( mtime.getTime() );
			atmosSync.success(this, file, path, size);
			return new TaskResult(true);
		} else {
			atmosSync.failure(this, file, path, failure);
			return new TaskResult(false);
		}
	}


	public void error(DownloadBlockTask downloadBlockTask, IOException e) {
		successful = false;
		failure = e;
		
		// Cancel the rest of the tasks
		for( DownloadBlockTask bt : blocks ) {
			bt.abort();
		}
	}

	public void complete(DownloadBlockTask downloadBlockTask) {
		completedBlocks.add(downloadBlockTask);
		
		l4j.debug( "Complete: " + downloadBlockTask );
	}


	public void setChannel(FileChannel channel) {
		this.channel = channel;
	}


	public void setBlocks(Set<DownloadBlockTask> blocks) {
		this.blocks = blocks;
	}


	public void setAtmosSync(AtmosSync atmosSync) {
		this.atmosSync = atmosSync;
	}


	public void setPath(ObjectPath path) {
		this.path = path;
	}


	public void setFile(File file) {
		this.file = file;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
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
		DownloadCompleteTask other = (DownloadCompleteTask) obj;
		if (path == null) {
			if (other.path != null)
				return false;
		} else if (!path.equals(other.path))
			return false;
		return true;
	}


	@Override
	public String toString() {
		return "DownloadCompleteTask [path=" + path + ", file=" + file + "]";
	}


	public void setSize(long filesize) {
		this.size = filesize;
	}


	public void setMtime(Date remoteMtime) {
		this.mtime = remoteMtime;
	}

}
