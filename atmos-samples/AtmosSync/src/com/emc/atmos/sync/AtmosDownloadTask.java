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
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;

import org.apache.log4j.Logger;

import com.emc.esu.api.DirectoryEntry;
import com.emc.esu.api.Extent;

public class AtmosDownloadTask extends TaskNode {
	private static final Logger l4j = Logger.getLogger( AtmosDownloadTask.class );
	
	public static final long CHUNK_SIZE = 4 * 1024 * 1024;

	private File file;
	private DirectoryEntry ent;
	private AtmosSync atmosSync;

	public AtmosDownloadTask(File file, DirectoryEntry ent, AtmosSync sync) {
		this.file = file;
		this.ent = ent;
		this.atmosSync = sync;
	}

	@Override
	protected TaskResult execute() throws Exception {
		try {
			Date remoteMtime = null;
			
			// See if our mtime field is set
			if( ent.getUserMetadata().getMetadata( AtmosSync.MTIME_NAME ) != null ) {
				remoteMtime = new Date( Long.parseLong( ent.getUserMetadata().getMetadata( AtmosSync.MTIME_NAME ).getValue() ) );
			} else {
				// Check the regular Atmos mtime
				DateFormat df = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss'Z'" );
				df.setTimeZone( TimeZone.getTimeZone("UTC") );
				remoteMtime = df.parse( ent.getSystemMetadata().getMetadata( "mtime" ).getValue() );
			}
			
			long filesize = Long.parseLong( 
					ent.getSystemMetadata().getMetadata("size").getValue() );

			// Compare
			if( file.exists() && !atmosSync.isForce() ) {
				if( file.lastModified() == remoteMtime.getTime() && 
						file.length() == filesize ) {
					l4j.info( "Files are equal " + this );
					atmosSync.success(this, file, ent.getPath(), 0);
					return new TaskResult(true);
				}
			}
			
			
			// Do the download
			// Break up the file into chunks
			RandomAccessFile raf = new RandomAccessFile(file, "rw");
			FileChannel channel = raf.getChannel();
	
			long blockcount = filesize/CHUNK_SIZE;
			if( filesize%CHUNK_SIZE != 0 ) {
				blockcount++;
			}
			
			Set<DownloadBlockTask> blocks = new HashSet<DownloadBlockTask>();
			DownloadCompleteTask dct = new DownloadCompleteTask();
			for(long i=0; i<blockcount; i++) {
				long offset = i*CHUNK_SIZE;
				long size = CHUNK_SIZE;
				if( offset+size > filesize ) {
					size = filesize-offset;
				}
				Extent extent = new Extent(offset, size);
				
				DownloadBlockTask b = new DownloadBlockTask();
				b.setChannel(channel);
				b.setEsu(atmosSync.getEsu());
				b.setExtent(extent);
				b.setListener(dct);
				b.setPath(ent.getPath());
				
				dct.addParent(b);
	
				blocks.add( b );
				
				b.addParent( this );
				b.addToGraph( atmosSync.getGraph() );
			}
			
			dct.setChannel( channel );
			dct.setBlocks( blocks );
			dct.setAtmosSync( atmosSync );
			dct.setPath( ent.getPath() );
			dct.setFile( file );
			dct.addToGraph( atmosSync.getGraph() );
			dct.setSize( filesize );
			dct.setMtime( remoteMtime );
	
			return new TaskResult(true);
		} catch( Exception e ) {
			atmosSync.failure(this, file, ent.getPath(), e );
			return new TaskResult(false);
		}
	}

}
