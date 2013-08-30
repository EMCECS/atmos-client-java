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

import org.apache.log4j.Logger;

import com.emc.atmos.sync.AtmosSync.METADATA_MODE;
import com.emc.esu.api.EsuException;
import com.emc.esu.api.ObjectPath;

public class AtmosMkdirTask extends TaskNode {
	private static final Logger l4j = Logger.getLogger(AtmosMkdirTask.class);
	
	private ObjectPath dirPath;
	private AtmosSync sync;
	
	public AtmosMkdirTask( ObjectPath dirPath, AtmosSync sync ) {
		this.dirPath = dirPath;
		this.sync = sync;
	}

	@Override
	protected TaskResult execute() throws Exception {
		boolean exists = false;
		
		try {
			sync.getEsu().getAllMetadata(dirPath);
			exists = true;
		} catch( EsuException e ) {
			if( e.getHttpCode() == 404 ) {
				// Doesn't exist
				l4j.debug( "remote object " + dirPath + " doesn't exist" );
			} else {
				l4j.error( "mkdirs failed for " + dirPath + ": " + e , e );
				return new TaskResult(false);
			}
		} catch( Exception e ) {
			l4j.error( "mkdirs failed for " + dirPath + ": " + e , e );
			return new TaskResult(false);
		}
		if( !exists ) {
			l4j.info( "mkdir: " + dirPath );
			if( sync.getMetadataMode() == METADATA_MODE.BOTH 
					|| sync.getMetadataMode() == METADATA_MODE.DIRECTORIES ) {
				//l4j.info( "Creating " + dirPath + " with metadata " + sync.getMeta() );
				sync.getEsu().createObjectOnPath(dirPath, sync.getAcl(), 
						sync.getMeta(), null, null);
			} else {
				sync.getEsu().createObjectOnPath(dirPath, sync.getAcl(), 
						null, null, null);				
			}
		}
		
		return new TaskResult(true);
	}

}
