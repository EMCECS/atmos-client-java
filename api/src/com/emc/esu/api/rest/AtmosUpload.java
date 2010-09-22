package com.emc.esu.api.rest;

import java.io.File;

import com.emc.esu.api.EsuException;
import com.emc.esu.api.ObjectPath;

public class AtmosUpload {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String atmosHost = args[0];
		int port = Integer.parseInt( args[1] );
		String atmosUid = args[2];
		String atmosSecret = args[3];
		String atmosPath = args[4];
		File localFile = new File(args[5]);
		
		EsuRestApi esu = new EsuRestApi( atmosHost, port, atmosUid, atmosSecret);
		
		if( atmosPath.endsWith( "/" ) ) {
			atmosPath = atmosPath + localFile.getName();
		}
		
		System.err.println( "Uploading " + localFile + " to " + atmosPath );
		try {
			UploadHelper uh = new UploadHelper( esu, new byte[5000] );
			uh.createObjectOnPath( new ObjectPath(atmosPath), localFile, null, null );
		} catch( EsuException e ) {
			System.err.println( "Upload Failed" );
			e.printStackTrace();
		}
	}

}
