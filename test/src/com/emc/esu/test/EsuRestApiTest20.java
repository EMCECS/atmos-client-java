package com.emc.esu.test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.junit.Before;

import com.emc.esu.api.rest.LBEsuRestApi;

public class EsuRestApiTest20 extends EsuApiTest20 {
    /**
     * UID to run tests with.  Set in properties file or -Datmos.uid.
     */
    private String uid2;

    /**
     * Shared secret for UID.  Set in atmos.properties or -Datmos.secret
     */
    private String secret;

    /**
     * Hostname or IP of ESU server.  Set in atmos.properties or -Datmos.host
     */
    private String host;
    
    /**
     * Port of ESU server (usually 80 or 443). Set in atmos.properties or -Datmos.port
     */
    private int port = 80;
    	
    public EsuRestApiTest20() {
    	super();
    	
    	InputStream in = ClassLoader.getSystemResourceAsStream( "atmos.properties" );
    	if( in != null ) {
    		try {
				System.getProperties().load(in);
			} catch (IOException e) {
				throw new RuntimeException( "Could not load atmos.properties", e);
			}
    	}
    	
    	uid2 = System.getProperty( "atmos.uid" );
    	if( uid2 == null ) {
    		throw new RuntimeException( "atmos.uid is null.  Set in atmos.properties or on command line with -Datmos.uid" );
    	}
    	secret = System.getProperty( "atmos.secret" );
    	if( secret == null ) {
    		throw new RuntimeException( "atmos.secret is null.  Set in atmos.properties or on command line with -Datmos.secret" );
    	}
    	host = System.getProperty( "atmos.host" );
    	if( host == null ) {
    		throw new RuntimeException( "atmos.host is null.  Set in atmos.properties or on command line with -Datmos.host" );
    	}
    	port = Integer.parseInt( System.getProperty( "atmos.port" ) );
    }
       
    /**
     * @see junit.framework.TestCase#setUp()
     */
    @Before
    public void setUp() throws Exception {
        esu = new LBEsuRestApi( Arrays.asList(new String[] { host }), port, uid2, secret );
        ((LBEsuRestApi)esu).setUnicodeEnabled(true);
        uid = uid2;
    }


}
