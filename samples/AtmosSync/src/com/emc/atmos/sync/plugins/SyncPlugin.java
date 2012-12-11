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

import com.emc.esu.api.Metadata;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

/**
 * Basic SyncPlugin parent class.  All plugins should inherit from this class.
 * To have your plugin participate in command-line parsing, implement the
 * following methods and then register your class with AtmosSync2.
 * <ul>
 * <li>getDocumentation()</li>
 * <li>getName</li>
 * <li>getOptions</li>
 * </ul>
 * If you do not want your plugin to configure itself via the command line,
 * (e.g. if you are using Spring), you can simply leave those methods empty.
 * @author cwikj
 */
public abstract class SyncPlugin {
	private SyncPlugin next;
	
	/**
	 * This is the main method of the plugin that processes the SyncObject
	 * being transferred.  In this method, you should implement your main
	 * logic for the plugin.  To pass the object to the next plugin in the
	 * chain, you must call <code>getNext().filter(obj)</code>.  However, if
	 * you decide that you do not wish to send the object down the chain, you
	 * can simply return.  Once the above method returns, your object has
	 * completed its journey down the chain and has "come back".  This is the
	 * point where you can implement any post-processing logic.
	 * @param obj the SyncObject to inspect and/or modify.
	 */
	public abstract void filter(SyncObject obj);
	
	/**
	 * This method returns the Apache commons CLI "Options" object.  This
	 * exposes any options <em>unique to this plugin</em>.  For options that
	 * may be shared across multiple plugins, see the CommonOptions class
	 * for options that are shared.
	 * @return the completed Options object
	 * @see com.emc.atmos.sync.plugins.CommonOptions
	 */
	public abstract Options getOptions();
	
	/**
	 * Called from the CLI to parse the given command line.  This method should
	 * inspect the given options to see whether this plugin should be used and
	 * if so, extract any other options it needs to configure iself.  If the
	 * plugin should be included, return true.  If an argument has an invalid
	 * setting, you can throw an IllegalArgumentException here.
	 * @param line The arguments passed from the command line.
	 * @return true if this plugin should be activated.
	 */
	public abstract boolean parseOptions(CommandLine line);
	
	/**
	 * If this plugin is to be included in the process, this method will be 
	 * called just before processing starts.  All of the plugins will have been
	 * confgured at this point.  This gives the plugin a last chance to
	 * inspect all of the other plugins present and throw an exception if some
	 * sort of incompatibility has been discovered.  For instance, if this
	 * plugin only supports Atmos destinations in object mode, it could
	 * traverse the plugin chain (via getNext()) until it reaches the source
	 * and then validate that the source is an AtmosDestination and that the
	 * destination's namespaceRoot is null.
	 * @param first the first plugin in the chain.
	 */
	public abstract void validateChain(SyncPlugin first);

	/**
	 * The name of this plugin.  This will be used when advertising the plugin
	 * on the command-line help.
	 * @return the plugin's name
	 */
	public abstract String getName();
	
	/**
	 * A description of the plugin's operation.  This will be printed in the
	 * command-line help under the plugin's name and just before its
	 * command-line argument list.
	 * @return the plugin's help documentation
	 */
	public abstract String getDocumentation();
	
	/**
	 * Performs cleanup activities when the process is complete (e.g. close
	 * file handles or database connections).
	 */
	public void cleanup() {
	}

	/**
	 * Returns the next pluign in the chain, creating a linked-list.
	 * @return the next plugin
	 */
	public SyncPlugin getNext() {
		return next;
	}

	/**
	 * Sets the next plugin in the chain.
	 * @param next the next plugin to set
	 */
	public void setNext(SyncPlugin next) {
		this.next = next;
	}

    protected String getMetaValue(SyncObject obj, String tagName) {
        if (obj.getMetadata() != null) {
            if (obj.getMetadata().getSystemMetadata() != null) {
                Metadata meta = obj.getMetadata().getSystemMetadata().getMetadata(tagName);
                if (meta != null) return meta.getValue();
            }
            if (obj.getMetadata().getMetadata() != null) {
                Metadata meta = obj.getMetadata().getMetadata().getMetadata(tagName);
                if (meta != null) return meta.getValue();
            }
        }
        return null;
    }
}
