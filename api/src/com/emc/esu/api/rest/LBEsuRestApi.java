package com.emc.esu.api.rest;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * Implements a simple load-balanced version of EsuRestApi that round-robins through
 * a list of access point hostnames.  This should increase performance in a heavily
 * threaded environment by distributing requests against a number of hosts.
 * @author cwikj
 */
public class LBEsuRestApi extends EsuRestApi {
	private static final Logger l4j = Logger.getLogger(LBEsuRestApi.class);
	
	private List<String> hosts;
	private long requestCount = 0L;
	
    public LBEsuRestApi(List<String> hosts, int port, String uid, String sharedSecret) {
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
		
		String host = hosts.get( (int)(requestCount++ % hosts.size()) );
		
	    URI uri = new URI( proto, null, host, uriport, resource, query, null );
	    URL u = uri.toURL();
	    l4j.debug( "URL: " + u );
	    return u;
	}
    
    

}
