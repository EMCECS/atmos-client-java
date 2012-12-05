// Copyright (c) 2008, EMC Corporation.
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
package com.emc.esu.test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.emc.esu.api.EsuApi;
import com.emc.esu.api.rest.LBEsuRestApi;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.emc.esu.api.EsuException;
import com.emc.esu.api.rest.EsuRestApi;
import com.emc.esu.sysmgmt.SysMgmtApi;

public class EsuRestApiTest extends EsuApiTest {
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
     * Comma-delimited list of access nodes.  Set in atmos.properties or -Datmos.hosts
     */
    private String hosts;

    /**
     * Port of ESU server (usually 80 or 443). Set in atmos.properties or -Datmos.port
     */
    private int port = 80;

    public EsuRestApiTest() {
    	super();

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
        hosts = System.getProperty( "atmos.hosts" );
    	port = Integer.parseInt( System.getProperty( "atmos.port" ) );
    }

    /**
     * @see junit.framework.TestCase#setUp()
     */
    @Before
    public void setUp() throws Exception {
        esu = new LBEsuRestApi( Arrays.asList( (hosts == null) ? new String[]{host} : hosts.split( "," ) ),
                                port, uid2, secret );
        esu.setUnicodeEnabled(true);
        uid = uid2;
        SysMgmtApi.disableCertificateValidation();
    }


    /**
     * Test handling signature failures.  Should throw an exception with
     * error code 1032.
     */
    @Test
    public void testSignatureFailure() throws Exception {
       EsuApi tempEsu = esu;
       try {
           // Fiddle with the secret key
           esu = new EsuRestApi( host, port, uid, secret.toUpperCase() );
           testCreateEmptyObject();
           Assert.fail( "Expected exception to be thrown" );
       } catch( EsuException e ) {
           Assert.assertEquals( "Expected error code 1032 for signature failure",
                   1032, e.getAtmosCode() );
       } finally {
           esu = tempEsu;
       }
    }

    /**
     * Test general HTTP errors by generating a 404.
     */
    @Test
    public void testFourOhFour() throws Exception {
        try {
            // Fiddle with the context
            ((EsuRestApi)esu).setContext( "/restttttttttt" );
            testCreateEmptyObject();
            Assert.fail( "Expected exception to be thrown" );
        } catch( EsuException e ) {
            Assert.assertEquals( "Expected error code 404 for bad context root",
                    404, e.getHttpCode() );
        }

    }

    @Test
    public void testServerOffset() throws Exception {
    	long offset = ((EsuRestApi)esu).calculateServerOffset();
    	l4j.info("Server offset: " + offset + " milliseconds");
    }

    /**
     * NOTE: This method does not actually test that the custom headers are sent over the wire. Run tcpmon or wireshark
     * to verify
     */
    @Test
    public void testCustomHeaders() throws Exception {
        Map<String, String> customHeaders = new HashMap<String, String>();
        customHeaders.put( "myCustomHeader", "Hello World!" );
        ((EsuRestApi) this.esu).setCustomHeaders( customHeaders );
        this.esu.getServiceInformation();
    }
}
