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

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

public class CommonOptions extends SyncPlugin {
	public static final String METADATA_ONLY_OPTION = "metadata-only";
	public static final String METADATA_ONLY_DESC = "Instructs the destination plugin to only synchronize metadata";

    public static final String INCLUDE_RETENTION_EXPIRATION_OPTION = "include-retention-expiration";
    public static final String INCLUDE_RETENTION_EXPIRATION_DESC = "Instructs the destination plugin to *attempt* to replicate the source retention/expiration end-dates for each object (if enabled).  If the destination is an Atmos cloud, the target policy must enable retention/deletion immediately for this to work.";

    public static final String FORCE_OPTION = "force";
	public static final String FORCE_DESC = "Instructs the destination plugin to overwrite any existing objects";
	
	public static final String DELETE_OPTION = "delete";
	public static final String DELETE_DESC = "Instructs the source plugin to delete content after it is written to the destination.  Not supported by all source plugins";
	
	public static final String SOURCE_THREADS_OPTION = "source-threads";
	public static final String SOURCE_THREADS_DESC = "For multithreaded source plugins, the number of threads to use";
	public static final String SOURCE_THREADS_ARG_NAME = "thread-count";
	
	public static final String SOURCE_OPTION = "source";
	public static final String SOURCE_DESC = "The URI for the synchronization source.  Examples:\n" +
			"http://uid:secret@host:port  -- Uses Atmos as the source; could also be https.\n" +
			"file:///tmp/atmos/           -- Reads from a directory\n" +
			"tar:///tmp/atmos/backup.tar  -- Reads from a TAR archive\n" +
			"\nOther plugins may be available.  See their documentation for URI formats";
	public static final String SOURCE_ARG_NAME = "source-uri";
	
	public static final String DESTINATION_OPTION = "destination";
	public static final String DESTINATION_DESC = "The URI for the synchronization destination.  Examples:\n" +
			"http://uid:secret@host:port  -- Uses Atmos as the destination; could also be https.\n" +
			"file:///tmp/atmos/           -- Writes to a directory\n" +
			"tar:///tmp/atmos/backup.tar  -- Writes to a TAR archive\n" +
			"\nOther plugins may be available.  See their documentation for URI formats";
	public static final String DESTINATION_ARG_NAME = "destination-uri";
	
	public static final String HELP_OPTION = "help";
	public static final String HELP_DESC = "Displays this help content";
	
	public static final String SPRING_CONFIG_OPTION = "spring-config";
	public static final String SPRING_CONFIG_DESC = "Specifies a Spring bean configuration file.  In this mode, Spring is used to initialize the application configuration from a spring context XML file.  It is assumed that there is a bean named 'chain' containing an AtmosSync2 object.  This object will be initialized and executed.  In this mode all other CLI arguments are ignored.";
	public static final String SPRING_CONFIG_ARG_NAME = "path-to-spring-file";
	
	public static final String RECURSIVE_OPTION = "recursive";
	public static final String RECURSIVE_DESC = "Makes source plugins recursive, specifically the Atmos source when using namespaces and the filesystem source.";

    public static final String TIMINGS_OPTION = "timing";
    public static final String TIMINGS_DESC = "Enabled timings for all plug-ins that support it.  When specified, plug-ins will collect and periodically log average timing for various operations (i.e. read-metadata, stream-object, write-metadata, etc.)";

    public static final String TIMING_WINDOW_OPTION = "timing-window";
    public static final String TIMING_WINDOW_DESC = "Sets the timing window to use for timings.  Every {window-size} objects, timing statistics will be averaged and logged";
    public static final String TIMING_WINDOW_ARG_NAME = "window-size";

    @Override
	public void filter(SyncObject obj) {
		throw new UnsupportedOperationException("This plugin should never actually be used.");
	}

	@SuppressWarnings("static-access")
	@Override
	public Options getOptions() {
		Options opts = new Options();
		opts.addOption(OptionBuilder.withDescription(METADATA_ONLY_DESC)
				.withLongOpt(METADATA_ONLY_OPTION).create());
        opts.addOption(OptionBuilder.withDescription(INCLUDE_RETENTION_EXPIRATION_DESC)
                .withLongOpt(INCLUDE_RETENTION_EXPIRATION_OPTION).create());
		opts.addOption(OptionBuilder.withDescription(FORCE_DESC)
				.withLongOpt(FORCE_OPTION).create());
		opts.addOption(OptionBuilder.withDescription(SOURCE_THREADS_DESC)
				.withLongOpt(SOURCE_THREADS_OPTION)
				.hasArg().withArgName(SOURCE_THREADS_ARG_NAME).create());
		opts.addOption(OptionBuilder.withDescription(HELP_DESC)
				.withLongOpt(HELP_OPTION).create());
		opts.addOption(OptionBuilder.withDescription(SOURCE_DESC)
				.withLongOpt(SOURCE_OPTION)
				.hasArg().withArgName(SOURCE_ARG_NAME).create());
		opts.addOption(OptionBuilder.withDescription(DESTINATION_DESC)
				.withLongOpt(DESTINATION_OPTION)
				.hasArg().withArgName(DESTINATION_ARG_NAME).create());
		opts.addOption(OptionBuilder.withDescription(DELETE_DESC)
				.withLongOpt(DELETE_OPTION).create());
		opts.addOption(OptionBuilder.withDescription(RECURSIVE_DESC)
				.withLongOpt(RECURSIVE_OPTION).create());
		opts.addOption(OptionBuilder.withLongOpt(SPRING_CONFIG_OPTION)
				.withDescription(SPRING_CONFIG_DESC).hasArg()
				.withArgName(SPRING_CONFIG_ARG_NAME).create());
        opts.addOption(OptionBuilder.withDescription(TIMINGS_DESC)
                .withLongOpt(TIMINGS_OPTION).create());
        opts.addOption(OptionBuilder.withLongOpt(TIMING_WINDOW_OPTION)
                .withDescription(TIMING_WINDOW_DESC).hasArg()
                .withArgName(TIMING_WINDOW_ARG_NAME).create());
		return opts;
	}

	@Override
	public boolean parseOptions(CommandLine line) {
		// Never use this
		return false;
	}

	@Override
	public void validateChain(SyncPlugin first) {
		// Since this plugin doesn't actually do anything, we won't veto any
		// configurations.
	}

	@Override
	public String getName() {
		return "Common Options";
	}

	@Override
	public String getDocumentation() {
		return "Common options shared by multiple plugins.";
	}

}
