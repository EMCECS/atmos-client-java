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
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import javax.activation.MimetypesFileTypeMap;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;

import com.emc.atmos.sync.TaskNode;
import com.emc.atmos.sync.TaskResult;
import com.emc.atmos.sync.util.AtmosMetadata;
import com.emc.atmos.sync.util.CountingInputStream;

/**
 * The filesystem source reads data from a file or directory.
 * @author cwikj
 */
public class FilesystemSource extends MultithreadedSource {
	private static final Logger l4j = Logger.getLogger(FilesystemSource.class);
	
	public static final String IGNORE_META_OPT = "ignore-meta-dir";
	public static final String IGNORE_META_DESC = "Ignores any metadata in the " + AtmosMetadata.META_DIR + " directory";
	
	public static final String ABSOLUTE_PATH_OPT = "use-absolute-path";
	public static final String ABSOLUTE_PATH_DESC = "Uses the absolute path to the file when storing it instead of the relative path from the source dir.";

	private File source;
	private boolean recursive;
	private boolean useAbsolutePath = false;
	private boolean ignoreMeta = false;
	
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
		rootTask.addToGraph(graph);
		
		runQueue();
		
		if(!running) {
			// We were terminated
			pool.shutdownNow();
		}

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
		
	public class ReadFileTask extends TaskNode {
		private File f;

		public ReadFileTask(File f) {
			this.f = f;
		}

		@Override
		protected TaskResult execute() throws Exception {
			if(f.isDirectory()) {
				for(File child : f.listFiles()) {
					if(child.isDirectory() && child.getName().equals(AtmosMetadata.META_DIR)) {
						// skip
						continue;
					}
					if(!child.isDirectory() || (child.isDirectory() && recursive)) {
 						ReadFileTask chTask = new ReadFileTask(child);
 						chTask.addParent(this);
 						chTask.addToGraph(graph);
 					}
				}
				
			}
			
			FileSyncObject fso = null;
			try {
				fso = new FileSyncObject(f);
			} catch(Exception e) {
				throw new RuntimeException("Failed to create fso: " + f, e);
			}
			try {
				getNext().filter(fso);
				complete(fso);
			} catch(Exception e) {
				failed(fso, e);
				return TaskResult.FAILURE;
			} catch(Throwable t) {
				l4j.fatal("Uncaught throwable: " + t.getMessage(), t);
			}
			
			return TaskResult.SUCCESS;
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
