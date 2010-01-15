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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;
import org.junit.Assert;

import com.emc.esu.api.Acl;
import com.emc.esu.api.EsuApi;
import com.emc.esu.api.EsuException;
import com.emc.esu.api.DirectoryEntry;
import com.emc.esu.api.rest.DownloadHelper;
import com.emc.esu.api.Extent;
import com.emc.esu.api.Grant;
import com.emc.esu.api.Grantee;
import com.emc.esu.api.Identifier;
import com.emc.esu.api.Metadata;
import com.emc.esu.api.MetadataList;
import com.emc.esu.api.MetadataTag;
import com.emc.esu.api.MetadataTags;
import com.emc.esu.api.ObjectId;
import com.emc.esu.api.ObjectMetadata;
import com.emc.esu.api.ObjectPath;
import com.emc.esu.api.ObjectResult;
import com.emc.esu.api.Permission;
import com.emc.esu.api.rest.UploadHelper;

/**
 * Implements testcases that are independent of the protocol (REST vs. SOAP).
 * Note that this class does not implement TestCase; it is called by the 
 * REST and SOAP testcases.
 */
public class EsuApiTest {
    public static Logger l4j = Logger.getLogger( EsuApiTest.class );
    
    private EsuApi esu;
    private List<Identifier> cleanup = new ArrayList<Identifier>();

    public EsuApiTest( EsuApi esu ) {
        this.esu = esu;
    }
    
    /**
     * Tear down after a test is run.  Cleans up objects that were created
     * during the test.  Set cleanUp=false to disable this behavior.
     */
    public void tearDown() {
        for( Iterator<Identifier> i = cleanup.iterator(); i.hasNext(); ) {
        	Identifier cleanItem = i.next();
            try {
                this.esu.deleteObject( cleanItem );
            } catch( Exception e ) {
                System.out.println( "Failed to delete " + cleanItem + ": " + e.getMessage() );
            }
        }
    }

    //
    // TESTS START HERE
    //

    /**
     * Test creating one empty object.  No metadata, no content.
     */
    public void testCreateEmptyObject() throws Exception {
        ObjectId id = this.esu.createObject( null, null, null, null );
        Assert.assertNotNull( "null ID returned", id );
        cleanup.add( id );

        // Read back the content
        String content = new String( this.esu.readObject( id, null, null ), "UTF-8" );
        Assert.assertEquals( "object content wrong", "", content );

    }

    /**
     * Test creating one empty object on a path.  No metadata, no content.
     */
    public void testCreateEmptyObjectOnPath() throws Exception {
    	ObjectPath op = new ObjectPath( "/" + rand8char() );
        ObjectId id = this.esu.createObjectOnPath( op, null, null, null, null );
        cleanup.add( op );
        l4j.debug( "Path: " + op + " ID: " + id );
        Assert.assertNotNull( id );

        // Read back the content
        String content = new String( this.esu.readObject( op, null, null ), "UTF-8" );
        Assert.assertEquals( "object content wrong", "", content );
        content = new String( this.esu.readObject( id, null, null ), "UTF-8" );
        Assert.assertEquals( "object content wrong when reading by id", "", content );
    }

    
    private String rand8char() {
		Random r = new Random();
		StringBuffer sb = new StringBuffer( 8 );
		for( int i=0; i<8; i++ ) {
			sb.append((char)('a' + r.nextInt(26)));
		}
		return sb.toString();
	}

	/**
     * Test creating an object with content but without metadata
     */
    public void testCreateObjectWithContent() throws Exception {
        ObjectId id = this.esu.createObject( null, null, "hello".getBytes( "UTF-8" ), "text/plain" );
        Assert.assertNotNull( "null ID returned", id );
        cleanup.add( id );

        // Read back the content
        String content = new String( this.esu.readObject( id, null, null ), "UTF-8" );
        Assert.assertEquals( "object content wrong", "hello", content );
    }
    
	public void testCreateObjectWithContentStream() throws Exception {
		InputStream in = new ByteArrayInputStream( "hello".getBytes( "UTF-8" ) );
        ObjectId id = this.esu.createObjectFromStream( null, null, in, 5, "text/plain" );
        in.close();
        Assert.assertNotNull( "null ID returned", id );
        cleanup.add( id );

        // Read back the content
        String content = new String( this.esu.readObject( id, null, null ), "UTF-8" );
        Assert.assertEquals( "object content wrong", "hello", content );
	}

    /**
     * Test creating an object with metadata but no content.
     */
    public void testCreateObjectWithMetadataOnPath() {
    	ObjectPath op = new ObjectPath( "/" + rand8char() + ".tmp" );
        MetadataList mlist = new MetadataList();
        Metadata listable = new Metadata( "listable", "foo", true );
        Metadata unlistable = new Metadata( "unlistable", "bar", false );
        Metadata listable2 = new Metadata( "listable2", "foo2 foo2", true );
        Metadata unlistable2 = new Metadata( "unlistable2", "bar2 bar2", false );
        mlist.addMetadata( listable );
        mlist.addMetadata( unlistable );
        mlist.addMetadata( listable2 );
        mlist.addMetadata( unlistable2 );
        ObjectId id = this.esu.createObjectOnPath( op, null, mlist, null, null );
        //this.esu.updateObject( op, null, mlist, null, null, null );
        Assert.assertNotNull( "null ID returned", id );
        cleanup.add( op );

        // Read and validate the metadata
        MetadataList meta = this.esu.getUserMetadata( op, null );
        Assert.assertNotNull( "value of 'listable' missing", meta.getMetadata( "listable" ) );
        Assert.assertNotNull( "value of 'listable2' missing", meta.getMetadata( "listable2" ) );
        Assert.assertNotNull( "value of 'unlistable' missing", meta.getMetadata( "unlistable" ) );
        Assert.assertNotNull( "value of 'unlistable2' missing", meta.getMetadata( "unlistable2" ));
        
        Assert.assertEquals( "value of 'listable' wrong", "foo", meta.getMetadata( "listable" ).getValue() );
        Assert.assertEquals( "value of 'listable2' wrong", "foo2 foo2", meta.getMetadata( "listable2" ).getValue() );
        Assert.assertEquals( "value of 'unlistable' wrong", "bar", meta.getMetadata( "unlistable" ).getValue() );
        Assert.assertEquals( "value of 'unlistable2' wrong", "bar2 bar2", meta.getMetadata( "unlistable2" ).getValue() );
        // Check listable flags
        Assert.assertEquals( "'listable' is not listable", true, meta.getMetadata( "listable" ).isListable()  );
        Assert.assertEquals( "'listable2' is not listable", true, meta.getMetadata( "listable2" ).isListable() );
        Assert.assertEquals( "'unlistable' is listable" , false, meta.getMetadata( "unlistable" ).isListable() );
        Assert.assertEquals( "'unlistable2' is listable", false, meta.getMetadata( "unlistable2" ).isListable() );
    }
    
    /**
     * Test creating an object with metadata but no content.
     */
    public void testCreateObjectWithMetadata() {
        MetadataList mlist = new MetadataList();
        Metadata listable = new Metadata( "listable", "foo", true );
        Metadata unlistable = new Metadata( "unlistable", "bar", false );
        Metadata listable2 = new Metadata( "listable2", "foo2 foo2", true );
        Metadata unlistable2 = new Metadata( "unlistable2", "bar2 bar2", false );
        mlist.addMetadata( listable );
        mlist.addMetadata( unlistable );
        mlist.addMetadata( listable2 );
        mlist.addMetadata( unlistable2 );
        ObjectId id = this.esu.createObject( null, mlist, null, null );
        Assert.assertNotNull( "null ID returned", id );
        cleanup.add( id );

        // Read and validate the metadata
        MetadataList meta = this.esu.getUserMetadata( id, null );
        Assert.assertEquals( "value of 'listable' wrong", "foo", meta.getMetadata( "listable" ).getValue() );
        Assert.assertEquals( "value of 'listable2' wrong", "foo2 foo2", meta.getMetadata( "listable2" ).getValue() );
        Assert.assertEquals( "value of 'unlistable' wrong", "bar", meta.getMetadata( "unlistable" ).getValue() );
        Assert.assertEquals( "value of 'unlistable2' wrong", "bar2 bar2", meta.getMetadata( "unlistable2" ).getValue() );
        // Check listable flags
        Assert.assertEquals( "'listable' is not listable", true, meta.getMetadata( "listable" ).isListable()  );
        Assert.assertEquals( "'listable2' is not listable", true, meta.getMetadata( "listable2" ).isListable() );
        Assert.assertEquals( "'unlistable' is listable" , false, meta.getMetadata( "unlistable" ).isListable() );
        Assert.assertEquals( "'unlistable2' is listable", false, meta.getMetadata( "unlistable2" ).isListable() );

    }

    /**
     * Test reading an object's content
     */
    public void testReadObject() throws Exception {
        ObjectId id = this.esu.createObject( null, null, "hello".getBytes( "UTF-8" ), "text/plain" );
        Assert.assertNotNull( "null ID returned", id );
        cleanup.add( id );

        // Read back the content
        String content = new String( this.esu.readObject( id, null, null ), "UTF-8" );
        Assert.assertEquals( "object content wrong", "hello", content );

        // Read back only 2 bytes
        Extent extent = new Extent( 1, 2 );
        content = new String( this.esu.readObject( id, extent, null ), "UTF-8" );
        Assert.assertEquals( "partial object content wrong", "el", content );               
    }

    /**
     * Test reading an ACL back
     */
    public void testReadAcl( String uid ) {
        // Create an object with an ACL
        Acl acl = new Acl();
        acl.addGrant( new Grant( new Grantee( uid, Grantee.GRANT_TYPE.USER ), Permission.FULL_CONTROL ) );
        acl.addGrant( new Grant( Grantee.OTHER, Permission.READ ) );
        ObjectId id = this.esu.createObject( acl, null, null, null );
        Assert.assertNotNull( "null ID returned", id );
        cleanup.add( id );

        // Read back the ACL and make sure it matches
        Acl newacl = this.esu.getAcl( id );
        l4j.info( "Comparing " + newacl + " with " + acl );

        Assert.assertEquals( "ACLs don't match", acl, newacl );

    }
    
    public void testReadAclByPath( String uid ) {
    	ObjectPath op = new ObjectPath( "/" + rand8char() + ".tmp" );
        // Create an object with an ACL
        Acl acl = new Acl();
        acl.addGrant( new Grant( new Grantee( uid, Grantee.GRANT_TYPE.USER ), Permission.FULL_CONTROL ) );
        acl.addGrant( new Grant( Grantee.OTHER, Permission.READ ) );
        ObjectId id = this.esu.createObjectOnPath( op, acl, null, null, null );
        Assert.assertNotNull( "null ID returned", id );
        cleanup.add( op );

        // Read back the ACL and make sure it matches
        Acl newacl = this.esu.getAcl( op );
        l4j.info( "Comparing " + newacl + " with " + acl );

        Assert.assertEquals( "ACLs don't match", acl, newacl );
    	
    }

    /**
     * Test reading back user metadata
     */
    public void testGetUserMetadata() {
        // Create an object with user metadata
        MetadataList mlist = new MetadataList();
        Metadata listable = new Metadata( "listable", "foo", true );
        Metadata unlistable = new Metadata( "unlistable", "bar", false );
        Metadata listable2 = new Metadata( "listable2", "foo2 foo2", true );
        Metadata unlistable2 = new Metadata( "unlistable2", "bar2 bar2", false );
        mlist.addMetadata( listable );
        mlist.addMetadata( unlistable );
        mlist.addMetadata( listable2 );
        mlist.addMetadata( unlistable2 );
        ObjectId id = this.esu.createObject( null, mlist, null, null );
        Assert.assertNotNull( "null ID returned", id );
        cleanup.add( id );

        // Read only part of the metadata
        MetadataTags mtags = new MetadataTags();
        mtags.addTag( new MetadataTag( "listable", true ) );
        mtags.addTag( new MetadataTag( "unlistable", false ) );
        MetadataList meta = this.esu.getUserMetadata( id, mtags );
        Assert.assertEquals( "value of 'listable' wrong", "foo", meta.getMetadata( "listable" ).getValue() );
        Assert.assertNull( "value of 'listable2' should not have been returned", meta.getMetadata( "listable2" ) );
        Assert.assertEquals( "value of 'unlistable' wrong", "bar", meta.getMetadata( "unlistable" ).getValue() );
        Assert.assertNull( "value of 'unlistable2' should not have been returned", meta.getMetadata( "unlistable2" ) );

    }

    /**
     * Test deleting user metadata
     */
    public void testDeleteUserMetadata() {
        // Create an object with metadata
        MetadataList mlist = new MetadataList();
        Metadata listable = new Metadata( "listable", "foo", true );
        Metadata unlistable = new Metadata( "unlistable", "bar", false );
        Metadata listable2 = new Metadata( "listable2", "foo2 foo2", true );
        Metadata unlistable2 = new Metadata( "unlistable2", "bar2 bar2", false );
        mlist.addMetadata( listable );
        mlist.addMetadata( unlistable );
        mlist.addMetadata( listable2 );
        mlist.addMetadata( unlistable2 );
        ObjectId id = this.esu.createObject( null, mlist, null, null );
        Assert.assertNotNull( "null ID returned", id );
        cleanup.add( id );

        // Delete a couple of the metadata entries
        MetadataTags mtags = new MetadataTags();
        mtags.addTag( new MetadataTag( "listable2", true ) );
        mtags.addTag( new MetadataTag( "unlistable2", false ) );
        this.esu.deleteUserMetadata( id, mtags );

        // Read back the metadata for the object and ensure the deleted
        // entries don't exist
        MetadataList meta = this.esu.getUserMetadata( id, null );
        Assert.assertEquals( "value of 'listable' wrong", "foo", meta.getMetadata( "listable" ).getValue() );
        Assert.assertNull( "value of 'listable2' should not have been returned", meta.getMetadata( "listable2" ) );
        Assert.assertEquals( "value of 'unlistable' wrong", "bar", meta.getMetadata( "unlistable" ).getValue() );
        Assert.assertNull( "value of 'unlistable2' should not have been returned", meta.getMetadata( "unlistable2" ) );
    }

    /**
     * Test creating object versions
     */
    public void testVersionObject() {
        // Create an object
        MetadataList mlist = new MetadataList();
        Metadata listable = new Metadata( "listable", "foo", true );
        Metadata unlistable = new Metadata( "unlistable", "bar", false );
        Metadata listable2 = new Metadata( "listable2", "foo2 foo2", true );
        Metadata unlistable2 = new Metadata( "unlistable2", "bar2 bar2", false );
        mlist.addMetadata( listable );
        mlist.addMetadata( unlistable );
        mlist.addMetadata( listable2 );
        mlist.addMetadata( unlistable2 );
        ObjectId id = this.esu.createObject( null, mlist, null, null );
        Assert.assertNotNull( "null ID returned", id );
        cleanup.add( id );

        // Version the object
        ObjectId vid = this.esu.versionObject( id );
        cleanup.add( vid );
        Assert.assertNotNull( "null version ID returned", vid );
        
        Assert.assertFalse( "Version ID shoudn't be same as original ID", id.equals(vid) );

        // Fetch the version and read its data
        MetadataList meta = this.esu.getUserMetadata( vid, null );
        Assert.assertEquals( "value of 'listable' wrong", "foo", meta.getMetadata( "listable" ).getValue() );
        Assert.assertEquals( "value of 'listable2' wrong", "foo2 foo2", meta.getMetadata( "listable2" ).getValue() );
        Assert.assertEquals( "value of 'unlistable' wrong", "bar", meta.getMetadata( "unlistable" ).getValue() );
        Assert.assertEquals( "value of 'unlistable2' wrong", "bar2 bar2", meta.getMetadata( "unlistable2" ).getValue() );

    }

    /**
     * Test listing the versions of an object
     */
    public void testListVersions() {
        // Create an object
        MetadataList mlist = new MetadataList();
        Metadata listable = new Metadata( "listable", "foo", true );
        Metadata unlistable = new Metadata( "unlistable", "bar", false );
        Metadata listable2 = new Metadata( "listable2", "foo2 foo2", true );
        Metadata unlistable2 = new Metadata( "unlistable2", "bar2 bar2", false );
        mlist.addMetadata( listable );
        mlist.addMetadata( unlistable );
        mlist.addMetadata( listable2 );
        mlist.addMetadata( unlistable2 );
        ObjectId id = this.esu.createObject( null, mlist, null, null );
        Assert.assertNotNull( "null ID returned", id );
        cleanup.add( id );

        // Version the object
        ObjectId vid1 = this.esu.versionObject( id );
        cleanup.add( vid1 );
        Assert.assertNotNull( "null version ID returned", vid1 );
        ObjectId vid2 = this.esu.versionObject( id );
        cleanup.add( vid2 );
        Assert.assertNotNull( "null version ID returned", vid2 );

        // List the versions and ensure their IDs are correct
        List<Identifier> versions = this.esu.listVersions( id );
        Assert.assertEquals( "Wrong number of versions returned", 3, versions.size() );
        Assert.assertTrue( "version 1 not found in version list", versions.contains( vid1 ) );
        Assert.assertTrue( "version 2 not found in version list", versions.contains( vid2 ) );
        Assert.assertTrue( "base object not found in version list", versions.contains( id ) );
    }

    /**
     * Test listing the system metadata on an object
     */
    public void testGetSystemMetadata() {
        // Create an object
        MetadataList mlist = new MetadataList();
        Metadata listable = new Metadata( "listable", "foo", true );
        Metadata unlistable = new Metadata( "unlistable", "bar", false );
        Metadata listable2 = new Metadata( "listable2", "foo2 foo2", true );
        Metadata unlistable2 = new Metadata( "unlistable2", "bar2 bar2", false );
        mlist.addMetadata( listable );
        mlist.addMetadata( unlistable );
        mlist.addMetadata( listable2 );
        mlist.addMetadata( unlistable2 );
        ObjectId id = this.esu.createObject( null, mlist, null, null );
        Assert.assertNotNull( "null ID returned", id );
        cleanup.add( id );

        // Read only part of the metadata
        MetadataTags mtags = new MetadataTags();
        mtags.addTag( new MetadataTag( "atime", false ) );
        mtags.addTag( new MetadataTag( "ctime", false ) );
        MetadataList meta = this.esu.getSystemMetadata( id, mtags );
        Assert.assertNotNull( "value of 'atime' missing", meta.getMetadata( "atime" ) );
        Assert.assertNull( "value of 'mtime' should not have been returned", meta.getMetadata( "mtime" ) );
        Assert.assertNotNull( "value of 'ctime' missing", meta.getMetadata( "ctime" ) );
        Assert.assertNull( "value of 'gid' should not have been returned", meta.getMetadata( "gid" ) );
        Assert.assertNull( "value of 'listable' should not have been returned", meta.getMetadata( "listable" ) );
    }

    /**
     * Test listing objects by a tag
     */
    public void testListObjects() {
        // Create an object
        MetadataList mlist = new MetadataList();
        Metadata listable = new Metadata( "listable", "foo", true );
        Metadata unlistable = new Metadata( "unlistable", "bar", false );
        Metadata listable2 = new Metadata( "list/able/2", "foo2 foo2", true );
        Metadata unlistable2 = new Metadata( "list/able/not", "bar2 bar2", false );
        mlist.addMetadata( listable );
        mlist.addMetadata( unlistable );
        mlist.addMetadata( listable2 );
        mlist.addMetadata( unlistable2 );
        ObjectId id = this.esu.createObject( null, mlist, null, null );
        Assert.assertNotNull( "null ID returned", id );
        cleanup.add( id );

        // List the objects.  Make sure the one we created is in the list
        List<Identifier> objects = this.esu.listObjects( "listable" );
        Assert.assertTrue( "No objects returned", objects.size() > 0 );
        Assert.assertTrue( "object not found in list" , objects.contains( id ) );

        // Check for unlisted
        try {
            this.esu.listObjects( "unlistable" );
            Assert.fail( "Exception not thrown!" );
        } catch( EsuException e ) {
            // This should happen.
            Assert.assertEquals( "Expected 1003 for not found", 1003, e.getAtmosCode() );
        }
    }
    
    /**
     * Test listing objects by a tag
     */
    public void testListObjectsWithMetadata() {
        // Create an object
        MetadataList mlist = new MetadataList();
        Metadata listable = new Metadata( "listable", "foo", true );
        Metadata unlistable = new Metadata( "unlistable", "bar", false );
        Metadata listable2 = new Metadata( "list/able/2", "foo2 foo2", true );
        Metadata unlistable2 = new Metadata( "list/able/not", "bar2 bar2", false );
        mlist.addMetadata( listable );
        mlist.addMetadata( unlistable );
        mlist.addMetadata( listable2 );
        mlist.addMetadata( unlistable2 );
        ObjectId id = this.esu.createObject( null, mlist, null, null );
        Assert.assertNotNull( "null ID returned", id );
        cleanup.add( id );

        // List the objects.  Make sure the one we created is in the list
        List<ObjectResult> objects = this.esu.listObjectsWithMetadata( "listable" );
        Assert.assertTrue( "No objects returned", objects.size() > 0 );
        
        // Find the item.
        boolean found = false;
        for( Iterator<ObjectResult> i = objects.iterator(); i.hasNext(); ) {
            ObjectResult or = i.next();
            if( or.getId().equals( id ) ) {
                found = true;
                // check metadata
                Assert.assertEquals( "Wrong value on metadata", 
                        or.getMetadata().getMetadata( "listable" ).getValue(), "foo" );
            }
        }
        Assert.assertTrue( "object not found in list" , found );

    }

    /**
     * Test fetching listable tags
     */
    public void testGetListableTags() {
        // Create an object
        ObjectId id = this.esu.createObject( null, null, null, null );
        Assert.assertNotNull( "null ID returned", id );
        cleanup.add( id );

        MetadataList mlist = new MetadataList();
        Metadata listable = new Metadata( "listable", "foo", true );
        Metadata unlistable = new Metadata( "unlistable", "bar", false );
        Metadata listable2 = new Metadata( "list/able/2", "foo2 foo2", true );
        Metadata unlistable2 = new Metadata( "list/able/not", "bar2 bar2", false );
        mlist.addMetadata( listable );
        mlist.addMetadata( unlistable );
        mlist.addMetadata( listable2 );
        mlist.addMetadata( unlistable2 );
        this.esu.updateObject( id, null, mlist, null, null, null );

        // List tags.  Ensure our object's tags are in the list.
        MetadataTags tags = this.esu.getListableTags( (String)null );
        Assert.assertTrue( "listable tag not returned", tags.contains( "listable" ) );
        Assert.assertTrue( "list/able/2 root tag not returned", tags.contains( "list" ) );
        Assert.assertFalse( "list/able/not tag returned", tags.contains( "list/able/not" ) );

        // List child tags
        tags = this.esu.getListableTags( "list/able" );
        Assert.assertFalse( "non-child returned", tags.contains( "listable" ) );
        Assert.assertTrue( "list/able/2 tag not returned", tags.contains( "2" ) );
        Assert.assertFalse( "list/able/not tag returned", tags.contains( "not" ) );

    }

    /**
     * Test listing the user metadata tags on an object
     */
    public void testListUserMetadataTags() {
        // Create an object
        MetadataList mlist = new MetadataList();
        Metadata listable = new Metadata( "listable", "foo", true );
        Metadata unlistable = new Metadata( "unlistable", "bar", false );
        Metadata listable2 = new Metadata( "list/able/2", "foo2 foo2", true );
        Metadata unlistable2 = new Metadata( "list/able/not", "bar2 bar2", false );
        mlist.addMetadata( listable );
        mlist.addMetadata( unlistable );
        mlist.addMetadata( listable2 );
        mlist.addMetadata( unlistable2 );
        ObjectId id = this.esu.createObject( null, mlist, null, null );
        Assert.assertNotNull( "null ID returned", id );
        cleanup.add( id );

        // List tags
        MetadataTags tags = this.esu.listUserMetadataTags( id );
        Assert.assertTrue( "listable tag not returned", tags.contains( "listable" ) );
        Assert.assertTrue( "list/able/2 tag not returned", tags.contains( "list/able/2" ) );
        Assert.assertTrue( "unlistable tag not returned", tags.contains( "unlistable" ) );
        Assert.assertTrue( "list/able/not tag not returned", tags.contains( "list/able/not" ) );
        Assert.assertFalse( "unknown tag returned", tags.contains( "unknowntag" ) );

        // Check listable flag
        Assert.assertEquals( "'listable' is not listable", true, tags.getTag( "listable" ).isListable() );
        Assert.assertEquals( "'list/able/2' is not listable", true, tags.getTag( "list/able/2" ).isListable() );
        Assert.assertEquals( "'unlistable' is listable", false, tags.getTag( "unlistable" ).isListable() );
        Assert.assertEquals( "'list/able/not' is listable", false, tags.getTag( "list/able/not" ).isListable() );
    }

    /**
     * Test executing a query.
     */
    public void testQueryObjects( String uid ) {
        // Create an object
        MetadataList mlist = new MetadataList();
        Metadata listable = new Metadata( "listable", "foo", true );
        Metadata unlistable = new Metadata( "unlistable", "bar", false );
        Metadata listable2 = new Metadata( "list/able/2", "foo2 foo2", true );
        Metadata unlistable2 = new Metadata( "list/able/not", "bar2 bar2", false );
        mlist.addMetadata( listable );
        mlist.addMetadata( unlistable );
        mlist.addMetadata( listable2 );
        mlist.addMetadata( unlistable2 );
        ObjectId id = this.esu.createObject( null, mlist, null, null );
        Assert.assertNotNull( "null ID returned", id );
        cleanup.add( id );

        // Query for all objects for the current UID
        String query = "for $h in collection() where $h/maui:MauiObject[uid=\"" +
            uid + "\"] return $h";
        l4j.info( "Query: " + query );
        List<Identifier> objects = this.esu.queryObjects( query );

        // Ensure the search results contains the object we just created
        Assert.assertTrue( "object not found in list", objects.contains( id ) );

    }

    /**
     * Tests updating an object's metadata
     */
    public void testUpdateObjectMetadata() throws Exception {
        // Create an object
        MetadataList mlist = new MetadataList();
        Metadata unlistable = new Metadata( "unlistable", "foo", false );
        mlist.addMetadata( unlistable );
        ObjectId id = this.esu.createObject( null, mlist, "hello".getBytes( "UTF-8" ), null );
        Assert.assertNotNull( "null ID returned", id );
        cleanup.add( id );

        // Update the metadata
        unlistable.setValue( "bar" );
        this.esu.setUserMetadata( id, mlist );

        // Re-read the metadata
        MetadataList meta = this.esu.getUserMetadata( id, null );
        Assert.assertEquals( "value of 'unlistable' wrong", "bar", meta.getMetadata( "unlistable" ).getValue() );
        
        // Check that content was not modified
        String content = new String( this.esu.readObject( id, null, null ), "UTF-8" );
        Assert.assertEquals( "object content wrong", "hello", content );

    }
    
    public void testUpdateObjectAcl( String uid ) throws Exception {
        // Create an object with an ACL
        Acl acl = new Acl();
        acl.addGrant( new Grant( new Grantee( uid, Grantee.GRANT_TYPE.USER ), Permission.FULL_CONTROL ) );
        Grant other = new Grant( Grantee.OTHER, Permission.READ );
        acl.addGrant( other );
        ObjectId id = this.esu.createObject( acl, null, null, null );
        Assert.assertNotNull( "null ID returned", id );
        cleanup.add( id );

        // Read back the ACL and make sure it matches
        Acl newacl = this.esu.getAcl( id );
        l4j.info( "Comparing " + newacl + " with " + acl );

        Assert.assertEquals( "ACLs don't match", acl, newacl );
        
        // Change the ACL and update the object.
        acl.removeGrant( other );
        Grant o2 = new Grant( Grantee.OTHER, Permission.NONE );
        acl.addGrant( o2 );
        this.esu.setAcl( id, acl );
        
        // Read the ACL back and check it
        newacl = this.esu.getAcl( id );
        l4j.info( "Comparing " + newacl + " with " + acl );
        Assert.assertEquals( "ACLs don't match", acl, newacl );
    }

    /**
     * Tests updating an object's contents
     */
    public void testUpdateObjectContent() throws Exception {
        // Create an object
        ObjectId id = this.esu.createObject( null, null, "hello".getBytes( "UTF-8" ), "text/plain" );
        Assert.assertNotNull( "null ID returned", id );
        cleanup.add( id );

        // Update part of the content
        Extent extent = new Extent( 1,1 );
        this.esu.updateObject( id, null, null, extent, "u".getBytes( "UTF-8" ), null ); 

        // Read back the content and check it
        String content = new String( this.esu.readObject( id, null, null ), "UTF-8" );
        Assert.assertEquals( "object content wrong", "hullo", content );
    }

	public void testUpdateObjectContentStream() throws Exception {
        // Create an object
        ObjectId id = this.esu.createObject( null, null, "hello".getBytes( "UTF-8" ), "text/plain" );
        Assert.assertNotNull( "null ID returned", id );
        cleanup.add( id );

        // Update part of the content
        Extent extent = new Extent( 1,1 );
        InputStream in = new ByteArrayInputStream( "u".getBytes( "UTF-8" ) );
        this.esu.updateObjectFromStream( id, null, null, extent, in, 1, null ); 
        in.close();

        // Read back the content and check it
        String content = new String( this.esu.readObject( id, null, null ), "UTF-8" );
        Assert.assertEquals( "object content wrong", "hullo", content );
	}

    /**
     * Test replacing an object's entire contents
     */
    public void testReplaceObjectContent() throws Exception {
        // Create an object
        ObjectId id = this.esu.createObject( null, null, "hello".getBytes( "UTF-8" ), "text/plain" );
        Assert.assertNotNull( "null ID returned", id );
        cleanup.add( id );

        // Update all of the content
        this.esu.updateObject( id, null, null, null, "bonjour".getBytes( "UTF-8" ), null ); 

        // Read back the content and check it
        String content = new String( this.esu.readObject( id, null, null ), "UTF-8" );
        Assert.assertEquals( "object content wrong", "bonjour", content  );
    }

    /**
     * Test the UploadHelper's create method
     */
    public void testCreateHelper() throws Exception {
        // use a blocksize of 1 to test multiple transfers.
        UploadHelper uploadHelper = new UploadHelper( this.esu, new byte[1] );
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write( "hello".getBytes( "UTF-8" ) );

        // Create an object from our file stream
        ObjectId id = uploadHelper.createObject( 
                new ByteArrayInputStream( baos.toByteArray() ),
                null, null, true );
        cleanup.add( id );

        // Read contents back and check them
        String content = new String( this.esu.readObject( id, null, null ), "UTF-8" );
        Assert.assertEquals( "object content wrong", "hello", content );
    }

    /**
     * Test the UploadHelper's update method
     */
    public void testUpdateHelper() throws Exception {
        // use a blocksize of 1 to test multiple transfers.
        UploadHelper uploadHelper = new UploadHelper( this.esu, new byte[1] );

        // Create an object with content.
        ObjectId id = this.esu.createObject( null, null, "Four score and twenty years ago".getBytes( "UTF-8" ), "text/plain" );
        Assert.assertNotNull( "null ID returned", id );
        cleanup.add( id );

        // update the object contents
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write( "hello".getBytes( "UTF-8" ) );

        uploadHelper.updateObject( id, 
                new ByteArrayInputStream( baos.toByteArray() ), null, null, true );

        // Read contents back and check them
        String content = new String( this.esu.readObject( id, null, null ), "UTF-8" );
        Assert.assertEquals( "object content wrong", "hello", content );            
    }

    /**
     * Tests the download helper.  Tests both single and multiple requests.
     */
    public void testDownloadHelper() throws Exception {
        // Create an object with content.
        ObjectId id = this.esu.createObject( null, null, "Four score and twenty years ago".getBytes( "UTF-8" ), "text/plain" );
        Assert.assertNotNull( "null ID returned", id );
        cleanup.add( id );

        // Download the content
        DownloadHelper downloadHelper = new DownloadHelper( this.esu, null );
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        downloadHelper.readObject( id, baos, false );

        // Check the download
        String data = new String( baos.toByteArray(), "UTF-8" );
        Assert.assertEquals( "object content wrong", "Four score and twenty years ago", data );                                             

        // Download again 1 byte in a request
        downloadHelper = new DownloadHelper( this.esu, new byte[1] );
        baos = new ByteArrayOutputStream();
        downloadHelper.readObject( id, baos, false );

        // Check the download
        data = new String( baos.toByteArray(), "UTF-8" );
        Assert.assertEquals( "object content wrong", "Four score and twenty years ago", data );                                             
    }
    
    public void testUploadDownload() throws Exception {
        // Create a byte array to test
        int size=10*1024*1024;
        byte[] testData = new byte[size];
        for( int i=0; i<size; i++ ) {
            testData[i] = (byte)(i%0x93);
        }
        UploadHelper uh = new UploadHelper( this.esu, null );
        
        ObjectId id = uh.createObject( new ByteArrayInputStream( testData ), null, null, true );
        cleanup.add( id );
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream( size );
        
        DownloadHelper dl = new DownloadHelper( this.esu, new byte[4*1024*1024] );
        dl.readObject( id, baos, true );
        
        Assert.assertFalse( "Download should have been OK", dl.isFailed() );
        Assert.assertNull( "Error should have been null", dl.getError() );
        
        byte[] outData = baos.toByteArray();
        
        // Check the files
        Assert.assertEquals( "File lengths differ", testData.length, outData.length );

        Assert.assertArrayEquals( "Data contents differ", testData, outData );
        
    }

	public void testListDirectory() throws Exception {
		String dir = rand8char();
		String file = rand8char();
		String dir2 = rand8char();
        ObjectPath dirPath = new ObjectPath( "/" + dir + "/" );
    	ObjectPath op = new ObjectPath( "/" + dir + "/" + file );
    	ObjectPath dirPath2 = new ObjectPath( "/" + dir + "/" + dir2 + "/" );
    	
    	ObjectId dirId = this.esu.createObjectOnPath( dirPath, null, null, null, null );
        ObjectId id = this.esu.createObjectOnPath( op, null, null, null, null );
        this.esu.createObjectOnPath( dirPath2, null, null, null, null );
        cleanup.add( op );
        cleanup.add( dirPath2 );
        cleanup.add( dirPath );
        l4j.debug( "Path: " + op + " ID: " + id );
        Assert.assertNotNull( id );
        Assert.assertNotNull( dirId );

        // Read back the content
        String content = new String( this.esu.readObject( op, null, null ), "UTF-8" );
        Assert.assertEquals( "object content wrong", "", content );
        content = new String( this.esu.readObject( id, null, null ), "UTF-8" );
        Assert.assertEquals( "object content wrong when reading by id", "", content );
        
        // List the parent path
        List<DirectoryEntry> dirList = esu.listDirectory( dirPath );
        l4j.debug( "Dir content: " + content );
        Assert.assertTrue( "File not found in directory", directoryContains( dirList, op ) );
        Assert.assertTrue( "subdirectory not found in directory", directoryContains( dirList, dirPath2 ) );
	}
	
	private boolean directoryContains( List<DirectoryEntry> dir, ObjectPath path ) {
		for( Iterator<DirectoryEntry> i = dir.iterator(); i.hasNext(); ) {
			DirectoryEntry de = i.next();
			if( de.getPath().equals( path ) ) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * This method tests various legal and illegal pathnames
	 * @throws Exception
	 */
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
	 * Tests the 'get all metadata' call using a path
	 * @param uid
	 * @throws Exception
	 */
	public void testGetAllMetadataByPath( String uid ) throws Exception {
    	ObjectPath op = new ObjectPath( "/" + rand8char() + ".tmp" );
        // Create an object with an ACL
        Acl acl = new Acl();
        acl.addGrant( new Grant( new Grantee( uid, Grantee.GRANT_TYPE.USER ), Permission.FULL_CONTROL ) );
        acl.addGrant( new Grant( Grantee.OTHER, Permission.READ ) );
        MetadataList mlist = new MetadataList();
        Metadata listable = new Metadata( "listable", "foo", true );
        Metadata unlistable = new Metadata( "unlistable", "bar", false );
        Metadata listable2 = new Metadata( "listable2", "foo2 foo2", true );
        Metadata unlistable2 = new Metadata( "unlistable2", "bar2 bar2", false );
        mlist.addMetadata( listable );
        mlist.addMetadata( unlistable );
        mlist.addMetadata( listable2 );
        mlist.addMetadata( unlistable2 );

        String mimeType = "test/mimetype";
        String content = "test";

        ObjectId id = this.esu.createObjectOnPath( op, acl, null, content.getBytes("UTF-8"), mimeType );
        this.esu.updateObject( op, null, mlist, null, null, mimeType );
        Assert.assertNotNull( "null ID returned", id );
        cleanup.add( op );
        
        // Read it back with HEAD call
        ObjectMetadata om = this.esu.getAllMetadata( op );
        Assert.assertNotNull( "value of 'listable' missing", om.getMetadata().getMetadata( "listable" ) );
        Assert.assertNotNull( "value of 'unlistable' missing", om.getMetadata().getMetadata( "unlistable" ) );
        Assert.assertNotNull( "value of 'atime' missing", om.getMetadata().getMetadata( "atime" ) );
        Assert.assertNotNull( "value of 'ctime' missing", om.getMetadata().getMetadata( "ctime" ) );
        Assert.assertEquals( "value of 'listable' wrong", "foo", om.getMetadata().getMetadata( "listable" ).getValue() );
        Assert.assertEquals( "value of 'unlistable' wrong", "bar", om.getMetadata().getMetadata( "unlistable" ).getValue() );
        Assert.assertEquals( "Mimetype incorrect", mimeType, om.getMimeType() );

        // Check the ACL
        // not checking this by path because an extra groupid is added 
        // during the create calls by path.
        //Assert.assertEquals( "ACLs don't match", acl, om.getAcl() );

	}
	
	public void testGetAllMetadataById( String uid ) throws Exception {
        // Create an object with an ACL
        Acl acl = new Acl();
        acl.addGrant( new Grant( new Grantee( uid, Grantee.GRANT_TYPE.USER ), Permission.FULL_CONTROL ) );
        acl.addGrant( new Grant( Grantee.OTHER, Permission.READ ) );
        MetadataList mlist = new MetadataList();
        Metadata listable = new Metadata( "listable", "foo", true );
        Metadata unlistable = new Metadata( "unlistable", "bar", false );
        Metadata listable2 = new Metadata( "listable2", "foo2 foo2", true );
        Metadata unlistable2 = new Metadata( "unlistable2", "bar2 bar2", false );
        mlist.addMetadata( listable );
        mlist.addMetadata( unlistable );
        mlist.addMetadata( listable2 );
        mlist.addMetadata( unlistable2 );
        
        String mimeType = "test/mimetype";
        String content = "test";

        ObjectId id = this.esu.createObject( acl, mlist, content.getBytes( "UTF-8" ), mimeType );
        
        Assert.assertNotNull( "null ID returned", id );
        cleanup.add( id );
        
        // Read it back with HEAD call
        ObjectMetadata om = this.esu.getAllMetadata( id );
        Assert.assertNotNull( "value of 'listable' missing", om.getMetadata().getMetadata( "listable" ) );
        Assert.assertNotNull( "value of 'unlistable' missing", om.getMetadata().getMetadata( "unlistable" ) );
        Assert.assertNotNull( "value of 'atime' missing", om.getMetadata().getMetadata( "atime" ) );
        Assert.assertNotNull( "value of 'ctime' missing", om.getMetadata().getMetadata( "ctime" ) );
        Assert.assertEquals( "value of 'listable' wrong", "foo", om.getMetadata().getMetadata( "listable" ).getValue() );
        Assert.assertEquals( "value of 'unlistable' wrong", "bar", om.getMetadata().getMetadata( "unlistable" ).getValue() );
        Assert.assertEquals( "Mimetype incorrect", mimeType, om.getMimeType() );

        // Check the ACL
        //Assert.assertEquals( "ACLs don't match", acl, om.getAcl() );
		
	}

	/**
	 * Tests getting object replica information.
	 */
	public void testGetObjectReplicaInfo() throws Exception {
        ObjectId id = this.esu.createObject( null, null, "hello".getBytes( "UTF-8" ), "text/plain" );
        Assert.assertNotNull( "null ID returned", id );
        cleanup.add( id );
        
        MetadataTags mt = new MetadataTags();
        mt.addTag( new MetadataTag( "user.maui.lso", false ) );
        MetadataList meta = this.esu.getUserMetadata( id, mt );
        Assert.assertNotNull( meta.getMetadata( "user.maui.lso" ) );
        l4j.debug( "Replica info: " + meta.getMetadata( "user.maui.lso" ) );
	}

	public void testCreateHelperWithPath() throws Exception {
		String dir = rand8char();
		String file = rand8char();

    	ObjectPath op = new ObjectPath( "/" + dir + "/" + file );

    	// use a blocksize of 1 to test multiple transfers.
        UploadHelper uploadHelper = new UploadHelper( this.esu, new byte[1] );
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write( "hello".getBytes( "UTF-8" ) );

        // Create an object from our file stream
        ObjectId id = uploadHelper.createObjectOnPath( op,
                new ByteArrayInputStream( baos.toByteArray() ),
                null, null, true );
        cleanup.add( op );

        // Read contents back and check them
        String content = new String( this.esu.readObject( id, null, null ), "UTF-8" );
        Assert.assertEquals( "object content wrong", "hello", content );
		
	}

	public void testUpdateHelperWithPath() throws Exception {
		String dir = rand8char();
		String file = rand8char();

    	ObjectPath op = new ObjectPath( "/" + dir + "/" + file );

    	// use a blocksize of 1 to test multiple transfers.
        UploadHelper uploadHelper = new UploadHelper( this.esu, new byte[1] );

        // Create an object with content.
        ObjectId id = this.esu.createObjectOnPath( op, null, null, "Four score and twenty years ago".getBytes( "UTF-8" ), "text/plain" );
        Assert.assertNotNull( "null ID returned", id );
        cleanup.add( op );

        // update the object contents
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write( "hello".getBytes( "UTF-8" ) );

        uploadHelper.updateObject( op, 
                new ByteArrayInputStream( baos.toByteArray() ), null, null, true );

        // Read contents back and check them
        String content = new String( this.esu.readObject( id, null, null ), "UTF-8" );
        Assert.assertEquals( "object content wrong", "hello", content );            
	}

    public void testGetShareableUrl( String uid ) throws Exception {
        // Create an object with content.
        String str = "Four score and twenty years ago";
        ObjectId id = this.esu.createObject( null, null, str.getBytes( "UTF-8" ), "text/plain" );
        Assert.assertNotNull( "null ID returned", id );
        cleanup.add( id );

        Calendar c = Calendar.getInstance();
        c.add( Calendar.HOUR, 4 );
        Date expiration = c.getTime();
        URL u = esu.getShareableUrl( id, expiration );
        
        l4j.debug( "Sharable URL: " + u );
        
        InputStream stream = (InputStream)u.getContent();
        BufferedReader br = new BufferedReader( new InputStreamReader( stream ) );
        String content = br.readLine();
        l4j.debug( "Content: " + content );
        Assert.assertEquals( "URL does not contain proper content", 
                str, content.toString() );
    }
    
    public void testGetShareableUrlWithPath( String uid ) throws Exception {
        // Create an object with content.
        String str = "Four score and twenty years ago";
        ObjectPath op = new ObjectPath( "/" + rand8char() + ".txt" );
        ObjectId id = this.esu.createObjectOnPath( op, null, null, str.getBytes( "UTF-8" ), "text/plain" );
        Assert.assertNotNull( "null ID returned", id );
        cleanup.add( op );

        Calendar c = Calendar.getInstance();
        c.add( Calendar.HOUR, 4 );
        Date expiration = c.getTime();
        URL u = esu.getShareableUrl( op, expiration );
        
        l4j.debug( "Sharable URL: " + u );
        
        InputStream stream = (InputStream)u.getContent();
        BufferedReader br = new BufferedReader( new InputStreamReader( stream ) );
        String content = br.readLine();
        l4j.debug( "Content: " + content );
        Assert.assertEquals( "URL does not contain proper content", 
                str, content.toString() );
    }
    
    public void testExpiredSharableUrl( String uid ) throws Exception {
        // Create an object with content.
        String str = "Four score and twenty years ago";
        ObjectId id = this.esu.createObject( null, null, 
                str.getBytes( "UTF-8" ), "text/plain" );
        Assert.assertNotNull( "null ID returned", id );
        cleanup.add( id );

        Calendar c = Calendar.getInstance();
        c.add( Calendar.HOUR, -4 );
        Date expiration = c.getTime();
        URL u = esu.getShareableUrl( id, expiration );
        
        l4j.debug( "Sharable URL: " + u );
        
        try {
            InputStream stream = (InputStream)u.getContent();
            BufferedReader br = new BufferedReader( new InputStreamReader( stream ) );
            String content = br.readLine();
            l4j.debug( "Content: " + content );
            Assert.fail( "Request should have failed" );
        } catch( Exception e ) {
            l4j.debug( "Error (expected): " + e );
        }
    }

	public void testReadObjectStream() throws Exception {
        ObjectId id = this.esu.createObject( null, null, "hello".getBytes( "UTF-8" ), "text/plain" );
        Assert.assertNotNull( "null ID returned", id );
        cleanup.add( id );

        // Read back the content
        InputStream in = this.esu.readObjectStream( id, null );
        BufferedReader br = new BufferedReader( new InputStreamReader( in, "UTF-8" ) );
        String content = br.readLine();
        br.close();
        Assert.assertEquals( "object content wrong", "hello", content );

        // Read back only 2 bytes
        Extent extent = new Extent( 1, 2 );
        in = this.esu.readObjectStream( id, extent );
        br = new BufferedReader( new InputStreamReader( in, "UTF-8" ) );
        content = br.readLine();
        br.close();
        Assert.assertEquals( "partial object content wrong", "el", content );               
	}

	
	
}
