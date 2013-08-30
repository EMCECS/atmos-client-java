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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;

import com.emc.atmos.sync.AtmosSync.METADATA_MODE;
import com.emc.esu.api.EsuException;
import com.emc.esu.api.Metadata;
import com.emc.esu.api.MetadataList;
import com.emc.esu.api.ObjectMetadata;
import com.emc.esu.api.ObjectPath;

public class AtmosUploadTask extends TaskNode {
	private static final Logger l4j = Logger.getLogger( AtmosUploadTask.class );

	private File file;
	private ObjectPath objectPath;
	private String mimeType;
	private AtmosSync atmosSync;

	public AtmosUploadTask(File file, ObjectPath objectPath, String mimeType,
			AtmosSync sync) {
		
		this.file = file;
		this.objectPath = objectPath;
		this.mimeType = mimeType;
		this.atmosSync = sync;
	}

	@Override
	protected TaskResult execute() throws Exception {
		// Check to see if the local file exists and/or matches the local
		boolean exists = false;
		ObjectMetadata om = null;
		try {
			om = atmosSync.getEsu().getAllMetadata(objectPath);
			exists = true;
		} catch( EsuException e ) {
			if( e.getHttpCode() == 404 ) {
				// Doesn't exist
				l4j.debug( "remote object " + objectPath + " doesn't exist" );
			} else {
				atmosSync.failure( this, file, objectPath, e );
				return new TaskResult(false);
			}
		} catch( Exception e ) {
			atmosSync.failure( this, file, objectPath, e );
			return new TaskResult(false);
		}
		
		if( exists && !atmosSync.isForce() ) {
			// Check size and atmossync_mtime
			long remoteSize = Long.parseLong(om.getMetadata().getMetadata( "size" ).getValue() );
			if( remoteSize != file.length() ) {
				l4j.debug( file + " size (" + file.length() + ") differs from remote " + objectPath + "(" + remoteSize + ")" );
			} else {
				// Check mtime
				if( om.getMetadata().getMetadata( AtmosSync.MTIME_NAME ) != null ) {
					long remoteMtime = Long.parseLong(om.getMetadata().getMetadata( AtmosSync.MTIME_NAME ).getValue() );
					if( remoteMtime != file.lastModified() ) {
						l4j.debug( file + " timestamp (" + file.lastModified() + ") differs from remote " + objectPath + "(" + remoteMtime + ")" );
					} else {
						// Matches
						l4j.debug( file + " matches remote " + objectPath );
						
						if( atmosSync.isDelete() ) {
							cleanup( file );
						}

						atmosSync.success( this, file, objectPath, 0 );
						return new TaskResult(true);
					}
				}
			}
		}
		
		// If we're here, it's time to copy!
		l4j.debug( "Uploading " + file + " to " + objectPath + " (" + mimeType + "): " + file.length() + " bytes" );
		
		// If ACLs are required, do a quick check to make sure the directory
		// exists.  There could be a slight delay sometimes between hosts.
		if( atmosSync.getAcl() != null ) {
			boolean dirExists = false;
			ObjectPath parentDir = AtmosSync.getParentDir( objectPath );
			
			dirExists = pathExists( parentDir );
			
			if( !dirExists ) {
				l4j.info( "Sleeping 100ms to wait for parent" );
				Thread.sleep(100);
				
				dirExists = pathExists( parentDir );
				if( !dirExists ) {
					throw new RuntimeException( "Parent directory " + parentDir + " does not exist!" );
				}
			}
			

		}
		
		InputStream in = null;
		try {
			in = new FileInputStream( file );
					
			MetadataList mlist = new MetadataList();
			mlist.addMetadata( new Metadata(AtmosSync.MTIME_NAME, ""+file.lastModified(), false ) );
			
			if( atmosSync.getMeta() != null 
					&& (atmosSync.getMetadataMode() == METADATA_MODE.BOTH 
					|| atmosSync.getMetadataMode() == METADATA_MODE.FILES ) ) {
				for( Metadata m : atmosSync.getMeta() ) {
					mlist.addMetadata( m );
				}
			}
			
			if( exists ) {
				atmosSync.getEsu().updateObjectFromStream(objectPath, atmosSync.getAcl(), mlist, null, in, file.length(), mimeType);
			} else {
				atmosSync.getEsu().createObjectFromStreamOnPath(objectPath, atmosSync.getAcl(), mlist, in, file.length(), mimeType);
			}
			
			if( atmosSync.isDelete() ) {
				cleanup( file );
			}

			atmosSync.success(this, file, objectPath, file.length());
			return new TaskResult( true );
		} catch (FileNotFoundException e) {
			atmosSync.failure(this, file, objectPath, e);
			return new TaskResult(false);
		} catch (EsuException e ) {
			atmosSync.failure(this, file, objectPath, e);
			return new TaskResult(false);
		} catch (Throwable t ) {
			atmosSync.failure(this, file, objectPath, new RuntimeException(t.toString(),t));
			return new TaskResult(false);
		} finally {
			if( in != null ) {
				try {
					in.close();
				} catch (IOException e) {
					// Ignore
				}
			}
		}
		

	}
	
	private boolean pathExists( ObjectPath path ) {
		try {
			atmosSync.getEsu().getAllMetadata( path );
			return true;
		} catch( EsuException e ) {
			if( e.getHttpCode() == 404 ) {
				// Doesn't exist
				l4j.debug( "remote object " + path + " doesn't exist" );
			} else {
				l4j.error( "get dir failed for " + path + ": " + e , e );
			}
		} catch( Exception e ) {
			l4j.error( "get dir failed for " + path + ": " + e , e );
		}
		
		return false;
	}
	
	@Override
	public boolean equals(Object obj) {
		if( !(obj instanceof AtmosUploadTask) ) {
			return false;
		}
		AtmosUploadTask other = (AtmosUploadTask)obj;
		return other.file.equals(file) && other.objectPath.equals(objectPath);
	}

	@Override
	public int hashCode() {
		return file.hashCode();
	}

	@Override
	public String toString() {
		return file.toString() + " -> " + objectPath.toString();
	}
	
	private void cleanup(File file) {
		if( file.equals( atmosSync.getLocalroot() ) ) {
			// Stop here
			return;
		}
		l4j.info( "Deleting " + file );
		if( !file.delete() ) {
			l4j.warn( "Failed to delete " + file );
			return;
		}
		
		// If it's a directory, see if it's empty.
		File parent = file.getParentFile();
		if( parent.isDirectory() && parent.listFiles().length == 0 ) {
			cleanup( parent );
		}
		
	}

}
