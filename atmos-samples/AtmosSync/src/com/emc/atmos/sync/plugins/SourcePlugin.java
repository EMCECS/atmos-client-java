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

/**
 * The base class for all source plugins.  Source plugins will be executed
 * and then "push" data down the chain.  So, instead of implementing your
 * logic in filter(), you implement it in run().
 * @author cwikj
 */
public abstract class SourcePlugin extends SyncPlugin implements Runnable {

	/**
	 * Throws an exception since SourcePlugins dont ever filter() objects.
	 */
	@Override
	public void filter(SyncObject obj) {
		throw new UnsupportedOperationException("Source Plugins don't filter");
	}

	/**
	 * Starts execution of the source.  This should determine the objects to
	 * transfer, create SyncObjects for them, and send them down the pluign
	 * chain.  Once all objects have completed transferring, this method
	 * should return.
	 */
	public abstract void run();
	
	/**
	 * When called, this should shut down the transfer operations as soon as
	 * possible.
	 */
	public abstract void terminate();
	
	/**
	 * Print statistics about the transfers to stdout.  This will be called
	 * by the main AtmosSync2 class after run() has completed to print a 
	 * summary to the console.
	 */
	public abstract void printStats();

}
