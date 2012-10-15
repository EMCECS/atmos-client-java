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

/**
 * @author cwikj
 *
 */
public class OverrideMimetypePlugin extends SyncPlugin {
	private static final String OVERRIDE_MIMETYPE_OPTION = "override-mimetype";
	private static final String OVERRIDE_MIMETYPE_DESC = "enables the " +
			"override mimetype plugin that will override the mimetype of " +
			"an object.";
	private static final String OVERRIDE_MIMETYPE_ARG_NAME = "mimetype";
	private static final String FORCE_MIMETYPE_OPTION = "force-mimetype";
	private static final String FORCE_MIMETYPE_DESC = "If specified, the " +
			"mimetype will be overwritten regardless of its prior value.";
	
	private String mimeType;
	private boolean force;

	@Override
	public void filter(SyncObject obj) {
		if(force) {
			obj.getMetadata().setContentType(mimeType);
		} else {
			if(obj.getMetadata().getContentType() == null || 
					obj.getMetadata().getContentType().equals(
							"application/octet-stream")) {
				obj.getMetadata().setContentType(mimeType);
			}
		}
		
		getNext().filter(obj);
	}

	/* (non-Javadoc)
	 * @see com.emc.atmos.sync.plugins.SyncPlugin#getOptions()
	 */
	@SuppressWarnings("static-access")
	@Override
	public Options getOptions() {
		Options opts = new Options();
		opts.addOption(OptionBuilder
				.withLongOpt(OVERRIDE_MIMETYPE_OPTION)
				.withDescription(OVERRIDE_MIMETYPE_DESC)
				.hasArg().withArgName(OVERRIDE_MIMETYPE_ARG_NAME).create());
		
		opts.addOption(OptionBuilder
				.withLongOpt(FORCE_MIMETYPE_OPTION)
				.withDescription(FORCE_MIMETYPE_DESC).create());
		
		return opts;
	}

	@Override
	public boolean parseOptions(CommandLine line) {
		if(!line.hasOption(OVERRIDE_MIMETYPE_OPTION)) {
			return false;
		}
		mimeType = line.getOptionValue(OVERRIDE_MIMETYPE_OPTION);
		
		force = line.hasOption(FORCE_MIMETYPE_OPTION);
		
		return true;
	}

	@Override
	public void validateChain(SyncPlugin first) {
	}

	@Override
	public String getName() {
		return "Override Mimetype";
	}

	@Override
	public String getDocumentation() {
		return "This plugin allows you to override the default mimetype " +
				"of objects getting transferred.  It is useful for instances " +
				"where the mimetype of an object cannot be inferred from " +
				"its extension or is nonstandard (not in Java's " +
				"mime.types file).  You can also use the force option to " +
				"override the mimetype of all objects.";
	}

}
