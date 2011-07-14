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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.activation.MimetypesFileTypeMap;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;

import com.emc.esu.api.Acl;
import com.emc.esu.api.EsuApi;
import com.emc.esu.api.EsuException;
import com.emc.esu.api.Grant;
import com.emc.esu.api.Grantee;
import com.emc.esu.api.ObjectPath;
import com.emc.esu.api.Grantee.GRANT_TYPE;
import com.emc.esu.api.rest.EsuRestApiApache;
import com.emc.esu.api.rest.LBEsuRestApiApache;

/**
 * Utility to recursively upload files to Atmos, similar to rsync.
 * @author cwikj
 */
public class AtmosSync {
	
	private static final Logger l4j = Logger.getLogger(AtmosSync.class);
	public static final String MTIME_NAME = "atmossync_mtime";

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
				"Atmos Access Point Host(s).  Use more than once to round-robin hosts.");
		o.setRequired(true);
		o.setArgs(Option.UNLIMITED_VALUES);
		options.addOption(o);

		o = new Option("p", "port", true,
				"Atmos Access Point Port (Default 80)");
		o.setRequired(false);
		options.addOption(o);

		o = new Option("r", "remoteroot", true, "Remote root path (e.g. \"/\")");
		o.setRequired(true);
		options.addOption(o);

		o = new Option("l", "localroot", true, "Local root path.");
		o.setRequired(true);
		options.addOption(o);

		o = new Option("t", "threads", true, "Thread count.  Defaults to 8");
		o.setRequired(false);
		options.addOption(o);

		o = new Option("ua", "useracl", true,
				"User ACL (UID=READ|WRITE|FULL_CONTROL).  May be used more than once.");
		o.setRequired(false);
		o.setArgs(Option.UNLIMITED_VALUES);
		options.addOption(o);

		o = new Option(
				"ga",
				"groupacl",
				true,
				"Group ACL (group=READ|WRITE|FULL_CONTROL).  May be used more than once.  Usually, group is 'other'");
		o.setRequired(false);
		o.setArgs(Option.UNLIMITED_VALUES);
		options.addOption(o);
		
		o = new Option("D", "delete", false, "Delete local files after successful upload" );
		o.setRequired(false);
		options.addOption( o );

		// create the parser
		CommandLineParser parser = new GnuParser();
		try {
			// parse the command line arguments
			CommandLine line = parser.parse(options, args);
			String uid = line.getOptionValue("uid");
			String secret = line.getOptionValue("secret");
			String[] host = line.getOptionValues("host");
			int port = Integer.parseInt(line.getOptionValue("port", "80"));
			String remoteroot = line.getOptionValue("remoteroot");
			String localroot = line.getOptionValue("localroot");
			int threads = Integer.parseInt(line.getOptionValue("threads", "8"));
			String[] useracl = line.getOptionValues("useracl");
			String[] groupacl = line.getOptionValues("groupacl");
			boolean delete = line.hasOption( "delete" );

			AtmosSync sync = new AtmosSync(uid, secret, host, port, remoteroot, localroot, threads, useracl, groupacl, delete);
			sync.start();
		} catch (ParseException exp) {
			// oops, something went wrong
			System.err.println("Parsing failed.  Reason: " + exp.getMessage());
			// automatically generate the help statement
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("java -jar AtmosSync.jar", options);
		} catch (InterruptedException e) {
			l4j.error( "Execution interrupted " + e, e );
			System.exit( 4 );
		}
		System.exit(0);
	}

	private EsuApi esu;
	private EsuApi esuSingle;
	private File localroot;
	private String remoteroot;
	private int threads;
	private Acl acl;
	private String[] hosts;
	
	private long byteCount;
	private int fileCount;
	private int failedCount = 0;
	private int completedCount = 0;
	private LinkedBlockingQueue<Runnable> queue;
	private ThreadPoolExecutor pool;
	private Set<SyncItem> itemsRemaining;
	private Set<SyncItem> failedItems;
	
	private MimetypesFileTypeMap mimeMap;
	private boolean delete;
	
	private void start() throws InterruptedException {
		// Make sure localroot exists
		if( !localroot.exists() ) {
			l4j.error( "The local root " + localroot + " does not exist!" );
			System.exit( 1 );
		}
		
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
			// Check server version
			if( version.startsWith("1.2") || version.startsWith("1.3") ) {
				l4j.error( "AtmosSync requires Atmos 1.4+, server is running " + version );
				System.exit( 2 );
			}
			l4j.info( "Connected to atmos " + version + " on host(s) " + Arrays.asList(hosts) );
		} catch( Exception e ) {
			l4j.error( "Error connecting to server: " + e, e );
			System.exit( 3 );
		}
		
		l4j.info( "Starting sync from " + localroot + " to " + remoteroot );
		long start = System.currentTimeMillis();
		
		queue = new LinkedBlockingQueue<Runnable>();
		pool = new ThreadPoolExecutor(threads, threads, 15, TimeUnit.SECONDS, queue);
		itemsRemaining = Collections.synchronizedSet(new HashSet<SyncItem>());
		failedItems = Collections.synchronizedSet( new HashSet<SyncItem>() );

		doSync( "" );
		
		synchronized( this ) {
			for( SyncItem s : itemsRemaining ) {
				pool.submit(s);
			}
		}
		
		while( itemsRemaining.size() > 0 ) {
			Thread.sleep(500);
		}
		
		long end = System.currentTimeMillis();
		long secs = ((end-start)/1000);
		if( secs == 0 ) {
			secs = 1;
		}
		
		long rate = byteCount / secs;
		System.out.println("Uploaded " + byteCount + " bytes in " + secs + " seconds (" + rate + " bytes/s)" );
		System.out.println("Successful Files: " + completedCount + " Failed Files: " + failedCount );
		System.out.println("Failed Files: " + failedItems );

	}

	private void doSync(String relpath) {
		File localsync = new File( localroot, relpath );
		ObjectPath remotesync = new ObjectPath( remoteroot + relpath );
		l4j.debug( "Synchronizing " + localsync + " to " + remotesync );
		if( acl != null ) {
			mkdirs( remotesync, acl );
		}
		
		File[] files = localsync.listFiles();
		for( File f : files ) {
			if( f.isDirectory() ) {
				doSync( relpath + f.getName() + "/" );
			} else if( f.isFile() ) {
				String mimeType = mimeMap.getContentType(f);
				String objectPath = encodeObjectPath( remoteroot + relpath + f.getName() );
				SyncItem item = new SyncItem( esu, this, f, new ObjectPath( objectPath ), acl, mimeType );
				itemsRemaining.add(item);
				fileCount++;
			}
		}
		
		if( files.length == 0 && delete ) {
			localsync.delete();
		}
	}
	
	/**
	 * Encode characters that Atmos doesn't support (specifically @ and ?)
	 * @param op the path
	 * @return the path with invalid characters replaced
	 */
	private String encodeObjectPath( String op ) {
		op = op.replace( "?", "." );
		op = op.replace( "@", "." );
		return op;
	}

	public AtmosSync(String uid, String secret, String[] hosts, int port,
			String remoteroot, String localroot, int threads, String[] useracl,
			String[] groupacl, boolean delete) {
		this.esu = new LBEsuRestApiApache(Arrays.asList(hosts), port, uid, secret);
		this.esuSingle = new EsuRestApiApache(hosts[0], port, uid, secret);
		
		this.hosts = hosts;
		this.remoteroot = remoteroot;
		this.localroot = new File(localroot);
		this.threads = threads;
		this.mimeMap = new MimetypesFileTypeMap();
		this.delete = delete;
		
		if( useracl != null || groupacl != null ) {
			this.acl = new Acl();
			if( useracl != null ) {
				parseAcl(useracl, acl, GRANT_TYPE.USER);
			}
			if( groupacl != null ) {
				parseAcl(groupacl, acl, GRANT_TYPE.GROUP);
			}
		}
	}

	public synchronized void failure(SyncItem syncItem, File file, ObjectPath objectPath,
			Exception e) {
		
		if( e instanceof EsuException ) {
			l4j.error( "Failed to sync " + file + " to " + objectPath + ": " + e + " code: " + ((EsuException)e).getAtmosCode(), e );
			
		} else {
			l4j.error( "Failed to sync " + file + " to " + objectPath + ": " + e, e );
		}
		failedCount++;
		failedItems.add( syncItem );

		if( !itemsRemaining.remove(syncItem) ) {
			l4j.error( "Failed to remove " + syncItem );
		}		
	}

	public synchronized void success(SyncItem syncItem, File file, ObjectPath objectPath,
			long bytes) {
		byteCount += bytes;
		completedCount++;
		int pct = completedCount*100 / fileCount;
		l4j.info( pct + "% (" + completedCount + "/" + fileCount +") Completed: " + file );
		if( !itemsRemaining.remove(syncItem) ) {
			l4j.error( "Failed to remove " + syncItem );
		}
		
		if( delete ) {
			cleanup( syncItem.getFile() );
		}
	}

	private void cleanup(File file) {
		if( file.equals( localroot ) ) {
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

	private void mkdirs(ObjectPath dir, Acl acl) {
		if( dir == null ) {
			return;
		}
		
		boolean exists = false;
		try {
			esuSingle.getAllMetadata(dir);
			exists = true;
		} catch( EsuException e ) {
			if( e.getHttpCode() == 404 ) {
				// Doesn't exist
				l4j.debug( "remote object " + dir + " doesn't exist" );
			} else {
				l4j.error( "mkdirs failed for " + dir + ": " + e , e );
				return;
			}
		} catch( Exception e ) {
			l4j.error( "mkdirs failed for " + dir + ": " + e , e );
			return;
		}
		if( !exists ) {
			l4j.info( "mkdirs: " + dir );
			esuSingle.createObjectOnPath(dir, acl, null, null, null);
			mkdirs( getParentDir(dir), acl );
		}
	}

	private ObjectPath getParentDir(ObjectPath op) {
		String ops = op.toString();
		if( ops.endsWith( "/" ) ) {
			ops = ops.substring( 0, ops.length()-1 );
		}
		int lastslash = ops.lastIndexOf( '/' );
		if( lastslash == 0 ) {
			// root
			return null;
		}
		return new ObjectPath(ops.substring( 0, lastslash+1 ));
	}


	private void parseAcl(String[] aclstrs, Acl acl, GRANT_TYPE gtype ) {
		for( String str : aclstrs ) {
			String[] parts = str.split( "=", 2 );
			acl.addGrant( new Grant(new Grantee(parts[0], gtype), parts[1] ) );
		}
	}

}
