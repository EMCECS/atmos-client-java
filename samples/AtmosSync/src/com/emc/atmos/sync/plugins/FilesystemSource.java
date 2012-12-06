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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.Date;
import java.util.Map;

import javax.activation.MimetypesFileTypeMap;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;

import com.emc.atmos.sync.util.AtmosMetadata;
import com.emc.atmos.sync.util.CountingInputStream;
import com.emc.esu.api.Metadata;

/**
 * The filesystem source reads data from a file or directory.
 * @author cwikj
 */
public class FilesystemSource extends MultithreadedCrawlSource {
	private static final Logger l4j = Logger.getLogger(FilesystemSource.class);
	
	public static final String IGNORE_META_OPT = "ignore-meta-dir";
	public static final String IGNORE_META_DESC = "Ignores any metadata in the " + AtmosMetadata.META_DIR + " directory";
	
	public static final String ABSOLUTE_PATH_OPT = "use-absolute-path";
	public static final String ABSOLUTE_PATH_DESC = "Uses the absolute path to the file when storing it instead of the relative path from the source dir.";
	
	public static final String DELETE_OLDER_OPT = "delete-older-than";
	public static final String DELETE_OLDER_DESC = "when --delete is used, add this option to only delete files that have been modified more than <delete-age> milliseconds ago";
	public static final String DELETE_OLDER_ARG_NAME = "delete-age";

	private File source;
	private boolean recursive;
	private boolean useAbsolutePath = false;
	private boolean ignoreMeta = false;
	private boolean delete = false;
	private long deleteOlderThan = 0;
	
	private MimetypesFileTypeMap mimeMap;

	public FilesystemSource() {
		mimeMap = new MimetypesFileTypeMap();
	}

	/**
	 * @see com.emc.atmos.sync.plugins.SourcePlugin#run()
	 */
	@Override
	public void run() {
		running = true;
		initQueue();
		
		// Enqueue the root task.
		ReadFileTask rootTask = new ReadFileTask(source);
		submitCrawlTask(rootTask);
		
		runQueue();
		
	}

	/**
	 * @see com.emc.atmos.sync.plugins.SourcePlugin#terminate()
	 */
	@Override
	public void terminate() {
		running = false;
	}

	/**
	 * @see com.emc.atmos.sync.plugins.SyncPlugin#getOptions()
	 */
	@SuppressWarnings("static-access")
	@Override
	public Options getOptions() {
		Options opts = new Options();
		opts.addOption(OptionBuilder.withDescription(IGNORE_META_DESC)
				.withLongOpt(IGNORE_META_OPT).create());
		opts.addOption(OptionBuilder.withDescription(ABSOLUTE_PATH_DESC)
				.withLongOpt(ABSOLUTE_PATH_OPT).create());
		opts.addOption(OptionBuilder.withLongOpt(DELETE_OLDER_OPT)
				.withDescription(DELETE_OLDER_DESC)
				.hasArg().withArgName(DELETE_OLDER_ARG_NAME).create());
		addOptions(opts);
		
		return opts;
	}

	/**
	 * @see com.emc.atmos.sync.plugins.SyncPlugin#parseOptions(org.apache.commons.cli.CommandLine)
	 */
	@Override
	public boolean parseOptions(CommandLine line) {
		String sourceOption = line.getOptionValue(CommonOptions.SOURCE_OPTION);
		if(sourceOption == null) {
			return false;
		}
		if(sourceOption.startsWith("file://")) {
			URI u;
			try {
				u = new URI(sourceOption);
			} catch (URISyntaxException e) {
				throw new RuntimeException("Failed to parse URI: " + sourceOption + ": " + e.getMessage(), e);
			}
			source = new File(u);
			if(!source.exists()) {
				throw new RuntimeException("The source " + source + " does not exist");
			}
			
			if(line.hasOption(CommonOptions.RECURSIVE_OPTION)) {
				recursive = true;
			}
			if(line.hasOption(IGNORE_META_OPT)) {
				ignoreMeta = true;
			}
			if(line.hasOption(CommonOptions.DELETE_OPTION)) {
				delete = true;
			}
			if(line.hasOption(DELETE_OLDER_OPT)) {
				deleteOlderThan = Long.parseLong(line.getOptionValue(DELETE_OLDER_OPT));
			}
			
			// Parse threading options
			super.parseOptions(line);
			
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
		return "Filesystem Source";
	}

	/**
	 * @see com.emc.atmos.sync.plugins.SyncPlugin#getDocumentation()
	 */
	@Override
	public String getDocumentation() {
		return "The filesystem source reads data from a file or directory.  " + 
				"It is triggered by setting the source to a valid File URL:\n" +
				"file://<path>, e.g. file:///home/user/myfiles\n" +
				"If the URL refers to a file, only that file will be " + 
				"transferred.  If a directory is specified, the contents of " +
				"the directory will be transferred.  If the --recursive" + 
				"flag is set, the subdirectories will also be recursively " +
				"transferred.  By default, any Atmos metadata files inside " +
				AtmosMetadata.META_DIR + " directories will be assigned to their " +
				"corresponding files; use --" + IGNORE_META_OPT +
				" to ignore the metadata directory.";
	}
		
	public class ReadFileTask implements Runnable {
		private File f;
		private Map<String, String> extraMetadata;

		public ReadFileTask(File f) {
			this.f = f;
		}

		@Override
		public void run() {
			FileSyncObject fso = null;
			try {
				fso = new FileSyncObject(f);
				if(extraMetadata != null) {
					for(String key : extraMetadata.keySet()) {
						String value = extraMetadata.get(key);
						fso.getMetadata().getMetadata().addMetadata(
								new Metadata(key, value, false));
					}
				}
			} catch(Exception e) {
				l4j.error("Error creating FileSyncObject: " + e, e);
				return;
			}
			try {
				getNext().filter(fso);
				
				if(delete) {
					// Try to lock the file first.  If this fails, the file is
					// probably open for write somewhere.
					// Note that on a mac, you can apparently delete files that
					// someone else has open for writing, and can lock files 
					// too.
					if(f.isDirectory()) {
						// Just try and delete
						if(!f.delete()) {
							LogMF.warn(l4j, "Failed to delete {0}", f);
						}						
					} else {
						boolean tryDelete = true;
						if(deleteOlderThan > 0) {
							if(System.currentTimeMillis() - f.lastModified() < deleteOlderThan) {
								LogMF.debug(l4j, 
										"Not deleting {0}; it is not at least {1} ms old", 
										f, deleteOlderThan);
								tryDelete = false;
							}
						}
						RandomAccessFile raf = null;
						if(tryDelete) {
							try {
								raf = new RandomAccessFile(f, "rw");
								FileChannel fc = raf.getChannel();
								FileLock flock = fc.lock();
								// If we got here, we should be good.
								flock.release();
								if(!f.delete()) {
									LogMF.warn(l4j, "Failed to delete {0}", f);
								}
							} catch(IOException e) {
								LogMF.info(l4j, 
										"File {0} not deleted, it appears to be open: {1}", 
										f, e.getMessage());
							} finally {
								if(raf != null) {
									raf.close();
								}
							}
						}
					}

					
				}
				
				complete(fso);
			} catch(Exception e) {
				failed(fso, e);
				return;
			} catch(Throwable t) {
				l4j.fatal("Uncaught throwable: " + t.getMessage(), t);
			}
			try {
				if(f.isDirectory()) {
					LogMF.debug(l4j, ">Crawling {0}", f);
					for(File child : f.listFiles()) {
						if(child.isDirectory() && child.getName().equals(AtmosMetadata.META_DIR)) {
							// skip
							continue;
						}
						if(!child.isDirectory() || (child.isDirectory() && recursive)) {
	 						ReadFileTask chTask = new ReadFileTask(child);
	 						
	 						// Directories that need crawling go into the crawler
	 						// queue.  All other objects go into the bounded 
	 						// transfer queue.  Note that adding to the transfer
	 						// queue might block if it's full.
	 						if(child.isDirectory()) {
	 							LogMF.debug(l4j, "+crawl {0}", child);
	 							submitCrawlTask(chTask);
	 						} else {
	 							LogMF.debug(l4j, "+transfer {0}", child);
	 							submitTransferTask(chTask);
	 						}
	 					}
					}
					LogMF.debug(l4j, "<Done Crawling {0}", f);
				}
			} catch(Exception e) { 
				l4j.error("Error enumerating directory: " + f, e);
				failed(fso, e);
			}
			
		}

		/**
		 * @return the extraMetadata
		 */
		public Map<String, String> getExtraMetadata() {
			return extraMetadata;
		}

		/**
		 * @param extraMetadata the extraMetadata to set
		 */
		public void setExtraMetadata(Map<String, String> extraMetadata) {
			this.extraMetadata = extraMetadata;
		}		
		
	}
	
	public class FileSyncObject extends SyncObject {
		private File f;
		private CountingInputStream in;
		private String relativePath;
		
		@Override
		public boolean isDirectory() {
			return f.isDirectory();
		}
		
		public FileSyncObject(File f) {
			this.f = f;
			setSize(f.length());
			setSourceURI(f.toURI());
			
			File metaFile = AtmosMetadata.getMetaFile(f);
			if(metaFile.exists() && !ignoreMeta) {
				try {
					setMetadata(AtmosMetadata.fromFile(metaFile));
				} catch (Exception e) {
					LogMF.warn(l4j, "Could not read metadata from {0}: {1}", metaFile, e.getMessage());
				}
			} else {
				// Default is empty, but we'll throw in the mime type.
				AtmosMetadata am = new AtmosMetadata();
				am.setContentType(mimeMap.getContentType(f));
				am.setMtime(new Date(f.lastModified()));
				setMetadata(am);
			}
			
            relativePath = f.getAbsolutePath();
            if(!useAbsolutePath && relativePath.startsWith(source.getAbsolutePath())) {
                relativePath = relativePath.substring(source.getAbsolutePath().length());
			}
            if(File.separatorChar == '\\') {
                relativePath = relativePath.replace('\\', '/');
            }
            if(relativePath.startsWith("/")) {
				relativePath = relativePath.substring(1);
			}
			if(f.isDirectory() && !relativePath.endsWith("/") && relativePath.length()>0) {
				relativePath += "/"; // Dirs must end with a slash (except for root);
			}
		}


		@Override
		public synchronized InputStream getInputStream() {
			if(f.isDirectory()) {
				return null;
			}
			if(in == null) {
				try {
					in = new CountingInputStream(new FileInputStream(f));
				} catch (FileNotFoundException e) {
					throw new RuntimeException("Could not open file:" + f, e);
				}
			}
			
			return in;
		}
		
		public long getBytesRead() {
			if(in != null) {
				return in.getBytesRead();
			} else {
				return 0;
			}
		}

		@Override
		public String getRelativePath() {
			return relativePath;
		}
		
	}

	/**
	 * @return the source
	 */
	public File getSource() {
		return source;
	}

	/**
	 * @param source the source to set
	 */
	public void setSource(File source) {
		this.source = source;
	}

	/**
	 * @return the recursive
	 */
	public boolean isRecursive() {
		return recursive;
	}

	/**
	 * @param recursive the recursive to set
	 */
	public void setRecursive(boolean recursive) {
		this.recursive = recursive;
	}

	/**
	 * @return the useAbsolutePath
	 */
	public boolean isUseAbsolutePath() {
		return useAbsolutePath;
	}

	/**
	 * @param useAbsolutePath the useAbsolutePath to set
	 */
	public void setUseAbsolutePath(boolean useAbsolutePath) {
		this.useAbsolutePath = useAbsolutePath;
	}

	/**
	 * @return the ignoreMeta
	 */
	public boolean isIgnoreMeta() {
		return ignoreMeta;
	}

	/**
	 * @param ignoreMeta the ignoreMeta to set
	 */
	public void setIgnoreMeta(boolean ignoreMeta) {
		this.ignoreMeta = ignoreMeta;
	}

	/**
	 * @return the mimeMap
	 */
	public MimetypesFileTypeMap getMimeMap() {
		return mimeMap;
	}

	/**
	 * @param mimeMap the mimeMap to set
	 */
	public void setMimeMap(MimetypesFileTypeMap mimeMap) {
		this.mimeMap = mimeMap;
	}
}
