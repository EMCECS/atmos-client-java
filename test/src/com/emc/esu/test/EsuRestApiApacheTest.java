/**
 * 
 */
package com.emc.esu.test;

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
     * UID to run tests with.  Change this value to your UID.
     */
    private String uid2 = "<put your UID here>";

    /**
     * Shared secret for UID.  Change this value to your UID's shared secret
     */
    private String secret = "<put your secret key here>";

    /**
     * Hostname or IP of ESU server.  Change this value to your server's
     * hostname or ip address.
     */
    private String host = "<put your host here>";
    
    /**
     * Port of ESU server (usually 80 or 443)
     */
    private int port = 80;

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
