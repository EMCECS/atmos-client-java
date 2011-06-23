/**
 * 
 */
package com.emc.esu.test;

import java.io.IOException;
import java.io.InputStream;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import com.emc.esu.api.EsuException;
import com.emc.esu.api.rest.EsuRestApiApache;

/**
 * @author jason
 *
 */
public class EsuRestApiApacheTest extends EsuApiTest {
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
    
    public EsuRestApiApacheTest() {
    	InputStream in = ClassLoader.getSystemResourceAsStream("atmos.properties");
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
        esu = new EsuRestApiApache( host, port, uid2, secret );
        uid = uid2;
    }



    //
    // TESTS START HERE
    //




    /**
     * Test handling signature failures.  Should throw an exception with
     * error code 1032.
     */
    @Test
    public void testSignatureFailure() throws Exception {
       try {
           // Fiddle with the secret key
           esu = new EsuRestApiApache( host, port, uid, secret.toUpperCase() );
           //test = new EsuApiTest( esu );
           testCreateEmptyObject();
           Assert.fail( "Expected exception to be thrown" );
       } catch( EsuException e ) {
           Assert.assertEquals( "Expected error code 1032 for signature failure", 
                   1032, e.getAtmosCode() );
       }
    }

    /**
     * Test general HTTP errors by generating a 404.
     */
    @Test
    public void testFourOhFour() throws Exception {
        try {
            // Fiddle with the context
            ((EsuRestApiApache)esu).setContext( "/restttttttttt" );
            testCreateEmptyObject();
            Assert.fail( "Expected exception to be thrown" );
        } catch( EsuException e ) {
            Assert.assertEquals( "Expected error code 404 for bad context root", 
                    404, e.getHttpCode() );
        }
        
    }


}
