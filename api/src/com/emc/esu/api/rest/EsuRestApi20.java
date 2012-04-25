package com.emc.esu.api.rest;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;

import com.emc.esu.api.*;
import org.apache.log4j.Logger;

public class EsuRestApi20 extends EsuRestApi implements EsuApi20 {
    private static final Logger l4j = Logger.getLogger(EsuRestApi20.class);

	public EsuRestApi20(String host, int port, String uid, String sharedSecret) {
		super(host, port, uid, sharedSecret);
	}

	@Override
	public void hardLink(ObjectPath source, ObjectPath target) {
        try {
            String resource = getResourcePath(context, source);
            String query = "hardlink";
            URL u = buildUrl(resource, query);
            HttpURLConnection con = (HttpURLConnection) u.openConnection();

            // Build headers
            Map<String, String> headers = new HashMap<String, String>();

            headers.put("x-emc-uid", uid);

            String destPath = target.toString();
            if (destPath.startsWith("/"))
            {
                destPath = destPath.substring(1);
            }
            headers.put("x-emc-path", destPath);

            // Add date
            headers.put("Date", getDateHeader());
            
            // Compute checksum
            // Sign request
            signRequest("POST", resource, query, headers);
            configureRequest( con, "POST", headers );

            con.connect();

            // Check response
            if (con.getResponseCode() > 299) {
                handleError(con);
            }

            con.disconnect();

        } catch (MalformedURLException e) {
            throw new EsuException("Invalid URL", e);
        } catch (IOException e) {
            throw new EsuException("Error connecting to server", e);
        } catch (GeneralSecurityException e) {
            throw new EsuException("Error computing request signature", e);
        } catch (URISyntaxException e) {
            throw new EsuException("Invalid URL", e);
        }

	}

    @Override
    public ObjectId createObjectFromSegment(Acl acl, MetadataList metadata,
                                            BufferSegment data, String mimeType, Checksum checksum) {

        try {
            String resource = context + "/objects";
            URL u = buildUrl(resource, null);
            HttpURLConnection con = (HttpURLConnection) u.openConnection();

            // Build headers
            Map<String, String> headers = new HashMap<String, String>();

            // Figure out the mimetype
            if (mimeType == null) {
                mimeType = "application/octet-stream";
            }

            headers.put("Content-Type", mimeType);
            headers.put("x-emc-uid", uid);

            // Process metadata
            if (metadata != null) {
                processMetadata(metadata, headers);
            }

            l4j.debug("meta " + headers.get("x-emc-meta"));

            // Add acl
            if (acl != null) {
                processAcl(acl, headers);
            }

            // Process data
            if (data == null) {
                data = new BufferSegment(new byte[0]);
            }
            con.setFixedLengthStreamingMode(data.getSize());
            con.setDoOutput(true);

            // Add date
            headers.put("Date", getDateHeader());

            // Compute checksum
            if ( checksum != null && checksum.isSendToServer() ) {
                checksum.update( data.getBuffer(), data.getOffset(), data.getSize() );
                headers.put( "x-emc-wschecksum", checksum.toString() );
            }

            // Request checksum from server
            if ( checksum != null && checksum.isGetFromServer() ) {
                headers.put( "x-emc-generate-checksum", checksum.getAlgorithmName() );
            }

            // Sign request
            signRequest("POST", resource, null, headers);
            configureRequest( con, "POST", headers );

            con.connect();

            // post data
            OutputStream out = null;
            try {
                out = con.getOutputStream();
                out.write(data.getBuffer(), data.getOffset(), data.getSize());
                out.close();
            } catch (IOException e) {
                silentClose(out);
                con.disconnect();
                throw new EsuException("Error posting data", e);
            }

            // Check response
            if (con.getResponseCode() > 299) {
                handleError(con);
            }

            // If requested, the checksum calculated on the server is in a response header
            if ( checksum != null && checksum.isGetFromServer() ) {
                checksum.setServerValue( con.getHeaderField( "x-emc-content-checksum" ) );
            }
            
            // The new object ID is returned in the location response header
            String location = con.getHeaderField("location");
            con.disconnect();

            // Parse the value out of the URL
            return getObjectId(location);
        } catch (MalformedURLException e) {
            throw new EsuException("Invalid URL", e);
        } catch (IOException e) {
            throw new EsuException("Error connecting to server", e);
        } catch (GeneralSecurityException e) {
            throw new EsuException("Error computing request signature", e);
        } catch (URISyntaxException e) {
            throw new EsuException("Invalid URL", e);
        }
    }

    @Override
    public ObjectId createObjectFromSegmentOnPath(ObjectPath path, Acl acl,
                                                  MetadataList metadata, BufferSegment data, String mimeType, Checksum checksum) {
        try {
            String resource = getResourcePath(context, path);
            URL u = buildUrl(resource, null);
            HttpURLConnection con = (HttpURLConnection) u.openConnection();

            // Build headers
            Map<String, String> headers = new HashMap<String, String>();

            // Figure out the mimetype
            if (mimeType == null) {
                mimeType = "application/octet-stream";
            }

            headers.put("Content-Type", mimeType);
            headers.put("x-emc-uid", uid);

            // Process metadata
            if (metadata != null) {
                processMetadata(metadata, headers);
            }

            l4j.debug("meta " + headers.get("x-emc-meta"));

            // Add acl
            if (acl != null) {
                processAcl(acl, headers);
            }

            // Process data
            if (data == null) {
                data = new BufferSegment(new byte[0]);
            }
            con.setFixedLengthStreamingMode(data.getSize());
            con.setDoOutput(true);

            // Add date
            headers.put("Date", getDateHeader());

            // Compute checksum
            if ( checksum != null && checksum.isSendToServer() ) {
                checksum.update( data.getBuffer(), data.getOffset(), data.getSize() );
                headers.put( "x-emc-wschecksum", checksum.toString() );
            }

            // Request checksum from server
            if ( checksum != null && checksum.isGetFromServer() ) {
                headers.put( "x-emc-generate-checksum", checksum.getAlgorithmName() );
            }

            // Sign request
            signRequest("POST", resource, null, headers);
            configureRequest( con, "POST", headers );

            con.connect();

            // post data
            OutputStream out = null;
            try {
                out = con.getOutputStream();
                out.write(data.getBuffer(), data.getOffset(), data.getSize());
                out.close();
            } catch (IOException e) {
                silentClose(out);
                con.disconnect();
                throw new EsuException("Error posting data", e);
            }

            // Check response
            if (con.getResponseCode() > 299) {
                handleError(con);
            }

            // If requested, the checksum calculated on the server is in a response header
            if ( checksum != null && checksum.isGetFromServer() ) {
                checksum.setServerValue( con.getHeaderField( "x-emc-content-checksum" ) );
            }

            // The new object ID is returned in the location response header
            String location = con.getHeaderField("location");
            con.disconnect();

            // Parse the value out of the URL
            return getObjectId(location);

        } catch (MalformedURLException e) {
            throw new EsuException("Invalid URL", e);
        } catch (IOException e) {
            throw new EsuException("Error connecting to server", e);
        } catch (GeneralSecurityException e) {
            throw new EsuException("Error computing request signature", e);
        } catch (URISyntaxException e) {
            throw new EsuException("Invalid URL", e);
        }
    }

    @Override
    public void updateObjectFromSegment(Identifier id, Acl acl,
                                        MetadataList metadata, Extent extent, BufferSegment data,
                                        String mimeType, Checksum checksum) {
        try {
            String resource = getResourcePath(context, id);
            URL u = buildUrl(resource, null);
            HttpURLConnection con = (HttpURLConnection) u.openConnection();

            // Build headers
            Map<String, String> headers = new HashMap<String, String>();

            // Figure out the mimetype
            if (mimeType == null) {
                mimeType = "application/octet-stream";
            }

            headers.put("Content-Type", mimeType);
            headers.put("x-emc-uid", uid);

            // Process metadata
            if (metadata != null) {
                processMetadata(metadata, headers);
            }

            l4j.debug("meta " + headers.get("x-emc-meta"));

            // Add acl
            if (acl != null) {
                processAcl(acl, headers);
            }

            // Add extent if needed
            if (extent != null && !extent.equals(Extent.ALL_CONTENT)) {
                headers.put(extent.getHeaderName(), extent.toString());
            }

            // Process data
            if (data == null) {
                data = new BufferSegment(new byte[0]);
            }
            con.setFixedLengthStreamingMode(data.getSize());
            con.setDoOutput(true);

            // Add date
            headers.put("Date", getDateHeader());

            // Compute checksum
            if( checksum != null && checksum.isSendToServer() ) {
                checksum.update( data.getBuffer(), data.getOffset(), data.getSize() );
                headers.put( "x-emc-wschecksum", checksum.toString() );
            }
            
            // Request checksum from server
            if ( checksum != null && checksum.isGetFromServer() ) {
                headers.put( "x-emc-generate-checksum", checksum.getAlgorithmName() );
            }

            // Sign request
            signRequest("PUT", resource, null, headers);
            configureRequest( con, "PUT", headers );

            con.connect();

            // post data
            OutputStream out = null;
            try {
                out = con.getOutputStream();
                out.write(data.getBuffer(), data.getOffset(), data.getSize());
                out.close();
            } catch (IOException e) {
                silentClose(out);
                con.disconnect();
                throw new EsuException("Error posting data", e);
            }

            // Check response
            if (con.getResponseCode() > 299) {
                handleError(con);
            }

            // If requested, the checksum calculated on the server is in a response header
            if ( checksum != null && checksum.isGetFromServer() ) {
                checksum.setServerValue( con.getHeaderField( "x-emc-content-checksum" ) );
            }

            con.disconnect();
        } catch (MalformedURLException e) {
            throw new EsuException("Invalid URL", e);
        } catch (IOException e) {
            throw new EsuException("Error connecting to server", e);
        } catch (GeneralSecurityException e) {
            throw new EsuException("Error computing request signature", e);
        } catch (URISyntaxException e) {
            throw new EsuException("Invalid URL", e);
        }

    }
}
