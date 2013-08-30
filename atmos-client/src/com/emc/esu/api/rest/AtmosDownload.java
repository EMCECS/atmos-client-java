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
