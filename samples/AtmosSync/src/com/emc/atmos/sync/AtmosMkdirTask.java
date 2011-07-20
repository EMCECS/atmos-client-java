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
