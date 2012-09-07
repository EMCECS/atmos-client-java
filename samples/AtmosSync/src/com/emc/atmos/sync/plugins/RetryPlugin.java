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
import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;

import com.emc.esu.api.EsuException;

/**
 * @author cwikj
 * 
 */
public class RetryPlugin extends SyncPlugin {
	private static final Logger l4j = Logger.getLogger(RetryPlugin.class);
	private static final String RETRY_OPTION = "retries";
	private static final String RETRY_DESC = "Activates the retry plugin and " +
			"sets the max number of retries";
	private static final String RETRY_OPT_DESC = "max-retries";

	private int maxRetries = 3;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.emc.atmos.sync.plugins.SyncPlugin#filter(com.emc.atmos.sync.plugins
	 * .SyncObject)
	 */
	@Override
	public void filter(SyncObject obj) {
		Exception lastError = null;
		int retryCount = 0;
		while(retryCount < maxRetries) {
			try {
				getNext().filter(obj);
				return;
			} catch(EsuException e) {
				LogMF.warn(l4j, "Trapped Atmos Exception code {0}, HTTP {1} ({2})", e.getAtmosCode(), e.getHttpCode(), e);
				lastError = e;
				
				// By default, don't retry 400s (Bad Request)
				if(e.getHttpCode() >= 400 && e.getHttpCode() <= 499) {
					LogMF.warn(l4j, "Not retrying error {0}", e.getAtmosCode());
					throw e;
				}
				
				// For Atmos 1040 (server too busy), wait a few seconds
				if(e.getAtmosCode() == 1040) {
					l4j.info("Atmos code 1040 (too busy) for obj, sleeping 5 sec");
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e1) {
					}
				}
				
				retryCount++;
				LogMF.info(l4j, "Retry #{0}, Error: {1}", retryCount, e);
				continue;
			} catch(Exception e) {
				lastError = e;
				retryCount++;
				LogMF.info(l4j, "Retry #{0}, Error: {1}", retryCount, e);
				continue;				
			}
			
		}
		
		throw new RuntimeException("Retry failed: " + lastError, lastError);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.emc.atmos.sync.plugins.SyncPlugin#getOptions()
	 */
	@SuppressWarnings("static-access")
	@Override
	public Options getOptions() {
		Options opts = new Options();
		
		opts.addOption(OptionBuilder.withLongOpt(RETRY_OPTION)
				.withDescription(RETRY_DESC).hasArg()
				.withArgName(RETRY_OPT_DESC).create());
		
		return opts;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.emc.atmos.sync.plugins.SyncPlugin#parseOptions(org.apache.commons
	 * .cli.CommandLine)
	 */
	@Override
	public boolean parseOptions(CommandLine line) {
		if(line.hasOption(RETRY_OPTION)) {
			setMaxRetries(Integer.parseInt(line.getOptionValue(RETRY_OPTION)));
			LogMF.info(l4j, "Operations will be retried up to {0} times.", 
					maxRetries);
			return true;
		} else {
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.emc.atmos.sync.plugins.SyncPlugin#validateChain(com.emc.atmos.sync
	 * .plugins.SyncPlugin)
	 */
	@Override
	public void validateChain(SyncPlugin first) {
		while(first != null) {
			if(first.getNext() == null) {
				// Dest
				if(!(first instanceof AtmosDestination) || !(first instanceof DummyDestination)) {
					throw new RuntimeException("The RetryPlugin can only be " +
							"used with an Atmos or dummy destination");
				}
			}
			first = first.getNext();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.emc.atmos.sync.plugins.SyncPlugin#getName()
	 */
	@Override
	public String getName() {
		return "Retry";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.emc.atmos.sync.plugins.SyncPlugin#getDocumentation()
	 */
	@Override
	public String getDocumentation() {
		return "Allows for retrying operations on an Atmos destination.  On " +
				"any non-client error (e.g. HTTP 5XXs), the operation will " +
				"be retried.  In the case of Atmos code 1040 (server busy), " +
				"the plugin will pause the thread for 5 seconds before it " +
				"retries.  Note that if you are using multiple plugins " +
				"between the source and destination, you should use a Spring " +
				"configuration since when using plugins from the command " + 
				"line you cannot guarantee execution order.";
	}

	/**
	 * @return the maxRetries
	 */
	public int getMaxRetries() {
		return maxRetries;
	}

	/**
	 * @param maxRetries
	 *            the maxRetries to set
	 */
	public void setMaxRetries(int maxRetries) {
		this.maxRetries = maxRetries;
	}

}
