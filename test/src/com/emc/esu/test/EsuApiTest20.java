package com.emc.esu.test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Date;

import com.emc.esu.api.*;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

public class EsuApiTest20 extends EsuApiTest {
	private static final Logger l4j = Logger.getLogger(EsuApiTest20.class);
	
	protected EsuApi20 getEsuApi20() {
		return (EsuApi20)this.esu;
	}
	
    @Test
    public void testHardLink() throws Exception {
        ObjectPath op1 = new ObjectPath("/" + rand8char() + ".tmp");
        ObjectPath op2 = new ObjectPath("/" + rand8char() + ".tmp");

        ObjectId id = this.esu.createObjectOnPath( op1, null, null, "Four score and seven years ago".getBytes( "UTF-8" ), "text/plain" );
        Assert.assertNotNull( "null ID returned", id );
        cleanup.add( id );
        
        l4j.info("nlink after create: " + this.esu.getSystemMetadata(
        		op1, null).getMetadata("nlink").getValue());
        
        // Rename
        getEsuApi20().hardLink( op1, op2 );

        l4j.info("nlink after hardlink: " + this.esu.getSystemMetadata(
        		op1, null).getMetadata("nlink").getValue());

        // Read back the content
        String content = new String( this.esu.readObject( op2, null, null ), "UTF-8" );
        Assert.assertEquals( "object content wrong", "Four score and seven years ago", content );
   	
        getEsuApi20().deleteObject(op2);
        
        l4j.info("nlink after delete: " + this.esu.getSystemMetadata(
        		op1, null).getMetadata("nlink").getValue());
        
    }

    
    @Test
    public void testGetShareableUrlAndDisposition() throws Exception {
        // Create an object with content.
        String str = "Four score and twenty years ago";
        ObjectId id = this.esu.createObject( null, null, str.getBytes( "UTF-8" ), "text/plain" );
        Assert.assertNotNull( "null ID returned", id );
        cleanup.add( id );

        String disposition="attachment; filename=\"foo bar.txt\"";
        
        Calendar c = Calendar.getInstance();
        c.add( Calendar.HOUR, 4 );
        Date expiration = c.getTime();
        URL u = getEsuApi20().getShareableUrl( id, expiration, disposition );
        
        l4j.debug( "Sharable URL: " + u );
        
        InputStream stream = (InputStream)u.getContent();
        BufferedReader br = new BufferedReader( new InputStreamReader( stream ) );
        String content = br.readLine();
        l4j.debug( "Content: " + content );
        Assert.assertEquals( "URL does not contain proper content", 
                str, content.toString() );
    }
    
    @Test
    public void testGetShareableUrlWithPathAndDisposition() throws Exception {
        // Create an object with content.
        String str = "Four score and twenty years ago";
        ObjectPath op = new ObjectPath( "/" + rand8char() + ".txt" );
        ObjectId id = this.esu.createObjectOnPath( op, null, null, str.getBytes( "UTF-8" ), "text/plain" );
        Assert.assertNotNull( "null ID returned", id );
        //cleanup.add( op );

        String disposition="attachment; filename=\"foo bar.txt\"";

        Calendar c = Calendar.getInstance();
        c.add( Calendar.HOUR, 4 );
        Date expiration = c.getTime();
        URL u = getEsuApi20().getShareableUrl( op, expiration, disposition );
        
        l4j.debug( "Sharable URL: " + u );
        
        InputStream stream = (InputStream)u.getContent();
        BufferedReader br = new BufferedReader( new InputStreamReader( stream ) );
        String content = br.readLine();
        l4j.debug( "Content: " + content );
        Assert.assertEquals( "URL does not contain proper content", 
                str, content.toString() );
    }
    
    @Test
    public void testGetShareableUrlWithPathAndUTF8Disposition() throws Exception {
        // Create an object with content.
        String str = "Four score and twenty years ago";
        ObjectPath op = new ObjectPath( "/" + rand8char() + ".txt" );
        ObjectId id = this.esu.createObjectOnPath( op, null, null, str.getBytes( "UTF-8" ), "text/plain" );
        Assert.assertNotNull( "null ID returned", id );
        //cleanup.add( op );

        
        // One cryllic, one accented, and one japanese character
        // RFC5987
        String disposition="attachment; filename=\"no UTF support.txt\"; filename*=UTF-8''" + URLEncoder.encode("бöｼ.txt", "UTF-8");

        Calendar c = Calendar.getInstance();
        c.add( Calendar.HOUR, 4 );
        Date expiration = c.getTime();
        URL u = getEsuApi20().getShareableUrl( op, expiration, disposition );
        
        l4j.debug( "Sharable URL: " + u );
        
        InputStream stream = (InputStream)u.getContent();
        BufferedReader br = new BufferedReader( new InputStreamReader( stream ) );
        String content = br.readLine();
        l4j.debug( "Content: " + content );
        Assert.assertEquals( "URL does not contain proper content", 
                str, content.toString() );
    }
    
    @Test
    public void testGetServiceInformationFeatures() throws Exception {
    	ServiceInformation info = this.esu.getServiceInformation();
    	l4j.info("Supported features: " + info.getFeatures());
    	
    	Assert.assertTrue("Expected at least one feature", info.getFeatures().size()>0);
    	
    }

    @Test
    public void testGenerateChecksum() throws Exception {
        byte[] content = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()-=[]\\;',./_+{}|:\"<>?`~".getBytes( "UTF-8" );

        String expectedSha0Hash = "SHA0/0bf7c1a19fb63615f0a6ef5c5861a874c96418ca";
        String expectedSha1Hash = "SHA1/d3e5e1f07c4c0c8ee010591e240de95d6cd7b7af";
        String expectedMd5Hash = "MD5/519dedc3865353cc8fe275a4aa02b6b9";

        Checksum sha0Checksum = new Checksum( Checksum.Algorithm.SHA0, Checksum.Behavior.GET_FROM_SERVER );
        Checksum sha1Checksum = new Checksum( Checksum.Algorithm.SHA1, Checksum.Behavior.GET_FROM_SERVER );
        Checksum md5Checksum = new Checksum( Checksum.Algorithm.MD5, Checksum.Behavior.GET_FROM_SERVER );

        // calculate hashes
        sha0Checksum.update( content, 0, content.length );
        sha1Checksum.update( content, 0, content.length );
        md5Checksum.update( content, 0, content.length );
        
        // verify hashes
        Assert.assertEquals( "local SHA0 hash is invalid", expectedSha0Hash, sha0Checksum.toString(false) );
        Assert.assertEquals( "local SHA1 hash is invalid", expectedSha1Hash, sha1Checksum.toString(false) );
        Assert.assertEquals( "local MD5 hash is invalid", expectedMd5Hash, md5Checksum.toString(false) );
        
        // create object and verify SHA0 server hash
        ObjectId oid = this.esu.createObject( null, null, content, null, sha0Checksum );
        cleanup.add( oid );
        Assert.assertEquals( "server SHA0 hash is invalid", expectedSha0Hash, sha0Checksum.getServerValue() );
        
        // update object and verify SHA1 server hash
        this.esu.updateObject( oid, null, null, null, content, null, sha1Checksum );
        Assert.assertEquals( "server SHA1 hash is invalid", expectedSha1Hash, sha1Checksum.getServerValue() );

        // update object and verify MD5 server hash
        this.esu.updateObject( oid, null, null, null, content, null, md5Checksum );
        Assert.assertEquals( "server MD5 hash is invalid", expectedMd5Hash, md5Checksum.getServerValue() );
    }

}
