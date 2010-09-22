package com.emc.esu.api.rest;

import java.io.File;

import com.emc.esu.api.EsuException;
import com.emc.esu.api.Identifier;
import com.emc.esu.api.ObjectId;
import com.emc.esu.api.ObjectPath;

public class AtmosDownload {

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
		
		System.err.println( "Downloading " + atmosPath + " to " + localFile );
		
		Identifier id = null;
		if( atmosPath.contains( "/" ) ) {
			id = new ObjectPath( atmosPath );
		} else {
			id = new ObjectId( atmosPath );
		}
		
		try {
			DownloadHelper dh = new DownloadHelper( esu, new byte[5000] );
			dh.readObject( id,localFile );
		} catch( EsuException e ) {
			System.err.println( "Download Failed" );
			e.printStackTrace();
		}
	}

}
