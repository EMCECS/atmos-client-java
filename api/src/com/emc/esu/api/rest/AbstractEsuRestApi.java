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
//      POSSIBILITY OF SUCH DAMAGE.package com.emc.esu.api.rest;
package com.emc.esu.api.rest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
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
import com.emc.esu.api.Checksum;
import com.emc.esu.api.EsuApi;
import com.emc.esu.api.EsuException;
import com.emc.esu.api.Extent;
import com.emc.esu.api.Grant;
import com.emc.esu.api.Grantee;
import com.emc.esu.api.Identifier;
import com.emc.esu.api.Metadata;
import com.emc.esu.api.MetadataList;
import com.emc.esu.api.MetadataTag;
import com.emc.esu.api.MetadataTags;
import com.emc.esu.api.ObjectId;
import com.emc.esu.api.ObjectPath;
import com.emc.esu.api.ObjectResult;
import com.emc.esu.api.Permission;
import com.emc.esu.api.Grantee.GRANT_TYPE;
import com.emc.esu.api.ServiceInformation;

/**
 * Encapsulates common REST API functionality that is independant of 
 * the transport layer, e.g. signature generation and getShareableUrl.
 * @author Jason Cwik
 */
public abstract class AbstractEsuRestApi implements EsuApi {
    private static final DateFormat HEADER_FORMAT = new SimpleDateFormat(
            "EEE, d MMM yyyy HH:mm:ss z", Locale.ENGLISH);
    private static final Pattern OBJECTID_EXTRACTOR = Pattern
            .compile("/\\w+/objects/([0-9a-f]{44})");
    private static final Logger l4j = Logger.getLogger( AbstractEsuRestApi.class );

    protected String host;
    protected int port;
    protected String uid;
    protected byte[] secret;

    protected String context = "/rest";
    protected String proto;

    /**
     * Creates a new AbstractEsuRestApi
     * @param host the host running the web services
     * @param port the port number, e.g. 80 or 443
     * @param uid the web service UID
     * @param sharedSecret the UID's shared secret key
     */
    public AbstractEsuRestApi(String host, int port, String uid,
            String sharedSecret) {
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
     * Gets the context root of the REST api. By default this is /rest.
     * 
     * @return the context
     */
    public String getContext() {
        return context;
    }

    /**
     * Overrides the default context root of the REST api.
     * 
     * @param context the context to set
     */
    public void setContext(String context) {
        this.context = context;
    }

    /**
     * Returns the protocol being used (http or https).
     * 
     * @return the proto
     */
    public String getProtocol() {
        return proto;
    }

    /**
     * Overrides the protocol selection. By default, https will be used for port
     * 443. Http will be used otherwise
     * 
     * @param proto the proto to set
     */
    public void setProtocol(String proto) {
        this.proto = proto;
    }
    
    
    /**
     * Creates a new object in the cloud.
     * 
     * @param acl Access control list for the new object. May be null to use a
     *            default ACL
     * @param metadata Metadata for the new object. May be null for no metadata.
     * @param data The initial contents of the object. May be appended to later.
     *            May be null to create an object with no content.
     * @param mimeType the MIME type of the content. Optional, may be null. If
     *            data is non-null and mimeType is null, the MIME type will
     *            default to application/octet-stream.
     * @return Identifier of the newly created object.
     * @throws EsuException if the request fails.
     */
    public ObjectId createObject(Acl acl, MetadataList metadata, byte[] data,
            String mimeType) {
        return createObjectFromSegment(acl, metadata, data == null ? null
                : new BufferSegment(data), mimeType, null);
    }
    
    /**
     * Creates a new object in the cloud.
     * 
     * @param acl Access control list for the new object. May be null to use a
     *            default ACL
     * @param metadata Metadata for the new object. May be null for no metadata.
     * @param data The initial contents of the object. May be appended to later.
     *            May be null to create an object with no content.
     * @param mimeType the MIME type of the content. Optional, may be null. If
     *            data is non-null and mimeType is null, the MIME type will
     *            default to application/octet-stream.
     * @param checksum if not null, use the Checksum object to compute
     * the checksum for the create object request.  If appending
     * to the object with subsequent requests, use the same
     * checksum object for each request.
     * @return Identifier of the newly created object.
     * @throws EsuException if the request fails.
     */
    public ObjectId createObject(Acl acl, MetadataList metadata, byte[] data,
            String mimeType, Checksum checksum ) {
        return createObjectFromSegment(acl, metadata, data == null ? null
                : new BufferSegment(data), mimeType, checksum);
    }

    /**
     * Creates a new object in the cloud using a BufferSegment.
     * 
     * @param acl Access control list for the new object. May be null to use a
     *            default ACL
     * @param metadata Metadata for the new object. May be null for no metadata.
     * @param data The initial contents of the object. May be appended to later.
     *            May be null to create an object with no content.
     * @param mimeType the MIME type of the content. Optional, may be null. If
     *            data is non-null and mimeType is null, the MIME type will
     *            default to application/octet-stream.
     * @return Identifier of the newly created object.
     * @throws EsuException if the request fails.
     */
    public ObjectId createObjectFromSegment(Acl acl, MetadataList metadata,
            BufferSegment data, String mimeType) {
    	return createObjectFromSegment( acl, metadata, data, mimeType, null );
    }

    /**
     * Creates a new object in the cloud on the specified path.
     * 
     * @param path The path to create the object on.
     * @param acl Access control list for the new object. May be null to use a
     *            default ACL
     * @param metadata Metadata for the new object. May be null for no metadata.
     * @param data The initial contents of the object. May be appended to later.
     *            May be null to create an object with no content.
     * @param mimeType the MIME type of the content. Optional, may be null. If
     *            data is non-null and mimeType is null, the MIME type will
     *            default to application/octet-stream.
     * @return the ObjectId of the newly-created object for references by ID.
     * @throws EsuException if the request fails.
     */
    public ObjectId createObjectOnPath(ObjectPath path, Acl acl,
            MetadataList metadata, byte[] data, String mimeType) {
        return createObjectFromSegmentOnPath(path, acl, metadata,
                data == null ? null : new BufferSegment(data), mimeType, null);

    }
    
    /**
     * Creates a new object in the cloud on the specified path.
     * 
     * @param path The path to create the object on.
     * @param acl Access control list for the new object. May be null to use a
     *            default ACL
     * @param metadata Metadata for the new object. May be null for no metadata.
     * @param data The initial contents of the object. May be appended to later.
     *            May be null to create an object with no content.
     * @param mimeType the MIME type of the content. Optional, may be null. If
     *            data is non-null and mimeType is null, the MIME type will
     *            default to application/octet-stream.
     * @param checksum if not null, use the Checksum object to compute
     * the checksum for the create object request.  If appending
     * to the object with subsequent requests, use the same
     * checksum object for each request.
     * @return the ObjectId of the newly-created object for references by ID.
     * @throws EsuException if the request fails.
     */
    public ObjectId createObjectOnPath(ObjectPath path, Acl acl,
            MetadataList metadata, byte[] data, String mimeType, Checksum checksum) {
        return createObjectFromSegmentOnPath(path, acl, metadata,
                data == null ? null : new BufferSegment(data), mimeType, checksum);

    }    
  
    /**
     * Creates a new object in the cloud using a BufferSegment on the given
     * path.
     * 
     * @param path the path to create the object on.
     * @param acl Access control list for the new object. May be null to use a
     *            default ACL
     * @param metadata Metadata for the new object. May be null for no metadata.
     * @param data The initial contents of the object. May be appended to later.
     *            May be null to create an object with no content.
     * @param mimeType the MIME type of the content. Optional, may be null. If
     *            data is non-null and mimeType is null, the MIME type will
     *            default to application/octet-stream.
     * @return the ObjectId of the newly-created object for references by ID.
     * @throws EsuException if the request fails.
     */
    public ObjectId createObjectFromSegmentOnPath(ObjectPath path, Acl acl,
            MetadataList metadata, BufferSegment data, String mimeType) {
    	return createObjectFromSegmentOnPath( path, acl, metadata, data, mimeType, null );
    }

    
    /**
     * Updates an object in the cloud and optionally its metadata and ACL.
     * 
     * @param id The ID of the object to update
     * @param acl Access control list for the new object. Optional, default is
     *            NULL to leave the ACL unchanged.
     * @param metadata Metadata list for the new object. Optional, default is
     *            NULL for no changes to the metadata.
     * @param data The new contents of the object. May be appended to later.
     *            Optional, default is NULL (no content changes).
     * @param extent portion of the object to update. May be null to indicate
     *            the whole object is to be replaced. If not null, the extent
     *            size must match the data size.
     * @param mimeType the MIME type of the content. Optional, may be null. If
     *            data is non-null and mimeType is null, the MIME type will
     *            default to application/octet-stream.
     * @throws EsuException if the request fails.
     */
    public void updateObject(Identifier id, Acl acl, MetadataList metadata,
            Extent extent, byte[] data, String mimeType) {
        updateObjectFromSegment(id, acl, metadata, extent, data == null ? null
                : new BufferSegment(data), mimeType, null);
    }
    
    /**
     * Updates an object in the cloud and optionally its metadata and ACL.
     * 
     * @param id The ID of the object to update
     * @param acl Access control list for the new object. Optional, default is
     *            NULL to leave the ACL unchanged.
     * @param metadata Metadata list for the new object. Optional, default is
     *            NULL for no changes to the metadata.
     * @param data The new contents of the object. May be appended to later.
     *            Optional, default is NULL (no content changes).
     * @param extent portion of the object to update. May be null to indicate
     *            the whole object is to be replaced. If not null, the extent
     *            size must match the data size.
     * @param mimeType the MIME type of the content. Optional, may be null. If
     *            data is non-null and mimeType is null, the MIME type will
     *            default to application/octet-stream.
     * @param checksum if not null, use the Checksum object to compute
     * the checksum for the update object request.  If appending
     * to the object with subsequent requests, use the same
     * checksum object for each request.
     * @throws EsuException if the request fails.
     */
    public void updateObject(Identifier id, Acl acl, MetadataList metadata,
            Extent extent, byte[] data, String mimeType, Checksum checksum ) {
        updateObjectFromSegment(id, acl, metadata, extent, data == null ? null
                : new BufferSegment(data), mimeType, checksum);
    }

    /**
     * Updates an object in the cloud and optionally its metadata and ACL.
     * 
     * @param id The ID of the object to update
     * @param acl Access control list for the new object. Optional, default is
     *            NULL to leave the ACL unchanged.
     * @param metadata Metadata list for the new object. Optional, default is
     *            NULL for no changes to the metadata.
     * @param data The new contents of the object. May be appended to later.
     *            Optional, default is NULL (no content changes).
     * @param extent portion of the object to update. May be null to indicate
     *            the whole object is to be replaced. If not null, the extent
     *            size must match the data size.
     * @param mimeType the MIME type of the content. Optional, may be null. If
     *            data is non-null and mimeType is null, the MIME type will
     *            default to application/octet-stream.
     * @throws EsuException if the request fails.
     */
    public void updateObjectFromSegment(Identifier id, Acl acl,
            MetadataList metadata, Extent extent, BufferSegment data,
            String mimeType) {
    	updateObjectFromSegment( id, acl, metadata, extent, data, mimeType, null );
    }

    /**
     * Generates an HMAC-SHA1 signature of the given input string using the
     * shared secret key.
     * @param input the string to sign
     * @return the HMAC-SHA1 signature in Base64 format
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws IllegalStateException
     * @throws UnsupportedEncodingException
     */
    protected String sign( String input ) throws NoSuchAlgorithmException, InvalidKeyException, IllegalStateException, UnsupportedEncodingException {
        // Compute the signature hash
        Mac mac = Mac.getInstance( "HmacSHA1" );
        SecretKeySpec key = new SecretKeySpec( secret, "HmacSHA1" );
        mac.init( key );
        l4j.debug( "Hashing: \n" + input.toString() );

        byte[] hashData = mac.doFinal( input.toString().getBytes( "ISO-8859-1" ) );

        // Encode the hash in Base64.
        String hashOut = new String( Base64.encodeBase64( hashData ), "UTF-8" );
        
        l4j.debug( "Hash: " + hashOut );

        return hashOut;
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
            
            StringBuffer sb = new StringBuffer();
            sb.append( "GET\n" );
            sb.append( resource.toLowerCase() + "\n" );
            sb.append( uid + "\n" );
            sb.append( ""+(expiration.getTime()/1000) );
            
            String signature = sign( sb.toString() );
            String query = "uid=" + URLEncoder.encode( uid, "UTF8" ) + "&expires=" + (expiration.getTime()/1000) +
                "&signature=" + URLEncoder.encode( signature, "UTF8" );
            
            // We do this a little strangely here.  Technically, the trailing "=" in the Base-64 signature
            // should be encoded since it's a "reserved" character.  Atmos 1.2 is strict about this, but
            // 1.3 relaxes the rules a bit.  Most URL generators (java.net.URI included) don't have facilities
            // to break down the query components and encode them individually.  Therefore, we encode the
            // query ourselves here and append it to the generated URL.  This will then work with both
            // 1.2 and 1.3.
            URL u = buildUrl( resource, null );
                u = new URL( u.toString() + "?" + query );

                l4j.debug( "URL: " + u );
            
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
        } catch (URISyntaxException e) {
            throw new EsuException( "Invalid URL", e );
        }
    }

    /**
     * Gets the appropriate resource path depending on identifier
     * type.
     */
    protected String getResourcePath( String ctx, Identifier id ) {
                if( id instanceof ObjectId ) {
                        return ctx + "/objects/" + id;
                } else {
                        return ctx + "/namespace" + id;
                }
        }
    
    
    /**
     * Builds a new URL to the given resource
     * @throws URISyntaxException 
     * @throws MalformedURLException 
     */
    protected URL buildUrl(String resource, String query ) throws URISyntaxException, MalformedURLException  {
        URI uri = new URI( proto, null, host, port, resource, query, null );
        URL u = uri.toURL();
        l4j.debug( "URL: " + u );
        return u;
    }

    /**
     * Helper method that closes a stream ignoring errors.
     * @param out the OutputStream to close
     */
    protected void silentClose(OutputStream out) {
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
     * Parses the given header text and appends to the metadata list
     * @param meta the metadata list to append to
     * @param header the metadata header to parse
     * @param listable true if the header being parsed contains listable metadata.
     */
    protected void readMetadata(MetadataList meta, String header, boolean listable) {
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
    protected void processTags(MetadataTags tags, Map<String, String> headers) {
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
    protected void readAcl(Acl acl, String header, GRANT_TYPE type) {
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
    @SuppressWarnings("rawtypes")
    protected List<Identifier> parseObjectList( byte[] response ) {
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

    @SuppressWarnings("rawtypes")
	protected List<Identifier> parseVersionList( byte[] response ) {
        List<Identifier> objs = new ArrayList<Identifier>();
        
        // Use JDOM to parse the XML
        SAXBuilder sb = new SAXBuilder();
        try {
            Document d = sb.build( new ByteArrayInputStream( response ) );
            
            // The ObjectID element is part of a namespace so we need to use
            // the namespace to identify the elements.
            Namespace esuNs = Namespace.getNamespace( "http://www.emc.com/cos/" );

            List children = d.getRootElement().getChildren( "Ver", esuNs );
            
            l4j.debug( "Found " + children.size() + " objects" );
            for( Iterator i=children.iterator(); i.hasNext(); ) {
                Object o = i.next();
                if( o instanceof Element ) {
                    Element objectIdElement = (Element)((Element)o).getChildren( "OID", esuNs ).get(0);
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
    @SuppressWarnings("rawtypes")
	protected List<ObjectResult> parseObjectListWithMetadata( byte[] response ) {
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
    protected void readTags( MetadataTags tags, String header, boolean listable) {
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
     * Iterates through the given metadata and adds the appropriate metadata
     * headers to the request.
     * 
     * @param metadata the metadata to add
     * @param headers the map of request headers.
     */
    protected void processMetadata(MetadataList metadata,
            Map<String, String> headers) {

        StringBuffer listable = new StringBuffer();
        StringBuffer nonListable = new StringBuffer();

        l4j.debug("Processing " + metadata.count() + " metadata entries");

        for (Iterator<Metadata> i = metadata.iterator(); i.hasNext();) {
            Metadata meta = i.next();
            if (meta.isListable()) {
                if (listable.length() > 0) {
                    listable.append(", ");
                }
                listable.append(formatTag(meta));
            } else {
                if (nonListable.length() > 0) {
                    nonListable.append(", ");
                }
                nonListable.append(formatTag(meta));
            }
        }

        // Only set the headers if there's data
        if (listable.length() > 0) {
            headers.put("x-emc-listable-meta", listable.toString());
        }
        if (nonListable.length() > 0) {
            headers.put("x-emc-meta", nonListable.toString());
        }

    }

    /**
     * Formats a tag value for passing in the header.
     */
    protected Object formatTag(Metadata meta) {
        // strip commas and newlines for now.
        String fixed = meta.getValue().replace(",", "");
        fixed = fixed.replace("\n", "");
        return meta.getName() + "=" + fixed;
    }

    /**
     * Enumerates the given ACL and creates the appropriate request headers.
     * 
     * @param acl the ACL to enumerate
     * @param headers the set of request headers.
     */
    protected void processAcl(Acl acl, Map<String, String> headers) {
        StringBuffer userGrants = new StringBuffer();
        StringBuffer groupGrants = new StringBuffer();

        for (Iterator<Grant> i = acl.iterator(); i.hasNext();) {
            Grant grant = i.next();
            if (grant.getGrantee().getType() == Grantee.GRANT_TYPE.USER) {
                if (userGrants.length() > 0) {
                    userGrants.append(",");
                }
                userGrants.append(grant.toString());
            } else {
                if (groupGrants.length() > 0) {
                    groupGrants.append(",");
                }
                groupGrants.append(grant.toString());
            }
        }

        headers.put("x-emc-useracl", userGrants.toString());
        headers.put("x-emc-groupacl", groupGrants.toString());
    }
    
    /**
     * Generates the HMAC-SHA1 signature used to authenticate the request using
     * the Java security APIs.
     * 
     * @param con the connection object
     * @param method the HTTP method used
     * @param resource the resource path
     * @param headers the HTTP headers for the request
     * @throws IOException if character data cannot be encoded.
     * @throws GeneralSecurityException If errors occur generating the HMAC-SHA1
     *             signature.
     */
    protected void signRequest(String method, URL resource, Map<String, String> headers) throws IOException,
            GeneralSecurityException {
        // Build the string to hash.
        StringBuffer hashStr = new StringBuffer();
        hashStr.append(method + "\n");

        // If content type exists, add it. Otherwise add a blank line.
        if (headers.containsKey("Content-Type")) {
            l4j.debug("Content-Type: " + headers.get("Content-Type"));
            hashStr.append(headers.get("Content-Type").toLowerCase() + "\n");
        } else {
            hashStr.append("\n");
        }

        // If the range header exists, add it. Otherwise add a blank line.
        if (headers.containsKey("Range")) {
            hashStr.append(headers.get("Range") + "\n");
        } else if (headers.containsKey("Content-Range")) {
            hashStr.append(headers.get("Content-Range") + "\n");
        } else {
            hashStr.append("\n");
        }

        // Add the current date and the resource.
        hashStr.append(headers.get("Date") + "\n"
                + URLDecoder.decode(resource.getPath(), "UTF-8").toLowerCase());
        if (resource.getQuery() != null) {
            hashStr.append("?" + resource.getQuery() + "\n");
        } else {
            hashStr.append("\n");
        }

        // Do the 'x-emc' headers. The headers must be hashed in alphabetic
        // order and the values must be stripped of whitespace and newlines.
        List<String> keys = new ArrayList<String>();
        Map<String, String> newheaders = new HashMap<String, String>();

        // Extract the keys and values
        for (Iterator<String> i = headers.keySet().iterator(); i.hasNext();) {
            String key = i.next();
            if (key.indexOf("x-emc") == 0) {
                keys.add(key.toLowerCase());
                newheaders.put(key.toLowerCase(), headers.get(key).replace(
                        "\n", ""));
            }
        }

        // Sort the keys and add the headers to the hash string.
        Collections.sort(keys);
        boolean first = true;
        for (Iterator<String> i = keys.iterator(); i.hasNext();) {
            String key = i.next();
            if (!first) {
                hashStr.append("\n");
            } else {
                first = false;
            }
            // this.trace( "xheader: " . k . "." . newheaders[k] );
            hashStr.append(key + ':' + newheaders.get(key));
        }

        String hashOut = sign(hashStr.toString());
        
        headers.put( "x-emc-signature", hashOut );

    }


    /**
     * Gets the current time formatted for HTTP headers
     * @return
     */
    protected String getDateHeader() {
        TimeZone tz = TimeZone.getTimeZone("GMT");
        l4j.debug("TZ: " + tz);
        HEADER_FORMAT.setTimeZone(tz);
        String dateHeader = HEADER_FORMAT.format(new Date());
        l4j.debug("Date: " + dateHeader);
        return dateHeader;
    }
    
    protected ObjectId getObjectId( String location ) {
        Matcher m = OBJECTID_EXTRACTOR.matcher(location);
        if (m.find()) {
            String vid = m.group(1);
            l4j.debug("vId: " + vid);
            return new ObjectId(vid);
        } else {
            throw new EsuException("Could not find ObjectId in " + location);
        }
    }
    
    protected byte[] readStream( InputStream in, int contentLength ) throws IOException {
        try {
            byte[] output;
            // If we know the content length, read it directly into a buffer.
            if (contentLength != -1) {
                output = new byte[contentLength];

                int c = 0;
                while (c < contentLength) {
                    int read = in.read(output, c, contentLength - c);
                    if (read == -1) {
                        // EOF!
                        throw new EOFException(
                                "EOF reading response at position " + c
                                        + " size " + (contentLength - c));
                    }
                    c += read;
                }

                return output;
            } else {
                l4j.debug("Content length is unknown.  Buffering output.");
                // Else, use a ByteArrayOutputStream to collect the response.
                byte[] buffer = new byte[4096];

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                int c = 0;
                while ((c = in.read(buffer)) != -1) {
                    baos.write(buffer, 0, c);
                }
                baos.close();

                l4j.debug("Buffered " + baos.size() + " response bytes");

                return baos.toByteArray();
            }
        } finally {
            if (in != null) {
                in.close();
            }
        }

    }
    
    protected ServiceInformation parseServiceInformation( byte[] response ) {
    	return null;
    }
}
