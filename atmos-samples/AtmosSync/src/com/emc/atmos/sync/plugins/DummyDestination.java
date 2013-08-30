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

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;

/**
 * Dummy destination object that can be used to test sources or filter plugins.
 * @author cwikj
 */
public class DummyDestination extends DestinationPlugin {
	private static final Logger l4j = Logger.getLogger(DummyDestination.class);
	
	public static final String SINK_DATA_OPTION = "sink-data";
	public static final String SINK_DATA_DESC = "Read all data from the input stream";
	private boolean sinkData;

	/**
	 * @see com.emc.atmos.sync.plugins.SyncPlugin#filter(com.emc.atmos.sync.plugins.SyncObject)
	 */
	@Override
	public void filter(SyncObject obj) {
		try {
			obj.setDestURI(new URI("file:///dev/null"));
		} catch (URISyntaxException e) {
			throw new RuntimeException(
					"Failed to build dest URI: " + e.getMessage(), e);
		}
		if(sinkData) {
			LogMF.debug(l4j, "Sinking source object {0}", obj.getSourceURI());
			byte[] buffer = new byte[4096];
			InputStream in = null;
			try {
				in = obj.getInputStream();
				while(in != null && in.read(buffer) != -1) {
					// Do nothing!
				}
			} catch (IOException e) {
				throw new RuntimeException(
						"Failed to read input stream: " + e.getMessage(), e);
			} finally {
				if(in != null) {
					try {
						in.close();
					} catch (IOException e) {
						//Ignore
					}
				}
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
		opts.addOption(OptionBuilder.withDescription(SINK_DATA_DESC)
				.withLongOpt(SINK_DATA_OPTION).create());
		
		return opts;
	}

	/**
	 * @see com.emc.atmos.sync.plugins.SyncPlugin#parseOptions(org.apache.commons.cli.CommandLine)
	 */
	@Override
	public boolean parseOptions(CommandLine line) {
		if("dummy".equals(line.getOptionValue(CommonOptions.DESTINATION_OPTION))) {
			if(line.hasOption(SINK_DATA_OPTION)) {
				sinkData = true;
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
		// No known plugins this is incompatible with.
	}

	/**
	 * @see com.emc.atmos.sync.plugins.SyncPlugin#getName()
	 */
	@Override
	public String getName() {
		return "Dummy Destination";
	}

	/**
	 * @see com.emc.atmos.sync.plugins.SyncPlugin#getDocumentation()
	 */
	@Override
	public String getDocumentation() {
		return "The dummy destination simply discards any data received.  With" +
				" the --sink-data option it will also read all data from any " +
				"input streams and discard that too.  This plugin is mainly " +
				"used for testing sources and filters.  It is activated by " +
				"using the destination 'dummy'";
	}

	public boolean isSinkData() {
		return sinkData;
	}

	public void setSinkData(boolean sinkData) {
		this.sinkData = sinkData;
	}

}
