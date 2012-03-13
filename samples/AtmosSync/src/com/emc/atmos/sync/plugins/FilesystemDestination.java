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
package com.emc.atmos.sync.plugins;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;

import com.emc.atmos.sync.util.AtmosMetadata;

/**
 * The FilesystemDestination writes files to a local filesystem.
 * @author cwikj
 */
public class FilesystemDestination extends DestinationPlugin {
	private static final Logger l4j = Logger.getLogger(
			FilesystemDestination.class);
	
	public static final String NO_META_OPT = "no-fs-metadata";
	public static final String NO_META_DESC = "Disables writing metadata, ACL, and content type information to the " + AtmosMetadata.META_DIR + " directory";
	private File destination;
	private boolean noMetadata = false;
	private boolean force = false;

	/**
	 * @see com.emc.atmos.sync.plugins.SyncPlugin#filter(com.emc.atmos.sync.plugins.SyncObject)
	 */
	@Override
	public void filter(SyncObject obj) {
		File destFile = new File(destination, obj.getRelativePath());
		obj.setDestURI(destFile.toURI());
		
		LogMF.debug(l4j, "Writing {0} to {1}", obj.getSourceURI(), destFile);
		
		if(obj.isDirectory()) {
			mkdirs(destFile);
		} else {
			File parentDir = destFile.getParentFile();
			if(!parentDir.exists()) {
				parentDir.mkdirs();
			}
			// Copy the file data
			copyData(obj, destFile);
		}
		
		if(!noMetadata) {
			File metaFile = AtmosMetadata.getMetaFile(destFile);
			// mkdir as needed
			if(!metaFile.getParentFile().exists()) {
				mkdirs(metaFile.getParentFile());
			}
			Date ctime = obj.getMetadata().getCtime();
			if(ctime != null && !force && metaFile.exists()) {
				// Check date
				Date metaFileMtime = new Date(metaFile.lastModified());
				if(!ctime.after(metaFileMtime)) {
					LogMF.debug(l4j, "No change in metadata for {0}", obj.getSourceURI());
					return;
				}
			}
			try {
				obj.getMetadata().toFile(metaFile);
				if(ctime != null) {
					// Set the mtime to the source ctime (i.e. this 
					// metadata file's content is modified at the same
					// time as the source's metadata modification time)
					metaFile.setLastModified(ctime.getTime());
				}
			} catch (IOException e) {
				throw new RuntimeException("Failed to write metadata to: " + metaFile, e);
			}
		}

	}

	private void copyData(SyncObject obj, File destFile) {
		// Check timestamp if needed.
		Date mtime = obj.getMetadata().getMtime();
		if(mtime != null && !force && destFile.exists()) {
			Date destMtime = new Date(destFile.lastModified());
			if(!mtime.after(destMtime)) {
				LogMF.debug(l4j, "No change in content timestamps for {0}", obj.getSourceURI());
				return;
			}
		}
		
		byte[] buffer = new byte[65536];
		
		int c = 0;
		InputStream in = null;
		OutputStream out = null;
		try {
			in = obj.getInputStream();
			out = new FileOutputStream(destFile);
			while((c = in.read(buffer)) != -1) {
				out.write(buffer, 0, c);
			}
		} catch (IOException e) {
			throw new RuntimeException("Error writing: " + destFile + 
					": " + e.getMessage(), e);
		} finally {
			if(in != null) {
				try {
					in.close();
				} catch (IOException e) {
					// Ignore
				}
			}
			if(out != null) {
				try {
					out.close();
				} catch (IOException e) {
					// Ignore
				}
			}
		}
		
		// Set mtime if possible
		if(mtime != null) {
			destFile.setLastModified(mtime.getTime());
		}
	}


	/**
	 * Synchronized mkdir to prevent conflicts in threaded environment.
	 * @param destFile
	 */
	private static synchronized void mkdirs(File destFile) {
		if(!destFile.exists()) {
			if(!destFile.mkdirs()) {
				throw new RuntimeException("Failed to create directory " + 
						destFile);
			}
		}
	}

	/**
	 * @see com.emc.atmos.sync.plugins.SyncPlugin#getOptions()
	 */
	@SuppressWarnings("static-access")
	@Override
	public Options getOptions() {
		Options opts = new Options();
		
		opts.addOption(OptionBuilder.withDescription(NO_META_DESC)
				.withLongOpt(NO_META_OPT).create());
		
		return opts;
	}

	/**
	 * @see com.emc.atmos.sync.plugins.SyncPlugin#parseOptions(org.apache.commons.cli.CommandLine)
	 */
	@Override
	public boolean parseOptions(CommandLine line) {
		String destOption = line.getOptionValue(CommonOptions.DESTINATION_OPTION);
		if(destOption != null && destOption.startsWith("file://")) {
			URI u;
			try {
				u = new URI(destOption);
			} catch (URISyntaxException e) {
				throw new RuntimeException("Failed to parse URI: " + destOption + ": " + e.getMessage(), e);
			}
			destination = new File(u);
			if(!destination.exists()) {
				mkdirs(destination);
			}

			if(line.hasOption(NO_META_OPT)) {
				noMetadata = true;
			}
			
			if(line.hasOption(CommonOptions.FORCE_OPTION)) {
				force = true;
			}
			
			return true;
		}

		
		return false;
	}

	/**
	 * @see com.emc.atmos.sync.plugins.SyncPlugin#validateChain(com.emc.atmos.sync.plugins.SyncPlugin)
	 */
	@Override
	public void validateChain(SyncPlugin first) {
	}

	/**
	 * @see com.emc.atmos.sync.plugins.SyncPlugin#getName()
	 */
	@Override
	public String getName() {
		return "Filesystem Destination";
	}

	/**
	 * @see com.emc.atmos.sync.plugins.SyncPlugin#getDocumentation()
	 */
	@Override
	public String getDocumentation() {
		return "The filesystem desination writes data to a file or directory.  " + 
				"It is triggered by setting the desination to a valid File URL:\n" +
				"file://<path>, e.g. file:///home/user/myfiles\n" +
				"If the URL refers to a file, only that file will be " + 
				"transferred.  If a directory is specified, the source " + 
				"contents will be written into the directory.  By default, " + 
				"Atmos metadata, ACLs, and content type will be written to " +
				"a file with the same name inside the " + 
				AtmosMetadata.META_DIR + " directory.  Use the " + 
				NO_META_OPT + " to skip writing the metadata directory.  By " +
				"default, this plugin will check the mtime on the file and " +
				"its metadata file and only update if the source mtime and " +
				"ctime are later, respectively.  Use the --" + 
				CommonOptions.FORCE_OPTION + " to override this behavior and " +
				"always overwrite files.";
	}

}
