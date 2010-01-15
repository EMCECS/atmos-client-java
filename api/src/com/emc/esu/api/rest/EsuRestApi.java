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
package com.emc.esu.api.rest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;

import com.emc.esu.api.Acl;
import com.emc.esu.api.BufferSegment;
import com.emc.esu.api.EsuApi;
import com.emc.esu.api.EsuException;
import com.emc.esu.api.DirectoryEntry;
import com.emc.esu.api.Extent;
import com.emc.esu.api.Grant;
import com.emc.esu.api.Grantee;
import com.emc.esu.api.HttpInputStreamWrapper;
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
import com.emc.esu.api.Grantee.GRANT_TYPE;
import java.net.URLDecoder;

/**
 * Implements the REST version of the ESU API.  This class uses HttpUrlRequest
 * to perform object and metadata calls against the ESU server.  All of
 * the methods that communicate with the server are atomic and stateless so
 * the object can be used safely in a multithreaded environment.
 */
public class EsuRestApi implements EsuApi {
    private static final Logger l4j = Logger.getLogger( EsuRestApi.class );
    private static final DateFormat HEADER_FORMAT = new SimpleDateFormat( "EEE, d MMM yyyy HH:mm:ss z", Locale.ENGLISH );
    private static final Pattern OBJECTID_EXTRACTOR = Pattern.compile( "/\\w+/objects/([0-9a-f]{44})" );

    private String host;
    private int port;
    private String uid;
    private byte[] secret;

    private String context = "/rest";
    private String proto;


    /**
     * Creates a new EsuRestApi object.
     * @param host the hostname or IP address of the ESU server
     * @param port the port on the server to communicate with.  Generally
     * this is 80 for HTTP and 443 for HTTPS.
     * @param uid the username to use when connecting to the server
     * @param sharedSecret the Base64 encoded shared secret to use to sign
     * requests to the server.
     */
    public EsuRestApi( String host, int port, String uid, String sharedSecret ) {
        try {
            this.secret = Base64.decodeBase64( sharedSecret.getBytes( "UTF-8" ) );
        } catch (UnsupportedEncodingException e) {
            throw new EsuException( "Could not decode shared secret", e );
        }
        this.host = host;
        this.uid = uid;
        this.port = port;

        if( port == 443 ) {
            proto = "https";
        } else {
            proto = "http";
        }
    }



    /**
     * Creates a new object in the cloud.
     * @param acl Access control list for the new object.  May be null
     * to use a default ACL
     * @param metadata Metadata for the new object.  May be null for
     * no metadata.
     * @param data The initial contents of the object.  May be appended
     * to later.  May be null to create an object with no content.
     * @param mimeType the MIME type of the content.  Optional, 
     * may be null.  If data is non-null and mimeType is null, the MIME
     * type will default to application/octet-stream.
     * @return Identifier of the newly created object.
     * @throws EsuException if the request fails.
     */
    public ObjectId createObject( Acl acl, MetadataList metadata,
            byte[] data, String mimeType ) {
        return createObjectFromSegment( acl, metadata, 
                data==null?null:new BufferSegment( data ), mimeType );
    }
    
    /**
     * Creates a new object in the cloud.
     * @param acl Access control list for the new object.  May be null
     * to use a default ACL
     * @param metadata Metadata for the new object.  May be null for
     * no metadata.
     * @param data The initial contents of the object.  May be appended
     * to later.  The stream will NOT be closed at the end of the request.
     * @param length The length of the stream in bytes.  If the stream
     * is longer than the length, only length bytes will be written.  If
     * the stream is shorter than the length, an error will occur.
     * @param mimeType the MIME type of the content.  Optional, 
     * may be null.  If data is non-null and mimeType is null, the MIME
     * type will default to application/octet-stream.
     * @return Identifier of the newly created object.
     * @throws EsuException if the request fails.
     */
    public ObjectId createObjectFromStream( Acl acl, MetadataList metadata, 
            InputStream data, int length, String mimeType ) {
        try {
            String resource = context + "/objects";
            URL u = buildUrl( resource );
            HttpURLConnection con = (HttpURLConnection) u.openConnection();
            
            if( data == null ) {
            	throw new IllegalArgumentException( "Input stream is required" );
            }

            // Build headers
            Map<String,String> headers = new HashMap<String,String>();

            // Figure out the mimetype
            if( mimeType == null ) {
                mimeType = "application/octet-stream";
            }

            headers.put( "Content-Type", mimeType );
            headers.put( "x-emc-uid", uid );

            // Process metadata
            if( metadata != null ) {
                processMetadata( metadata, headers );
            }

            l4j.debug( "meta " + headers.get( "x-emc-meta" ) );

            // Add acl
            if( acl != null ) {
                processAcl( acl, headers );
            }

            con.setFixedLengthStreamingMode( length );
            con.setDoOutput( true );


            // Add date
            TimeZone tz = TimeZone.getTimeZone( "GMT" );
            l4j.debug( "TZ: " + tz );
            HEADER_FORMAT.setTimeZone( tz );
            String dateHeader = HEADER_FORMAT.format( new Date() );
            l4j.debug( "Date: " + dateHeader );
            headers.put( "Date", dateHeader );

            // Sign request
            signRequest( con, "POST", resource, headers );

            con.connect();

            // post data
            OutputStream out = null;
            byte[] buffer = new byte[128*1024];
            int read = 0;
            try {
                out = con.getOutputStream();
                while( read < length ) {
                	int c = data.read( buffer );
                	if( c == -1 ) {
                		throw new EsuException( "EOF encountered reading data stream" );
                	}
                	out.write( buffer, 0, c );
                	read += c;
                }
                out.close();
            } catch( IOException e ) {
                silentClose( out );
                con.disconnect();
                throw new EsuException( "Error posting data", e );
            }

            // Check response
            if( con.getResponseCode() > 299 ) {
                handleError( con );
            }

            // The new object ID is returned in the location response header
            String location = con.getHeaderField( "location" );
            con.disconnect();

            // Parse the value out of the URL
            Matcher m = OBJECTID_EXTRACTOR.matcher( location );
            if( m.find() ) {
                String id = m.group( 1 );
                l4j.debug( "Id: " + id );
                return new ObjectId( id );
            } else {
                throw new EsuException( "Could not find ObjectId in " + location );
            }
        } catch( MalformedURLException e ) {
            throw new EsuException( "Invalid URL", e );
        } catch (IOException e) {
            throw new EsuException( "Error connecting to server", e );
        } catch (GeneralSecurityException e) {
            throw new EsuException( "Error computing request signature", e );
        }    	
    }
    
    /**
     * Creates a new object in the cloud using a BufferSegment.
     * @param acl Access control list for the new object.  May be null
     * to use a default ACL
     * @param metadata Metadata for the new object.  May be null for
     * no metadata.
     * @param data The initial contents of the object.  May be appended
     * to later.  May be null to create an object with no content.
     * @param mimeType the MIME type of the content.  Optional, 
     * may be null.  If data is non-null and mimeType is null, the MIME
     * type will default to application/octet-stream.
     * @return Identifier of the newly created object.
     * @throws EsuException if the request fails.
     */
    public ObjectId createObjectFromSegment( Acl acl, MetadataList metadata,
            BufferSegment data, String mimeType ) {
        
        try {
            String resource = context + "/objects";
            URL u = buildUrl( resource );
            HttpURLConnection con = (HttpURLConnection) u.openConnection();

            // Build headers
            Map<String,String> headers = new HashMap<String,String>();

            // Figure out the mimetype
            if( mimeType == null ) {
                mimeType = "application/octet-stream";
            }

            headers.put( "Content-Type", mimeType );
            headers.put( "x-emc-uid", uid );

            // Process metadata
            if( metadata != null ) {
                processMetadata( metadata, headers );
            }

            l4j.debug( "meta " + headers.get( "x-emc-meta" ) );

            // Add acl
            if( acl != null ) {
                processAcl( acl, headers );
            }

            // Process data
            if( data == null ) {
                data = new BufferSegment( new byte[0] );
            }
            con.setFixedLengthStreamingMode( data.getSize() );
            con.setDoOutput( true );


            // Add date
            TimeZone tz = TimeZone.getTimeZone( "GMT" );
            l4j.debug( "TZ: " + tz );
            HEADER_FORMAT.setTimeZone( tz );
            String dateHeader = HEADER_FORMAT.format( new Date() );
            l4j.debug( "Date: " + dateHeader );
            headers.put( "Date", dateHeader );

            // Sign request
            signRequest( con, "POST", resource, headers );

            con.connect();

            // post data
            OutputStream out = null;
            try {
                out = con.getOutputStream();
                out.write( data.getBuffer(), data.getOffset(), data.getSize() );
                out.close();
            } catch( IOException e ) {
                silentClose( out );
                con.disconnect();
                throw new EsuException( "Error posting data", e );
            }

            // Check response
            if( con.getResponseCode() > 299 ) {
                handleError( con );
            }

            // The new object ID is returned in the location response header
            String location = con.getHeaderField( "location" );
            con.disconnect();

            // Parse the value out of the URL
            Matcher m = OBJECTID_EXTRACTOR.matcher( location );
            if( m.find() ) {
                String id = m.group( 1 );
                l4j.debug( "Id: " + id );
                return new ObjectId( id );
            } else {
                throw new EsuException( "Could not find ObjectId in " + location );
            }
        } catch( MalformedURLException e ) {
            throw new EsuException( "Invalid URL", e );
        } catch (IOException e) {
            throw new EsuException( "Error connecting to server", e );
        } catch (GeneralSecurityException e) {
            throw new EsuException( "Error computing request signature", e );
        }
    }




    /**
     * Creates a new object in the cloud using a BufferSegment on the
     * given path.
     * @param path the path to create the object on.
     * @param acl Access control list for the new object.  May be null
     * to use a default ACL
     * @param metadata Metadata for the new object.  May be null for
     * no metadata.
     * @param data The initial contents of the object.  May be appended
     * to later.  May be null to create an object with no content.
     * @param mimeType the MIME type of the content.  Optional, 
     * may be null.  If data is non-null and mimeType is null, the MIME
     * type will default to application/octet-stream.
     * @return the ObjectId of the newly-created object for references by ID.
     * @throws EsuException if the request fails.
     */
	public ObjectId createObjectFromSegmentOnPath(ObjectPath path, Acl acl,
			MetadataList metadata, BufferSegment data, String mimeType) {
        try {
            String resource = getResourcePath( context, path );
            URL u = buildUrl( resource );
            HttpURLConnection con = (HttpURLConnection) u.openConnection();

            // Build headers
            Map<String,String> headers = new HashMap<String,String>();

            // Figure out the mimetype
            if( mimeType == null ) {
                mimeType = "application/octet-stream";
            }

            headers.put( "Content-Type", mimeType );
            headers.put( "x-emc-uid", uid );

            // Process metadata
            if( metadata != null ) {
                processMetadata( metadata, headers );
            }

            l4j.debug( "meta " + headers.get( "x-emc-meta" ) );

            // Add acl
            if( acl != null ) {
                processAcl( acl, headers );
            }

            // Process data
            if( data == null ) {
                data = new BufferSegment( new byte[0] );
            }
            con.setFixedLengthStreamingMode( data.getSize() );
            con.setDoOutput( true );


            // Add date
            TimeZone tz = TimeZone.getTimeZone( "GMT" );
            l4j.debug( "TZ: " + tz );
            HEADER_FORMAT.setTimeZone( tz );
            String dateHeader = HEADER_FORMAT.format( new Date() );
            l4j.debug( "Date: " + dateHeader );
            headers.put( "Date", dateHeader );

            // Sign request
            signRequest( con, "POST", resource, headers );

            con.connect();

            // post data
            OutputStream out = null;
            try {
                out = con.getOutputStream();
                out.write( data.getBuffer(), data.getOffset(), data.getSize() );
                out.close();
            } catch( IOException e ) {
                silentClose( out );
                con.disconnect();
                throw new EsuException( "Error posting data", e );
            }

            // Check response
            if( con.getResponseCode() > 299 ) {
                handleError( con );
            }

            // The new object ID is returned in the location response header
            String location = con.getHeaderField( "location" );
            con.disconnect();
            
            // Parse the value out of the URL
            Matcher m = OBJECTID_EXTRACTOR.matcher( location );
            if( m.find() ) {
                String id = m.group( 1 );
                l4j.debug( "Id: " + id );
                return new ObjectId( id );
            } else {
                throw new EsuException( "Could not find ObjectId in " + location );
            }

        } catch( MalformedURLException e ) {
            throw new EsuException( "Invalid URL", e );
        } catch (IOException e) {
            throw new EsuException( "Error connecting to server", e );
        } catch (GeneralSecurityException e) {
            throw new EsuException( "Error computing request signature", e );
        }
	}



    /**
     * Creates a new object in the cloud on the specified path.
     * @param path The path to create the object on.
     * @param acl Access control list for the new object.  May be null
     * to use a default ACL
     * @param metadata Metadata for the new object.  May be null for
     * no metadata.
     * @param data The initial contents of the object.  May be appended
     * to later.  May be null to create an object with no content.
     * @param mimeType the MIME type of the content.  Optional, 
     * may be null.  If data is non-null and mimeType is null, the MIME
     * type will default to application/octet-stream.
     * @return the ObjectId of the newly-created object for references by ID.
     * @throws EsuException if the request fails.
     */
	public ObjectId createObjectOnPath(ObjectPath path, Acl acl,
			MetadataList metadata, byte[] data, String mimeType) {
        return createObjectFromSegmentOnPath(path, acl, metadata, 
                data==null?null:new BufferSegment( data ), mimeType );

	}




    /**
     * Deletes an object from the cloud.
     * @param id the identifier of the object to delete.
     */
    public void deleteObject(Identifier id) {
        try {
            String resource = getResourcePath( context, id );
            URL u = buildUrl( resource );
            HttpURLConnection con = (HttpURLConnection) u.openConnection();

            // Build headers
            Map<String,String> headers = new HashMap<String,String>();

            headers.put( "x-emc-uid", uid );

            // Add date
            TimeZone tz = TimeZone.getTimeZone( "GMT" );
            l4j.debug( "TZ: " + tz );
            HEADER_FORMAT.setTimeZone( tz );
            String dateHeader = HEADER_FORMAT.format( new Date() );
            l4j.debug( "Date: " + dateHeader );
            headers.put( "Date", dateHeader );

            // Sign request
            signRequest( con, "DELETE", resource, headers );
            
            con.connect();
            
            // Check response
            if( con.getResponseCode() > 299 ) {
                handleError( con );
            }
            con.disconnect();

        } catch( MalformedURLException e ) {
            throw new EsuException( "Invalid URL", e );
        } catch (IOException e) {
            throw new EsuException( "Error connecting to server", e );
        } catch (GeneralSecurityException e) {
            throw new EsuException( "Error computing request signature", e );
        }
    }


	/**
     * Deletes metadata items from an object.
     * @param id the identifier of the object whose metadata to 
     * delete.
     * @param tags the list of metadata tags to delete.
     */
    public void deleteUserMetadata(Identifier id, MetadataTags tags) {
        if( tags == null ) {
            throw new EsuException( "Must specify tags to delete" );
        }
        try {
        	String resource = getResourcePath( context, id ) + "?metadata/user";
            URL u = buildUrl( resource );
            HttpURLConnection con = (HttpURLConnection) u.openConnection();

            // Build headers
            Map<String,String> headers = new HashMap<String,String>();

            headers.put( "x-emc-uid", uid );
            
            // process tags
            if( tags != null ) {
                processTags( tags, headers );
            }

            // Add date
            TimeZone tz = TimeZone.getTimeZone( "GMT" );
            l4j.debug( "TZ: " + tz );
            HEADER_FORMAT.setTimeZone( tz );
            String dateHeader = HEADER_FORMAT.format( new Date() );
            l4j.debug( "Date: " + dateHeader );
            headers.put( "Date", dateHeader );

            // Sign request
            signRequest( con, "DELETE", resource, headers );
            
            con.connect();
            
            // Check response
            if( con.getResponseCode() > 299 ) {
                handleError( con );
            }
            con.disconnect();
            
        } catch( MalformedURLException e ) {
            throw new EsuException( "Invalid URL", e );
        } catch (IOException e) {
            throw new EsuException( "Error connecting to server", e );
        } catch (GeneralSecurityException e) {
            throw new EsuException( "Error computing request signature", e );
        }
    }

    /**
     * Returns an object's ACL
     * @param id the identifier of the object whose ACL to read
     * @return the object's ACL
     */
    public Acl getAcl(Identifier id) {
        try {
        	String resource = getResourcePath( context, id ) + "?acl";
            URL u = buildUrl( resource );
            HttpURLConnection con = (HttpURLConnection) u.openConnection();

            // Build headers
            Map<String,String> headers = new HashMap<String,String>();

            headers.put( "x-emc-uid", uid );
            
            // Add date
            TimeZone tz = TimeZone.getTimeZone( "GMT" );
            l4j.debug( "TZ: " + tz );
            HEADER_FORMAT.setTimeZone( tz );
            String dateHeader = HEADER_FORMAT.format( new Date() );
            l4j.debug( "Date: " + dateHeader );
            headers.put( "Date", dateHeader );

            // Sign request
            signRequest( con, "GET", resource, headers );
            
            con.connect();
            
            // Check response
            if( con.getResponseCode() > 299 ) {
                handleError( con );
            }
            
            // Parse return headers.  User grants are in x-emc-useracl and
            // group grants are in x-emc-groupacl
            Acl acl = new Acl();
            readAcl( acl, con.getHeaderField( "x-emc-useracl" ), Grantee.GRANT_TYPE.USER );              
            readAcl( acl, con.getHeaderField( "x-emc-groupacl" ), Grantee.GRANT_TYPE.GROUP );
            
            con.disconnect();
            return acl;

        } catch( MalformedURLException e ) {
            throw new EsuException( "Invalid URL", e );
        } catch (IOException e) {
            throw new EsuException( "Error connecting to server", e );
        } catch (GeneralSecurityException e) {
            throw new EsuException( "Error computing request signature", e );
        }
    }



    /**
     * Returns a list of the tags that are listable the current user's tennant.
     * @param tag optional.  If specified, the list will be limited to the tags
     * under the specified tag.  If null, only top level tags will be returned.
     * @return the list of listable tags.
     */
    public MetadataTags getListableTags(MetadataTag tag) {
        return getListableTags( tag == null?null:tag.getName() );
    }


    /**
     * Returns a list of the tags that are listable the current user's tennant.
     * @param tag optional.  If specified, the list will be limited to the tags
     * under the specified tag.  If null, only top level tags will be returned.
     * @return the list of listable tags.
     */
    public MetadataTags getListableTags( String tag ) {
        try {
            String resource = context + "/objects?listabletags";
            URL u = buildUrl( resource );
            HttpURLConnection con = (HttpURLConnection) u.openConnection();

            // Build headers
            Map<String,String> headers = new HashMap<String,String>();

            headers.put( "x-emc-uid", uid );
            
            // Add tag
            if( tag != null ) {
                headers.put( "x-emc-tags", tag );
            }

            // Add date
            TimeZone tz = TimeZone.getTimeZone( "GMT" );
            l4j.debug( "TZ: " + tz );
            HEADER_FORMAT.setTimeZone( tz );
            String dateHeader = HEADER_FORMAT.format( new Date() );
            l4j.debug( "Date: " + dateHeader );
            headers.put( "Date", dateHeader );

            // Sign request
            signRequest( con, "GET", resource, headers );
            
            con.connect();
            
            // Check response
            if( con.getResponseCode() > 299 ) {
                handleError( con );
            }
                        
            String header = con.getHeaderField( "x-emc-listable-tags" );
            l4j.debug( "x-emc-listable-tags: " + header );
            MetadataTags tags = new MetadataTags();
            readTags( tags, header, true );

            con.disconnect();
            return tags;
        } catch( MalformedURLException e ) {
            throw new EsuException( "Invalid URL", e );
        } catch (IOException e) {
            throw new EsuException( "Error connecting to server", e );
        } catch (GeneralSecurityException e) {
            throw new EsuException( "Error computing request signature", e );
        }
    }


    /**
     * Fetches the system metadata for the object.
     * @param id the identifier of the object whose system metadata
     * to fetch.
     * @param tags A list of system metadata tags to fetch.  Optional.
     * Default value is null to fetch all system metadata.
     * @return The list of system metadata for the object.
     */    
    public MetadataList getSystemMetadata(Identifier id,
            MetadataTags tags) {
        try {
        	String resource = getResourcePath( context, id ) + "?metadata/system";
            URL u = buildUrl( resource );
            HttpURLConnection con = (HttpURLConnection) u.openConnection();

            // Build headers
            Map<String,String> headers = new HashMap<String,String>();

            headers.put( "x-emc-uid", uid );
            
            // process tags
            if( tags != null ) {
                processTags( tags, headers );
            }

            // Add date
            TimeZone tz = TimeZone.getTimeZone( "GMT" );
            l4j.debug( "TZ: " + tz );
            HEADER_FORMAT.setTimeZone( tz );
            String dateHeader = HEADER_FORMAT.format( new Date() );
            l4j.debug( "Date: " + dateHeader );
            headers.put( "Date", dateHeader );

            // Sign request
            signRequest( con, "GET", resource, headers );
            
            con.connect();
            
            // Check response
            if( con.getResponseCode() > 299 ) {
                handleError( con );
            }
            
            // Parse return headers.  Regular metadata is in x-emc-meta and
            // listable metadata is in x-emc-listable-meta
            MetadataList meta = new MetadataList();
            readMetadata( meta, con.getHeaderField( "x-emc-meta" ), false );           
            readMetadata( meta, con.getHeaderField( "x-emc-listable-meta" ), true );
            
            con.disconnect();
            return meta;

        } catch( MalformedURLException e ) {
            throw new EsuException( "Invalid URL", e );
        } catch (IOException e) {
            throw new EsuException( "Error connecting to server", e );
        } catch (GeneralSecurityException e) {
            throw new EsuException( "Error computing request signature", e );
        }
    }


    /**
     * Fetches the user metadata for the object.
     * @param id the identifier of the object whose user metadata
     * to fetch.
     * @param tags A list of user metadata tags to fetch.  Optional.  If null,
     * all user metadata will be fetched.
     * @return The list of user metadata for the object.
     */
    public MetadataList getUserMetadata(Identifier id,
            MetadataTags tags) {
        try {
        	String resource = getResourcePath( context, id ) + "?metadata/user";
            URL u = buildUrl( resource );
            HttpURLConnection con = (HttpURLConnection) u.openConnection();

            // Build headers
            Map<String,String> headers = new HashMap<String,String>();

            headers.put( "x-emc-uid", uid );
            
            // process tags
            if( tags != null ) {
                processTags( tags, headers );
            }

            // Add date
            TimeZone tz = TimeZone.getTimeZone( "GMT" );
            l4j.debug( "TZ: " + tz );
            HEADER_FORMAT.setTimeZone( tz );
            String dateHeader = HEADER_FORMAT.format( new Date() );
            l4j.debug( "Date: " + dateHeader );
            headers.put( "Date", dateHeader );

            // Sign request
            signRequest( con, "GET", resource, headers );
            
            con.connect();
            
            // Check response
            if( con.getResponseCode() > 299 ) {
                handleError( con );
            }
            
            // Parse return headers.  Regular metadata is in x-emc-meta and
            // listable metadata is in x-emc-listable-meta
            MetadataList meta = new MetadataList();
            readMetadata( meta, con.getHeaderField( "x-emc-meta" ), false );           
            readMetadata( meta, con.getHeaderField( "x-emc-listable-meta" ), true );
            
            con.disconnect();
            return meta;

        } catch( MalformedURLException e ) {
            throw new EsuException( "Invalid URL", e );
        } catch (IOException e) {
            throw new EsuException( "Error connecting to server", e );
        } catch (GeneralSecurityException e) {
            throw new EsuException( "Error computing request signature", e );
        }
    }




    /**
     * Lists all objects with the given tag.
     * @param tag the tag to search for
     * @return The list of objects with the given tag.  If no objects
     * are found the array will be empty.
     * @throws EsuException if no objects are found (code 1003)
     */
    public List<Identifier> listObjects(MetadataTag tag) {
        return listObjects( tag.getName() );
    }


    /**
     * Lists all objects with the given tag.
     * @param tag the tag to search for
     * @return The list of objects with the given tag.  If no objects
     * are found the array will be empty.
     * @throws EsuException if no objects are found (code 1003)
     */
    public List<Identifier> listObjects(String tag) {
        try {
            String resource = context + "/objects";
            URL u = buildUrl( resource );
            HttpURLConnection con = (HttpURLConnection) u.openConnection();

            // Build headers
            Map<String,String> headers = new HashMap<String,String>();

            headers.put( "x-emc-uid", uid );
            
            // Add tag
            if( tag != null ) {
                headers.put( "x-emc-tags", tag );
            } else {
                throw new EsuException( "Tag cannot be null" );
            }

            // Add date
            TimeZone tz = TimeZone.getTimeZone( "GMT" );
            l4j.debug( "TZ: " + tz );
            HEADER_FORMAT.setTimeZone( tz );
            String dateHeader = HEADER_FORMAT.format( new Date() );
            l4j.debug( "Date: " + dateHeader );
            headers.put( "Date", dateHeader );

            // Sign request
            signRequest( con, "GET", resource, headers );
            
            con.connect();
            
            // Check response
            if( con.getResponseCode() > 299 ) {
                handleError( con );
            }
            
            // Get object id list from response
            byte[] response = readResponse( con, null );
            
            l4j.debug( "Response: " + new String( response, "UTF-8" ) );
            con.disconnect();
            
            return parseObjectList( response );

        } catch( MalformedURLException e ) {
            throw new EsuException( "Invalid URL", e );
        } catch (IOException e) {
            throw new EsuException( "Error connecting to server", e );
        } catch (GeneralSecurityException e) {
            throw new EsuException( "Error computing request signature", e );
        }
    }
    
    /**
     * Lists all objects with the given tag and returns both their
     * IDs and their metadata.
     * @param tag the tag to search for
     * @return The list of objects with the given tag.  If no objects
     * are found the array will be empty.
     * @throws EsuException if no objects are found (code 1003)
     */
    public List<ObjectResult> listObjectsWithMetadata(MetadataTag tag) {
        return listObjectsWithMetadata( tag.getName() );
    }


    /**
     * Lists all objects with the given tag and returns both their
     * IDs and their metadata.
     * @param tag the tag to search for
     * @return The list of objects with the given tag.  If no objects
     * are found the array will be empty.
     * @throws EsuException if no objects are found (code 1003)
     */
    public List<ObjectResult> listObjectsWithMetadata(String tag) {
        try {
            String resource = context + "/objects";
            URL u = buildUrl( resource );
            HttpURLConnection con = (HttpURLConnection) u.openConnection();

            // Build headers
            Map<String,String> headers = new HashMap<String,String>();

            headers.put( "x-emc-uid", uid );
            headers.put( "x-emc-include-meta", "1" );
            
            // Add tag
            if( tag != null ) {
                headers.put( "x-emc-tags", tag );
            } else {
                throw new EsuException( "Tag cannot be null" );
            }

            // Add date
            TimeZone tz = TimeZone.getTimeZone( "GMT" );
            l4j.debug( "TZ: " + tz );
            HEADER_FORMAT.setTimeZone( tz );
            String dateHeader = HEADER_FORMAT.format( new Date() );
            l4j.debug( "Date: " + dateHeader );
            headers.put( "Date", dateHeader );

            // Sign request
            signRequest( con, "GET", resource, headers );
            
            con.connect();
            
            // Check response
            if( con.getResponseCode() > 299 ) {
                handleError( con );
            }
            
            // Get object id list from response
            byte[] response = readResponse( con, null );
            
            l4j.debug( "Response: " + new String( response, "UTF-8" ) );
            con.disconnect();
            
            return parseObjectListWithMetadata( response );

        } catch( MalformedURLException e ) {
            throw new EsuException( "Invalid URL", e );
        } catch (IOException e) {
            throw new EsuException( "Error connecting to server", e );
        } catch (GeneralSecurityException e) {
            throw new EsuException( "Error computing request signature", e );
        }
    }


    /**
     * Returns the list of user metadata tags assigned to the object.
     * @param id the object whose metadata tags to list
     * @return the list of user metadata tags assigned to the object
     */
    public MetadataTags listUserMetadataTags(Identifier id) {
        try {
        	String resource = getResourcePath( context, id ) + "?metadata/tags";
            URL u = buildUrl( resource );
            HttpURLConnection con = (HttpURLConnection) u.openConnection();

            // Build headers
            Map<String,String> headers = new HashMap<String,String>();

            headers.put( "x-emc-uid", uid );
            
            // Add date
            TimeZone tz = TimeZone.getTimeZone( "GMT" );
            l4j.debug( "TZ: " + tz );
            HEADER_FORMAT.setTimeZone( tz );
            String dateHeader = HEADER_FORMAT.format( new Date() );
            l4j.debug( "Date: " + dateHeader );
            headers.put( "Date", dateHeader );

            // Sign request
            signRequest( con, "GET", resource, headers );
            
            con.connect();
            
            // Check response
            if( con.getResponseCode() > 299 ) {
                handleError( con );
            }
            
            // Get the user metadata tags out of x-emc-listable-tags and
            // x-emc-tags
            MetadataTags tags = new MetadataTags();
           
            readTags( tags, con.getHeaderField( "x-emc-listable-tags" ), true );
            readTags( tags, con.getHeaderField( "x-emc-tags" ), false );
            
            con.disconnect();
            return tags;

        } catch( MalformedURLException e ) {
            throw new EsuException( "Invalid URL", e );
        } catch (IOException e) {
            throw new EsuException( "Error connecting to server", e );
        } catch (GeneralSecurityException e) {
            throw new EsuException( "Error computing request signature", e );
        }
    }


    /**
     * Lists the versions of an object.
     * @param id the object whose versions to list.
     * @return The list of versions of the object.  If the object does
     * not have any versions, the array will be empty.
     */
    public List<Identifier> listVersions(Identifier id) {
        try {
        	String resource = getResourcePath( context, id ) + "?versions";
            URL u = buildUrl( resource );
            HttpURLConnection con = (HttpURLConnection) u.openConnection();

            // Build headers
            Map<String,String> headers = new HashMap<String,String>();

            headers.put( "x-emc-uid", uid );

            // Add date
            TimeZone tz = TimeZone.getTimeZone( "GMT" );
            l4j.debug( "TZ: " + tz );
            HEADER_FORMAT.setTimeZone( tz );
            String dateHeader = HEADER_FORMAT.format( new Date() );
            l4j.debug( "Date: " + dateHeader );
            headers.put( "Date", dateHeader );

            // Sign request
            signRequest( con, "GET", resource, headers );
            
            con.connect();
            
            // Check response
            if( con.getResponseCode() > 299 ) {
                handleError( con );
            }
            
            // Get object id list from response
            byte[] response = readResponse( con, null );
            
            l4j.debug( "Response: " + new String( response, "UTF-8" ) );
            
            con.disconnect();
            return parseObjectList( response );

        } catch( MalformedURLException e ) {
            throw new EsuException( "Invalid URL", e );
        } catch (IOException e) {
            throw new EsuException( "Error connecting to server", e );
        } catch (GeneralSecurityException e) {
            throw new EsuException( "Error computing request signature", e );
        }
    }


    /**
     * Executes a query for objects matching the specified XQuery string.
     * @param xquery the XQuery string to execute against the cloud.
     * @return the list of objects matching the query.  If no objects
     * are found, the array will be empty.
     */
    public List<Identifier> queryObjects(String xquery) {
        try {
            String resource = context + "/objects";
            URL u = buildUrl( resource );
            HttpURLConnection con = (HttpURLConnection) u.openConnection();

            // Build headers
            Map<String,String> headers = new HashMap<String,String>();

            headers.put( "x-emc-uid", uid );
            
            // Add query
            if( xquery != null ) {
                headers.put( "x-emc-xquery", xquery );
            } else {
                throw new EsuException( "Query cannot be null" );
            }

            // Add date
            TimeZone tz = TimeZone.getTimeZone( "GMT" );
            l4j.debug( "TZ: " + tz );
            HEADER_FORMAT.setTimeZone( tz );
            String dateHeader = HEADER_FORMAT.format( new Date() );
            l4j.debug( "Date: " + dateHeader );
            headers.put( "Date", dateHeader );

            // Sign request
            signRequest( con, "GET", resource, headers );
            
            con.connect();
            
            // Check response
            if( con.getResponseCode() > 299 ) {
                handleError( con );
            }
            
            // Get object id list from response
            byte[] response = readResponse( con, null );
            
            l4j.debug( "Response: " + new String( response, "UTF-8" ) );
            
            con.disconnect();
            return parseObjectList( response );

        } catch( MalformedURLException e ) {
            throw new EsuException( "Invalid URL", e );
        } catch (IOException e) {
            throw new EsuException( "Error connecting to server", e );
        } catch (GeneralSecurityException e) {
            throw new EsuException( "Error computing request signature", e );
        }
    }


    /**
     * Reads an object's content.
     * @param id the identifier of the object whose content to read.
     * @param extent the portion of the object data to read.  Optional.
     * Default is null to read the entire object.
     * @param buffer the buffer to use to read the extent.  Must be large
     * enough to read the response or an error will be thrown.  If null,
     * a buffer will be allocated to hold the response data.  If you pass
     * a buffer that is larger than the extent, only extent.getSize() bytes
     * will be valid.
     * @return the object data read as a byte array.
     */
    public byte[] readObject(Identifier id, Extent extent, byte[] buffer ) {
        try {
        	String resource = getResourcePath( context, id );
            URL u = buildUrl( resource );
            HttpURLConnection con = (HttpURLConnection) u.openConnection();

            // Build headers
            Map<String,String> headers = new HashMap<String,String>();

            headers.put( "x-emc-uid", uid );

            // Add date
            TimeZone tz = TimeZone.getTimeZone( "GMT" );
            l4j.debug( "TZ: " + tz );
            HEADER_FORMAT.setTimeZone( tz );
            String dateHeader = HEADER_FORMAT.format( new Date() );
            l4j.debug( "Date: " + dateHeader );
            headers.put( "Date", dateHeader );

             //Add extent if needed
            if( extent != null && !extent.equals( Extent.ALL_CONTENT ) ) {
                    long end = extent.getOffset() + (extent.getSize()-1);
                    headers.put( "Range", "bytes=" + extent.getOffset() + "-" + end ); 
            }

            // Sign request
            signRequest( con, "GET", resource, headers );

            con.connect();

            // Check response
            if( con.getResponseCode() > 299 ) {
                handleError( con );
            }

            // The requested content is in the response body.
            byte[] data = readResponse( con, buffer );
            con.disconnect();
            return data;

        } catch( MalformedURLException e ) {
            throw new EsuException( "Invalid URL", e );
        } catch (IOException e) {
            throw new EsuException( "Error connecting to server", e );
        } catch (GeneralSecurityException e) {
            throw new EsuException( "Error computing request signature", e );
        }

    }

    /**
     * Reads an object's content and returns an InputStream to read the content.
     * Since the input stream is linked to the HTTP connection, it is imperative
     * that you close the input stream as soon as you are done with the stream
     * to release the underlying connection.
     * @param id the identifier of the object whose content to read.
     * @param extent the portion of the object data to read.  Optional.
     * Default is null to read the entire object.
     * @return an InputStream to read the object data.
     */
	public InputStream readObjectStream(Identifier id, Extent extent) {
        try {
        	String resource = getResourcePath( context, id );
            URL u = buildUrl( resource );
            HttpURLConnection con = (HttpURLConnection) u.openConnection();

            // Build headers
            Map<String,String> headers = new HashMap<String,String>();

            headers.put( "x-emc-uid", uid );

            // Add date
            TimeZone tz = TimeZone.getTimeZone( "GMT" );
            l4j.debug( "TZ: " + tz );
            HEADER_FORMAT.setTimeZone( tz );
            String dateHeader = HEADER_FORMAT.format( new Date() );
            l4j.debug( "Date: " + dateHeader );
            headers.put( "Date", dateHeader );

             //Add extent if needed
            if( extent != null && !extent.equals( Extent.ALL_CONTENT ) ) {
                    long end = extent.getOffset() + (extent.getSize()-1);
                    headers.put( "Range", "bytes=" + extent.getOffset() + "-" + end ); 
            }

            // Sign request
            signRequest( con, "GET", resource, headers );

            con.connect();

            // Check response
            if( con.getResponseCode() > 299 ) {
                handleError( con );
            }

            return new HttpInputStreamWrapper( con.getInputStream(), con );

        } catch( MalformedURLException e ) {
            throw new EsuException( "Invalid URL", e );
        } catch (IOException e) {
            throw new EsuException( "Error connecting to server", e );
        } catch (GeneralSecurityException e) {
            throw new EsuException( "Error computing request signature", e );
        }
	}

    /**
     * Updates an object in the cloud and optionally its metadata and ACL.
     * @param id The ID of the object to update
     * @param acl Access control list for the new object. Optional, default
     * is NULL to leave the ACL unchanged.
     * @param metadata Metadata list for the new object.  Optional,
     * default is NULL for no changes to the metadata.
     * @param data The new contents of the object.  May be appended
     * to later. Optional, default is NULL (no content changes).
     * @param extent portion of the object to update.  May be null to indicate
     * the whole object is to be replaced.  If not null, the extent size must
     * match the data size.
     * @param mimeType the MIME type of the content.  Optional, 
     * may be null.  If data is non-null and mimeType is null, the MIME
     * type will default to application/octet-stream.
     * @throws EsuException if the request fails.
     */
    public void updateObject( Identifier id, Acl acl,
            MetadataList metadata, Extent extent, byte[] data,
            String mimeType ) {
        updateObjectFromSegment( id, acl, metadata, extent, 
                data==null?null:new BufferSegment( data ), mimeType );
    }
        
    /**
     * Updates an object in the cloud and optionally its metadata and ACL.
     * @param id The ID of the object to update
     * @param acl Access control list for the new object. Optional, default
     * is NULL to leave the ACL unchanged.
     * @param metadata Metadata list for the new object.  Optional,
     * default is NULL for no changes to the metadata.
     * @param data The new contents of the object.  May be appended
     * to later. Optional, default is NULL (no content changes).
     * @param extent portion of the object to update.  May be null to indicate
     * the whole object is to be replaced.  If not null, the extent size must
     * match the data size.
     * @param mimeType the MIME type of the content.  Optional, 
     * may be null.  If data is non-null and mimeType is null, the MIME
     * type will default to application/octet-stream.
     * @throws EsuException if the request fails.
     */
    public void updateObjectFromSegment( Identifier id, Acl acl,
            MetadataList metadata, Extent extent, BufferSegment data,
            String mimeType ) {
        try {
        	String resource = getResourcePath( context, id );
            URL u = buildUrl( resource );
            HttpURLConnection con = (HttpURLConnection) u.openConnection();

            // Build headers
            Map<String,String> headers = new HashMap<String,String>();

            // Figure out the mimetype
            if( mimeType == null ) {
                mimeType = "application/octet-stream";
            }

            headers.put( "Content-Type", mimeType );
            headers.put( "x-emc-uid", uid );

            // Process metadata
            if( metadata != null ) {
                processMetadata( metadata, headers );
            }

            l4j.debug( "meta " + headers.get( "x-emc-meta" ) );

            // Add acl
            if( acl != null ) {
                processAcl( acl, headers );
            }

            //Add extent if needed
            if( extent != null && !extent.equals( Extent.ALL_CONTENT ) ) {
                    long end = extent.getOffset() + (extent.getSize() - 1);
                    headers.put( "Range", "bytes=" + extent.getOffset() + "-" + end ); 
            }

            // Process data
            if( data == null ) {
                data = new BufferSegment( new byte[0] );
            }
            con.setFixedLengthStreamingMode( data.getSize() );
            con.setDoOutput( true );


            // Add date
            TimeZone tz = TimeZone.getTimeZone( "GMT" );
            l4j.debug( "TZ: " + tz );
            HEADER_FORMAT.setTimeZone( tz );
            String dateHeader = HEADER_FORMAT.format( new Date() );
            l4j.debug( "Date: " + dateHeader );
            headers.put( "Date", dateHeader );

            // Sign request
            signRequest( con, "PUT", resource, headers );

            con.connect();

            // post data
            OutputStream out = null;
            try {
                out = con.getOutputStream();
                out.write( data.getBuffer(), data.getOffset(), data.getSize() );
                out.close();
            } catch( IOException e ) {
                silentClose( out );
                con.disconnect();
                throw new EsuException( "Error posting data", e );
            }

            // Check response
            if( con.getResponseCode() > 299 ) {
                handleError( con );
            }
            con.disconnect();
        } catch( MalformedURLException e ) {
            throw new EsuException( "Invalid URL", e );
        } catch (IOException e) {
            throw new EsuException( "Error connecting to server", e );
        } catch (GeneralSecurityException e) {
            throw new EsuException( "Error computing request signature", e );
        }

    }
    
    /**
     * Updates an object in the cloud.
     * @param id The ID of the object to update
     * @param acl Access control list for the new object. Optional, default
     * is NULL to leave the ACL unchanged.
     * @param metadata Metadata list for the new object.  Optional,
     * default is NULL for no changes to the metadata.
     * @param data The updated data to apply to the object.  Requred.  Note
     * that the input stream is NOT closed at the end of the request.
     * @param extent portion of the object to update.  May be null to indicate
     * the whole object is to be replaced.  If not null, the extent size must
     * match the data size.
     * @param length The length of the stream in bytes.  If the stream
     * is longer than the length, only length bytes will be written.  If
     * the stream is shorter than the length, an error will occur.
     * @param mimeType the MIME type of the content.  Optional, 
     * may be null.  If data is non-null and mimeType is null, the MIME
     * type will default to application/octet-stream.
     * @throws EsuException if the request fails.
     */
    public void updateObjectFromStream( Identifier id, Acl acl, MetadataList metadata, 
            Extent extent, InputStream data, int length, String mimeType ) {
        try {
        	String resource = getResourcePath( context, id );
            URL u = buildUrl( resource );
            HttpURLConnection con = (HttpURLConnection) u.openConnection();

            // Build headers
            Map<String,String> headers = new HashMap<String,String>();

            // Figure out the mimetype
            if( mimeType == null ) {
                mimeType = "application/octet-stream";
            }

            headers.put( "Content-Type", mimeType );
            headers.put( "x-emc-uid", uid );

            // Process metadata
            if( metadata != null ) {
                processMetadata( metadata, headers );
            }

            l4j.debug( "meta " + headers.get( "x-emc-meta" ) );

            // Add acl
            if( acl != null ) {
                processAcl( acl, headers );
            }

            //Add extent if needed
            if( extent != null && !extent.equals( Extent.ALL_CONTENT ) ) {
                    long end = extent.getOffset() + (extent.getSize() - 1);
                    headers.put( "Range", "bytes=" + extent.getOffset() + "-" + end ); 
            }

            con.setFixedLengthStreamingMode( length );
            con.setDoOutput( true );


            // Add date
            TimeZone tz = TimeZone.getTimeZone( "GMT" );
            l4j.debug( "TZ: " + tz );
            HEADER_FORMAT.setTimeZone( tz );
            String dateHeader = HEADER_FORMAT.format( new Date() );
            l4j.debug( "Date: " + dateHeader );
            headers.put( "Date", dateHeader );

            // Sign request
            signRequest( con, "PUT", resource, headers );

            con.connect();

            // post data
            OutputStream out = null;
            byte[] buffer = new byte[128*1024];
            int read = 0;
            try {
                out = con.getOutputStream();
                while( read < length ) {
                	int c = data.read( buffer );
                	if( c == -1 ) {
                		throw new EsuException( "EOF encountered reading data stream" );
                	}
                	out.write( buffer, 0, c );
                	read += c;
                }
                out.close();
            } catch( IOException e ) {
                silentClose( out );
                con.disconnect();
                throw new EsuException( "Error posting data", e );
            }

            // Check response
            if( con.getResponseCode() > 299 ) {
                handleError( con );
            }
            con.disconnect();
        } catch( MalformedURLException e ) {
            throw new EsuException( "Invalid URL", e );
        } catch (IOException e) {
            throw new EsuException( "Error connecting to server", e );
        } catch (GeneralSecurityException e) {
            throw new EsuException( "Error computing request signature", e );
        }
    	
    }

    
    
    /**
     * Writes the metadata into the object. If the tag does not exist, it is 
     * created and set to the corresponding value. If the tag exists, the 
     * existing value is replaced.
     * @param id the identifier of the object to update
     * @param metadata metadata to write to the object.
     */
    public void setUserMetadata( Identifier id, MetadataList metadata ) {
        try {
        	String resource = getResourcePath( context, id ) + "?metadata/user";
            URL u = buildUrl( resource );
            HttpURLConnection con = (HttpURLConnection) u.openConnection();

            // Build headers
            Map<String,String> headers = new HashMap<String,String>();

            headers.put( "x-emc-uid", uid );

            // Process metadata
            if( metadata != null ) {
                processMetadata( metadata, headers );
            }

            // Add date
            TimeZone tz = TimeZone.getTimeZone( "GMT" );
            l4j.debug( "TZ: " + tz );
            HEADER_FORMAT.setTimeZone( tz );
            String dateHeader = HEADER_FORMAT.format( new Date() );
            l4j.debug( "Date: " + dateHeader );
            headers.put( "Date", dateHeader );

            // Sign request
            signRequest( con, "POST", resource, headers );

            con.connect();

            // Check response
            if( con.getResponseCode() > 299 ) {
                handleError( con );
            }
            
            // Read the response to complete the request (will be empty)
            InputStream in = con.getInputStream();
            in.close();
            con.disconnect();
        } catch( MalformedURLException e ) {
            throw new EsuException( "Invalid URL", e );
        } catch (IOException e) {
            throw new EsuException( "Error connecting to server", e );
        } catch (GeneralSecurityException e) {
            throw new EsuException( "Error computing request signature", e );
        }
        
    }
    
    /**
     * Sets (overwrites) the ACL on the object.
     * @param id the identifier of the object to change the ACL on.
     * @param acl the new ACL for the object.
     */
    public void setAcl( Identifier id, Acl acl ) {
        try {
        	String resource = getResourcePath( context, id ) + "?acl";
            URL u = buildUrl( resource );
            HttpURLConnection con = (HttpURLConnection) u.openConnection();

            // Build headers
            Map<String,String> headers = new HashMap<String,String>();

            headers.put( "x-emc-uid", uid );


            // Add acl
            if( acl != null ) {
                processAcl( acl, headers );
            }

            // Add date
            TimeZone tz = TimeZone.getTimeZone( "GMT" );
            l4j.debug( "TZ: " + tz );
            HEADER_FORMAT.setTimeZone( tz );
            String dateHeader = HEADER_FORMAT.format( new Date() );
            l4j.debug( "Date: " + dateHeader );
            headers.put( "Date", dateHeader );

            // Sign request
            signRequest( con, "POST", resource, headers );

            con.connect();

            // Check response
            if( con.getResponseCode() > 299 ) {
                handleError( con );
            }
            
            // Read the response to complete the request (will be empty)
            InputStream in = con.getInputStream();
            in.close();
            con.disconnect();
        } catch( MalformedURLException e ) {
            throw new EsuException( "Invalid URL", e );
        } catch (IOException e) {
            throw new EsuException( "Error connecting to server", e );
        } catch (GeneralSecurityException e) {
            throw new EsuException( "Error computing request signature", e );
        }

    }



    /**
     * Creates a new immutable version of an object.
     * @param id the object to version
     * @return the id of the newly created version
     */
    public ObjectId versionObject(Identifier id) {
        try {
        	String resource = getResourcePath( context, id ) + "?versions";
            URL u = buildUrl( resource );
            HttpURLConnection con = (HttpURLConnection) u.openConnection();

            // Build headers
            Map<String,String> headers = new HashMap<String,String>();

            headers.put( "x-emc-uid", uid );

            // Add date
            TimeZone tz = TimeZone.getTimeZone( "GMT" );
            l4j.debug( "TZ: " + tz );
            HEADER_FORMAT.setTimeZone( tz );
            String dateHeader = HEADER_FORMAT.format( new Date() );
            l4j.debug( "Date: " + dateHeader );
            headers.put( "Date", dateHeader );

            // Sign request
            signRequest( con, "POST", resource, headers );
            
            con.connect();

            // Check response
            if( con.getResponseCode() > 299 ) {
                handleError( con );
            }

            // The new object ID is returned in the location response header
            String location = con.getHeaderField( "location" );

            // Parse the value out of the URL
            Matcher m = OBJECTID_EXTRACTOR.matcher( location );
            if( m.find() ) {
                String vid = m.group( 1 );
                l4j.debug( "vId: " + vid );
                return new ObjectId( vid );
            } else {
                throw new EsuException( "Could not find ObjectId in " + location );
            }
            
       } catch( MalformedURLException e ) {
            throw new EsuException( "Invalid URL", e );
        } catch (IOException e) {
            throw new EsuException( "Error connecting to server", e );
        } catch (GeneralSecurityException e) {
            throw new EsuException( "Error computing request signature", e );
        }
    }



    /**
     * Gets the context root of the REST api.  By default this is /rest.
     * @return the context
     */
    public String getContext() {
        return context;
    }



    /**
     * Overrides the default context root of the REST api.
     * @param context the context to set
     */
    public void setContext(String context) {
        this.context = context;
    }



    /**
     * Returns the protocol being used (http or https).
     * @return the proto
     */
    public String getProtocol() {
        return proto;
    }



    /**
     * Overrides the protocol selection.  By default, https will be used for
     * port 443.  Http will be used otherwise
     * @param proto the proto to set
     */
    public void setProtocol(String proto) {
        this.proto = proto;
    }
    
    
    
    /**
     * Lists the contents of a directory.
     * @param path the path to list.  Must be a directory.
     * @return the directory entries in the directory.
     */
	@SuppressWarnings("unchecked")
	public List<DirectoryEntry> listDirectory(ObjectPath path) {
		if( !path.isDirectory() ) {
			throw new EsuException( "listDirectory must be called with a directory path" );
		}
		
		// Read out the directory's contents
		byte[] dir = readObject( path, null, null );
		
		// Parse
        List<DirectoryEntry> objs = new ArrayList<DirectoryEntry>();
        
        // Use JDOM to parse the XML
        SAXBuilder sb = new SAXBuilder();
        try {
            Document d = sb.build( new ByteArrayInputStream( dir ) );
            
            // The ObjectID element is part of a namespace so we need to use
            // the namespace to identify the elements.
            Namespace esuNs = Namespace.getNamespace( "http://www.emc.com/cos/" );

            List children = d.getRootElement().getChild( "DirectoryList", esuNs ).getChildren( "DirectoryEntry", esuNs );
            l4j.debug( "Found " + children.size() + " objects" );
            for( Iterator i=children.iterator(); i.hasNext(); ) {
                Object o = i.next();
                if( o instanceof Element ) {
                	DirectoryEntry de = new DirectoryEntry();
                	de.setId( new ObjectId(
                			((Element) o).getChildText( "ObjectID", esuNs) ) );
                	String name = ((Element) o).getChildText( "Filename", esuNs );
                	String type = ((Element) o).getChildText( "FileType", esuNs );
                	
                	name = path.toString() + name;
                	if( "directory".equals( type ) ) {
                		name += "/";                		
                	}
                	de.setPath( new ObjectPath( name ) );
                	de.setType( type );
                	
                    objs.add( de );
                } else {
                    l4j.debug( o + " is not an Element!" );
                }
            }
            
        } catch (JDOMException e) {
            throw new EsuException( "Error parsing response", e );
        } catch (IOException e) {
            throw new EsuException( "Error reading response", e );
        }

        return objs;

		
	}
    


	public ObjectMetadata getAllMetadata( Identifier id ) {
        try {
        	String resource = getResourcePath( context, id );
            URL u = buildUrl( resource );
            HttpURLConnection con = (HttpURLConnection) u.openConnection();

            // Build headers
            Map<String,String> headers = new HashMap<String,String>();

            headers.put( "x-emc-uid", uid );

            // Add date
            TimeZone tz = TimeZone.getTimeZone( "GMT" );
            l4j.debug( "TZ: " + tz );
            HEADER_FORMAT.setTimeZone( tz );
            String dateHeader = HEADER_FORMAT.format( new Date() );
            l4j.debug( "Date: " + dateHeader );
            headers.put( "Date", dateHeader );

            // Sign request
            signRequest( con, "HEAD", resource, headers );

            con.connect();

            // Check response
            if( con.getResponseCode() > 299 ) {
                handleError( con );
            }

            // Parse return headers.  User grants are in x-emc-useracl and
            // group grants are in x-emc-groupacl
            Acl acl = new Acl();
            readAcl( acl, con.getHeaderField( "x-emc-useracl" ), Grantee.GRANT_TYPE.USER );              
            readAcl( acl, con.getHeaderField( "x-emc-groupacl" ), Grantee.GRANT_TYPE.GROUP );

            // Parse return headers.  Regular metadata is in x-emc-meta and
            // listable metadata is in x-emc-listable-meta
            MetadataList meta = new MetadataList();
            readMetadata( meta, con.getHeaderField( "x-emc-meta" ), false );           
            readMetadata( meta, con.getHeaderField( "x-emc-listable-meta" ), true );
            
            ObjectMetadata om = new ObjectMetadata();
            om.setAcl( acl );
            om.setMetadata( meta );
            om.setMimeType( con.getContentType() );
            
            return om;

        } catch( MalformedURLException e ) {
            throw new EsuException( "Invalid URL", e );
        } catch (IOException e) {
            throw new EsuException( "Error connecting to server", e );
        } catch (GeneralSecurityException e) {
            throw new EsuException( "Error computing request signature", e );
        }
	}



    
    /////////////////////
    // Private Methods //
    /////////////////////

    /**
     * Builds a new URL to the given resource
     */
    private URL buildUrl(String resource) throws MalformedURLException {
        return new URL( proto + "://" + host + ":" + port + resource );
    }

    /**
     * Helper method that closes a stream ignoring errors.
     * @param out the OutputStream to close
     */
    private void silentClose(OutputStream out) {
        if( out == null ) {
            return;
        }
        try {
            out.close();
        } catch (IOException e) {
            // ignore
        }
    }

    /**
     * Iterates through the given metadata and adds the appropriate metadata
     * headers to the request.
     * @param metadata the metadata to add
     * @param headers the map of request headers.
     */
    private void processMetadata(MetadataList metadata,
            Map<String, String> headers) {

        StringBuffer listable = new StringBuffer();
        StringBuffer nonListable = new StringBuffer();

        l4j.debug( "Processing " + metadata.count() + " metadata entries" );

        for( Iterator<Metadata> i = metadata.iterator(); i.hasNext(); ) {
            Metadata meta = i.next();
            if( meta.isListable() ) {
                if( listable.length() > 0 ) {
                    listable.append( ", " );
                }
                listable.append( formatTag( meta ) );
            } else {
                if( nonListable.length() > 0 ) {
                    nonListable.append( ", " );
                }
                nonListable.append( formatTag( meta ) );
            }
        }

        // Only set the headers if there's data
        if( listable.length() > 0 ) {
            headers.put( "x-emc-listable-meta", listable.toString() );
        }
        if( nonListable.length() > 0 ) {
            headers.put( "x-emc-meta", nonListable.toString() );
        }

    }



    /**
     * Formats a tag value for passing in the header.
     */
    private Object formatTag(Metadata meta) {
        // strip commas and newlines for now.
        String fixed = meta.getValue().replace( ",", "" );
        fixed = fixed.replace( "\n", "" );
        return meta.getName() + "=" + fixed;
    }


    /**
     * Enumerates the given ACL and creates the appropriate request headers.
     * @param acl the ACL to enumerate
     * @param headers the set of request headers.
     */
    private void processAcl(Acl acl, Map<String, String> headers) {
        StringBuffer userGrants = new StringBuffer();
        StringBuffer groupGrants = new StringBuffer();

        for( Iterator<Grant> i = acl.iterator(); i.hasNext(); ) {
            Grant grant = i.next();
            if( grant.getGrantee().getType() == Grantee.GRANT_TYPE.USER ) {
                if( userGrants.length() > 0 ) {
                    userGrants.append( "," );
                }
                userGrants.append( grant.toString() );
            } else {
                if( groupGrants.length() > 0 ) {
                    groupGrants.append( "," );
                }
                groupGrants.append( grant.toString() );
            }
        }

        headers.put( "x-emc-useracl", userGrants.toString() );
        headers.put( "x-emc-groupacl",  groupGrants.toString() );
    }


    /**
     * Generates the HMAC-SHA1 signature used to authenticate the request using
     * the Java security APIs.
     * @param con the connection object
     * @param method the HTTP method used
     * @param resource the resource path
     * @param headers the HTTP headers for the request
     * @throws IOException if character data cannot be encoded.
     * @throws GeneralSecurityException If errors occur generating the HMAC-SHA1
     * signature.
     */
    private void signRequest(HttpURLConnection con, String method,
            String resource, Map<String, String> headers ) throws IOException, GeneralSecurityException {
        // Build the string to hash.
        StringBuffer hashStr = new StringBuffer();
        hashStr.append( method + "\n" );

        // If content type exists, add it.  Otherwise add a blank line.
        if( headers.containsKey( "Content-Type" ) ) {
            l4j.debug( "Content-Type: " + headers.get( "Content-Type" ) );
            hashStr.append( headers.get( "Content-Type" ).toLowerCase() + "\n" );
        } else {
            hashStr.append( "\n" );
        }

        // If the range header exists, add it.  Otherwise add a blank line.
        if( headers.containsKey( "Range" ) ) {
            hashStr.append( headers.get( "Range" ) + "\n" );
        } else {
            hashStr.append( "\n" );
        }

        // Add the current date and the resource.
        hashStr.append( headers.get( "Date" ) + "\n" +
                URLDecoder.decode(resource, "UTF-8").toLowerCase() + "\n" );

        // Do the 'x-emc' headers.  The headers must be hashed in alphabetic
        // order and the values must be stripped of whitespace and newlines.
        List<String> keys = new ArrayList<String>(); 
        Map<String,String> newheaders = new HashMap<String,String>();

        // Extract the keys and values
        for( Iterator<String> i = headers.keySet().iterator(); i.hasNext(); ) {
            String key = i.next();
            if( key.indexOf( "x-emc" ) == 0 ) {
                keys.add( key.toLowerCase() );
                newheaders.put( key.toLowerCase(), headers.get( key ).replace( "\n", "" ) );
            }
        }

        // Sort the keys and add the headers to the hash string.
        Collections.sort( keys );
        boolean first = true;
        for( Iterator<String> i = keys.iterator(); i.hasNext(); ) {
            String key = i.next();
            if( !first ) {
                hashStr.append( "\n" );
            } else {
                first = false;
            }
            //this.trace( "xheader: " . k . "." . newheaders[k] );
            hashStr.append( key + ':' + newheaders.get( key ) );
        }


        String hashOut = sign( hashStr.toString() );
        
        // Can set all the headers, etc now.
        for( Iterator<String> i = headers.keySet().iterator(); i.hasNext(); ) {
            String name = i.next();
            con.setRequestProperty( name, headers.get( name ) );
        }

        // Set the signature header
        con.setRequestProperty( "x-emc-signature", hashOut );

        // Set the method.
        con.setRequestMethod( method );

    }
    
    private String sign( String input ) throws NoSuchAlgorithmException, InvalidKeyException, IllegalStateException, UnsupportedEncodingException {
        // Compute the signature hash
        Mac mac = Mac.getInstance( "HmacSHA1" );
        SecretKeySpec key = new SecretKeySpec( secret, "HmacSHA1" );
        mac.init( key );
        l4j.debug( "Hashing: \n" + input.toString() );

        byte[] hashData = mac.doFinal( input.toString().getBytes( "UTF-8" ) );

        // Encode the hash in Base64.
        String hashOut = new String( Base64.encodeBase64( hashData ), "UTF-8" );
        
        l4j.debug( "Hash: " + hashOut );

        return hashOut;
    }


    /**
     * Attempts to generate a reasonable error message from from a request.  If
     * the error is from the web service, there should be a message and code
     * in the response body encapsulated in XML.
     * 
     * @param con the connection from the failed request.
     */
    private void handleError( HttpURLConnection con ) {
        int http_code = 0;
        // Try and read the response body.
        try {
            http_code = con.getResponseCode();
            byte[] response = readResponse( con, null );
            l4j.debug( "Error response: " + new String( response, "UTF-8" ) );
            SAXBuilder sb = new SAXBuilder();
            
            Document d = sb.build( new ByteArrayInputStream( response ) );
            
            String code = d.getRootElement().getChildText( "Code" );
            String message = d.getRootElement().getChildText( "Message" );
            
            if( code == null && message == null ) {
                // not an error from ESU
                throw new EsuException( con.getResponseMessage(), http_code );
            }
            
            l4j.debug( "Error: " + code + " message: " + message );
            throw new EsuException( message, http_code, Integer.parseInt( code ) );
            
        } catch( IOException e ) {
            l4j.debug( "Could not read error response body", e );
            // Just throw what we know from the response
            try {
                throw new EsuException( con.getResponseMessage(), http_code );
            } catch (IOException e1) {
                l4j.warn( "Could not get response code/message!", e );
                throw new EsuException( "Could not get response code", e, http_code );
            }
        } catch (JDOMException e) {
            try {
                l4j.debug( "Could not parse response body for " + 
                        http_code + ": " + con.getResponseMessage(),
                        e );
                throw new EsuException( "Could not parse response body for " + 
                        http_code + ": " + con.getResponseMessage(),
                        e, http_code );
            } catch (IOException e1) {
                throw new EsuException( "Could not parse response body", e1, http_code );
            }
            
        }
        
    }

    /**
     * Reads the response body and returns it in a byte array.
     * 
     * @param con the HTTP connection
     * @param buffer The buffer to use to read the response.  The response
     * buffer must be large enough to read the entire response or an error will
     * be thrown.
     * @return the byte array containing the response body.  Note that if you 
     * pass in a buffer, this will the same buffer object.  Be sure to check
     * the content length to know what data in the buffer is valid 
     * (from zero to contentLength).
     * @throws IOException if reading the response stream fails.
     */
    private byte[] readResponse( HttpURLConnection con, byte[] buffer ) throws IOException {
        InputStream in = null;
        if( con.getResponseCode() > 299 ) {
            in = con.getErrorStream();
            if( in == null ) {
                in = con.getInputStream();
            }
        } else {
            in = con.getInputStream();
        }
        if( in == null ) {
            // could not get stream
            return new byte[0];
        }
        try {
            byte[] output;
            int contentLength = con.getContentLength();
            // If we know the content length, read it directly into a buffer.
            if( contentLength != -1 ) {
                if( buffer != null && buffer.length < con.getContentLength() ) {
                    throw new EsuException( "The response buffer was not long enough to hold the response: " + buffer.length + "<" + con.getContentLength() );                
                }
                if( buffer != null ) {
                    output = buffer;
                } else {
                    output = new byte[con.getContentLength()];
                }
    
                int c = 0;
                while( c < contentLength ) {
                    int read = in.read( output, c, contentLength - c );
                    if( read == -1 ) {
                        // EOF!
                        throw new EOFException( "EOF reading response at position " + c + " size " + (contentLength-c) );
                    }
                    c += read;
                }
                
                return output;
            } else {
                l4j.debug( "Content length is unknown.  Buffering output." );
                // Else, use a ByteArrayOutputStream to collect the response.
                if( buffer == null ) {
                    buffer = new byte[4096];
                }
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                int c = 0;
                while( (c = in.read( buffer ) ) != -1 ) {
                    baos.write( buffer, 0, c );
                }
                baos.close();
                
                l4j.debug( "Buffered " + baos.size() + " response bytes" );
                
                return baos.toByteArray();
            }
        } finally {
            if( in != null ) {
                in.close();
            }
        }
    }

    
    /**
     * Parses the given header text and appends to the metadata list
     * @param meta the metadata list to append to
     * @param header the metadata header to parse
     * @param listable true if the header being parsed contains listable metadata.
     */
    private void readMetadata(MetadataList meta, String header, boolean listable) {
        if (header == null) {
            return;
        }

        String[] attrs = header.split(",");
        for (int i = 0; i < attrs.length; i++) {
            String[] nvpair = attrs[i].split("=", 2);
            String name = nvpair[0];
            String value = nvpair[1];

            name = name.trim();

            Metadata m = new Metadata(name, value, listable);
            l4j.debug("Meta: " + m);
            meta.addMetadata(m);
        }
    }

    /**
     * Enumerates the given list of metadata tags and sets the x-emc-tags
     * header.
     * @param tags the tag list to enumerate
     * @param headers the HTTP request headers
     */
    private void processTags(MetadataTags tags, Map<String, String> headers) {
        StringBuffer taglist = new StringBuffer();

        l4j.debug("Processing " + tags.count() + " metadata tag entries");

        for (Iterator<MetadataTag> i = tags.iterator(); i.hasNext();) {
            MetadataTag tag = i.next();
            if (taglist.length() > 0) {
                taglist.append(",");
            }
            taglist.append(tag.getName());
        }

        if (taglist.length() > 0) {
            headers.put("x-emc-tags", taglist.toString());
        }
    }

    /**
     * Parses the value of an ACL response header and builds an ACL
     * @param acl a reference to the ACL to append to
     * @param header the acl response header
     * @param type the type of Grantees in the header (user or group)
     */
    private void readAcl(Acl acl, String header, GRANT_TYPE type) {
        l4j.debug("readAcl: " + header);
        String[] grants = header.split(",");
        for (int i = 0; i < grants.length; i++) {
            String[] nvpair = grants[i].split("=", 2);
            String grantee = nvpair[0];
            String permission = nvpair[1];

            grantee = grantee.trim();

            // Currently, the server returns "FULL" instead of "FULL_CONTROL".
            // For consistency, change this to value use in the request
            if ("FULL".equals(permission)) {
                permission = Permission.FULL_CONTROL;
            }

            l4j.debug("grant: " + grantee + "." + permission + " (" + type
                    + ")");

            Grantee ge = new Grantee(grantee, type);
            Grant gr = new Grant(ge, permission);
            l4j.debug("Grant: " + gr);
            acl.addGrant(gr);
        }
    }



    /**
     * Parses an XML response and extracts the list of ObjectIDs.
     * @param response the response byte array to parse as XML
     * @return the list of object IDs contained in the response.
     */
    @SuppressWarnings("unchecked")
    private List<Identifier> parseObjectList( byte[] response ) {
        List<Identifier> objs = new ArrayList<Identifier>();
        
        // Use JDOM to parse the XML
        SAXBuilder sb = new SAXBuilder();
        try {
            Document d = sb.build( new ByteArrayInputStream( response ) );
            
            // The ObjectID element is part of a namespace so we need to use
            // the namespace to identify the elements.
            Namespace esuNs = Namespace.getNamespace( "http://www.emc.com/cos/" );

            List children = d.getRootElement().getChildren( "Object", esuNs );
            
            l4j.debug( "Found " + children.size() + " objects" );
            for( Iterator i=children.iterator(); i.hasNext(); ) {
                Object o = i.next();
                if( o instanceof Element ) {
                    Element objectIdElement = (Element)((Element)o).getChildren( "ObjectID", esuNs ).get(0);
                    ObjectId oid = new ObjectId( objectIdElement.getText() );
                    l4j.debug( oid.toString() );
                    objs.add( oid );
                } else {
                    l4j.debug( o + " is not an Element!" );
                }
            }
            
        } catch (JDOMException e) {
            throw new EsuException( "Error parsing response", e );
        } catch (IOException e) {
            throw new EsuException( "Error reading response", e );
        }

        return objs;
    }
    
    /**
     * Parses an XML response and extracts the list of ObjectIDs
     * and metadata.
     * @param response the response byte array to parse as XML
     * @return the list of object IDs contained in the response.
     */
    @SuppressWarnings("unchecked")
    private List<ObjectResult> parseObjectListWithMetadata( byte[] response ) {
        List<ObjectResult> objs = new ArrayList<ObjectResult>();
        
        // Use JDOM to parse the XML
        SAXBuilder sb = new SAXBuilder();
        try {
            Document d = sb.build( new ByteArrayInputStream( response ) );
            
            // The ObjectID element is part of a namespace so we need to use
            // the namespace to identify the elements.
            Namespace esuNs = Namespace.getNamespace( "http://www.emc.com/cos/" );

            List children = d.getRootElement().getChildren( "Object", esuNs );
            
            l4j.debug( "Found " + children.size() + " objects" );
            for( Iterator i=children.iterator(); i.hasNext(); ) {
                Object o = i.next();
                if( o instanceof Element ) {
                	Element e = (Element)o;
                	ObjectResult obj = new ObjectResult();
                    Element objectIdElement = e.getChild( "ObjectID", esuNs );
                    ObjectId oid = new ObjectId( objectIdElement.getText() );
                    obj.setId( oid );
                    
                    // next, get metadata
                    Element sMeta = e.getChild( "SystemMetadataList", esuNs );
                    Element uMeta = e.getChild( "UserMetadataList", esuNs );
                    obj.setMetadata( new MetadataList() );
                    
                    for( Iterator m = sMeta.getChildren( "Metadata" , esuNs ).iterator(); m.hasNext(); ) {
                        Element metaElement = (Element)m.next();
                        
                        String mName = metaElement.getChildText( "Name", esuNs );
                        String mValue = metaElement.getChildText( "Value", esuNs );
                        
                        obj.getMetadata().addMetadata( new Metadata( mName, mValue, false ) );
                    }
                    for( Iterator m = uMeta.getChildren( "Metadata" , esuNs ).iterator(); m.hasNext(); ) {
                        Element metaElement = (Element)m.next();
                        
                        String mName = metaElement.getChildText( "Name", esuNs );
                        String mValue = metaElement.getChildText( "Value", esuNs );
                        String mListable = metaElement.getChildText( "Listable", esuNs );
                        
                        obj.getMetadata().addMetadata( new Metadata( mName, mValue, "true".equals( mListable ) ) );
                    }
                    
                    objs.add( obj );
                } else {
                    l4j.debug( o + " is not an Element!" );
                }
            }
            
        } catch (JDOMException e) {
            throw new EsuException( "Error parsing response", e );
        } catch (IOException e) {
            throw new EsuException( "Error reading response", e );
        }

        return objs;
    }
    
    /**
     * Parses the given header and appends to the list of metadata tags.
     * @param tags the list of metadata tags to append to
     * @param header the header to parse
     * @param listable true if the metadata tags in the header are listable
     */
    private void readTags( MetadataTags tags, String header, boolean listable) {
        if (header == null) {
            return;
        }

        String[] attrs = header.split(",");
        for (int i = 0; i < attrs.length; i++) {
            String attr = attrs[i].trim();
            tags.addTag(new MetadataTag(attr, listable));
        }
    }

    /**
     * Gets the appropriate resource path depending on identifier
     * type.
     */
    private String getResourcePath( String ctx, Identifier id ) {
		if( id instanceof ObjectId ) {
			return ctx + "/objects/" + id;
		} else {
			return ctx + "/namespace" + id;
		}
	}



    /**
     * An Atmos user (UID) can construct a pre-authenticated URL to an 
     * object, which may then be used by anyone to retrieve the 
     * object (e.g., through a browser). This allows an Atmos user 
     * to let a non-Atmos user download a specific object. The 
     * entire object/file is read.
     * @param id the object to generate the URL for
     * @param expiration the expiration date of the URL
     * @return a URL that can be used to share the object's content
     */
    public URL getShareableUrl(Identifier id, Date expiration) {
        try {
            String resource = getResourcePath( context, id );
            String uidEnc = URLEncoder.encode( uid, "UTF-8" );
            
            StringBuffer sb = new StringBuffer();
            sb.append( "GET\n" );
            sb.append( resource.toLowerCase() + "\n" );
            sb.append( uid + "\n" );
            sb.append( ""+(expiration.getTime()/1000) );
            
            String signature = sign( sb.toString() );
            resource += "?uid=" + uidEnc + "&expires=" + (expiration.getTime()/1000) +
                "&signature=" + URLEncoder.encode( signature, "UTF-8" );
            
            URL u = buildUrl( resource );
            
            return u;
        } catch (UnsupportedEncodingException e) {
            throw new EsuException( "Unsupported encoding", e );
        } catch (InvalidKeyException e) {
            throw new EsuException( "Invalid secret key", e );
        } catch (NoSuchAlgorithmException e) {
            throw new EsuException( "Missing signature algorithm", e );
        } catch (IllegalStateException e) {
            throw new EsuException( "Error signing request", e );
        } catch (MalformedURLException e) {
            throw new EsuException( "Invalid URL format", e );
        }
    }



}
