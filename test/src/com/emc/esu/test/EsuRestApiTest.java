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

import org.junit.Before;

import com.emc.esu.api.rest.EsuRestApi;

public class EsuRestApiTest extends EsuApiTest {
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
    private String host = "accesspoint.emccis.com";
    
    /**
     * Port of ESU server (usually 80 or 443)
     */
    private int port = 80;
       
    /**
     * @see junit.framework.TestCase#setUp()
     */
    @Before
    public void setUp() throws Exception {
        esu = new EsuRestApi( host, port, uid2, secret );
        uid = uid2;
    }


//    /**
//     * Test handling signature failures.  Should throw an exception with
//     * error code 1032.
//     */
//    @Test
//    public void testSignatureFailure() throws Exception {
//       try {
//           // Fiddle with the secret key
//           esu = new EsuRestApi( host, port, uid, secret.toUpperCase() );
//           testCreateEmptyObject();
//           Assert.fail( "Expected exception to be thrown" );
//       } catch( EsuException e ) {
//           Assert.assertEquals( "Expected error code 1032 for signature failure", 
//                   1032, e.getAtmosCode() );
//       }
//    }
//
//    /**
//     * Test general HTTP errors by generating a 404.
//     */
//    @Test
//    public void testFourOhFour() throws Exception {
//        try {
//            // Fiddle with the context
//            ((EsuRestApi)esu).setContext( "/restttttttttt" );
//            testCreateEmptyObject();
//            Assert.fail( "Expected exception to be thrown" );
//        } catch( EsuException e ) {
//            Assert.assertEquals( "Expected error code 404 for bad context root", 
//                    404, e.getHttpCode() );
//        }
//        
//    }


}
