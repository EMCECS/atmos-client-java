package com.emc.cdp.mgmt.test;

import com.emc.cdp.mgmt.CdpMgmtApi;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

public class CdpMgmtApiTest {
    private static final String PROPERTIES_FILE = "cdp.properties";

    private CdpMgmtApi api;
    private String protocol;
    private String host;
    private int port;
    private String username;
    private String password;

    public CdpMgmtApiTest() {
        super();

        InputStream in = ClassLoader.getSystemResourceAsStream( PROPERTIES_FILE );
        if ( in != null ) {
            try {
                System.getProperties().load( in );
            } catch ( IOException e ) {
                throw new RuntimeException( "Could not load " + PROPERTIES_FILE, e );
            }
        }

        String key = "cdp.mgmt.protocol";
        protocol = System.getProperty( key );
        if ( protocol == null ) {
            missingValue( key );
        }

        key = "cdp.mgmt.host";
        host = System.getProperty( key );
        if ( host == null ) {
            missingValue( key );
        }

        key = "cdp.mgmt.port";
        String portStr = System.getProperty( key );
        if ( portStr == null ) {
            missingValue( key );
        }
        port = Integer.parseInt( portStr );

        key = "cdp.mgmt.username";
        username = System.getProperty( key );
        if ( username == null ) {
            missingValue( key );
        }

        key = "cdp.mgmt.password";
        password = System.getProperty( key );
        if ( password == null ) {
            missingValue( key );
        }
    }

    @Before
    public void setUp() throws Exception {
        api = new CdpMgmtApi( protocol, host, port, username, password );
    }

    @Test
    public void testLogin() throws Exception {
        api.login();
    }

    private void missingValue( String key ) {
        throw new RuntimeException( key + " is null.  Set in " + PROPERTIES_FILE + " or on command line with -D" + key );
    }
}
