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
package com.emc.atmos.cleanup;

import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;

import com.emc.esu.api.DirectoryEntry;
import com.emc.esu.api.EsuApi;
import com.emc.esu.api.ListOptions;
import com.emc.esu.api.ObjectPath;
import com.emc.esu.api.rest.EsuRestApiApache;

/**
 * Recursively deletes objects in the Atmos namespace (like rm -r).
 * @author cwikj
 *
 */
public class AtmosCleanup {
	private static final Logger l4j = Logger.getLogger(AtmosCleanup.class);
	private String host;
	private String remoteroot;
	private EsuApi esu;
	private int dirCount;
	private int fileCount;
	
	public AtmosCleanup(String uid, String secret, String host, int port,
			String remoteroot) {
		this.host = host;
		this.remoteroot = remoteroot;
		this.esu = new EsuRestApiApache(host, port, uid, secret);
		
		dirCount = 0;
		fileCount = 0;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Options options = new Options();
		Option o = new Option("u", "uid", true, "Atmos UID");
		o.setRequired(true);
		options.addOption(o);

		o = new Option("s", "secret", true, "Atmos Shared Secret");
		o.setRequired(true);
		options.addOption(o);

		o = new Option("h", "host", true,
				"Atmos Access Point Host");
		o.setRequired(true);
		options.addOption(o);

		o = new Option("p", "port", true,
				"Atmos Access Point Port (Default 80)");
		o.setRequired(false);
		options.addOption(o);

		o = new Option("r", "remoteroot", true, "Remote root path (e.g. \"/\")");
		o.setRequired(true);
		options.addOption(o);

		// create the parser
		CommandLineParser parser = new GnuParser();
		try {
			// parse the command line arguments
			CommandLine line = parser.parse(options, args);
			String uid = line.getOptionValue("uid");
			String secret = line.getOptionValue("secret");
			String host = line.getOptionValue("host");
			int port = Integer.parseInt(line.getOptionValue("port", "80"));
			String remoteroot = line.getOptionValue("remoteroot");

			AtmosCleanup cleanup = new AtmosCleanup(uid, secret, host, port, remoteroot);
			cleanup.start();
		} catch (ParseException exp) {
			// oops, something went wrong
			System.err.println("Parsing failed.  Reason: " + exp.getMessage());
			// automatically generate the help statement
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("AtmosThreadedDownload", options);
		} 
		
		System.exit(0);
	}

	private void start() {
		// Make sure remote path is in the correct format
		if( !remoteroot.startsWith( "/" ) ) {
			remoteroot = "/" + remoteroot;
		}
		if( !remoteroot.endsWith( "/" ) ) {
			// Must be a dir (ends with /)
			remoteroot = remoteroot + "/";
		}
		
		// Test connection to server
		try {
			String version = esu.getServiceInformation().getAtmosVersion();
			l4j.info( "Connected to atmos " + version + " on host " + host );
		} catch( Exception e ) {
			l4j.error( "Error connecting to server: " + e, e );
			System.exit( 3 );
		}
		
		ObjectPath op = new ObjectPath( remoteroot );
		
		doCleanup(op);
		
		l4j.info( "deleted " + fileCount + " files and " + dirCount + " directories" );
		
		System.exit( 0 );
	}

	private void doCleanup(ObjectPath op) {
		l4j.debug( "Descending into " + op );
		ListOptions options = new ListOptions();
		List<DirectoryEntry> ents = esu.listDirectory(op, options);
		while( options.getToken() != null ) {
			l4j.debug( "Continuing " + op + " on token " + options.getToken() );
			ents.addAll( esu.listDirectory( op, options ) );
		}
		
		for( DirectoryEntry ent : ents ) {
			if( ent.getPath().toString().equals( "/apache/" ) ) {
				// Skip listable tags dir
				continue;
			}
			if( "directory".equals( ent.getType() ) ) {
				doCleanup( ent.getPath() );
			} else {
				esu.deleteObject( ent.getPath() );
				fileCount++;
			}
		}
		
		if( !op.toString().equals("/") ) {
			// Delete directory
			esu.deleteObject( op );
			dirCount++;
		}
	}

}
