// Copyright (c) 2011, EMC Corporation.
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

import com.emc.esu.api.Acl;
import com.emc.esu.api.EsuApi;
import com.emc.esu.api.EsuException;
import com.emc.esu.api.Metadata;
import com.emc.esu.api.MetadataList;
import com.emc.esu.api.ObjectMetadata;
import com.emc.esu.api.ObjectPath;

public class SyncItem implements Runnable {
	private static final Logger l4j = Logger.getLogger( SyncItem.class );

	private EsuApi esu;
	private AtmosSync atmosSync;
	private File file;
	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	private ObjectPath objectPath;
	private Acl acl;

	private String mimeType;

	public SyncItem( EsuApi esu, AtmosSync atmosSync, File f,
			ObjectPath objectPath, Acl acl, String mimeType) {
		this.esu = esu;
		this.atmosSync = atmosSync;
		this.file = f;
		this.objectPath = objectPath;
		this.acl = acl;
		this.mimeType = mimeType;
	}

	@Override
	public void run() {
		// Check to see if the local file exists and/or matches the local
		boolean exists = false;
		ObjectMetadata om = null;
		try {
			om = esu.getAllMetadata(objectPath);
			exists = true;
		} catch( EsuException e ) {
			if( e.getHttpCode() == 404 ) {
				// Doesn't exist
				l4j.debug( "remote object " + objectPath + " doesn't exist" );
			} else {
				atmosSync.failure( this, file, objectPath, e );
				return;
			}
		} catch( Exception e ) {
			atmosSync.failure( this, file, objectPath, e );
			return;
		}
		
		if( exists ) {
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
						atmosSync.success( this, file, objectPath, 0 );
						return;
					}
				}
			}
		}
		
		// If we're here, it's time to copy!
		l4j.debug( "Uploading " + file + " to " + objectPath + " (" + mimeType + "): " + file.length() + " bytes" );
		InputStream in = null;
		try {
			in = new FileInputStream(file);
					
			MetadataList mlist = new MetadataList();
			mlist.addMetadata( new Metadata(AtmosSync.MTIME_NAME, ""+file.lastModified(), false ) );
			
			if( exists ) {
				esu.updateObjectFromStream(objectPath, acl, mlist, null, in, file.length(), mimeType);
			} else {
				esu.createObjectFromStreamOnPath(objectPath, acl, mlist, in, file.length(), mimeType);
			}
			atmosSync.success(this, file, objectPath, file.length());
		} catch (FileNotFoundException e) {
			atmosSync.failure(this, file, objectPath, e);
			return;
		} catch (EsuException e ) {
			atmosSync.failure(this, file, objectPath, e);
			return;
		} catch (Throwable t ) {
			atmosSync.failure(this, file, objectPath, new RuntimeException(t.toString(),t));
			return;
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


	@Override
	public boolean equals(Object obj) {
		SyncItem other = (SyncItem)obj;
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

}
