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
package com.emc.atmos.cleanup;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.emc.esu.api.DirectoryEntry;
import com.emc.esu.api.EsuException;
import com.emc.esu.api.ListOptions;
import com.emc.esu.api.ObjectPath;

public class DeleteDirTask extends TaskNode {
	private static final Logger l4j = Logger.getLogger(DeleteDirTask.class);
	
	private ObjectPath dirPath;
	private AtmosCleanup cleanup;
	private Set<DeleteDirChild> parentDirs = new HashSet<DeleteDirChild>();

	@Override
	protected TaskResult execute() throws Exception {
		//l4j.debug( "Descending into " + dirPath );
		
		try {
		
			// All entries in this directory will become parents of the child task
			// that deletes the current directory after it's empty.
			DeleteDirChild child = new DeleteDirChild();
			child.addParent(this);
			child.addToGraph(cleanup.getGraph());
			for(DeleteDirChild ch : parentDirs) {
				ch.addParent(child);
			}
			
			ListOptions options = new ListOptions();
			List<DirectoryEntry> ents = cleanup.getEsu().listDirectory(dirPath, options);
			while( options.getToken() != null ) {
				l4j.debug( "Continuing " + dirPath + " on token " + options.getToken() );
				ents.addAll( cleanup.getEsu().listDirectory( dirPath, options ) );
			}
			
			for( DirectoryEntry ent : ents ) {
				if( ent.getPath().toString().equals( "/apache/" ) ) {
					// Skip listable tags dir
					continue;
				}
				if( "directory".equals( ent.getType() ) ) {
					DeleteDirTask ddt = new DeleteDirTask();
					ddt.setDirPath(ent.getPath());
					ddt.setCleanup(cleanup);
					ddt.addParent(this);
					
					
					cleanup.increment(ent.getPath());
					ddt.addToGraph(cleanup.getGraph());
					child.addParent(ddt);
					// If we have any other children to depend on, add them
					for(DeleteDirChild ch : parentDirs) {
						ch.addParent(ddt);
						ddt.parentDirs.add(ch);
					}
					ddt.parentDirs.add(child);
				} else {
					DeleteFileTask dft = new DeleteFileTask();
					dft.setFilePath(ent.getPath());
					dft.setCleanup(cleanup);
					dft.addParent(this);
					cleanup.increment(ent.getPath());
					dft.addToGraph(cleanup.getGraph());
					child.addParent(dft);
				}
			}
			
		} catch(Exception e) {
			cleanup.failure(this, dirPath, e);
			return new TaskResult(false);
		}
		return new TaskResult(true);
	}

	public ObjectPath getDirPath() {
		return dirPath;
	}

	public void setDirPath(ObjectPath dirPath) {
		this.dirPath = dirPath;
	}

	public AtmosCleanup getCleanup() {
		return cleanup;
	}

	public void setCleanup(AtmosCleanup cleanup) {
		this.cleanup = cleanup;
	}
	
	
	/**
	 * You can't delete the directory until all the children are deleted, so
	 * this task will depend on all the children before delete.
	 */
	public class DeleteDirChild extends TaskNode {
	    public ObjectPath getDirPath() {
	        return dirPath;
	    }

		@Override
		protected TaskResult execute() throws Exception {
			if( !dirPath.toString().equals("/") ) {
				// Delete directory
				try {
					cleanup.getEsu().deleteObject( dirPath );
				} catch(EsuException e) {
					cleanup.failure(this, dirPath, e);
					return new TaskResult(false);
				}
			}
			cleanup.success(this, dirPath);

			return new TaskResult(true);
		}

		
		@Override
		public String toString() {
			return "DeleteDirTask$DeleteDirChild [dirPath=" + dirPath + "]";
		}
		
		@Override
		public boolean equals(Object obj) {
		    if(!(obj instanceof DeleteDirChild)) {
		        return false;
		    }
		    return dirPath.equals(((DeleteDirChild)obj).getDirPath());
		}
		
		@Override
		public int hashCode() {
		    return dirPath.hashCode();
		}
	}


	@Override
	public String toString() {
		return "DeleteDirTask [dirPath=" + dirPath + ", parentDirs="
				+ parentDirs + "]";
	}

	@Override
	public int hashCode() {
		return dirPath.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DeleteDirTask other = (DeleteDirTask) obj;
		if (dirPath == null) {
			if (other.dirPath != null)
				return false;
		} else if (!dirPath.equals(other.dirPath))
			return false;
		return true;
	}


}
