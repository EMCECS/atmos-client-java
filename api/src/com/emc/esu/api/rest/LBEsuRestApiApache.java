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
package com.emc.esu.api.rest;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import org.apache.log4j.Logger;

public class LBEsuRestApiApache extends EsuRestApiApache {
	private static final Logger l4j = Logger.getLogger(LBEsuRestApiApache.class);
	
	public static enum LBMode { ROUND_ROBIN, ROUND_ROBIN_THREADS };
	
	private List<String> hosts;
	private long requestCount = 0L;
	private LBMode mode = LBMode.ROUND_ROBIN_THREADS;
	private ThreadLocal<String> threadHost = new ThreadLocal<String>();
	
    public LBEsuRestApiApache(List<String> hosts, int port, String uid, String sharedSecret) {
        super(hosts.get(0), port, uid, sharedSecret);
    	this.hosts = hosts;
    }

	@Override
	protected URL buildUrl(String resource, String query) throws URISyntaxException, MalformedURLException {
		
		int uriport =0;
		if( "http".equals(proto) && port == 80 ) {
			// Default port
			uriport = -1;
		} else if( "https".equals(proto) && port == 443 ) {
			uriport = -1;
		} else {
			uriport = port;
		}
		
		String host = null;
		
		if( mode == LBMode.ROUND_ROBIN_THREADS ) {
			// Bind thread to a specific host
			if( threadHost.get() == null ) {
				threadHost.set( hosts.get( (int)(requestCount++ % hosts.size()) ) );
				l4j.info( "Thread bound to " + threadHost.get() );
			}
			host = threadHost.get();
		} else {
			host = hosts.get( (int)(requestCount++ % hosts.size()) );
		}
		
	    URI uri = new URI( proto, null, host, uriport, resource, query, null );
	    URL u = uri.toURL();
	    l4j.debug( "URL: " + u );
	    return u;
	}

	public LBMode getMode() {
		return mode;
	}

	public void setMode(LBMode mode) {
		this.mode = mode;
	}
}
