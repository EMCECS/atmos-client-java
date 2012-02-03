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
package com.emc.atmos.sync;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import com.emc.atmos.sync.plugins.AtmosDestination;
import com.emc.atmos.sync.plugins.AtmosSource;
import com.emc.atmos.sync.plugins.CommonOptions;
import com.emc.atmos.sync.plugins.DatabaseIdMapper;
import com.emc.atmos.sync.plugins.DestinationPlugin;
import com.emc.atmos.sync.plugins.DummyDestination;
import com.emc.atmos.sync.plugins.FilesystemDestination;
import com.emc.atmos.sync.plugins.FilesystemSource;
import com.emc.atmos.sync.plugins.GladinetMapper;
import com.emc.atmos.sync.plugins.IdLoggerPlugin;
import com.emc.atmos.sync.plugins.MetadataPlugin;
import com.emc.atmos.sync.plugins.S3Source;
import com.emc.atmos.sync.plugins.SourcePlugin;
import com.emc.atmos.sync.plugins.StripAclPlugin;
import com.emc.atmos.sync.plugins.SyncPlugin;

/**
 * New plugin-based sync program.  Can be configured in two ways:
 * 1) through a command-line parser
 * 2) through Spring.  Call run() on the AtmosSync2 object after your beans are
 * initialized.
 * @author cwikj
 */
public class AtmosSync2 implements Runnable, InitializingBean, DisposableBean {
	private static final Logger l4j = Logger.getLogger(AtmosSync2.class);

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Set<SyncPlugin> plugins = new HashSet<SyncPlugin>();
		
		plugins.add(new MetadataPlugin());
		plugins.add(new CommonOptions());
		plugins.add(new AtmosSource());
		plugins.add(new DummyDestination());
		plugins.add(new AtmosDestination());
		plugins.add(new StripAclPlugin());
		plugins.add(new IdLoggerPlugin());
		plugins.add(new DatabaseIdMapper());
		plugins.add(new FilesystemSource());
		plugins.add(new FilesystemDestination());
		plugins.add(new GladinetMapper());
		plugins.add(new S3Source());
		
		Map<String,SyncPlugin> optionMap = new HashMap<String, SyncPlugin>();
		
		// Add the generic options
		Options options = new Options();
		
		// Merge the options
		for(SyncPlugin plugin : plugins) {
			for(Iterator<?> i = plugin.getOptions().getOptions().iterator(); 
					i.hasNext(); ) {
				Option o = (Option)i.next();
				if(options.hasOption(o.getOpt())) {
					System.err.println("The plugin " + 
							optionMap.get(o.getOpt()).getName() + 
							" already installed option " + o.getOpt());
				} else {
					options.addOption(o);
					optionMap.put(o.getOpt(), plugin);
				}
			}
		}
		
		GnuParser gnuParser = new  GnuParser();
		CommandLine line = null;
		try {
			line = gnuParser.parse(options, args);
		} catch (ParseException e) {
			System.err.println(e.getMessage());
			help(plugins);
			System.exit(2);
		}
		
		// Special check for help
		if(line.hasOption(CommonOptions.HELP_OPTION)) {
			help(plugins);
			System.exit(0);
		}
		
		// Let the plugins parse the options and decide whether they want to
		// be included.
		AtmosSync2 sync = new AtmosSync2();
		for(SyncPlugin plugin : plugins) {
			if(plugin.parseOptions(line)) {
				sync.addPlugin(plugin);
			}
		}
		
		// do the sanity check (Spring will do this too)
		sync.afterPropertiesSet();
		
		sync.run();
		
		System.exit(0);
	}
	
	private static void help(Set<SyncPlugin> plugins) {
		HelpFormatter fmt = new HelpFormatter();
		
		// Make sure we do CommonOptions first
		for(SyncPlugin plugin : plugins) {
			if(plugin instanceof CommonOptions) {
				fmt.printHelp( "java -jar AtmosSync2.jar -source {source-uri} -destination {destination-uri} [options ...]\nCommon Options:", plugin.getOptions());
			}
		}
		System.out.println("\nThe following plugins are also installed and can be configured with their own options:\n");
		
		// Do the rest
		for(SyncPlugin plugin : plugins) {
			if(!(plugin instanceof CommonOptions)) {
				fmt.printHelp(plugin.getName() + " (" + plugin.getClass().getName() + ")\n" + plugin.getDocumentation(), plugin.getOptions());
			}
		}
	}

	
	private SourcePlugin source;
	private DestinationPlugin destination;
	private List<SyncPlugin> pluginChain;
	
	public AtmosSync2() {
		this.pluginChain = new ArrayList<SyncPlugin>();
	}

	private void addPlugin(SyncPlugin plugin) {
		if(plugin instanceof SourcePlugin) {
			LogMF.info(l4j, "Source: {0}: {1}", plugin.getName(), plugin.getClass());
			setSource((SourcePlugin) plugin);
		} else if(plugin instanceof DestinationPlugin) {
			LogMF.info(l4j, "Destination: {0}: {1}", plugin.getName(), plugin.getClass());
			setDestination((DestinationPlugin) plugin);
		} else {
			LogMF.info(l4j, "Plugin: {0}: {1}", plugin.getName(), plugin.getClass());
			pluginChain.add(plugin);
		}
	}

	public SourcePlugin getSource() {
		return source;
	}

	public void setSource(SourcePlugin source) {
		if(this.source != null) {
			throw new IllegalStateException(
					"A source plugin is already configured (" + 
							source.getName() +")");
		}
		this.source = source;
	}

	public DestinationPlugin getDestination() {
		return destination;
	}

	public void setDestination(DestinationPlugin destination) {
		if(this.destination != null) {
			throw new IllegalStateException(
					"A destination plugin is already configured (" + 
							destination.getName() +")");
		}
		this.destination = destination;
	}

	public List<SyncPlugin> getPluginChain() {
		return pluginChain;
	}

	public void setPluginChain(List<SyncPlugin> pluginChain) {
		this.pluginChain = pluginChain;
	}

	@Override
	public void run() {
		try {
			source.run();
			
			source.printStats();
		} catch(Throwable t) {
			t.printStackTrace();
		}
	}
	
	private void cleanup() {
		// Invoke cleanup on the plugins
		SyncPlugin p = source;
		while(p != null) {
			try {
				p.cleanup();
			} catch(Exception e) {
				LogMF.warn(l4j, "Failed to cleanup plugin {0}: {1}", p.getName(), e.getMessage());
			}
			p = p.getNext();
		}
	}

	@Override
	public void afterPropertiesSet() {
		// Some validation (must have source and destination)
		Assert.notNull(source, "Source plugin must be specified");
		Assert.notNull(destination, "Destination plugin must be specified");
		
		// Build the plugin chain
		SyncPlugin child = destination;
		for(int i=pluginChain.size()-1; i>=0; i--) {
			SyncPlugin current = pluginChain.get(i);
			current.setNext(child);
			child = current;
		}
		source.setNext(child);
		
		// Ask each plugin to validate the chain (resolves incompatible plugins)
		SyncPlugin p = source;
		while(p != null) {
			p.validateChain(source);
			p = p.getNext();
		}
	}

	@Override
	public void destroy() throws Exception {
		cleanup();
	}



}
