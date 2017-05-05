/*
 * Copyright (c) 2013-2016, EMC Corporation.
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * + Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 * + Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 * + The name of EMC Corporation may not be used to endorse or promote
 *   products derived from this software without specific prior written
 *   permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.emc.atmos.api.test;

import com.emc.atmos.AtmosException;
import com.emc.atmos.StickyThreadAlgorithm;
import com.emc.atmos.api.*;
import com.emc.atmos.api.bean.*;
import com.emc.atmos.api.jersey.AtmosApiBasicClient;
import com.emc.atmos.api.jersey.AtmosApiClient;
import com.emc.atmos.api.multipart.MultipartEntity;
import com.emc.atmos.api.request.*;
import com.emc.atmos.util.AtmosClientFactory;
import com.emc.atmos.util.RandomInputStream;
import com.emc.atmos.util.ReorderedFormDataContentDisposition;
import com.emc.util.ConcurrentJunitRunner;
import com.emc.util.StreamUtil;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.BodyPart;
import com.sun.jersey.multipart.FormDataMultiPart;
import org.apache.log4j.Logger;
import org.junit.*;
import org.junit.runner.RunWith;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@RunWith(ConcurrentJunitRunner.class)
public class AtmosApiClientTest {
    private static final Logger l4j = Logger.getLogger( AtmosApiClientTest.class );

    /**
     * Use this as a prefix for namespace object paths and you won't have to clean up after yourself.
     * This also keeps all test objects under one folder, which is easy to delete should something go awry.
     */
    private static final String TEST_DIR_PREFIX = "/test_" + AtmosApiClientTest.class.getSimpleName();

    private AtmosConfig config;
    protected AtmosApi api;
    private boolean isEcs = false;

    private List<ObjectIdentifier> cleanup = Collections.synchronizedList( new ArrayList<ObjectIdentifier>() );
    private List<ObjectPath> cleanupDirs = Collections.synchronizedList( new ArrayList<ObjectPath>() );

    public AtmosApiClientTest() throws Exception {
        config = createAtmosConfig();
        api = new AtmosApiClient( config );
        isEcs = AtmosClientFactory.atmosIsEcs();
    }

    private AtmosConfig createAtmosConfig() throws Exception {
        AtmosConfig config = AtmosClientFactory.getAtmosConfig();
        Assume.assumeTrue("Could not load Atmos configuration", config != null);
        config.setDisableSslValidation( false );
        config.setEnableExpect100Continue( false );
        config.setEnableRetry( false );
        config.setLoadBalancingAlgorithm( new StickyThreadAlgorithm() );
        return config;
    }

    @After
    public void tearDown() {
        for ( ObjectIdentifier cleanItem : cleanup ) {
            try {
                api.delete( cleanItem );
            } catch ( Throwable t ) {
                System.out.println( "Failed to delete " + cleanItem + ": " + t.getMessage() );
            }
        }
        try { // if test directories exists, recursively delete them
            for ( ObjectPath testDir : cleanupDirs ) {
                deleteRecursively( testDir );
            }
        } catch ( AtmosException e ) {
            if ( e.getHttpCode() != 404 ) {
                l4j.warn( "Could not delete test dir: ", e );
            }
        }

        if (!isEcs) {
            try {
                ListAccessTokensResponse response = this.api.listAccessTokens( new ListAccessTokensRequest() );
                if ( response.getTokens() != null ) {
                    for ( AccessToken token : response.getTokens() ) {
                        this.api.deleteAccessToken( token.getId() );
                    }
                }
            } catch ( Exception e ) {
                System.out.println( "Failed to delete access tokens: " + e.getMessage() );
            }
        }
    }

    private void deleteRecursively( ObjectPath path ) {
        if ( path.isDirectory() ) {
            ListDirectoryRequest request = new ListDirectoryRequest().path( path );
            do {
                for ( DirectoryEntry entry : this.api.listDirectory( request ).getEntries() ) {
                    deleteRecursively( new ObjectPath( path, entry ) );
                }
            } while ( request.getToken() != null );
        }
        this.api.delete( path );
    }

    private ObjectPath createTestDir( String name ) {
        if (!name.endsWith("/")) name = name + "/";
        ObjectPath path = new ObjectPath( TEST_DIR_PREFIX + "_" + name );
        this.api.createDirectory( path );
        cleanupDirs.add( path );
        return path;
    }

    //
    // TESTS START HERE
    //

    @Test
    public void testUtf8JavaEncoding() throws Exception {
        String oneByteCharacters = "Hello";
        String twoByteCharacters = "\u0410\u0411\u0412\u0413"; // Cyrillic letters
        String twoByteEscaped = "%D0%90%D0%91%D0%92%D0%93";
        String fourByteCharacters = "\ud841\udf0e\ud841\udf31\ud841\udf79\ud843\udc53"; // Chinese symbols
        String fourByteEscaped = "%F0%A0%9C%8E%F0%A0%9C%B1%F0%A0%9D%B9%F0%A0%B1%93";
        Assert.assertEquals( "2-byte characters failed",
                             URLEncoder.encode( twoByteCharacters, "UTF-8" ),
                             twoByteEscaped );
        Assert.assertEquals( "4-byte characters failed",
                             URLEncoder.encode( fourByteCharacters, "UTF-8" ),
                             fourByteEscaped );
        Assert.assertEquals( "2-byte/4-byte mix failed",
                             URLEncoder.encode( twoByteCharacters + fourByteCharacters, "UTF-8" ),
                             twoByteEscaped + fourByteEscaped );
        Assert.assertEquals( "1-byte/2-byte mix failed",
                             URLEncoder.encode( oneByteCharacters + twoByteCharacters, "UTF-8" ),
                             oneByteCharacters + twoByteEscaped );
        Assert.assertEquals( "1-4 byte mix failed",
                             URLEncoder.encode( oneByteCharacters + twoByteCharacters + fourByteCharacters, "UTF-8" ),
                             oneByteCharacters + twoByteEscaped + fourByteEscaped );
    }

    @Test
    public void testCreateSubtenant() throws Exception {
        Assume.assumeTrue(isEcs);

        String subtenantId = this.api.createSubtenant(new CreateSubtenantRequest());
        l4j.warn("Subtenant ID: " + subtenantId);
        Assert.assertNotNull(subtenantId);

        this.api.deleteSubtenant(subtenantId);
    }

    @Test
    public void testCreateSubtenantWithCustomId() throws Exception {
        Assume.assumeTrue(isEcs);

        String customId = "15f570b8be2942c0b400a6b7cbfa03b5";
        CreateSubtenantRequest request = new CreateSubtenantRequest();
        request.setCustomSubtenantId(customId);

        String subtenantId = this.api.createSubtenant(request);
        this.api.deleteSubtenant(subtenantId);

        Assert.assertEquals(customId, subtenantId);
    }

    /**
     * Test creating one empty object.  No metadata, no content.
     */
    @Test
    public void testCreateEmptyObject() throws Exception {
        ObjectId id = this.api.createObject( null, null );
        Assert.assertNotNull( "null ID returned", id );
        cleanup.add( id );

        // Read back the content
        String content = new String( this.api.readObject( id, null, byte[].class ), "UTF-8" );
        Assert.assertEquals( "object content wrong", "", content );

    }

    /**
     * Test creating one empty object on a path.  No metadata, no content.
     */
    @Test
    public void testCreateEmptyObjectOnPath() throws Exception {
        ObjectPath op = new ObjectPath( "/" + rand8char() );
        ObjectId id = this.api.createObject( op, null, null );
        cleanup.add( op );
        l4j.debug( "Path: " + op + " ID: " + id );
        Assert.assertNotNull( id );

        // Read back the content
        String content = new String( this.api.readObject( op, null, byte[].class ), "UTF-8" );
        Assert.assertEquals( "object content wrong", "", content );
        content = new String( this.api.readObject( id, null, byte[].class ), "UTF-8" );
        Assert.assertEquals( "object content wrong when reading by id", "", content );
    }

    @Test
    public void testCreateEmptyObjectWithCustomId() throws Exception {
        Assume.assumeTrue(isEcs);

        String oid = "574e49dea38dc7990574e55963a183057e59f370da57";
        CreateObjectResponse response = this.api.createObject(new CreateObjectRequest().customObjectId(oid).content(""));
        cleanup.add(response.getObjectId());
        Assert.assertEquals(oid, response.getObjectId().getId());

        this.api.getObjectMetadata(new ObjectId(oid)); // will throw an error if the ID doesn't exist
    }

    /**
     * Tests using some extended characters when creating on a path.  This particular test
     * uses one cryllic, one accented, and one japanese character.
     */
    @Test
    public void testUnicodePath() throws Exception {
        String dirName = rand8char();
        ObjectPath path = new ObjectPath( "/" + dirName + "/бöｼ.txt" );
        ObjectId id = this.api.createObject( path, null, null );
        Assert.assertNotNull( "null ID returned", id );
        cleanup.add( id );

        ObjectPath parent = new ObjectPath( "/" + dirName + "/" );
        ListDirectoryResponse response = this.api.listDirectory( new ListDirectoryRequest().path( parent ) );
        boolean found = false;
        for ( DirectoryEntry ent : response.getEntries() ) {
            if ( new ObjectPath( parent, ent.getFilename() ).equals( path ) ) {
                found = true;
            }
        }
        Assert.assertTrue( "Did not find unicode file in dir", found );

        // Check read
        this.api.readObject( path, null, byte[].class );
    }

    /**
     * Tests using some extra characters that might break URIs
     */
    @Test
    public void testExtraPath() throws Exception {
        ObjectPath path = new ObjectPath( "/" + rand8char() + "/a+=-  _!#$%^&*(),.z.txt" );
        //ObjectPath path = new ObjectPath("/zimbramailbox/c8b4/511a-63c4-4ac9-8ff7+1c578de044be/stage/3r0sFrgUgL2ApCSkl3pobSX9D+k-1");
        byte[] data = "Hello World".getBytes( "UTF-8" );
        InputStream in = new ByteArrayInputStream( data );
        CreateObjectRequest request = new CreateObjectRequest().identifier( path ).content( in )
                                                               .contentLength( data.length );
        ObjectId id = this.api.createObject( request ).getObjectId();
        Assert.assertNotNull( "null ID returned", id );
        cleanup.add( id );
    }

    @Test
    public void testUtf8Path() throws Exception {
        String oneByteCharacters = "Hello! ,";
        String twoByteCharacters = "\u0410\u0411\u0412\u0413"; // Cyrillic letters
        String fourByteCharacters = "\ud841\udf0e\ud841\udf31\ud841\udf79\ud843\udc53"; // Chinese symbols
        String crazyName = oneByteCharacters + twoByteCharacters + fourByteCharacters;
        byte[] content = "Crazy name creation test.".getBytes( "UTF-8" );
        ObjectPath parent = createTestDir( "Utf8Path" );
        ObjectPath path = new ObjectPath( parent, crazyName );

        // create crazy-name object
        this.api.createObject( path, content, "text/plain" );

        cleanup.add( path );

        // verify name in directory list
        boolean found = false;
        ListDirectoryRequest request = new ListDirectoryRequest().path( parent );
        for ( DirectoryEntry entry : this.api.listDirectory( request ).getEntries() ) {
            if ( new ObjectPath( parent, entry.getFilename() ).equals( path ) ) {
                found = true;
                break;
            }
        }
        Assert.assertTrue( "crazyName not found in directory listing", found );

        // verify content
        Assert.assertTrue( "content does not match",
                           Arrays.equals( content, this.api.readObject( path, null, byte[].class ) ) );
    }

    @Test
    public void testUtf8Content() throws Exception {
        String oneByteCharacters = "Hello! ,";
        String twoByteCharacters = "\u0410\u0411\u0412\u0413"; // Cyrillic letters
        String fourByteCharacters = "\ud841\udf0e\ud841\udf31\ud841\udf79\ud843\udc53"; // Chinese symbols
        byte[] content = (oneByteCharacters + twoByteCharacters + fourByteCharacters).getBytes( "UTF-8" );

        // create object with multi-byte UTF-8 content
        ObjectId oid = api.createObject( content, "text/plain" );
        cleanup.add( oid );

        byte[] readContent = this.api.readObject( oid, null, byte[].class );

        // verify content
        Assert.assertTrue( "content does not match", Arrays.equals( content, readContent ) );
    }

    /**
     * Test creating an object with content but without metadata
     */
    @Test
    public void testCreateObjectWithContent() throws Exception {
        ObjectId id = this.api.createObject( "hello".getBytes( "UTF-8" ), "text/plain" );
        Assert.assertNotNull( "null ID returned", id );
        cleanup.add( id );

        // Read back the content
        String content = new String( this.api.readObject( id, null, byte[].class ), "UTF-8" );
        Assert.assertEquals( "object content wrong", "hello", content );
    }

    @Test
    public void testCreateObjectWithSegment() throws Exception {
        byte[] content = "hello".getBytes( "UTF-8" );
        ObjectId id = api.createObject( new BufferSegment( content, 0, content.length ), null );
        cleanup.add( id );

        // Read back the content
        String result = new String( this.api.readObject( id, null, byte[].class ), "UTF-8" );
        Assert.assertEquals( "object content wrong", "hello", result );
    }

    @Test
    public void testCreateObjectWithContentStream() throws Exception {
        InputStream in = new ByteArrayInputStream( "hello".getBytes( "UTF-8" ) );
        CreateObjectRequest request = new CreateObjectRequest().content( in ).contentLength( 5 )
                                                               .contentType( "text/plain" );
        ObjectId id = this.api.createObject( request ).getObjectId();
        in.close();
        Assert.assertNotNull( "null ID returned", id );
        cleanup.add( id );

        // Read back the content
        String content = new String( this.api.readObject( id, null, byte[].class ), "UTF-8" );
        Assert.assertEquals( "object content wrong", "hello", content );
    }

    @Test
    public void testCreateObjectWithContentStreamOnPath() throws Exception {
        ObjectPath op = new ObjectPath( "/" + rand8char() + ".tmp" );
        InputStream in = new ByteArrayInputStream( "hello".getBytes( "UTF-8" ) );
        CreateObjectRequest request = new CreateObjectRequest();
        request.identifier( op ).content( in ).contentLength( 5 ).contentType( "text/plain" );
        ObjectId id = this.api.createObject( request ).getObjectId();
        in.close();
        Assert.assertNotNull( "null ID returned", id );
        cleanup.add( id );

        // Read back the content
        String content = new String( this.api.readObject( id, null, byte[].class ), "UTF-8" );
        Assert.assertEquals( "object content wrong", "hello", content );
    }

    /**
     * Test creating an object with metadata but no content.
     */
    @Test
    public void testCreateObjectWithMetadataOnPath() {
        ObjectPath op = new ObjectPath( "/" + rand8char() + ".tmp" );
        CreateObjectRequest request = new CreateObjectRequest().identifier( op );
        Metadata listable = new Metadata( "listable", "foo", true );
        Metadata unlistable = new Metadata( "unlistable", "bar", false );
        Metadata listable2 = new Metadata( "listable2", "foo2 foo2", true );
        Metadata unlistable2 = new Metadata( "unlistable2", "bar2 bar2", false );
        request.userMetadata( listable, unlistable, listable2, unlistable2 );
        ObjectId id = this.api.createObject( request ).getObjectId();
        //this.esu.updateObject( op, null, mlist, null, null, null );
        Assert.assertNotNull( "null ID returned", id );
        cleanup.add( op );

        // Read and validate the metadata
        Map<String, Metadata> meta = this.api.getUserMetadata( op );
        Assert.assertNotNull( "value of 'listable' missing", meta.get( "listable" ) );
        Assert.assertNotNull( "value of 'listable2' missing", meta.get( "listable2" ) );
        Assert.assertNotNull( "value of 'unlistable' missing", meta.get( "unlistable" ) );
        Assert.assertNotNull( "value of 'unlistable2' missing", meta.get( "unlistable2" ) );

        Assert.assertEquals( "value of 'listable' wrong", "foo", meta.get( "listable" ).getValue() );
        Assert.assertEquals( "value of 'listable2' wrong", "foo2 foo2", meta.get( "listable2" ).getValue() );
        Assert.assertEquals( "value of 'unlistable' wrong", "bar", meta.get( "unlistable" ).getValue() );
        Assert.assertEquals( "value of 'unlistable2' wrong", "bar2 bar2", meta.get( "unlistable2" ).getValue() );
        // Check listable flags
        Assert.assertEquals( "'listable' is not listable", true, meta.get( "listable" ).isListable() );
        Assert.assertEquals( "'listable2' is not listable", true, meta.get( "listable2" ).isListable() );
        Assert.assertEquals( "'unlistable' is listable", false, meta.get( "unlistable" ).isListable() );
        Assert.assertEquals( "'unlistable2' is listable", false, meta.get( "unlistable2" ).isListable() );
    }

    /**
     * Test creating an object with metadata but no content.
     */
    @Test
    public void testCreateObjectWithMetadata() {
        CreateObjectRequest request = new CreateObjectRequest();
        Metadata listable = new Metadata( "listable", "foo", true );
        Metadata unlistable = new Metadata( "unlistable", "bar", false );
        Metadata listable2 = new Metadata( "listable2", "foo2 foo2", true );
        Metadata unlistable2 = new Metadata( "unlistable2", "bar2 bar2", false );
        Metadata listable3 = new Metadata( "listable3", null, true );
        Metadata quotes = new Metadata( "ST_modalities", "\\\"US\"\\", false );
        Metadata withCommas = new Metadata( "withcommas", "I, Robot", false );
        Metadata withEquals = new Metadata( "withequals", "name=value", false );
        request.userMetadata( listable, unlistable, listable2, unlistable2, listable3, quotes, withCommas, withEquals );
        ObjectId id = this.api.createObject( request ).getObjectId();
        Assert.assertNotNull( "null ID returned", id );
        cleanup.add( id );

        // Read and validate the metadata
        Map<String, Metadata> meta = this.api.getUserMetadata( id );
        Assert.assertEquals( "value of 'listable' wrong", "foo", meta.get( "listable" ).getValue() );
        Assert.assertEquals( "value of 'listable2' wrong", "foo2 foo2", meta.get( "listable2" ).getValue() );
        Assert.assertEquals( "value of 'unlistable' wrong", "bar", meta.get( "unlistable" ).getValue() );
        Assert.assertEquals( "value of 'unlistable2' wrong",
                             "bar2 bar2",
                             meta.get( "unlistable2" ).getValue() );
        Assert.assertNotNull( "listable3 missing", meta.get( "listable3" ) );
        Assert.assertTrue( "Value of listable3 should be empty",
                           meta.get( "listable3" ).getValue() == null
                           || meta.get( "listable3" ).getValue().length() == 0 );
        Assert.assertEquals( "Value of withcommas wrong", "I, Robot", meta.get( "withcommas" ).getValue() );
        Assert.assertEquals( "Value of withequals wrong", "name=value", meta.get( "withequals" ).getValue() );

        // Check listable flags
        Assert.assertEquals( "'listable' is not listable", true, meta.get( "listable" ).isListable() );
        Assert.assertEquals( "'listable2' is not listable", true, meta.get( "listable2" ).isListable() );
        Assert.assertEquals( "'listable3' is not listable", true, meta.get( "listable3" ).isListable() );
        Assert.assertEquals( "'unlistable' is listable", false, meta.get( "unlistable" ).isListable() );
        Assert.assertEquals( "'unlistable2' is listable", false, meta.get( "unlistable2" ).isListable() );

    }

    /**
     * Test creating an object with metadata but no content.
     */
    @Test
    public void testMetadataNormalizeSpace() {
        CreateObjectRequest request = new CreateObjectRequest();
        Metadata unlistable = new Metadata( "unlistable", "bar  bar   bar    bar", false );
        Metadata leadingSpacesOdd = new Metadata( "leadingodd", "   spaces", false );
        Metadata trailingSpacesOdd = new Metadata( "trailingodd", "spaces   ", false );
        Metadata leadingSpacesEven = new Metadata( "leadingeven", "    SPACES", false );
        Metadata trailingSpacesEven = new Metadata( "trailingeven", "spaces    ", false );
        request.userMetadata( unlistable, leadingSpacesOdd, trailingSpacesOdd, leadingSpacesEven, trailingSpacesEven );
        ObjectId id = this.api.createObject( request ).getObjectId();
        Assert.assertNotNull( "null ID returned", id );
        cleanup.add( id );

        // Read and validate the metadata
        Map<String, Metadata> meta = this.api.getUserMetadata( id );
        Assert.assertEquals( "value of 'unlistable' wrong",
                             "bar  bar   bar    bar",
                             meta.get( "unlistable" ).getValue() );
        // Check listable flags
        Assert.assertEquals( "'unlistable' is listable", false, meta.get( "unlistable" ).isListable() );

    }

    /**
     * Test reading an object's content
     */
    @Test
    public void testReadObject() throws Exception {
        ObjectId id = this.api.createObject( "hello".getBytes( "UTF-8" ), "text/plain" );
        Assert.assertNotNull( "null ID returned", id );
        cleanup.add( id );

        // Read back the content
        String content = new String( this.api.readObject( id, null, byte[].class ), "UTF-8" );
        Assert.assertEquals( "object content wrong", "hello", content );

        // Read back only 2 bytes
        Range range = new Range( 1, 2 );
        content = new String( this.api.readObject( id, range, byte[].class ), "UTF-8" );
        Assert.assertEquals( "partial object content wrong", "el", content );
    }

    @Test
    public void testResponseProperties() throws Exception {
        // Subtract a second since the HTTP dates only have 1s precision.
        Date now = new Date();
        CreateObjectRequest request = new CreateObjectRequest().content( "hello".getBytes( "UTF-8" ) )
                                                               .contentType( "text/plain" );
        CreateObjectResponse response = this.api.createObject( request );
        Assert.assertNotNull( "null ID returned", response.getObjectId() );
        Assert.assertEquals( "location wrong", "/rest/objects/" + response.getObjectId(), response.getLocation() );
        cleanup.add( response.getObjectId() );

        // Read back the content
        ReadObjectResponse<String> readResponse = api.readObject( new ReadObjectRequest().identifier( response.getObjectId() ),
                                                                  String.class );
        Assert.assertEquals( "object content wrong", "hello", readResponse.getObject() );
        Assert.assertEquals( "HTTP status wrong", 200, readResponse.getHttpStatus() );
        Assert.assertEquals( "HTTP message wrong", "OK", readResponse.getHttpMessage() );
        Assert.assertFalse( "HTTP headers empty", readResponse.getHeaders().isEmpty() );
        Assert.assertTrue( "HTTP content-type wrong",
                           readResponse.getContentType().matches( "text/plain(; charset=UTF-8)?" ) );
        Assert.assertEquals( "HTTP content-length wrong", 5, readResponse.getContentLength() );
        Assert.assertTrue( "HTTP response date wrong", Math.abs( response.getDate().getTime() - now.getTime() ) < (1000 * 60 * 5) );
        // apparently last-modified isn't included in GET requests
        // Assert.assertTrue( "HTTP last modified date wrong", readResponse.getLastModified().after( now ) );
    }

    /**
     * Test reading an ACL back
     */
    @Test
    public void testReadAcl() {
        // Create an object with an ACL
        Acl acl = new Acl();
        acl.addUserGrant( stripUid( config.getTokenId() ), Permission.FULL_CONTROL );
        acl.addGroupGrant( Acl.GROUP_OTHER, Permission.READ );
        ObjectId id = this.api.createObject( new CreateObjectRequest().acl( acl ) ).getObjectId();
        Assert.assertNotNull( "null ID returned", id );
        cleanup.add( id );

        // Read back the ACL and make sure it matches
        Acl newacl = this.api.getAcl( id );
        l4j.info( "Comparing " + newacl + " with " + acl );

        Assert.assertEquals( "ACLs don't match", acl, newacl );

    }

    /**
     * Inside an ACL, you use the UID only, not SubtenantID/UID
     */
    private String stripUid( String uid ) {
        int slash = uid.indexOf( '/' );
        if ( slash != -1 ) {
            return uid.substring( slash + 1 );
        } else {
            return uid;
        }
    }

    @Test
    public void testReadAclByPath() {
        ObjectPath op = new ObjectPath( "/" + rand8char() + ".tmp" );
        // Create an object with an ACL
        Acl acl = new Acl();
        acl.addUserGrant( stripUid( config.getTokenId() ), Permission.FULL_CONTROL );
        acl.addGroupGrant( Acl.GROUP_OTHER, Permission.READ );
        ObjectId id = this.api.createObject( new CreateObjectRequest().identifier( op ).acl( acl ) ).getObjectId();
        Assert.assertNotNull( "null ID returned", id );
        cleanup.add( op );

        // Read back the ACL and make sure it matches
        Acl newacl = this.api.getAcl( op );
        l4j.info( "Comparing " + newacl + " with " + acl );

        Assert.assertEquals( "ACLs don't match", acl, newacl );

    }

    /**
     * Test reading back user metadata
     */
    @Test
    public void testGetUserMetadata() {
        // Create an object with user metadata
        CreateObjectRequest request = new CreateObjectRequest();
        Metadata listable = new Metadata( "listable", "foo", true );
        Metadata unlistable = new Metadata( "unlistable", "bar", false );
        Metadata listable2 = new Metadata( "listable2", "foo2 foo2", true );
        Metadata unlistable2 = new Metadata( "unlistable2", "bar2 bar2", false );
        request.userMetadata( listable, unlistable, listable2, unlistable2 );
        ObjectId id = this.api.createObject( request ).getObjectId();
        Assert.assertNotNull( "null ID returned", id );
        cleanup.add( id );

        // Read only part of the metadata
        Map<String, Metadata> meta = this.api.getUserMetadata( id, "listable", "unlistable" );
        Assert.assertEquals( "value of 'listable' wrong", "foo", meta.get( "listable" ).getValue() );
        Assert.assertNull( "value of 'listable2' should not have been returned", meta.get( "listable2" ) );
        Assert.assertEquals( "value of 'unlistable' wrong", "bar", meta.get( "unlistable" ).getValue() );
        Assert.assertNull( "value of 'unlistable2' should not have been returned", meta.get( "unlistable2" ) );

    }

    /**
     * Test deleting user metadata
     */
    @Test
    public void testDeleteUserMetadata() {
        // Create an object with metadata
        CreateObjectRequest request = new CreateObjectRequest();
        Metadata listable = new Metadata( "listable", "foo", true );
        Metadata unlistable = new Metadata( "unlistable", "bar", false );
        Metadata listable2 = new Metadata( "listable2", "foo2 foo2", true );
        Metadata unlistable2 = new Metadata( "unlistable2", "bar2 bar2", false );
        request.userMetadata( listable, unlistable, listable2, unlistable2 );
        ObjectId id = this.api.createObject( request ).getObjectId();
        Assert.assertNotNull( "null ID returned", id );
        cleanup.add( id );

        // Delete a couple of the metadata entries
        this.api.deleteUserMetadata( id, "listable2", "unlistable2" );

        // Read back the metadata for the object and ensure the deleted
        // entries don't exist
        Map<String, Metadata> meta = this.api.getUserMetadata( id );
        Assert.assertEquals( "value of 'listable' wrong", "foo", meta.get( "listable" ).getValue() );
        Assert.assertNull( "value of 'listable2' should not have been returned", meta.get( "listable2" ) );
        Assert.assertEquals( "value of 'unlistable' wrong", "bar", meta.get( "unlistable" ).getValue() );
        Assert.assertNull( "value of 'unlistable2' should not have been returned", meta.get( "unlistable2" ) );
    }

    /**
     * Test creating object versions
     */
    @Test
    public void testVersionObject() throws Exception {
        Assume.assumeFalse(isEcs);
        // Create an object
        String content = "Version Test";
        CreateObjectRequest request = new CreateObjectRequest().content( content );
        Metadata listable = new Metadata( "listable", "foo", true );
        Metadata unlistable = new Metadata( "unlistable", "bar", false );
        Metadata listable2 = new Metadata( "listable2", "foo2 foo2", true );
        Metadata unlistable2 = new Metadata( "unlistable2", "bar2 bar2", false );
        request.userMetadata( listable, unlistable, listable2, unlistable2 );
        ObjectId id = this.api.createObject( request ).getObjectId();
        Assert.assertNotNull( "null ID returned", id );
        cleanup.add( id );

        // Version the object
        ObjectId vid = this.api.createVersion( id );
        cleanup.add( vid );
        Assert.assertNotNull( "null version ID returned", vid );

        Assert.assertFalse( "Version ID shoudn't be same as original ID", id.equals( vid ) );

        // Fetch the version and read its data
        Assert.assertEquals( "Version content wrong", content, this.api.readObject( vid, null, String.class ) );

        Map<String, Metadata> meta = this.api.getUserMetadata( vid );
        Assert.assertEquals( "value of 'listable' wrong", "foo", meta.get( "listable" ).getValue() );
        Assert.assertEquals( "value of 'listable2' wrong", "foo2 foo2", meta.get( "listable2" ).getValue() );
        Assert.assertEquals( "value of 'unlistable' wrong", "bar", meta.get( "unlistable" ).getValue() );
        Assert.assertEquals( "value of 'unlistable2' wrong",
                             "bar2 bar2",
                             meta.get( "unlistable2" ).getValue() );

    }

    /**
     * Test listing the versions of an object
     */
    @Test
    public void testListVersions() {
        Assume.assumeFalse(isEcs);
        // Create an object
        CreateObjectRequest request = new CreateObjectRequest();
        Metadata listable = new Metadata( "listable", "foo", true );
        Metadata unlistable = new Metadata( "unlistable", "bar", false );
        Metadata listable2 = new Metadata( "listable2", "foo2 foo2", true );
        Metadata unlistable2 = new Metadata( "unlistable2", "bar2 bar2", false );
        request.userMetadata( listable, unlistable, listable2, unlistable2 );
        ObjectId id = this.api.createObject( request ).getObjectId();
        cleanup.add( id );
        Assert.assertNotNull( "null ID returned", id );

        // Version the object
        ObjectId vid1 = this.api.createVersion( id );
        cleanup.add( vid1 );
        Assert.assertNotNull( "null version ID returned", vid1 );
        ObjectId vid2 = this.api.createVersion( id );
        cleanup.add( vid2 );
        Assert.assertNotNull( "null version ID returned", vid2 );

        // List the versions and ensure their IDs are correct
        ListVersionsResponse response = this.api.listVersions( new ListVersionsRequest().objectId( id ) );

        List<ObjectVersion> versions = response.getVersions();
        Assert.assertEquals( "Wrong number of versions returned", 2, versions.size() );
        Assert.assertTrue( "Version number less than zero", versions.get( 0 ).getVersionNumber() >= 0 );
        Assert.assertNotNull( "Version itime is null", versions.get( 0 ).getItime() );
        Assert.assertTrue( "Version number less than zero", versions.get( 1 ).getVersionNumber() >= 0 );
        Assert.assertNotNull( "Version itime is null", versions.get( 1 ).getItime() );


        List<ObjectId> versionIds = response.getVersionIds();
        Assert.assertEquals( "Wrong number of versions returned", 2, versionIds.size() );
        Assert.assertTrue( "version 1 not found in version list", versionIds.contains( vid1 ) );
        Assert.assertTrue( "version 2 not found in version list", versionIds.contains( vid2 ) );
    }

    /**
     * Test listing the versions of an object
     */
    @Test
    public void testListVersionsLong() {
        Assume.assumeFalse(isEcs);
        // Create an object
        CreateObjectRequest request = new CreateObjectRequest();
        Metadata listable = new Metadata( "listable", "foo", true );
        Metadata unlistable = new Metadata( "unlistable", "bar", false );
        Metadata listable2 = new Metadata( "listable2", "foo2 foo2", true );
        Metadata unlistable2 = new Metadata( "unlistable2", "bar2 bar2", false );
        request.userMetadata( listable, unlistable, listable2, unlistable2 );
        ObjectId id = this.api.createObject( request ).getObjectId();
        cleanup.add( id );
        Assert.assertNotNull( "null ID returned", id );

        // Version the object
        ObjectId vid1 = this.api.createVersion( id );
        ObjectVersion v1 = new ObjectVersion( 0, vid1, null );
        cleanup.add( vid1 );
        Assert.assertNotNull( "null version ID returned", vid1 );
        ObjectId vid2 = this.api.createVersion( id );
        cleanup.add( vid2 );
        ObjectVersion v2 = new ObjectVersion( 1, vid2, null );
        Assert.assertNotNull( "null version ID returned", vid2 );

        // List the versions and ensure their IDs are correct
        ListVersionsRequest vRequest = new ListVersionsRequest();
        vRequest.objectId( id ).setLimit( 1 );
        List<ObjectVersion> versions = new ArrayList<ObjectVersion>();
        do {
            ListVersionsResponse response = this.api.listVersions( vRequest );
            if ( response.getVersions() != null ) versions.addAll( response.getVersions() );
        } while ( vRequest.getToken() != null );
        Assert.assertEquals( "Wrong number of versions returned", 2, versions.size() );
        Assert.assertTrue( "version 1 not found in version list", versions.contains( v1 ) );
        Assert.assertTrue( "version 2 not found in version list", versions.contains( v2 ) );
        for ( ObjectVersion v : versions ) {
            Assert.assertNotNull( "oid null in version", v.getVersionId() );
            Assert.assertTrue( "Invalid version number in version", v.getVersionNumber() > -1 );
            Assert.assertNotNull( "itime null in version", v.getItime() );
        }
    }

    /**
     * Test listing the versions of an object
     */
    @Test
    public void testDeleteVersion() {
        Assume.assumeFalse(isEcs);
        // Create an object
        CreateObjectRequest request = new CreateObjectRequest();
        Metadata listable = new Metadata( "listable", "foo", true );
        Metadata unlistable = new Metadata( "unlistable", "bar", false );
        Metadata listable2 = new Metadata( "listable2", "foo2 foo2", true );
        Metadata unlistable2 = new Metadata( "unlistable2", "bar2 bar2", false );
        request.userMetadata( listable, unlistable, listable2, unlistable2 );
        ObjectId id = this.api.createObject( request ).getObjectId();
        cleanup.add( id );
        Assert.assertNotNull( "null ID returned", id );

        // Version the object
        ObjectId vid1 = this.api.createVersion( id );
        Assert.assertNotNull( "null version ID returned", vid1 );
        ObjectId vid2 = this.api.createVersion( id );
        cleanup.add( vid2 );
        Assert.assertNotNull( "null version ID returned", vid2 );

        // List the versions and ensure their IDs are correct
        List<ObjectId> versions = this.api.listVersions( new ListVersionsRequest().objectId( id ) ).getVersionIds();
        Assert.assertEquals( "Wrong number of versions returned", 2, versions.size() );
        Assert.assertTrue( "version 1 not found in version list", versions.contains( vid1 ) );
        Assert.assertTrue( "version 2 not found in version list", versions.contains( vid2 ) );

        // Delete a version
        this.api.deleteVersion( vid1 );
        versions = this.api.listVersions( new ListVersionsRequest().objectId( id ) ).getVersionIds();
        Assert.assertEquals( "Wrong number of versions returned", 1, versions.size() );
        Assert.assertFalse( "version 1 found in version list", versions.contains( vid1 ) );
        Assert.assertTrue( "version 2 not found in version list", versions.contains( vid2 ) );

    }

    @Test
    public void testRestoreVersion() throws IOException {
        Assume.assumeFalse(isEcs);
        ObjectId id = this.api.createObject( "Base Version Content".getBytes( "UTF-8" ), "text/plain" );
        Assert.assertNotNull( "null ID returned", id );
        cleanup.add( id );

        // Version the object
        ObjectId vId = this.api.createVersion( id );

        // Update the object content
        this.api.updateObject( id, "Child Version Content -- You should never see me".getBytes( "UTF-8" ) );

        // Restore the original version
        this.api.restoreVersion( id, vId );

        // Read back the content
        String content = new String( this.api.readObject( id, null, byte[].class ), "UTF-8" );
        Assert.assertEquals( "object content wrong", "Base Version Content", content );
    }

    /**
     * Test listing the system metadata on an object
     */
    @Test
    public void testGetSystemMetadata() {
        // Create an object
        CreateObjectRequest request = new CreateObjectRequest();
        Metadata listable = new Metadata( "listable", "foo", true );
        Metadata unlistable = new Metadata( "unlistable", "bar", false );
        Metadata listable2 = new Metadata( "listable2", "foo2 foo2", true );
        Metadata unlistable2 = new Metadata( "unlistable2", "bar2 bar2", false );
        request.userMetadata( listable, unlistable, listable2, unlistable2 );
        ObjectId id = this.api.createObject( request ).getObjectId();
        Assert.assertNotNull( "null ID returned", id );
        cleanup.add( id );

        // Read only part of the metadata
        Map<String, Metadata> meta = this.api.getSystemMetadata( id, "atime", "ctime" );
        Assert.assertNotNull( "value of 'atime' missing", meta.get( "atime" ) );
        Assert.assertNull( "value of 'mtime' should not have been returned", meta.get( "mtime" ) );
        Assert.assertNotNull( "value of 'ctime' missing", meta.get( "ctime" ) );
        Assert.assertNull( "value of 'gid' should not have been returned", meta.get( "gid" ) );
        Assert.assertNull( "value of 'listable' should not have been returned", meta.get( "listable" ) );
    }

    @Test
    public void testObjectExists() {
        ObjectId oid = api.createObject("Hello exists!", "text/plain");
        Assert.assertTrue("object exists!", api.objectExists(oid));

        api.delete(oid);
        Assert.assertFalse("object does not exist!", api.objectExists(oid));
    }

    /**
     * Test listing objects by a tag that doesn't exist
     */
    @Test
    public void testListObjectsNoExist() {
        ListObjectsRequest request = new ListObjectsRequest().metadataName( "this_tag_should_not_exist" );
        List<ObjectEntry> objects = this.api.listObjects( request ).getEntries();
        Assert.assertNotNull( "object list should be not null", objects );
        Assert.assertEquals( "No objects should be returned", 0, objects.size() );
    }

    /**
     * Test listing objects by a tag
     */
    @Test
    public void testListObjects() {
        // Create an object
        CreateObjectRequest request = new CreateObjectRequest();
        Metadata listable = new Metadata( "listable", "foo", true );
        Metadata unlistable = new Metadata( "unlistable", "bar", false );
        Metadata listable2 = new Metadata( "list/able/2", "foo2 foo2", true );
        Metadata unlistable2 = new Metadata( "list/able/not", "bar2 bar2", false );
        request.userMetadata( listable, unlistable, listable2, unlistable2 );
        ObjectId id = this.api.createObject( request ).getObjectId();
        Assert.assertNotNull( "null ID returned", id );
        cleanup.add( id );

        // List the objects.  Make sure the one we created is in the list
        ListObjectsRequest lRequest = new ListObjectsRequest().metadataName( "listable" );
        List<ObjectEntry> objects = this.api.listObjects( lRequest ).getEntries();
        ObjectEntry toFind = new ObjectEntry();
        toFind.setObjectId( id );
        Assert.assertTrue( "No objects returned", objects.size() > 0 );
        Assert.assertTrue( "object not found in list", objects.contains( toFind ) );

    }

    /**
     * Test listing objects by a tag
     */
    @Test
    public void testListObjectsWithMetadata() {
        // Create an object
        CreateObjectRequest request = new CreateObjectRequest();
        Metadata listable = new Metadata( "listable", "foo", true );
        Metadata unlistable = new Metadata( "unlistable", "bar", false );
        Metadata listable2 = new Metadata( "list/able/2", "foo2 foo2", true );
        Metadata unlistable2 = new Metadata( "list/able/not", "bar2 bar2", false );
        request.userMetadata( listable, unlistable, listable2, unlistable2 );
        ObjectId id = this.api.createObject( request ).getObjectId();
        Assert.assertNotNull( "null ID returned", id );
        cleanup.add( id );

        // List the objects.  Make sure the one we created is in the list
        ListObjectsRequest lRequest = new ListObjectsRequest().metadataName( "listable" ).includeMetadata( true );
        List<ObjectEntry> objects = this.api.listObjects( lRequest ).getEntries();
        Assert.assertTrue( "No objects returned", objects.size() > 0 );

        // Find the item.
        boolean found = false;
        for ( ObjectEntry or : objects ) {
            if ( or.getObjectId().equals( id ) ) {
                found = true;
                // check metadata
                Assert.assertEquals( "Wrong value on metadata",
                                     or.getUserMetadataMap().get( "listable" ).getValue(), "foo" );
            }
        }
        Assert.assertTrue( "object not found in list", found );
    }

    /**
     * Test listing objects by a tag, with only some of the metadata
     */
    @Test
    public void testListObjectsWithSomeMetadata() {
        // Create an object
        CreateObjectRequest request = new CreateObjectRequest();
        Metadata listable = new Metadata( "listable", "foo", true );
        Metadata unlistable = new Metadata( "unlistable", "bar", false );
        Metadata listable2 = new Metadata( "list/able/2", "foo2 foo2", true );
        Metadata unlistable2 = new Metadata( "list/able/not", "bar2 bar2", false );
        request.userMetadata( listable, unlistable, listable2, unlistable2 );
        ObjectId id = this.api.createObject( request ).getObjectId();
        Assert.assertNotNull( "null ID returned", id );
        cleanup.add( id );

        // List the objects.  Make sure the one we created is in the list
        ListObjectsRequest lRequest = new ListObjectsRequest();
        lRequest.metadataName( "listable" ).includeMetadata( true )
                .userMetadataNames( "listable" );
        List<ObjectEntry> objects = this.api.listObjects( lRequest ).getEntries();
        Assert.assertTrue( "No objects returned", objects.size() > 0 );

        // Find the item.
        boolean found = false;
        for ( ObjectEntry or : objects ) {
            if ( or.getObjectId().equals( id ) ) {
                found = true;
                // check metadata
                Assert.assertEquals( "Wrong value on metadata",
                                     or.getUserMetadataMap().get( "listable" ).getValue(), "foo" );

                // Other metadata should not be present
                Assert.assertNull( "unlistable should be missing",
                                   or.getUserMetadataMap().get( "unlistable" ) );
            }
        }
        Assert.assertTrue( "object not found in list", found );
    }

    /**
     * Test listing objects by a tag, paging the results
     */
    @Test
    public void testListObjectsPaged() {
        // Create two objects.
        CreateObjectRequest request = new CreateObjectRequest();
        Metadata listable = new Metadata( "listable", "foo", true );
        request.userMetadata( listable );
        ObjectId id1 = this.api.createObject( request ).getObjectId();
        ObjectId id2 = this.api.createObject( request ).getObjectId();
        Assert.assertNotNull( "null ID returned", id1 );
        Assert.assertNotNull( "null ID returned", id2 );
        cleanup.add( id1 );
        cleanup.add( id2 );

        // List the objects.  Make sure the one we created is in the list
        ListObjectsRequest lRequest = new ListObjectsRequest().metadataName( "listable" );
        lRequest.setIncludeMetadata( true );
        lRequest.setLimit( 1 );
        List<ObjectEntry> objects = this.api.listObjects( lRequest ).getEntries();
        Assert.assertTrue( "No objects returned", objects.size() > 0 );
        Assert.assertNotNull( "Token should be present", lRequest.getToken() );

        l4j.debug( "listObjectsPaged, Token: " + lRequest.getToken() );
        while ( lRequest.getToken() != null ) {
            // Subsequent pages
            objects.addAll( this.api.listObjects( lRequest ).getEntries() );
            l4j.debug( "listObjectsPaged, Token: " + lRequest.getToken() );
        }

        // Ensure our IDs exist
        ObjectEntry toFind1 = new ObjectEntry(), toFind2 = new ObjectEntry();
        toFind1.setObjectId( id1 );
        toFind2.setObjectId( id2 );
        Assert.assertTrue( "First object not found", objects.contains( toFind1 ) );
        Assert.assertTrue( "Second object not found", objects.contains( toFind2 ) );
    }


    /**
     * Test fetching listable tags
     */
    @Test
    public void testGetListableTags() {
        // Create an object
        ObjectId id = this.api.createObject( null, null );
        Assert.assertNotNull( "null ID returned", id );
        cleanup.add( id );

        UpdateObjectRequest request = new UpdateObjectRequest().identifier( id );
        Metadata listable = new Metadata( "listable", "foo", true );
        Metadata unlistable = new Metadata( "unlistable", "bar", false );
        Metadata listable2 = new Metadata( "list/able/2", "foo2 foo2", true );
        Metadata unlistable2 = new Metadata( "list/able/not", "bar2 bar2", false );
        request.userMetadata( listable, unlistable, listable2, unlistable2 );
        this.api.updateObject( request );

        // List tags.  Ensure our object's tags are in the list.
        Set<String> tags = this.api.listMetadata( null );
        Assert.assertTrue( "listable tag not returned", tags.contains( "listable" ) );
        Assert.assertTrue( "list/able/2 root tag not returned", tags.contains( "list" ) );
        Assert.assertFalse( "list/able/not tag returned", tags.contains( "list/able/not" ) );

        // List child tags
        tags = this.api.listMetadata( "list/able" );
        Assert.assertFalse( "non-child returned", tags.contains( "listable" ) );
        Assert.assertTrue( "list/able/2 tag not returned", tags.contains( "2" ) );
        Assert.assertFalse( "list/able/not tag returned", tags.contains( "not" ) );

    }

    /**
     * Test listing the user metadata tags on an object
     */
    @Test
    public void testListUserMetadataTags() {
        // Create an object
        CreateObjectRequest request = new CreateObjectRequest();
        Metadata listable = new Metadata( "listable", "foo", true );
        Metadata unlistable = new Metadata( "unlistable", "bar", false );
        Metadata listable2 = new Metadata( "list/able/2", "foo2 foo2", true );
        Metadata unlistable2 = new Metadata( "list/able/not", "bar2 bar2", false );
        request.userMetadata( listable, unlistable, listable2, unlistable2 );

        ObjectId id = this.api.createObject( request ).getObjectId();
        Assert.assertNotNull( "null ID returned", id );
        cleanup.add( id );

        // List tags
        Map<String, Boolean> metaNames = this.api.getUserMetadataNames( id );
        Assert.assertTrue( "listable tag not returned", metaNames.containsKey( "listable" ) );
        Assert.assertTrue( "list/able/2 tag not returned", metaNames.containsKey( "list/able/2" ) );
        Assert.assertTrue( "unlistable tag not returned", metaNames.containsKey( "unlistable" ) );
        Assert.assertTrue( "list/able/not tag not returned", metaNames.containsKey( "list/able/not" ) );
        Assert.assertFalse( "unknown tag returned", metaNames.containsKey( "unknowntag" ) );

        // Check listable flag
        Assert.assertEquals( "'listable' is not listable", true, metaNames.get( "listable" ) );
        Assert.assertEquals( "'list/able/2' is not listable", true, metaNames.get( "list/able/2" ) );
        Assert.assertEquals( "'unlistable' is listable", false, metaNames.get( "unlistable" ) );
        Assert.assertEquals( "'list/able/not' is listable", false, metaNames.get( "list/able/not" ) );
    }

    /**
     * Tests updating an object's metadata
     */
    @Test
    public void testUpdateObjectMetadata() throws Exception {
        // Create an object
        CreateObjectRequest request = new CreateObjectRequest().content( "hello".getBytes( "UTF-8" ) );
        Metadata unlistable = new Metadata( "unlistable", "foo", false );
        request.userMetadata( unlistable );
        ObjectId id = this.api.createObject( request ).getObjectId();
        Assert.assertNotNull( "null ID returned", id );
        cleanup.add( id );

        // Update the metadata
        unlistable.setValue( "bar" );
        this.api.setUserMetadata( id,
                                  request.getUserMetadata().toArray( new Metadata[request.getUserMetadata().size()] ) );

        // Re-read the metadata
        Map<String, Metadata> meta = this.api.getUserMetadata( id );
        Assert.assertEquals( "value of 'unlistable' wrong", "bar", meta.get( "unlistable" ).getValue() );

        // Check that content was not modified
        String content = new String( this.api.readObject( id, null, byte[].class ), "UTF-8" );
        Assert.assertEquals( "object content wrong", "hello", content );

    }

    @Test
    public void testUpdateObjectAcl() throws Exception {
        // Create an object with an ACL
        Acl acl = new Acl();
        acl.addUserGrant( stripUid( config.getTokenId() ), Permission.FULL_CONTROL );
        acl.addGroupGrant( Acl.GROUP_OTHER, Permission.READ );
        ObjectId id = this.api.createObject( new CreateObjectRequest().acl( acl ) ).getObjectId();
        Assert.assertNotNull( "null ID returned", id );
        cleanup.add( id );

        // Read back the ACL and make sure it matches
        Acl newacl = this.api.getAcl( id );
        l4j.info( "Comparing " + newacl + " with " + acl );

        Assert.assertEquals( "ACLs don't match", acl, newacl );

        // Change the ACL and update the object.
        acl.removeGroupGrant( Acl.GROUP_OTHER );
        acl.addGroupGrant( Acl.GROUP_OTHER, Permission.NONE );
        this.api.setAcl( id, acl );

        // Read the ACL back and check it
        newacl = this.api.getAcl( id );
        l4j.info( "Comparing " + newacl + " with " + acl );
        Assert.assertEquals( "ACLs don't match", acl, newacl );
    }

    /**
     * Tests updating an object's contents
     */
    @Test
    public void testUpdateObjectContent() throws Exception {
        // Create an object
        ObjectId id = this.api.createObject( "hello".getBytes( "UTF-8" ), "text/plain" );
        Assert.assertNotNull( "null ID returned", id );
        cleanup.add( id );

        // Update part of the content
        Range range = new Range( 1, 1 );
        this.api.updateObject( id, "u".getBytes( "UTF-8" ), range );

        // Read back the content and check it
        String content = new String( this.api.readObject( id, null, byte[].class ), "UTF-8" );
        Assert.assertEquals( "object content wrong", "hullo", content );
    }

    @Test
    public void testUpdateObjectContentStream() throws Exception {
        // Create an object
        ObjectId id = this.api.createObject( "hello".getBytes( "UTF-8" ), "text/plain" );
        Assert.assertNotNull( "null ID returned", id );
        cleanup.add( id );

        // Update part of the content
        InputStream in = new ByteArrayInputStream( "u".getBytes( "UTF-8" ) );
        UpdateObjectRequest request = new UpdateObjectRequest().identifier( id );
        request.range( new Range( 1, 1 ) ).content( in );
        request.setContentLength( 1 );
        this.api.updateObject( request );
        in.close();

        // Read back the content and check it
        String content = new String( this.api.readObject( id, null, byte[].class ), "UTF-8" );
        Assert.assertEquals( "object content wrong", "hullo", content );
    }

    /**
     * Test replacing an object's entire contents
     */
    @Test
    public void testReplaceObjectContent() throws Exception {
        // Create an object
        ObjectId id = this.api.createObject( "hello".getBytes( "UTF-8" ), "text/plain" );
        Assert.assertNotNull( "null ID returned", id );
        cleanup.add( id );

        // Update all of the content
        this.api.updateObject( id, "bonjour".getBytes( "UTF-8" ) );

        // Read back the content and check it
        String content = new String( this.api.readObject( id, null, byte[].class ), "UTF-8" );
        Assert.assertEquals( "object content wrong", "bonjour", content );
    }

    @Test
    public void testListDirectory() throws Exception {
        String dir = rand8char();
        String file = rand8char();
        String dir2 = rand8char();
        ObjectPath dirPath = new ObjectPath( "/" + dir + "/" );
        ObjectPath op = new ObjectPath( "/" + dir + "/" + file );
        ObjectPath dirPath2 = new ObjectPath( "/" + dir + "/" + dir2 + "/" );

        ObjectId dirId = this.api.createDirectory( dirPath );
        ObjectId id = this.api.createObject( op, null, null );
        this.api.createDirectory( dirPath2 );
        cleanup.add( op );
        cleanup.add( dirPath2 );
        cleanup.add( dirPath );
        l4j.debug( "Path: " + op + " ID: " + id );
        Assert.assertNotNull( id );
        Assert.assertNotNull( dirId );

        // Read back the content
        String content = new String( this.api.readObject( op, null, byte[].class ), "UTF-8" );
        Assert.assertEquals( "object content wrong", "", content );
        content = new String( this.api.readObject( id, null, byte[].class ), "UTF-8" );
        Assert.assertEquals( "object content wrong when reading by id", "", content );

        // List the parent path
        List<DirectoryEntry> dirList = api.listDirectory( new ListDirectoryRequest().path( dirPath ) ).getEntries();
        l4j.debug( "Dir content: " + content );
        Assert.assertTrue( "File not found in directory", directoryContains( dirList, op.getFilename() ) );
        Assert.assertTrue( "subdirectory not found in directory",
                           directoryContains( dirList, dirPath2.getFilename() ) );
    }

    @Test
    public void testListDirectoryPaged() throws Exception {
        String dir = rand8char();
        String file = rand8char();
        String dir2 = rand8char();
        ObjectPath dirPath = new ObjectPath( "/" + dir + "/" );
        ObjectPath op = new ObjectPath( "/" + dir + "/" + file );
        ObjectPath dirPath2 = new ObjectPath( "/" + dir + "/" + dir2 + "/" );

        ObjectId dirId = this.api.createDirectory( dirPath );
        ObjectId id = this.api.createObject( op, null, null );
        this.api.createDirectory( dirPath2 );
        cleanup.add( op );
        cleanup.add( dirPath2 );
        cleanup.add( dirPath );
        l4j.debug( "Path: " + op + " ID: " + id );
        Assert.assertNotNull( id );
        Assert.assertNotNull( dirId );

        // List the parent path
        ListDirectoryRequest request = new ListDirectoryRequest().path( dirPath );
        request.setLimit( 1 );
        List<DirectoryEntry> dirList = api.listDirectory( request ).getEntries();

        Assert.assertNotNull( "Token should have been returned", request.getToken() );
        l4j.debug( "listDirectoryPaged, token: " + request.getToken() );
        while ( request.getToken() != null ) {
            dirList.addAll( api.listDirectory( request ).getEntries() );
        }

        Assert.assertTrue( "File not found in directory", directoryContains( dirList, op.getFilename() ) );
        Assert.assertTrue( "subdirectory not found in directory",
                           directoryContains( dirList, dirPath2.getFilename() ) );
    }

    @Test
    public void testListDirectoryWithMetadata() throws Exception {
        String dir = rand8char();
        String file = rand8char();
        String dir2 = rand8char();
        ObjectPath dirPath = new ObjectPath( "/" + dir + "/" );
        ObjectPath op = new ObjectPath( "/" + dir + "/" + file );
        ObjectPath dirPath2 = new ObjectPath( "/" + dir + "/" + dir2 + "/" );

        CreateObjectRequest request = new CreateObjectRequest().identifier( op );
        Metadata listable = new Metadata( "listable", "foo", true );
        Metadata unlistable = new Metadata( "unlistable", "bar", false );
        Metadata listable2 = new Metadata( "list/able/2", "foo2 foo2", true );
        Metadata unlistable2 = new Metadata( "list/able/not", "bar2 bar2", false );
        request.userMetadata( listable, unlistable, listable2, unlistable2 );

        ObjectId dirId = this.api.createDirectory( dirPath );
        ObjectId id = this.api.createObject( request ).getObjectId();
        this.api.createDirectory( dirPath2 );
        cleanup.add( op );
        cleanup.add( dirPath2 );
        cleanup.add( dirPath );
        l4j.debug( "Path: " + op + " ID: " + id );
        Assert.assertNotNull( id );
        Assert.assertNotNull( dirId );

        // List the parent path
        ListDirectoryRequest lRequest = new ListDirectoryRequest().path( dirPath ).includeMetadata( true );
        List<DirectoryEntry> dirList = api.listDirectory( lRequest ).getEntries();
        Assert.assertTrue( "File not found in directory", directoryContains( dirList, op.getFilename() ) );
        Assert.assertTrue( "subdirectory not found in directory",
                           directoryContains( dirList, dirPath2.getFilename() ) );

        for ( DirectoryEntry de : dirList ) {
            if ( new ObjectPath( dirPath, de.getFilename() ).equals( op ) ) {
                // Check the metadata
                Assert.assertNotNull("missing metadata 'listable'", de.getUserMetadataMap().get( "listable" ));
                Assert.assertEquals( "Wrong value on metadata",
                                     de.getUserMetadataMap().get( "listable" ).getValue(), "foo" );

            }
        }
        Assert.assertTrue( "File not found in directory", directoryContains( dirList, op.getFilename() ) );
        Assert.assertTrue( "subdirectory not found in directory",
                           directoryContains( dirList, dirPath2.getFilename() ) );
    }

    @Test
    public void testListDirectoryWithSomeMetadata() throws Exception {
        String dir = rand8char();
        String file = rand8char();
        String dir2 = rand8char();
        ObjectPath dirPath = new ObjectPath( "/" + dir + "/" );
        ObjectPath op = new ObjectPath( "/" + dir + "/" + file );
        ObjectPath dirPath2 = new ObjectPath( "/" + dir + "/" + dir2 + "/" );

        CreateObjectRequest request = new CreateObjectRequest().identifier( op );
        Metadata listable = new Metadata( "listable", "foo", true );
        Metadata unlistable = new Metadata( "unlistable", "bar", false );
        Metadata listable2 = new Metadata( "list/able/2", "foo2 foo2", true );
        Metadata unlistable2 = new Metadata( "list/able/not", "bar2 bar2", false );
        request.userMetadata( listable, unlistable, listable2, unlistable2 );

        ObjectId dirId = this.api.createDirectory( dirPath );
        ObjectId id = this.api.createObject( request ).getObjectId();
        this.api.createDirectory( dirPath2 );
        cleanup.add( op );
        cleanup.add( dirPath2 );
        cleanup.add( dirPath );
        l4j.debug( "Path: " + op + " ID: " + id );
        Assert.assertNotNull( id );
        Assert.assertNotNull( dirId );

        // List the parent path
        ListDirectoryRequest lRequest = new ListDirectoryRequest().path( dirPath ).includeMetadata( true );
        lRequest.userMetadataNames( "listable" );
        List<DirectoryEntry> dirList = api.listDirectory( lRequest ).getEntries();
        Assert.assertTrue( "File not found in directory", directoryContains( dirList, op.getFilename() ) );
        Assert.assertTrue( "subdirectory not found in directory",
                           directoryContains( dirList, dirPath2.getFilename() ) );

        for ( DirectoryEntry de : dirList ) {
            if ( new ObjectPath( dirPath, de.getFilename() ).equals( op ) ) {
                // Check the metadata
                Assert.assertNotNull("Missing metadata 'listable'",
                        de.getUserMetadataMap().get( "listable" ));
                Assert.assertEquals( "Wrong value on metadata",
                                         de.getUserMetadataMap().get( "listable" ).getValue(), "foo" );
                // Other metadata should not be present
                Assert.assertNull( "unlistable should be missing",
                                   de.getUserMetadataMap().get( "unlistable" ) );
            }
        }
        Assert.assertTrue( "File not found in directory", directoryContains( dirList, op.getFilename() ) );
        Assert.assertTrue( "subdirectory not found in directory",
                           directoryContains( dirList, dirPath2.getFilename() ) );
    }


    private boolean directoryContains( List<DirectoryEntry> dir, String filename ) {
        for ( DirectoryEntry de : dir ) {
            if ( de.getFilename().equals( filename ) ) {
                return true;
            }
        }

        return false;
    }

    /**
     * This method tests various legal and illegal pathnames
     */
    @Test
    public void testPathNaming() throws Exception {
        ObjectPath path = new ObjectPath( "/some/file" );
        Assert.assertFalse( "File should not be directory", path.isDirectory() );
        path = new ObjectPath( "/some/file.txt" );
        Assert.assertFalse( "File should not be directory", path.isDirectory() );
        ObjectPath path2 = new ObjectPath( "/some/file.txt" );
        Assert.assertEquals( "Equal paths should be equal", path, path2 );

        path = new ObjectPath( "/some/file/with/long.path/extra.stuff.here.zip" );
        Assert.assertFalse( "File should not be directory", path.isDirectory() );

        path = new ObjectPath( "/" );
        Assert.assertTrue( "Directory should be directory", path.isDirectory() );

        path = new ObjectPath( "/long/path/with/lots/of/elements/" );
        Assert.assertTrue( "Directory should be directory", path.isDirectory() );

    }

    /**
     * Tests dot directories (you should be able to create them even though they break the URL specification.)
     */
    @Test
    public void testDotDirectories() throws Exception {
        ObjectPath parentPath = createTestDir("DotDirectories");
        ObjectPath dotPath = new ObjectPath( parentPath, "./" );
        ObjectPath dotdotPath = new ObjectPath( parentPath, "../" );
        String filename = rand8char();
        byte[] content = "Hello World!".getBytes("UTF-8");

        // test single dot path (./)
        ObjectId dirId = this.api.createDirectory( dotPath );
        Assert.assertNotNull( "null ID returned on dot path creation", dirId );
        ObjectId fileId = this.api.createObject( new ObjectPath( dotPath, filename ), content, "text/plain" );
        cleanup.add( fileId );
        cleanup.add( dirId );

        // make sure we only see one file (the "." path is its own directory and not a synonym for the current directory)
        List<DirectoryEntry> entries = this.api
                .listDirectory( new ListDirectoryRequest().path( dotPath ) ).getEntries();
        Assert.assertEquals( "dot path listing was not 1", entries.size(), 1 );
        Assert.assertEquals( "dot path listing did not contain test file",
                             entries.get( 0 ).getFilename(),
                             filename );

        // test double dot path (../)
        dirId = this.api.createDirectory( dotdotPath );
        Assert.assertNotNull( "null ID returned on dotdot path creation", dirId );
        fileId = this.api.createObject( new ObjectPath( dotdotPath, filename ), content, "text/plain" );
        cleanup.add( fileId );
        cleanup.add( dirId );

        // make sure we only see one file (the ".." path is its own directory and not a synonym for the parent directory)
        entries = this.api
                .listDirectory( new ListDirectoryRequest().path( dotdotPath ) ).getEntries();
        Assert.assertEquals( "dotdot path listing was not 1", entries.size(), 1 );
        Assert.assertEquals("dotdot path listing did not contain test file",
                entries.get(0).getFilename(),
                filename);
    }

    /**
     * Tests the 'get all metadata' call using a path
     */
    @Test
    public void testGetAllMetadataByPath() throws Exception {
        ObjectPath op = new ObjectPath( "/" + rand8char() + ".tmp" );
        String mimeType = "test/mimetype";

        // Create an object with an ACL
        Acl acl = new Acl();
        acl.addUserGrant( stripUid( config.getTokenId() ), Permission.FULL_CONTROL );
        acl.addGroupGrant( Acl.GROUP_OTHER, Permission.READ );
        CreateObjectRequest request = new CreateObjectRequest().identifier( op ).acl( acl );
        request.content( "test".getBytes( "UTF-8" ) ).contentType( mimeType );

        ObjectId id = this.api.createObject( request ).getObjectId();
        Assert.assertNotNull( "null ID returned", id );
        cleanup.add( op );

        Metadata listable = new Metadata( "listable", "foo", true );
        Metadata unlistable = new Metadata( "unlistable", "bar", false );
        Metadata listable2 = new Metadata( "listable2", "foo2 foo2", true );
        Metadata unlistable2 = new Metadata( "unlistable2", "bar2 bar2", false );

        this.api.updateObject( new UpdateObjectRequest().identifier( op )
                                                        .userMetadata( listable, unlistable, listable2, unlistable2 )
                                                        .contentType( mimeType ) );

        // Read it back with HEAD call
        ObjectMetadata om = this.api.getObjectMetadata( op );
        Assert.assertNotNull( "value of 'listable' missing", om.getMetadata().get( "listable" ) );
        Assert.assertNotNull( "value of 'unlistable' missing", om.getMetadata().get( "unlistable" ) );
        Assert.assertNotNull( "value of 'atime' missing", om.getMetadata().get( "atime" ) );
        Assert.assertNotNull( "value of 'ctime' missing", om.getMetadata().get( "ctime" ) );
        Assert.assertEquals( "value of 'listable' wrong", "foo", om.getMetadata().get( "listable" ).getValue() );
        Assert.assertEquals( "value of 'unlistable' wrong", "bar", om.getMetadata().get( "unlistable" ).getValue() );
        Assert.assertEquals( "Mimetype incorrect", mimeType, om.getContentType() );

        // Check the ACL
        // not checking this by path because an extra groupid is added
        // during the create calls by path.
        //Assert.assertEquals( "ACLs don't match", acl, om.getAcl() );
    }

    @Test
    public void testGetAllMetadataById() throws Exception {
        // Create an object with an ACL
        CreateObjectRequest request = new CreateObjectRequest();

        Acl acl = new Acl();
        acl.addUserGrant( stripUid( config.getTokenId() ), Permission.FULL_CONTROL );
        acl.addGroupGrant( Acl.GROUP_OTHER, Permission.READ );

        Metadata listable = new Metadata( "listable", "foo", true );
        Metadata unlistable = new Metadata( "unlistable", "bar", false );
        Metadata listable2 = new Metadata( "listable2", "foo2 foo2", true );
        Metadata unlistable2 = new Metadata( "unlistable2", "bar2 bar2", false );

        String mimeType = "test/mimetype";
        request.acl( acl ).userMetadata( listable, unlistable, listable2, unlistable2 );
        request.content( "test".getBytes( "UTF-8" ) ).contentType( mimeType );

        ObjectId id = this.api.createObject( request ).getObjectId();
        Assert.assertNotNull( "null ID returned", id );
        cleanup.add( id );

        // Read it back with HEAD call
        ObjectMetadata om = this.api.getObjectMetadata( id );
        Assert.assertNotNull( "value of 'listable' missing", om.getMetadata().get( "listable" ) );
        Assert.assertNotNull( "value of 'unlistable' missing", om.getMetadata().get( "unlistable" ) );
        Assert.assertNotNull( "value of 'atime' missing", om.getMetadata().get( "atime" ) );
        Assert.assertNotNull( "value of 'ctime' missing", om.getMetadata().get( "ctime" ) );
        Assert.assertEquals( "value of 'listable' wrong", "foo", om.getMetadata().get( "listable" ).getValue() );
        Assert.assertEquals( "value of 'unlistable' wrong", "bar", om.getMetadata().get( "unlistable" ).getValue() );
        Assert.assertEquals( "Mimetype incorrect", mimeType, om.getContentType() );

        // Check the ACL
        Assert.assertEquals( "ACLs don't match", acl, om.getAcl() );
    }

    /**
     * Tests getting object replica information.
     */
    @Test
    public void testGetObjectReplicaInfo() throws Exception {
        Assume.assumeFalse(isEcs);
        ObjectId id = this.api.createObject( "hello".getBytes( "UTF-8" ), "text/plain" );
        Assert.assertNotNull( "null ID returned", id );
        cleanup.add( id );

        Map<String, Metadata> meta = this.api.getUserMetadata( id, "user.maui.lso" );
        Assert.assertNotNull( meta.get( "user.maui.lso" ) );
        l4j.debug( "Replica info: " + meta.get( "user.maui.lso" ) );
    }

    @Test
    public void testGetShareableUrl() throws Exception {
        // Create an object with content.
        String str = "Four score and twenty years ago";
        ObjectId id = this.api.createObject( str.getBytes( "UTF-8" ), "text/plain" );
        Assert.assertNotNull( "null ID returned", id );
        cleanup.add( id );

        Calendar c = Calendar.getInstance();
        c.add( Calendar.HOUR, 4 );
        Date expiration = c.getTime();
        URL u = api.getShareableUrl( id, expiration );

        l4j.debug( "Sharable URL: " + u );

        InputStream stream = (InputStream) u.getContent();
        BufferedReader br = new BufferedReader( new InputStreamReader( stream ) );
        String content = br.readLine();
        l4j.debug( "Content: " + content );
        Assert.assertEquals( "URL does not contain proper content",
                             str, content );
    }

    @Test
    public void testGetShareableUrlWithPath() throws Exception {
        // Create an object with content.
        String str = "Four score and twenty years ago";
        ObjectPath op = new ObjectPath( "/" + rand8char() + ".txt" );
        ObjectId id = this.api.createObject( op, str.getBytes( "UTF-8" ), "text/plain" );
        Assert.assertNotNull( "null ID returned", id );
        cleanup.add( op );

        Calendar c = Calendar.getInstance();
        c.add( Calendar.HOUR, 4 );
        Date expiration = c.getTime();
        URL u = api.getShareableUrl( op, expiration );

        l4j.debug( "Sharable URL: " + u );

        InputStream stream = (InputStream) u.getContent();
        BufferedReader br = new BufferedReader( new InputStreamReader( stream ) );
        String content = br.readLine();
        l4j.debug( "Content: " + content );
        Assert.assertEquals( "URL does not contain proper content",
                             str, content );
    }

    @Test
    public void testExpiredSharableUrl() throws Exception {
        // Create an object with content.
        String str = "Four score and twenty years ago";
        ObjectId id = this.api.createObject( str.getBytes( "UTF-8" ), "text/plain" );
        Assert.assertNotNull( "null ID returned", id );
        cleanup.add( id );

        Calendar c = Calendar.getInstance();
        c.add( Calendar.HOUR, -4 );
        Date expiration = c.getTime();
        URL u = api.getShareableUrl( id, expiration );

        l4j.debug( "Sharable URL: " + u );

        try {
            InputStream stream = (InputStream) u.getContent();
            BufferedReader br = new BufferedReader( new InputStreamReader( stream ) );
            String content = br.readLine();
            l4j.debug( "Content: " + content );
            Assert.fail( "Request should have failed" );
        } catch ( Exception e ) {
            l4j.debug( "Error (expected): " + e );
        }
    }

    @Test
    public void testReadObjectStream() throws Exception {
        ObjectId id = this.api.createObject( "hello".getBytes( "UTF-8" ), "text/plain" );
        Assert.assertNotNull( "null ID returned", id );
        cleanup.add( id );

        // Read back the content
        InputStream in = this.api.readObjectStream( id, null ).getObject();
        BufferedReader br = new BufferedReader( new InputStreamReader( in, "UTF-8" ) );
        String content = br.readLine();
        br.close();
        Assert.assertEquals( "object content wrong", "hello", content );

        // Read back only 2 bytes
        Range range = new Range( 1, 2 );
        in = this.api.readObjectStream( id, range ).getObject();
        br = new BufferedReader( new InputStreamReader( in, "UTF-8" ) );
        content = br.readLine();
        br.close();
        Assert.assertEquals( "partial object content wrong", "el", content );
    }

    @Test
    public void testCreateRetentionPeriod() throws Exception {
        Assume.assumeTrue(isEcs);

        byte[] data = "hello".getBytes("UTF-8");
        Long retentionPeriod = 20L;

        try {
            CreateObjectRequest request = new CreateObjectRequest().content(data).contentType("text/plain");
            request.retentionPeriod(retentionPeriod);

            CreateObjectResponse response = this.api.createObject(request);
            cleanup.add(response.getObjectId());

            ReadObjectResponse response1 = this.api.readObject(new ReadObjectRequest().identifier(response.getObjectId()),
                    byte[].class);
            Assert.assertNotNull("Null object ID returned", response1.getObject());
            Assert.assertEquals("Retention period doesn't match", retentionPeriod, response1.getMetadata().getRetentionPeriod());
        } finally {
            Thread.sleep(retentionPeriod * 1000); // let retention expire
        }
    }

    // NOTE: this test requires a retention policy be configured named "atmos-client-test", with retention period set to 10 seconds
    @Test
    public void testCreateRetentionPolicy() throws Exception {
        Assume.assumeTrue(isEcs);
        byte[] data = "hello".getBytes("UTF-8");
        String retentionPolicy = "atmos-client-test";

        try {
            CreateObjectRequest request = new CreateObjectRequest().content(data).contentType("text/plain");
            request.retentionPolicy(retentionPolicy);

            CreateObjectResponse response = this.api.createObject(request);
            cleanup.add(response.getObjectId());

            ReadObjectResponse response1 = this.api.readObject(new ReadObjectRequest().identifier(response.getObjectId()),
                    byte[].class);
            Assert.assertNotNull("Null object ID returned", response1.getObject());
            Assert.assertEquals("Retention policy doesn't match", retentionPolicy, response1.getMetadata().getRetentionPolicy());
        } finally {
            Thread.sleep(10000); // let retention expire
        }
    }

    @Test
    public void testCreateChecksum() throws Exception {
        byte[] data = "hello".getBytes( "UTF-8" );
        RunningChecksum ck = new RunningChecksum(ChecksumAlgorithm.SHA1);
        ck.update( data, 0, data.length );

        CreateObjectRequest request = new CreateObjectRequest().content( data ).contentType( "text/plain" );
        request.wsChecksum( ck );

        CreateObjectResponse response = this.api.createObject( request );
        cleanup.add( response.getObjectId() );
        Assert.assertNotNull( "Null object ID returned", response.getObjectId() );
        Assert.assertEquals( "Checksum doesn't match", ck, response.getWsChecksum() );
    }

    @Test
    public void testCreateEmptyChecksum() throws Exception {
        RunningChecksum ck = new RunningChecksum(ChecksumAlgorithm.SHA1);
        CreateObjectRequest request = new CreateObjectRequest().wsChecksum(ck);

        CreateObjectResponse response = this.api.createObject(request);
        cleanup.add(response.getObjectId());
        Assert.assertNotNull("Null object ID returned", response.getObjectId());
        Assert.assertEquals("Checksum doesn't match", ck, response.getWsChecksum());
    }

    @Test
    public void testRecreateCustomOidWithChecksum() throws Exception {
        Assume.assumeTrue(isEcs);
        String oid = "54ec7281c39f229a054ecb2339c02605804951fc61c6";

        RunningChecksum ck = new RunningChecksum(ChecksumAlgorithm.SHA1);
        CreateObjectRequest request = new CreateObjectRequest().wsChecksum(ck);
        request.setCustomObjectId(oid);

        CreateObjectResponse response = this.api.createObject(request);
        cleanup.add(response.getObjectId());
        Assert.assertEquals(oid, response.getObjectId().getId());
        Assert.assertNotNull("Null object ID returned", response.getObjectId());
        Assert.assertEquals("Checksum doesn't match", ck, response.getWsChecksum());

        this.api.delete(new ObjectId(oid));
        Assert.assertFalse(this.api.objectExists(new ObjectId(oid)));

        response = this.api.createObject(request);
        Assert.assertEquals(oid, response.getObjectId().getId());
        Assert.assertNotNull("Null object ID returned", response.getObjectId());
        Assert.assertEquals("Checksum doesn't match", ck, response.getWsChecksum());

        byte[] data = "Hello Recreated Checksum Object".getBytes(Charset.forName("UTF-8"));
        ck.update(data, 0, data.length);
        UpdateObjectRequest uRequest = new UpdateObjectRequest()
                .identifier(new ObjectId(oid)).range(new Range(0, data.length - 1)).content(data).wsChecksum(ck);
        this.api.updateObject(uRequest);
        Assert.assertEquals("Checksum doesn't match", ck, this.api.getObjectMetadata(new ObjectId(oid)).getWsChecksum());
    }

    /**
     * Note, to test read checksums, see comment in testReadChecksum
     */
    @Test
    public void testUploadDownloadChecksum() throws Exception {
        // Create a byte array to test
        int totalSize = 10 * 1024 * 1024; // 10MB
        int chunkSize = 4 * 1024 * 1024; // 4MB
        byte[] testData = new byte[totalSize]; // 10MB
        for ( int i = 0; i < testData.length; i++ ) {
            testData[i] = (byte) (i % 0x93);
        }

        RunningChecksum sha1 = new RunningChecksum(ChecksumAlgorithm.SHA1);
        BufferSegment segment = new BufferSegment( testData, 0, chunkSize );

        // upload in chunks
        sha1.update(segment);
        l4j.debug("Create checksum: " + sha1);
        CreateObjectRequest request = new CreateObjectRequest();
        request.content(segment).userMetadata(new Metadata("policy", "erasure", false)).setWsChecksum(sha1);
        ObjectId id = api.createObject( request ).getObjectId();
        cleanup.add( id );

        while ( segment.getOffset() + segment.getSize() < totalSize ) {
            segment.setOffset( segment.getOffset() + chunkSize );
            if ( segment.getOffset() + chunkSize > totalSize ) segment.setSize( totalSize - segment.getOffset() );
            Range range = new Range( segment.getOffset(), segment.getOffset() + segment.getSize() - 1 );
            sha1.update(segment.getBuffer(), segment.getOffset(), segment.getSize());
            l4j.debug("Update checksum: " + sha1);
            api.updateObject( new UpdateObjectRequest().identifier( id ).range( range )
                    .content(segment).wsChecksum(sha1));
        }

        // download in chunks
        totalSize = Integer.parseInt( api.getSystemMetadata( id ).get( "size" ).getValue() );

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int first = 0, last = chunkSize - 1;
        Range range = new Range( first, last );
        ReadObjectResponse<byte[]> response;
        RunningChecksum readSha1 = new RunningChecksum(ChecksumAlgorithm.SHA1);
        do {
            response = api.readObject( new ReadObjectRequest().identifier( id ).ranges( range ),
                                       byte[].class );
            readSha1.update(response.getObject(), 0, response.getObject().length);
            out.write( response.getObject() );
            first += chunkSize;
            last += chunkSize;
            if ( last >= totalSize ) last = totalSize - 1;
            range = new Range( first, last );
        } while ( first < totalSize );

        byte[] outData = out.toByteArray();

        // verify checksum
        Assert.assertEquals("Write checksum doesn't match read checksum", sha1, readSha1);
        Assert.assertEquals("Read checksum doesn't match", readSha1, response.getWsChecksum());

        // Check the files
        Assert.assertEquals( "File lengths differ", testData.length, outData.length );
        Assert.assertArrayEquals( "Data contents differ", testData, outData );
    }

    @Ignore("TODO: Figure out why this fails")
    @Test
    public void testUtf8WhiteSpaceValues() throws Exception {
        String utf8String = "Hello ,\u0080 \r \u000B \t \n \t";

        CreateObjectRequest request = new CreateObjectRequest();
        request.userMetadata( new Metadata( "utf8Key", utf8String, false ) );

        ObjectId id = this.api.createObject( request ).getObjectId();
        cleanup.add( id );

        // get the user metadata and make sure all UTF8 characters are accurate
        Map<String, Metadata> metaMap = this.api.getUserMetadata( id );
        Assert.assertEquals( "UTF8 value does not match", utf8String, metaMap.get( "utf8Key" ).getValue() );

        // test set metadata with UTF8
        this.api.setUserMetadata( id, new Metadata( "newKey", utf8String + "2", false ) );

        // verify set metadata call (also testing getAllMetadata)
        ObjectMetadata objMeta = this.api.getObjectMetadata( id );
        metaMap = objMeta.getMetadata();
        //Assert.assertEquals( "UTF8 key does not match", meta.getName(), whiteSpaceString + "2" );
        //Assert.assertEquals( "UTF8 key value does not match", meta.getValue(), "newValue" );
        Assert.assertEquals( "UTF8 value does not match",
                             utf8String + "2",
                             metaMap.get( "newKey" ).getValue() );
    }

    @Test
    public void testUnicodeMetadata() throws Exception {
        CreateObjectRequest request = new CreateObjectRequest();

        Metadata nbspValue = new Metadata( "nbspvalue", "Nobreak\u00A0Value", false );
        Metadata nbspName = new Metadata( "Nobreak\u00A0Name", "regular text here", false );
        Metadata cryllic = new Metadata( "cryllic", "спасибо", false );
        l4j.debug( "NBSP Value: " + nbspValue );
        l4j.debug( "NBSP Name: " + nbspName );

        request.userMetadata( nbspValue, nbspName, cryllic );

        ObjectId id = this.api.createObject( request ).getObjectId();
        Assert.assertNotNull( "null ID returned", id );
        cleanup.add( id );

        // Read and validate the metadata
        Map<String, Metadata> meta = this.api.getUserMetadata( id );
        l4j.debug( "Read Back:" );
        l4j.debug( "NBSP Value: " + meta.get( "nbspvalue" ) );
        l4j.debug( "NBSP Name: " + meta.get( "Nobreak\u00A0Name" ) );
        Assert.assertEquals( "value of 'nobreakvalue' wrong",
                             "Nobreak\u00A0Value",
                             meta.get( "nbspvalue" ).getValue() );
        Assert.assertEquals( "Value of cryllic wrong", "спасибо", meta.get( "cryllic" ).getValue() );
    }

    @Test
    public void testUtf8Metadata() throws Exception {
        String oneByteCharacters = "Hello! ";
        String twoByteCharacters = "\u0410\u0411\u0412\u0413"; // Cyrillic letters
        String fourByteCharacters = "\ud841\udf0e\ud841\udf31\ud841\udf79\ud843\udc53"; // Chinese symbols
        String utf8String = oneByteCharacters + twoByteCharacters + fourByteCharacters;

        CreateObjectRequest request = new CreateObjectRequest();
        request.userMetadata( new Metadata( "utf8Key", utf8String, false ),
                              new Metadata( utf8String, "utf8Value", false ) );

        ObjectId id = this.api.createObject( request ).getObjectId();
        cleanup.add( id );

        // list all tags and make sure the UTF8 tag is in the list
        Map<String, Boolean> tags = this.api.getUserMetadataNames( id );
        Assert.assertTrue( "UTF8 key not found in tag list", tags.containsKey( utf8String ) );

        // get the user metadata and make sure all UTF8 characters are accurate
        Map<String, Metadata> metaMap = this.api.getUserMetadata( id );
        Metadata meta = metaMap.get( utf8String );
        Assert.assertEquals( "UTF8 key does not match", meta.getName(), utf8String );
        Assert.assertEquals( "UTF8 key value does not match", meta.getValue(), "utf8Value" );
        Assert.assertEquals( "UTF8 value does not match", metaMap.get( "utf8Key" ).getValue(), utf8String );

        // test set metadata with UTF8
        this.api.setUserMetadata( id, new Metadata( "newKey", utf8String + "2", false ),
                                  new Metadata( utf8String + "2", "newValue", false ) );

        // verify set metadata call (also testing getAllMetadata)
        ObjectMetadata objMeta = this.api.getObjectMetadata( id );
        metaMap = objMeta.getMetadata();
        meta = metaMap.get( utf8String + "2" );
        Assert.assertEquals( "UTF8 key does not match", meta.getName(), utf8String + "2" );
        Assert.assertEquals( "UTF8 key value does not match", meta.getValue(), "newValue" );
        Assert.assertEquals( "UTF8 value does not match",
                             metaMap.get( "newKey" ).getValue(),
                             utf8String + "2" );
    }

    @Test
    public void testUtf8MetadataFilter() throws Exception {
        String oneByteCharacters = "Hello! ";
        String twoByteCharacters = "\u0410\u0411\u0412\u0413"; // Cyrillic letters
        String fourByteCharacters = "\ud841\udf0e\ud841\udf31\ud841\udf79\ud843\udc53"; // Chinese symbols
        String utf8String = oneByteCharacters + twoByteCharacters + fourByteCharacters;

        CreateObjectRequest request = new CreateObjectRequest();
        request.userMetadata( new Metadata( "utf8Key", utf8String, false ) )
               .userMetadata( new Metadata( utf8String, "utf8Value", false ) );

        ObjectId id = this.api.createObject( request ).getObjectId();
        cleanup.add( id );

        // apply a filter that includes the UTF8 tag
        Map<String, Metadata> metaMap = this.api.getUserMetadata( id, utf8String );
        Assert.assertEquals( "UTF8 filter was not honored", metaMap.size(), 1 );
        Assert.assertNotNull( "UTF8 key was not found in filtered results", metaMap.get( utf8String ) );
    }

    @Test
    public void testUtf8DeleteMetadata() throws Exception {
        String oneByteCharacters = "Hello! ";
        String twoByteCharacters = "\u0410\u0411\u0412\u0413"; // Cyrillic letters
        String fourByteCharacters = "\ud841\udf0e\ud841\udf31\ud841\udf79\ud843\udc53"; // Chinese symbols
        String utf8String = oneByteCharacters + twoByteCharacters + fourByteCharacters;

        CreateObjectRequest request = new CreateObjectRequest();
        request.userMetadata( new Metadata( "utf8Key", utf8String, false ) )
               .userMetadata( new Metadata( utf8String, "utf8Value", false ) );

        ObjectId id = this.api.createObject( request ).getObjectId();
        cleanup.add( id );

        // delete the UTF8 tag
        this.api.deleteUserMetadata( id, utf8String );

        // verify delete was successful
        Map<String, Boolean> nameMap = this.api.getUserMetadataNames( id );
        Assert.assertFalse( "UTF8 key was not deleted", nameMap.containsKey( utf8String ) );
    }

    @Test
    public void testUtf8ListableMetadata() throws Exception {
        String oneByteCharacters = "Hello! ";
        String twoByteCharacters = "\u0410\u0411\u0412\u0413"; // Cyrillic letters
        String fourByteCharacters = "\ud841\udf0e\ud841\udf31\ud841\udf79\ud843\udc53"; // Chinese symbols
        String utf8String = oneByteCharacters + twoByteCharacters + fourByteCharacters;

        CreateObjectRequest request = new CreateObjectRequest();
        request.userMetadata( new Metadata( utf8String, "utf8Value", true ) );

        ObjectId id = this.api.createObject( request ).getObjectId();
        cleanup.add( id );

        Map<String, Metadata> metaMap = this.api.getUserMetadata( id );
        Metadata meta = metaMap.get( utf8String );
        Assert.assertEquals( "UTF8 key does not match", meta.getName(), utf8String );
        Assert.assertEquals( "UTF8 key value does not match", meta.getValue(), "utf8Value" );
        Assert.assertTrue( "UTF8 metadata is not listable", meta.isListable() );

        // verify we can list the tag and see our object
        boolean found = false;
        for ( ObjectEntry result : this.api
                .listObjects( new ListObjectsRequest().metadataName( utf8String ) ).getEntries() ) {
            if ( result.getObjectId().equals( id ) ) {
                found = true;
                break;
            }
        }
        Assert.assertTrue( "UTF8 tag listing did not contain the correct object ID", found );

        // verify we can list child tags of the UTF8 tag
        Set<String> tags = this.api.listMetadata( utf8String );
        Assert.assertNotNull( "UTF8 child tag listing was null", tags );
    }

    @Test
    public void testUtf8ListableTagWithComma() {
        String stringWithComma = "Hello, you!";

        CreateObjectRequest request = new CreateObjectRequest();
        request.userMetadata( new Metadata( stringWithComma, "value", true ) );

        ObjectId id = this.api.createObject( request ).getObjectId();
        cleanup.add( id );

        Map<String, Metadata> metaMap = this.api.getUserMetadata( id );
        Metadata meta = metaMap.get( stringWithComma );
        Assert.assertNotNull( meta );
        Assert.assertEquals( "key does not match", meta.getName(), stringWithComma );
        Assert.assertTrue( "metadata is not listable", meta.isListable() );

        boolean found = false;
        for ( ObjectEntry result : this.api
                .listObjects( new ListObjectsRequest().metadataName( stringWithComma ) ).getEntries() ) {
            if ( result.getObjectId().equals( id ) ) {
                found = true;
                break;
            }
        }
        Assert.assertTrue( "listing did not contain the correct object ID", found );
    }

    @Test
    public void testRename() throws Exception {
        ObjectPath op1 = new ObjectPath( "/" + rand8char() + ".tmp" );
        ObjectPath op2 = new ObjectPath( "/" + rand8char() + ".tmp" );

        ObjectId id = this.api.createObject( op1, "Four score and seven years ago".getBytes( "UTF-8" ), "text/plain" );
        Assert.assertNotNull( "null ID returned", id );
        cleanup.add( id );

        // Rename
        this.api.move( op1, op2, false );

        // Read back the content
        String content = new String( this.api.readObject( op2, null, byte[].class ), "UTF-8" );
        Assert.assertEquals( "object content wrong", "Four score and seven years ago", content );

    }

    @Test
    public void testRenameOverwrite() throws Exception {
        ObjectPath op1 = new ObjectPath( "/" + rand8char() + ".tmp" );
        ObjectPath op2 = new ObjectPath( "/" + rand8char() + ".tmp" );

        ObjectId id = this.api.createObject( op1, "Four score and seven years ago".getBytes( "UTF-8" ), "text/plain" );
        Assert.assertNotNull( "null ID returned", id );
        cleanup.add( id );

        ObjectId id2 = this.api.createObject( op2, "You should not see this".getBytes( "UTF-8" ), "text/plain" );
        cleanup.add( id2 );

        // Rename
        this.api.move( op1, op2, true );

        // Wait for overwrite to complete
        Thread.sleep( 5000 );

        // Read back the content
        String content = new String( this.api.readObject( op2, null, byte[].class ), "UTF-8" );
        Assert.assertEquals( "object content wrong", "Four score and seven years ago", content );

    }

    /**
     * Tests renaming a path to UTF-8 multi-byte characters.  This is a separate test from create as the characters are
     * passed in the headers instead of the URL itself.
     */
    @Test
    public void testUtf8Rename() throws Exception {
        ObjectPath parentDir = createTestDir("Utf8Rename");
        String oneByteCharacters = "Hello! ,";
        String twoByteCharacters = "\u0410\u0411\u0412\u0413"; // Cyrillic letters
        String fourByteCharacters = "\ud841\udf0e\ud841\udf31\ud841\udf79\ud843\udc53"; // Chinese symbols
        ObjectPath normalName = new ObjectPath( parentDir, rand8char() + ".tmp");
        String crazyName = oneByteCharacters + twoByteCharacters + fourByteCharacters;
        ObjectPath crazyPath = new ObjectPath( parentDir, crazyName );
        byte[] content = "This is a really crazy name.".getBytes( "UTF-8" );

        // normal name
        this.api.createObject( normalName, content, "text/plain" );

        // crazy multi-byte character name
        this.api.move( normalName, crazyPath, true );

        // Wait for overwrite to complete
        Thread.sleep( 5000 );

        // verify name in directory list
        List<DirectoryEntry> entries = this.api.listDirectory(new ListDirectoryRequest().path(parentDir)).getEntries();

        Assert.assertTrue( "crazyName not found in directory listing", directoryContains( entries, crazyName ) );

        // Read back the content
        Assert.assertTrue( "object content wrong",
                           Arrays.equals( content,
                                          this.api.readObject( crazyPath, null, byte[].class ) ) );
    }

    @Test
    public void testPositiveChecksumValidation() throws Exception {
        byte[] data = "Hello Checksums!".getBytes("UTF-8");
        RunningChecksum md5 = new RunningChecksum(ChecksumAlgorithm.MD5);
        RunningChecksum sha0 = new RunningChecksum(ChecksumAlgorithm.SHA0);
        RunningChecksum sha1 = new RunningChecksum(ChecksumAlgorithm.SHA1);
        md5.update(data, 0, data.length);
        sha0.update(data, 0, data.length);
        sha1.update(data, 0, data.length);

        CreateObjectRequest request = new CreateObjectRequest().content(data);
        ObjectId md5Id = api.createObject(request.wsChecksum(md5)).getObjectId();
        ObjectId sha0Id = api.createObject(request.wsChecksum(sha0)).getObjectId();
        ObjectId sha1Id = api.createObject(request.wsChecksum(sha1)).getObjectId();
        cleanup.add(md5Id);
        cleanup.add(sha0Id);
        cleanup.add(sha1Id);

        Assert.assertEquals("MD5 checksum was not equal",
                md5, api.readObject(new ReadObjectRequest().identifier(md5Id), byte[].class).getWsChecksum());
        Assert.assertEquals("SHA0 checksum was not equal",
                sha0, api.readObject(new ReadObjectRequest().identifier(sha0Id), byte[].class).getWsChecksum());
        Assert.assertEquals("SHA1 checksum was not equal",
                sha1, api.readObject(new ReadObjectRequest().identifier(sha1Id), byte[].class).getWsChecksum());

        // do a bunch of calls to make sure we don't try to validate
        api.getSystemMetadata(md5Id);
        api.getObjectMetadata(sha1Id);
        api.getObjectInfo(sha0Id);
        api.readObject(md5Id, new Range(1, 8), byte[].class);
        if (!isEcs) api.listVersions(new ListVersionsRequest().objectId(sha1Id));
        api.getAcl(sha0Id);

        Assert.assertTrue("object stream is not a ChecksummedInputStream",
                api.readObjectStream(sha0Id, null).getObject() instanceof ChecksummedInputStream);

        // test update
        byte[] appendData = " and stuff!".getBytes("UTF-8");
        md5.update(appendData, 0, appendData.length);
        UpdateObjectRequest uRequest = new UpdateObjectRequest().identifier(md5Id).content(appendData).wsChecksum(md5);
        uRequest.setRange(new Range(data.length, data.length + appendData.length - 1));
        api.updateObject(uRequest);

        Assert.assertTrue("object stream is not a ChecksummedInputStream",
                api.readObjectStream(md5Id, null).getObject() instanceof ChecksummedInputStream);

        api.readObject(md5Id, byte[].class);
    }

    /**
     * Tests readback with checksum verification.  In order to test this, create a policy
     * with erasure coding and then set a policy selector with "policy=erasure" to invoke
     * the erasure coding policy.
     */
    @Test
    public void testReadChecksum() throws Exception {
        byte[] data = "hello".getBytes( "UTF-8" );
        Metadata policy = new Metadata( "policy", "erasure", false );
        RunningChecksum wsChecksum = new RunningChecksum(ChecksumAlgorithm.SHA1);
        wsChecksum.update( data, 0, data.length );

        CreateObjectRequest request = new CreateObjectRequest().content( data ).contentType( "text/plain" );
        request.userMetadata( policy ).wsChecksum( wsChecksum );

        CreateObjectResponse response = this.api.createObject( request );
        Assert.assertNotNull( "null ID returned", response.getObjectId() );
        cleanup.add( response.getObjectId() );
        Assert.assertNotNull( "null ID returned", response.getObjectId() );
        Assert.assertEquals( "create checksums don't match", wsChecksum, response.getWsChecksum() );

        // Read back the content
        ReadObjectRequest readRequest = new ReadObjectRequest().identifier( response.getObjectId() );
        ReadObjectResponse<byte[]> readResponse = this.api.readObject( readRequest, byte[].class );
        Assert.assertArrayEquals( "object content wrong", data, readResponse.getObject() );
        Assert.assertEquals( "read checksums don't match", wsChecksum, readResponse.getWsChecksum() );
    }


    /**
     * Tests getting the service information
     */
    @Test
    public void testGetServiceInformation() throws Exception {
        ServiceInformation si = this.api.getServiceInformation();

        Assert.assertNotNull( "Atmos version is null", si.getAtmosVersion() );
    }

    /**
     * Test getting object info.  Note to fully run this testcase, you should
     * create a policy named 'retaindelete' that keys off of the metadata
     * policy=retaindelete that includes a retention and deletion criteria.
     */
    @Test
    public void testGetObjectInfo() throws Exception {
        CreateObjectRequest request = new CreateObjectRequest();
        request.content( "hello".getBytes( "UTF-8" ) ).contentType( "text/plain" );
        ObjectId id = this.api.createObject( request ).getObjectId();
        Assert.assertNotNull( "null ID returned", id );
        cleanup.add( id );

        api.setUserMetadata( id, new Metadata( "policy", "retain", false ) );

        // Read back the content
        String content = new String( this.api.readObject( id, null, byte[].class ), "UTF-8" );
        Assert.assertEquals( "object content wrong", "hello", content );

        // Get the object info
        ObjectInfo oi = this.api.getObjectInfo( id );
        Assert.assertNotNull( "ObjectInfo null", oi );
        Assert.assertNotNull( "ObjectInfo objectid null", oi.getObjectId() );
        if (!isEcs) {
            Assert.assertTrue("ObjectInfo numReplicas is 0", oi.getNumReplicas() > 0);
            Assert.assertNotNull("ObjectInfo replicas null", oi.getReplicas());
            Assert.assertNotNull("ObjectInfo selection null", oi.getSelection());
            Assert.assertTrue("ObjectInfo should have at least one replica", oi.getReplicas().size() > 0);
        }

        // only run these tests if the policy configuration is valid
        Map<String, Metadata> sysmeta = this.api.getSystemMetadata(id);
        Assume.assumeNotNull(sysmeta.get("policyname"));
        Assume.assumeTrue("policyname != retaindelete", "retaindelete".equals(sysmeta.get("policyname").getValue()));
        Assert.assertNotNull( "ObjectInfo expiration null", oi.getExpiration().getEndAt() );
        Assert.assertNotNull( "ObjectInfo retention null", oi.getRetention().getEndAt() );
        api.setUserMetadata( id, new Metadata( "user.maui.retentionEnable", "false", false ) );
    }

    @Test
    public void testHmac() throws Exception {
        // Compute the signature hash
        String input = "Hello World";
        byte[] secret = DatatypeConverter.parseBase64Binary("D7qsp4j16PBHWSiUbc/bt3lbPBY=");
        Mac mac = Mac.getInstance( "HmacSHA1" );
        SecretKeySpec key = new SecretKeySpec( secret, "HmacSHA1" );
        mac.init( key );
        l4j.debug( "Hashing: \n" + input );

        byte[] hashData = mac.doFinal( input.getBytes( "ISO-8859-1" ) );

        // Encode the hash in Base64.
        String hashOut = DatatypeConverter.printBase64Binary(hashData);

        l4j.debug( "Hash: " + hashOut );
    }

    @Test
    public void testDirectoryMetadata() throws Exception {
        ObjectPath dir = new ObjectPath( "/" + rand8char() + "/" );
        Metadata listable = new Metadata( "listable", "foo", true );
        Metadata unlistable = new Metadata( "unlistable", "bar", false );
        Metadata listable2 = new Metadata( "listable2", "foo2 foo2", true );
        Metadata unlistable2 = new Metadata( "unlistable2", "bar2 bar2", false );
        Metadata listable3 = new Metadata( "listable3", null, true );
        Metadata withCommas = new Metadata( "withcommas", "I, Robot", false );
        Metadata withEquals = new Metadata( "withequals", "name=value", false );
        ObjectId id = this.api.createDirectory( dir, null, listable, unlistable, listable2, unlistable2, listable3,
                                                withCommas, withEquals );
        Assert.assertNotNull( "null ID returned", id );
        cleanup.add( id );

        // Read and validate the metadata
        Map<String, Metadata> metaMap = this.api.getObjectMetadata( dir ).getMetadata();
        Assert.assertNotNull("Missing metadata 'listable'", metaMap.get( "listable" ));
        Assert.assertNotNull("Missing metadata 'listable2'", metaMap.get( "listable2" ));
        Assert.assertNotNull("Missing metadata 'unlistable'", metaMap.get( "unlistable" ));
        Assert.assertNotNull("Missing metadata 'unlistable2'", metaMap.get( "unlistable2" ));
        Assert.assertEquals( "value of 'listable' wrong", "foo", metaMap.get( "listable" ).getValue() );
        Assert.assertEquals( "value of 'listable2' wrong", "foo2 foo2", metaMap.get( "listable2" ).getValue() );
        Assert.assertEquals( "value of 'unlistable' wrong", "bar", metaMap.get( "unlistable" ).getValue() );
        Assert.assertEquals( "value of 'unlistable2' wrong", "bar2 bar2", metaMap.get( "unlistable2" ).getValue() );
        Assert.assertNotNull( "listable3 missing", metaMap.get( "listable3" ) );
        Assert.assertTrue( "Value of listable3 should be empty",
                           metaMap.get( "listable3" ).getValue() == null
                           || metaMap.get( "listable3" ).getValue().length() == 0 );
        Assert.assertEquals( "Value of withcommas wrong", "I, Robot", metaMap.get( "withcommas" ).getValue() );
        Assert.assertEquals( "Value of withequals wrong", "name=value", metaMap.get( "withequals" ).getValue() );

        // Check listable flags
        Assert.assertEquals( "'listable' is not listable", true, metaMap.get( "listable" ).isListable() );
        Assert.assertEquals( "'listable2' is not listable", true, metaMap.get( "listable2" ).isListable() );
        Assert.assertEquals( "'listable3' is not listable", true, metaMap.get( "listable3" ).isListable() );
        Assert.assertEquals( "'unlistable' is listable", false, metaMap.get( "unlistable" ).isListable() );
        Assert.assertEquals( "'unlistable2' is listable", false, metaMap.get( "unlistable2" ).isListable() );
    }

    /**
     * Tests fetching data with multiple ranges.
     */
    @Test
    public void testMultipleRanges() throws Exception {
        String input = "Four score and seven years ago";
        ObjectId id = api.createObject( input.getBytes( "UTF-8" ), "text/plain" );
        cleanup.add( id );
        Assert.assertNotNull( "Object null", id );

        Range[] ranges = new Range[5];
        ranges[0] = new Range( 27, 28 ); //ag
        ranges[1] = new Range( 9, 9 ); // e
        ranges[2] = new Range( 5, 5 ); // s
        ranges[3] = new Range( 4, 4 ); // ' '
        ranges[4] = new Range( 27, 29 ); // ago

        ReadObjectResponse<MultipartEntity> response = api.readObject( new ReadObjectRequest().identifier( id )
                                                                                              .ranges( ranges ),
                                                                       MultipartEntity.class );
        String out = new String( response.getObject().aggregateBytes(), "UTF-8" );
        Assert.assertEquals( "Content incorrect", "ages ago", out );
    }

    //---------- Features supported by the Atmos 2.0 REST API. ----------\\

    @Test
    public void testGetShareableUrlAndDisposition() throws Exception {
        // Create an object with content.
        String str = "Four score and twenty years ago";
        ObjectId id = this.api.createObject( str.getBytes( "UTF-8" ), "text/plain" );
        Assert.assertNotNull( "null ID returned", id );
        cleanup.add( id );

        String disposition = "attachment; filename=\"foo bar.txt\"";

        Calendar c = Calendar.getInstance();
        c.add( Calendar.HOUR, 4 );
        Date expiration = c.getTime();
        URL u = this.api.getShareableUrl( id, expiration, disposition );

        l4j.debug( "Sharable URL: " + u );

        InputStream stream = (InputStream) u.getContent();
        BufferedReader br = new BufferedReader( new InputStreamReader( stream ) );
        String content = br.readLine();
        l4j.debug( "Content: " + content );
        Assert.assertEquals( "URL does not contain proper content",
                             str, content );
    }

    @Test
    public void testGetShareableUrlWithPathAndDisposition() throws Exception {
        // Create an object with content.
        String str = "Four score and twenty years ago";
        ObjectPath op = new ObjectPath( "/" + rand8char() + ".txt" );
        ObjectId id = this.api.createObject( op, str.getBytes( "UTF-8" ), "text/plain" );
        Assert.assertNotNull( "null ID returned", id );
        //cleanup.add( op );

        String disposition = "attachment; filename=\"foo bar.txt\"";

        Calendar c = Calendar.getInstance();
        c.add( Calendar.HOUR, 4 );
        Date expiration = c.getTime();
        URL u = this.api.getShareableUrl( op, expiration, disposition );

        l4j.debug( "Sharable URL: " + u );

        InputStream stream = (InputStream) u.getContent();
        BufferedReader br = new BufferedReader( new InputStreamReader( stream ) );
        String content = br.readLine();
        l4j.debug( "Content: " + content );
        Assert.assertEquals( "URL does not contain proper content",
                             str, content );
    }

    @Test
    public void testGetShareableUrlWithPathAndUTF8Disposition() throws Exception {
        // Create an object with content.
        String str = "Four score and twenty years ago";
        ObjectPath op = new ObjectPath( "/" + rand8char() + ".txt" );
        ObjectId id = this.api.createObject( op, str.getBytes( "UTF-8" ), "text/plain" );
        Assert.assertNotNull( "null ID returned", id );
        //cleanup.add( op );

        // One cryllic, one accented, and one japanese character
        // RFC5987
        String disposition = "attachment; filename=\"no UTF support.txt\"; filename*=UTF-8''" + URLEncoder.encode(
                "бöｼ.txt",
                "UTF-8" );

        Calendar c = Calendar.getInstance();
        c.add( Calendar.HOUR, 4 );
        Date expiration = c.getTime();
        URL u = this.api.getShareableUrl( op, expiration, disposition );

        l4j.debug( "Sharable URL: " + u );

        InputStream stream = (InputStream) u.getContent();
        BufferedReader br = new BufferedReader( new InputStreamReader( stream ) );
        String content = br.readLine();
        l4j.debug( "Content: " + content );
        Assert.assertEquals( "URL does not contain proper content",
                             str, content );
    }

    @Test
    public void testGetServiceInformationFeatures() throws Exception {
        ServiceInformation info = this.api.getServiceInformation();
        l4j.info( "Supported features: " + info.getFeatures() );

        Assert.assertTrue( "Expected at least one feature", info.getFeatures().size() > 0 );

    }

    @Test
    public void testBug23750() throws Exception {
        byte[] data = new byte[1000];
        Arrays.fill( data, (byte) 0 );
        Metadata meta = new Metadata( "test", null, true );

        RunningChecksum sha1 = new RunningChecksum( ChecksumAlgorithm.SHA1 );
        sha1.update( data, 0, data.length );
        CreateObjectResponse response = this.api.createObject(
                new CreateObjectRequest().content( data ).wsChecksum( sha1 ).userMetadata( meta ) );

        try {
            Range range = new Range( 1000, 1999 );
            RunningChecksum md5 = new RunningChecksum(ChecksumAlgorithm.MD5);
            md5.update(data, 0, 1000);
            this.api.updateObject( new UpdateObjectRequest().identifier( response.getObjectId() ).content( data )
                    .range(range).wsChecksum(md5).userMetadata(meta));

            Assert.fail( "Should have triggered an exception" );
        } catch ( AtmosException e ) {
            // expected
        }
    }

    @Test
    public void testCrudKeys() throws Exception {
        Assume.assumeFalse(isEcs);
        ObjectKey key = new ObjectKey( "Test_key-pool#@!$%^..", "KEY_TEST" );
        String content = "Hello World!";

        CreateObjectRequest request = new CreateObjectRequest().identifier( key );
        request.content( content.getBytes( "UTF-8" ) ).contentType( "text/plain" );

        ObjectId oid = this.api.createObject( request ).getObjectId();
        Assert.assertNotNull( "Null object ID returned", oid );
        cleanup.add( oid );

        String readContent = new String( this.api.readObject( key, null, byte[].class ), "UTF-8" );
        Assert.assertEquals( "content mismatch", content, readContent );

        content = "Hello Waldo!";
        this.api.updateObject( new UpdateObjectRequest().identifier( key ).content( content.getBytes( "UTF-8" ) ) );

        readContent = new String( this.api.readObject( key, null, byte[].class ), "UTF-8" );
        Assert.assertEquals( "content mismatch", content, readContent );

        this.api.delete( key );

        try {
            this.api.readObject( key, null, byte[].class );
            Assert.fail( "Object still exists" );
        } catch ( AtmosException e ) {
            if ( e.getHttpCode() != 404 ) throw e;
        }
    }

    @Test
    public void testIssue9() throws Exception {
        int threadCount = 10;

        final int objectSize = 10 * 1000 * 1000; // not a power of 2
        final AtmosApi atmosApi = api;
        final List<ObjectIdentifier> cleanupList = new ArrayList<ObjectIdentifier>();
        ThreadPoolExecutor executor = new ThreadPoolExecutor( threadCount, threadCount, 0, TimeUnit.SECONDS,
                                                              new LinkedBlockingQueue<Runnable>() );
        try {
            for ( int i = 0; i < threadCount; i++ ) {
                executor.execute( new Thread() {
                    public void run() {
                        CreateObjectRequest request = new CreateObjectRequest();
                        request.content( new RandomInputStream( objectSize ) ).contentLength( objectSize )
                               .userMetadata( new Metadata( "test-data", null, true ) );
                        ObjectId oid = atmosApi.createObject( request ).getObjectId();
                        cleanupList.add( oid );
                    }
                } );
            }
            while ( true ) {
                Thread.sleep( 1000 );
                if ( executor.getActiveCount() < 1 ) break;
            }
        } finally {
            executor.shutdown();
            cleanup.addAll( cleanupList );
            if ( cleanupList.size() < threadCount ) Assert.fail( "At least one thread failed" );
        }
    }

    /**
     * Test handling signature failures.  Should throw an exception with
     * error code 1032.
     */
    @Test
    public void testSignatureFailure() throws Exception {
        byte[] goodSecret = config.getSecretKey();
        try {

            // Fiddle with the secret key
            byte[] badSecret = Arrays.copyOf( goodSecret, goodSecret.length );
            Arrays.fill( badSecret, 5, 10, (byte) 128 ); // indexes 5-9 will be 10000000 (binary)
            config.setSecretKey( badSecret );
            testCreateEmptyObject();
            Assert.fail( "Expected exception to be thrown" );
        } catch ( AtmosException e ) {
            Assert.assertEquals( "Expected error code 1032 for signature failure", 1032, e.getErrorCode() );
        } finally {
            config.setSecretKey( goodSecret );
        }
    }

    /**
     * Test general HTTP errors by generating a 404.
     */
    @Test
    public void testFourOhFour() throws Exception {
        try {
            // Fiddle with the context
            config.setContext( "/restttttttttt" );
            testCreateEmptyObject();
            Assert.fail( "Expected exception to be thrown" );
        } catch ( AtmosException e ) {
            Assert.assertEquals( "Expected error code 404 for bad context root", 404, e.getHttpCode() );
        } finally {
            config.setContext( AtmosConfig.DEFAULT_CONTEXT );
        }
    }

    @Test
    public void testServerOffset() throws Exception {
        long offset = api.calculateServerClockSkew();
        l4j.info( "Server offset: " + offset + " milliseconds" );
        testCreateEmptyObject(); // make sure requests still work after setting clock skew
    }

    /**
     * NOTE: This method does not actually test that the custom headers are sent over the wire. Run tcpmon or wireshark
     * to verify
     */
    @Test
    public void testCustomHeaders() throws Exception {
        final Map<String, String> customHeaders = new HashMap<String, String>();
        customHeaders.put( "myCustomHeader", "Hello World!" );

        ((AtmosApiClient) api).addClientFilter( new ClientFilter() {
            @Override
            public ClientResponse handle( ClientRequest clientRequest ) throws ClientHandlerException {
                for ( String name : customHeaders.keySet() )
                    clientRequest.getHeaders().add( name, customHeaders.get( name ) );
                return getNext().handle( clientRequest );
            }
        } );

        api.getServiceInformation();
    }

    @Test
    public void testServerGeneratedChecksum() throws Exception {
        Assume.assumeFalse(isEcs);
        byte[] data = "hello".getBytes( "UTF-8" );

        // generate our own checksum
        RunningChecksum md5 = new RunningChecksum( ChecksumAlgorithm.MD5 );
        md5.update( data, 0, data.length );

        CreateObjectRequest request = new CreateObjectRequest().content( data ).contentType( "text/plain" );
        request.setServerGeneratedChecksumAlgorithm( ChecksumAlgorithm.MD5 );
        CreateObjectResponse response = this.api.createObject( request );
        Assert.assertNotNull( "null ID returned", response.getObjectId() );
        cleanup.add( response.getObjectId() );

        // verify checksum
        Assert.assertEquals( md5.toString( false ), response.getServerGeneratedChecksum().toString( false ) );

        // Read back the content
        ReadObjectRequest readRequest = new ReadObjectRequest().identifier( response.getObjectId() );
        ReadObjectResponse<byte[]> readResponse = api.readObject( readRequest, byte[].class );
        String content = new String( readResponse.getObject(), "UTF-8" );
        Assert.assertEquals( "object content wrong", "hello", content );

        // verify checksum
        Assert.assertEquals( md5.toString( false ), readResponse.getServerGeneratedChecksum().toString( false ) );
    }

    @Ignore("Blocked by Bug 30073")
    @Test
    public void testReadAccessToken() throws Exception {
        Assume.assumeFalse(isEcs);
        ObjectPath parentDir = createTestDir("ReadAccessToken");
        ObjectPath path = new ObjectPath( parentDir, "read_token \n,<x> test" );
        ObjectId id = api.createObject( path, "hello", "text/plain" );

        Calendar expiration = Calendar.getInstance();
        expiration.add( Calendar.MINUTE, 5 ); // 5 minutes from now

        AccessTokenPolicy.Source source = new AccessTokenPolicy.Source();
        source.setAllowList(Collections.singletonList("10.0.0.0/8"));
        source.setDenyList(Collections.singletonList("1.1.1.1"));

        AccessTokenPolicy.ContentLengthRange range = new AccessTokenPolicy.ContentLengthRange();
        range.setFrom( 0 );
        range.setTo( 1024 ); // 1KB

        AccessTokenPolicy policy = new AccessTokenPolicy();
        policy.setExpiration( expiration.getTime() );
        policy.setSource( source );
        policy.setMaxDownloads( 2 );
        policy.setMaxUploads( 0 );
        policy.setContentLengthRange( range );

        CreateAccessTokenRequest request = new CreateAccessTokenRequest().identifier( id ).policy( policy );
        CreateAccessTokenResponse response = api.createAccessToken( request );

        String content = StreamUtil.readAsString( response.getTokenUrl().openStream() );
        Assert.assertEquals( "content from *id* access token doesn't match", content, "hello" );

        api.deleteAccessToken( response.getTokenUrl() );

        response = api.createAccessToken( new CreateAccessTokenRequest().identifier( path ).policy( policy ) );

        content = StreamUtil.readAsString( response.getTokenUrl().openStream() );
        Assert.assertEquals( "content from *path* access token doesn't match", content, "hello" );

        GetAccessTokenResponse getResponse = api.getAccessToken( response.getTokenUrl() );
        AccessToken token = getResponse.getToken();

        api.deleteAccessToken( token.getId() );

        Assert.assertEquals( "token ID doesn't match",
                             RestUtil.lastPathElement( response.getTokenUrl().getPath() ),
                             token.getId() );
        policy.setMaxDownloads( policy.getMaxDownloads() - 1 ); // we already used one
        Assert.assertEquals( "policy differs", policy, token );
    }

    @Ignore("Blocked by Bug 30073")
    @Test
    public void testWriteAccessToken() throws Exception {
        Assume.assumeFalse(isEcs);
        ObjectPath parentDir = createTestDir("WriteAccessToken");
        ObjectPath path = new ObjectPath( parentDir, "write_token_test" );

        Calendar expiration = Calendar.getInstance();
        expiration.add( Calendar.MINUTE, 10 ); // 10 minutes from now

        AccessTokenPolicy.Source source = new AccessTokenPolicy.Source();
        source.setAllowList(Collections.singletonList("10.0.0.0/8"));
        source.setDenyList(Collections.singletonList("1.1.1.1"));

        AccessTokenPolicy.ContentLengthRange range = new AccessTokenPolicy.ContentLengthRange();
        range.setFrom( 0 );
        range.setTo( 1024 ); // 1KB

        List<AccessTokenPolicy.FormField> formFields = new ArrayList<AccessTokenPolicy.FormField>();
        AccessTokenPolicy.FormField formField = new AccessTokenPolicy.FormField();
        formField.setName( "x-emc-meta" );
        formField.setOptional( true );
        formFields.add( formField );
        formField = new AccessTokenPolicy.FormField();
        formField.setName( "x-emc-listable-meta" );
        formField.setOptional( true );
        formFields.add( formField );

        AccessTokenPolicy policy = new AccessTokenPolicy();
        policy.setExpiration( expiration.getTime() );
        policy.setSource( source );
        policy.setMaxDownloads( 2 );
        policy.setMaxUploads( 1 );
        policy.setContentLengthRange( range );
        policy.setFormFieldList( formFields );

        CreateAccessTokenRequest request = new CreateAccessTokenRequest().identifier( path ).policy( policy );
        URL tokenUrl = api.createAccessToken( request ).getTokenUrl();

        Client client = Client.create();

        // prepare upload form
        String content = "Anonymous Upload Test";

        // note we have to specify content-disposition parameters in a specific order due to bug 27005
        FormDataContentDisposition contentDisposition = new ReorderedFormDataContentDisposition(
                "form-data; name=\"data\"; filename=\"test.txt\"" );
        BodyPart bodyPart = new BodyPart( content, MediaType.TEXT_PLAIN_TYPE ).contentDisposition( contentDisposition );

        FormDataMultiPart form = new FormDataMultiPart();
        form.field( "x-emc-meta", "color=gray,size=3,foo=bar" )
            .field( "x-emc-listable-meta", "listable=" )
            .bodyPart( bodyPart );

        // upload
        ClientResponse clientResponse = client.resource( tokenUrl.toURI() )
                                              .type( MediaType.MULTIPART_FORM_DATA_TYPE )
                                              .post( ClientResponse.class, form );
        Assert.assertEquals( "http status from upload is wrong", 201, clientResponse.getStatus() );
        ObjectId oid = new ObjectId( RestUtil.lastPathElement( clientResponse.getLocation().getPath() ) );
        cleanup.add( oid );

        clientResponse = client.resource( tokenUrl.toURI() ).get( ClientResponse.class );
        Assert.assertEquals( content, clientResponse.getEntity( String.class ) );

        // verify upload/download counts changed
        AccessToken token = api.getAccessToken( tokenUrl ).getToken();
        Assert.assertEquals( "upload count is wrong", 0, token.getMaxUploads() );
        Assert.assertEquals( "download count is wrong", 1, token.getMaxDownloads() );

        // read object via standard api (namespace) - just make sure it's there
        api.readObject( new ReadObjectRequest().identifier( path ), String.class );

        // " " (objectspace)
        ReadObjectResponse<String> response = api.readObject( new ReadObjectRequest().identifier( oid ), String.class );
        Assert.assertEquals( "content is wrong", content, response.getObject() );
        Assert.assertNotNull( "metadata is null", response.getMetadata() );
        Assert.assertEquals( "content-type is wrong", "text/plain", response.getMetadata().getContentType() );

        Map<String, Metadata> meta = response.getMetadata().getMetadata();
        Assert.assertTrue( "color missing from metadata", meta.containsKey( "color" ) );
        Assert.assertTrue( "size missing from metadata", meta.containsKey( "size" ) );
        Assert.assertTrue( "foo missing from metadata", meta.containsKey( "foo" ) );

        api.deleteAccessToken( tokenUrl );
    }

    @Ignore("Blocked by Bug 30073")
    @Test
    public void testListAccessTokens() throws Exception {
        Assume.assumeFalse(isEcs);
        ObjectPath parentDir = createTestDir("ListAccessTokens");
        ObjectPath path = new ObjectPath( parentDir, "read_token_test" );
        ObjectId id = api.createObject( path, "hello", "text/plain" );

        Calendar expiration = Calendar.getInstance();
        expiration.add( Calendar.MINUTE, 5 ); // 5 minutes from now

        AccessTokenPolicy.Source source = new AccessTokenPolicy.Source();
        source.setAllowList(Collections.singletonList("10.0.0.0/8"));
        source.setDenyList(Collections.singletonList("1.1.1.1"));

        AccessTokenPolicy.ContentLengthRange range = new AccessTokenPolicy.ContentLengthRange();
        range.setFrom( 0 );
        range.setTo( 1024 ); // 1KB

        AccessTokenPolicy policy = new AccessTokenPolicy();
        policy.setExpiration( expiration.getTime() );
        policy.setSource( source );
        policy.setMaxDownloads( 2 );
        policy.setMaxUploads( 0 );
        policy.setContentLengthRange( range );

        CreateAccessTokenRequest request = new CreateAccessTokenRequest().identifier( id ).policy( policy );
        URL tokenUrl1 = api.createAccessToken( request ).getTokenUrl();

        request = new CreateAccessTokenRequest().identifier( path ).policy( policy );
        URL tokenUrl2 = api.createAccessToken( request ).getTokenUrl();

        ListAccessTokensResponse response = api.listAccessTokens( new ListAccessTokensRequest() );
        Assert.assertNotNull( "access token list is null", response.getTokens() );
        Assert.assertEquals( "access token count wrong", 2, response.getTokens().size() );

        AccessToken token = response.getTokens().get( 0 );
        Assert.assertEquals( "token ID doesn't match", RestUtil.lastPathElement( tokenUrl1.getPath() ), token.getId() );
        Assert.assertEquals( "policy differs", policy, token );

        token = response.getTokens().get( 1 );
        Assert.assertEquals( "token ID doesn't match", RestUtil.lastPathElement( tokenUrl2.getPath() ), token.getId() );
        Assert.assertEquals( "policy differs", policy, token );
    }

    @Test
    public void testDisableSslValidation() throws Exception {
        Assume.assumeFalse(isEcs);
        config.setDisableSslValidation( true );
        api = new AtmosApiClient( config );
        List<URI> sslUris = new ArrayList<URI>();
        for ( URI uri : config.getEndpoints() ) {
            sslUris.add( new URI( "https", uri.getUserInfo(), uri.getHost(), uri.getPort(), uri.getPath(),
                                  uri.getQuery(), uri.getFragment() ) );
        }
        config.setEndpoints( sslUris.toArray( new URI[sslUris.size()] ) );

        cleanup.add( api.createObject( "Hello SSL!", null ) );
    }

    @Test
    public void testRetryFilter() throws Exception {
        final int retries = 3, delay = 500;
        final String flagMessage = "XXXXX";

        config.setEnableRetry( true );
        config.setMaxRetries( retries );
        config.setRetryDelayMillis( delay );
        api = new AtmosApiClient( config );

        CreateObjectRequest request = new CreateObjectRequest().contentLength( 1 ).contentType( "text/plain" );
        try {
            api.createObject( request.content( new RetryInputStream( config, flagMessage ) ) );
            Assert.fail( "Retried more than maxRetries times" );
        } catch ( ClientHandlerException e ) {
            Assert.assertEquals( "Wrong exception thrown", flagMessage, e.getCause().getMessage() );
        }

        config.setMaxRetries( retries + 1 );

        ObjectId oid = api.createObject( request.content( new RetryInputStream( config, flagMessage ) ) ).getObjectId();
        cleanup.add( oid );
        byte[] content = api.readObject( oid, null, byte[].class );
        Assert.assertEquals( "Content wrong size", 1, content.length );
        Assert.assertEquals( "Wrong content", (byte) 65, content[0] );

        try {
            api.createObject( request.content( new RetryInputStream( null, null ) {
                @Override
                public int read() throws IOException {
                    switch ( callCount++ ) {
                        case 0:
                            throw new AtmosException( "should not retry", 400 );
                        case 1:
                            return 65;
                    }
                    return -1;
                }
            } ) );
            Assert.fail( "HTTP 400 was retried and should not be" );
        } catch ( ClientHandlerException e ) {
            Assert.assertEquals( "Wrong http code", 400, ((AtmosException) e.getCause()).getHttpCode() );
        }

        try {
            api.createObject( request.content( new RetryInputStream( null, null ) {
                @Override
                public int read() throws IOException {
                    switch ( callCount++ ) {
                        case 0:
                            throw new RuntimeException( flagMessage );
                        case 1:
                            return 65;
                    }
                    return -1;
                }
            } ) );
            Assert.fail( "RuntimeException was retried and should not be" );
        } catch ( ClientHandlerException e ) {
            Assert.assertEquals( "Wrong exception message", flagMessage, e.getCause().getMessage() );
        }
    }

    @Test
    public void testExpect100Continue() throws Exception {
        config.setEnableExpect100Continue( true );

        InputStream is = new RandomInputStream( 5 );
        CreateObjectRequest request = new CreateObjectRequest().content( is ).contentLength( 5 );

        // test success first since some load-balancers screw up the next request after an E: 100-C failure
        cleanup.add( api.createObject( request ).getObjectId() );

        // now test failure
        String tokenId = config.getTokenId();
        config.setTokenId( "bogustokenid" );
        is = new RandomInputStream( 5 );
        try {
            api.createObject( request );
        } catch ( AtmosException e ) {
            Assert.assertEquals( "wrong error code", 1033, e.getErrorCode() );
            Assert.assertEquals( "input stream was read", 5, is.available() );
        } finally {
            config.setTokenId( tokenId );
            is.close();
        }
    }

    @Test
    public void testMultiThreadedBufferedWriter() throws Exception {
        int threadCount = 20;
        ThreadPoolExecutor executor = new ThreadPoolExecutor( threadCount, threadCount, 5000, TimeUnit.MILLISECONDS,
                                                              new LinkedBlockingQueue<Runnable>() );

        // test with String
        List<Throwable> errorList = Collections.synchronizedList( new ArrayList<Throwable>() );
        for ( int i = 0; i < threadCount; i++ ) {
            executor.execute( new ObjectTestThread<String>( "Test thread " + i,
                                                            "text/plain",
                                                            String.class,
                                                            errorList ) );
        }
        do {
            Thread.sleep( 500 );
        } while ( executor.getActiveCount() > 0 );
        if ( !errorList.isEmpty() ) {
            for ( Throwable t : errorList ) t.printStackTrace();
            Assert.fail( "At least one thread failed" );
        }

        // test with JAXB bean
        try {
            for ( int i = 0; i < threadCount; i++ ) {
                executor.execute( new ObjectTestThread<AccessTokenPolicy>( createTestTokenPolicy( "Test thread " + i,
                                                                                                  "x.x.x." + i ),
                                                                           "text/xml",
                                                                           AccessTokenPolicy.class,
                                                                           errorList ) );
            }
            do {
                Thread.sleep( 500 );
            } while ( executor.getActiveCount() > 0 );
        } finally {
            executor.shutdown();
        }
        if ( !errorList.isEmpty() ) {
            for ( Throwable t : errorList ) t.printStackTrace();
            Assert.fail( "At least one thread failed" );
        }
    }

    @Test
    public void testProxyConfiguration() {
        AtmosConfig config = AtmosClientFactory.getAtmosConfig();
        URI proxyUri = config.getProxyUri();

        // don't run this test without a proxy config
        Assume.assumeNotNull(proxyUri);

        // capture existing system props for safety
        String oldProxyHost = System.getProperty("http.proxyHost");
        String oldProxyPort = System.getProperty("http.proxyPort");
        try {
            // just create and delete an object in each scenario
            // 1) URLConnection - no proxy
            config.setProxyUri(null);
            AtmosApi atmos = new AtmosApiBasicClient(config);
            ObjectId oid = atmos.createObject("URLConnection with no proxy", "text/plain");
            atmos.delete(oid);

            // 2) Apache - no proxy
            atmos = new AtmosApiClient(config);
            oid = atmos.createObject("Apache with no proxy", "text/plain");
            atmos.delete(oid);

            // 3) URLConnection - with config proxy
            config.setProxyUri(proxyUri);
            atmos = new AtmosApiBasicClient(config);
            oid = atmos.createObject("URLConnection with config proxy", "text/plain");
            atmos.delete(oid);

            // 4) Apache - with config proxy
            atmos = new AtmosApiClient(config);
            oid = atmos.createObject("Apache with config proxy", "text/plain");
            atmos.delete(oid);

            // 5) URLConnection - with system props (old school) proxy
            config.setProxyUri(null);
            System.setProperty("http.proxyHost", proxyUri.getHost());
            System.setProperty("http.proxyPort", ""+proxyUri.getPort());
            atmos = new AtmosApiBasicClient(config);
            oid = atmos.createObject("URLConnection with sysprop proxy", "text/plain");
            atmos.delete(oid);

            // 6) Apache - with system props (old school) proxy
            // can't specify proxy user/pass in system props
            atmos = new AtmosApiClient(config);
            oid = atmos.createObject("Apache with sysprop proxy", "text/plain");
            atmos.delete(oid);
        } finally {
            // now play nice and reset old props
            if (oldProxyHost != null) System.setProperty("http.proxyHost", oldProxyHost);
            if (oldProxyPort != null) System.setProperty("http.proxyPort", oldProxyPort);
        }
    }

    @Test
    public void testRetention() throws Exception {
        Assume.assumeFalse(isEcs);
        Metadata retention = new Metadata("retentionperiod", "1year", false);
        CreateObjectRequest request = new CreateObjectRequest().content(null).userMetadata(retention);
        ObjectId oid = api.createObject(request.contentType("text/plain")).getObjectId();
        cleanup.add(oid);

        Thread.sleep(2000);

        // make sure retention is enabled
        ObjectInfo info = api.getObjectInfo(oid);
        Assume.assumeTrue(info.getRetention().isEnabled());
        Calendar newEnd = Calendar.getInstance();
        newEnd.setTime(info.getRetainedUntil());

        DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        String retentionEnd = "user.maui.retentionEnd";

        newEnd.set(Calendar.HOUR, 0);
        newEnd.set(Calendar.MINUTE, 0);
        newEnd.set(Calendar.SECOND, 0);
        newEnd.set(Calendar.MILLISECOND, 0);
        newEnd.add(Calendar.DATE, -1);
        try {
            api.setUserMetadata(oid, new Metadata(retentionEnd, iso8601Format.format(newEnd.getTime()), false));
            Assert.fail("should not be able to shorten retention period");
        } catch (AtmosException e) {
            Assert.assertEquals("Wrong error code", 1002, e.getErrorCode());
        }

        // disable retention so we can delete (won't work on compliant subtenants!)
        api.setUserMetadata(oid, new Metadata("user.maui.retentionEnable", "false", false));
    }

    @Test
    public void testUtf8Unencoded() throws Exception {
        AtmosConfig uConfig = createAtmosConfig();
        uConfig.setEncodeUtf8(false);
        AtmosApi uApi = new AtmosApiClient(uConfig);

        CreateObjectRequest request = new CreateObjectRequest();
        Metadata listable = new Metadata( "list\\able", "foo", true );
        Metadata unlistable = new Metadata( "un\\listØable", "bar", false );
        Metadata listable3 = new Metadata( "listable/3", null, true );
        Metadata backslashes = new Metadata( "backslashes", "\\US\\", false );
        request.userMetadata( listable, unlistable, listable3, backslashes );
        ObjectId id = this.api.createObject( request ).getObjectId();
        Assert.assertNotNull( "null ID returned", id );
        cleanup.add( id );

        // Read and validate the metadata unencoded
        Map<String, Metadata> meta = uApi.getUserMetadata( id );
        Assert.assertNotNull( "list\\able missing", meta.get( "list\\able" ) );
        Assert.assertEquals( "value of 'list\\able' wrong", "foo", meta.get( "list\\able" ).getValue() );
        Assert.assertNotNull( "un\\listØable missing", meta.get( "un\\listØable" ) );
        Assert.assertEquals( "value of 'un\\listØable' wrong", "bar", meta.get( "un\\listØable" ).getValue() );
        Assert.assertNotNull( "listable/3 missing", meta.get( "listable/3" ) );
        Assert.assertTrue( "Value of listable3 should be empty",
                meta.get( "listable/3" ).getValue() == null
                        || meta.get( "listable/3" ).getValue().length() == 0 );
        Assert.assertEquals( "value of 'un\\listØable' wrong", "bar", meta.get( "un\\listØable" ).getValue() );
        Assert.assertEquals( "value of 'backslashes' wrong", "\\US\\", meta.get( "backslashes" ).getValue() );

        // Check listable flags
        Assert.assertEquals( "'list\\able' is not listable", true, meta.get( "list\\able" ).isListable() );
        Assert.assertEquals( "'listable/3' is not listable", true, meta.get( "listable/3" ).isListable() );
        Assert.assertEquals( "'un\\listØable' is listable", false, meta.get( "un\\listØable" ).isListable() );
    }

    private String rand8char() {
        Random r = new Random();
        StringBuilder sb = new StringBuilder( 8 );
        for ( int i = 0; i < 8; i++ ) {
            sb.append( (char) ('a' + r.nextInt( 26 )) );
        }
        return sb.toString();
    }

    private AccessTokenPolicy createTestTokenPolicy( String allow, String deny ) {
        AccessTokenPolicy.Source source = new AccessTokenPolicy.Source();
        source.setAllowList(Collections.singletonList(allow));
        source.setDenyList(Collections.singletonList(deny));
        AccessTokenPolicy policy = new AccessTokenPolicy();
        policy.setExpiration( new Date( 1355897000000L ) );
        policy.setMaxDownloads( 5 );
        policy.setMaxUploads( 10 );
        policy.setSource( source );
        return policy;
    }

    private class RetryInputStream extends InputStream {
        int callCount = 0;
        private long now;
        private long lastTime;
        private AtmosConfig config;
        private String flagMessage;

        RetryInputStream( AtmosConfig config, String flagMessage ) {
            this.config = config;
            this.flagMessage = flagMessage;
        }

        @Override
        public int read() throws IOException {
            switch ( callCount++ ) {
                case 0:
                    lastTime = System.currentTimeMillis();
                    throw new AtmosException( "foo", 500 );
                case 1:
                    now = System.currentTimeMillis();
                    Assert.assertTrue( "Retry delay for 500 error was not honored",
                                       now - lastTime >= config.getRetryDelayMillis() );
                    lastTime = now;
                    throw new AtmosException( "bar", 500, 1040 );
                case 2:
                    now = System.currentTimeMillis();
                    Assert.assertTrue( "Retry delay for 1040 error was not honored",
                                       now - lastTime >= config.getRetryDelayMillis() + 300 );
                    lastTime = now;
                    throw new IOException( "baz" );
                case 3:
                    now = System.currentTimeMillis();
                    Assert.assertTrue( "Retry delay for IOException was not honored",
                                       now - lastTime >= config.getRetryDelayMillis() );
                    lastTime = now;
                    throw new AtmosException( flagMessage, 500 );
                case 4:
                    return 65;
            }
            return -1;
        }

        @Override
        public synchronized void reset() throws IOException {
        }

        @Override
        public boolean markSupported() {
            return true;
        }
    }

    private class ObjectTestThread<T> implements Runnable {
        private T content;
        private String contentType;
        private Class<T> objectType;
        private List<Throwable> errorList;

        ObjectTestThread( T content,
                          String contentType,
                          Class<T> objectType,
                          List<Throwable> errorList ) {
            this.content = content;
            this.contentType = contentType;
            this.objectType = objectType;
            this.errorList = errorList;
        }

        @Override
        public void run() {
            try {
                ObjectId oid = api.createObject( content, contentType );
                cleanup.add( oid );
                T readContent = api.readObject( oid, null, objectType );
                Assert.assertEquals( "Content for object " + oid + " not equal", content, readContent );
            } catch ( Throwable t ) {
                errorList.add( t );
            }
        }
    }
}