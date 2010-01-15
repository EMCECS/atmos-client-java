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

import junit.framework.Assert;
import junit.framework.TestCase;

import com.emc.esu.api.EsuException;
import com.emc.esu.api.rest.EsuRestApi;

public class EsuRestApiTest extends TestCase {
    /**
     * UID to run tests with.  Change this value to your UID.
     */
    private String uid = "<put your UID here>";

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
    
    private EsuApiTest test;
    private EsuRestApi esu;
    
    /**
     * Tear down after a test is run.  Cleans up objects that were created
     * during the test.  Set cleanUp=false to disable this behavior.
     */
    public void tearDown() {
        test.tearDown();
    }
    
    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        esu = new EsuRestApi( host, port, uid, secret );
        test = new EsuApiTest( esu );
    }



    //
    // TESTS START HERE
    //



    /**
     * Test creating one empty object.  No metadata, no content.
     */
    public void testCreateEmptyObject() throws Exception {
        test.testCreateEmptyObject();
    }

    public void testCreateEmptyObjectOnPath() throws Exception {
    	test.testCreateEmptyObjectOnPath();
    }
    
    public void testListDirectory() throws Exception {
    	test.testListDirectory();
    }
    
    /**
     * Test creating an object with content but without metadata
     */
    public void testCreateObjectWithContent() throws Exception {
        test.testCreateObjectWithContent();
    }
    
    /**
     * Test creating an object with content but without metadata
     */
    public void testCreateObjectWithContentStream() throws Exception {
        test.testCreateObjectWithContentStream();
    }

    /**
     * Test creating an object with metadata but no content.
     */
    public void testCreateObjectWithMetadata() {
        test.testCreateObjectWithMetadata();
    }
    
    public void testCreateObjectWithMetadataOnPath() {
    	test.testCreateObjectWithMetadataOnPath();
    }

    /**
     * Test handling signature failures.  Should throw an exception with
     * error code 1032.
     */
    public void testSignatureFailure() throws Exception {
       try {
           // Fiddle with the secret key
           esu = new EsuRestApi( host, port, uid, secret.toUpperCase() );
           test = new EsuApiTest( esu );
           test.testCreateEmptyObject();
           Assert.fail( "Expected exception to be thrown" );
       } catch( EsuException e ) {
           Assert.assertEquals( "Expected error code 1032 for signature failure", 
                   1032, e.getAtmosCode() );
       }
    }

    /**
     * Test general HTTP errors by generating a 404.
     */
    public void testFourOhFour() throws Exception {
        try {
            // Fiddle with the context
            esu.setContext( "/restttttttttt" );
            test.testCreateEmptyObject();
            Assert.fail( "Expected exception to be thrown" );
        } catch( EsuException e ) {
            Assert.assertEquals( "Expected error code 404 for bad context root", 
                    404, e.getHttpCode() );
        }
        
    }

    /**
     * Test reading an object's content
     */
    public void testReadObject() throws Exception {
        test.testReadObject();
    }
    
    /**
     * Test reading an object's content using a
     * stream.
     */
    public void testReadObjectStream() throws Exception {
        test.testReadObjectStream();
    }

    /**
     * Test reading an ACL back
     */
    public void testReadAcl() {
        test.testReadAcl( uid );
    }
    
    /**
     * Test reading back user metadata
     */
    public void testGetUserMetadata() {
        test.testGetUserMetadata();
    }

    /**
     * Test deleting user metadata
     */
    public void testDeleteUserMetadata() {
        test.testDeleteUserMetadata();
    }

    /**
     * Test creating object versions
     */
//    public void testVersionObject() {
//        test.testVersionObject();
//    }

    /**
     * Test listing the versions of an object
     */
//    public void testListVersions() {
//        test.testListVersions();
//    }

    /**
     * Test listing the system metadata on an object
     */
    public void testGetSystemMetadata() {
        test.testGetSystemMetadata();
    }

    /**
     * Test listing objects by a tag
     */
    public void testListObjects() {
        test.testListObjects();
    }
    
    /**
     * Test listing objects by a tag
     */
    public void testListObjectsWithMetadata() {
        test.testListObjectsWithMetadata();
    }

    /**
     * Test fetching listable tags
     */
    public void testGetListableTags() {
        test.testGetListableTags();
    }

    /**
     * Test listing the user metadata tags on an object
     */
    public void testListUserMetadataTags() {
        test.testListUserMetadataTags();
    }

//    /**
//     * Test executing a query.
//     */
//    public void testQueryObjects() {
//        test.testQueryObjects( uid );
//    }

    /**
     * Tests updating an object's metadata
     */
    public void testUpdateObjectMetadata() throws Exception {
        test.testUpdateObjectMetadata();
    }
    
    public void testUpdateObjectAcl() throws Exception {
        test.testUpdateObjectAcl( uid );
    }

    /**
     * Tests updating an object's contents
     */
    public void testUpdateObjectContent() throws Exception {
        test.testUpdateObjectContent();
    }
    
    /**
     * Tests updating an object's contents
     */
    public void testUpdateObjectContentStream() throws Exception {
        test.testUpdateObjectContentStream();
    }

    /**
     * Test replacing an object's entire contents
     */
    public void testReplaceObjectContent() throws Exception {
        test.testReplaceObjectContent();
    }

    /**
     * Test the UploadHelper's create method
     */
    public void testCreateHelper() throws Exception {
        test.testCreateHelper();
    }

    /**
     * Test the UploadHelper's update method
     */
    public void testUpdateHelper() throws Exception {
        test.testUpdateHelper();
    }
    
    public void testCreateHelperWithPath() throws Exception {
    	test.testCreateHelperWithPath();
    }
    
    public void testUpdateHelperWithPath() throws Exception {
    	test.testUpdateHelperWithPath();
    }

    /**
     * Tests the download helper.  Tests both single and multiple requests.
     */
    public void testDownloadHelper() throws Exception {
        test.testDownloadHelper();
    }
    
    public void testUploadDownload() throws Exception {
        test.testUploadDownload();
    }
    
	public void testPathNaming() throws Exception {
		test.testPathNaming();
	}
	
	public void testGetAllMetadataByPath() throws Exception {
		test.testGetAllMetadataByPath( uid );
	}
	
	public void testGetAllMetadataById() throws Exception {
		test.testGetAllMetadataById( uid );
	}
	
	public void testGetObjectReplicaInfo() throws Exception {
		test.testGetObjectReplicaInfo();
	}
	
	public void testGetShareableUrl() throws Exception {
	    test.testGetShareableUrl( uid );
	}
	
	public void testGetShareableUrlWithPath() throws Exception {
	    test.testGetShareableUrlWithPath( uid );
	}
	
	public void testExpiredSharableUrl() throws Exception {
	    test.testExpiredSharableUrl( uid );
	}

}
