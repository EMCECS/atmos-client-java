package com.emc.cdp.mgmt;

import com.emc.cdp.CdpException;
import com.emc.util.HttpUtil;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public abstract class CdpMgmtRequest<T extends CdpMgmtResponse> {
    private static final Logger l4j = Logger.getLogger( CdpMgmtRequest.class );

    public static final String PATH_PREFIX = "/cdp-rest/v1";

    protected CdpMgmtApi api;

    protected String path;
    protected String query;

    /**
     * implementations should call super(api) and also set path and query appropriately based on the respective
     * URL path (excluding the prefix above) and URL querystring for the request.
     *
     * @param api the management API object that contains configuration properties
     */
    public CdpMgmtRequest( CdpMgmtApi api ) {
        this.api = api;
        path = "";
        query = "";
    }

    /**
     * implementations should handle the details of the request here. a typical example would be
     * <pre>
     *
     * </pre>
     *
     * @param con
     * @throws IOException
     */
    protected abstract void handleConnection( HttpURLConnection con ) throws IOException;

    /**
     * implementations should construct/return the appropriate response object (subclass of CdpMgmtResponse) resulting
     * from this request. this method is called after the request is complete and successful.
     *
     * @param con a connection whose status is such that a response may be obtained
     * @return the CdpMgmtResponse resulting from this request
     */
    protected abstract T createResponse( HttpURLConnection con ) throws IOException;

    /**
     * Main execution method for this request. default implementation calls getConnection, then calls handleConnection()
     * passing in that connection, then calls createResponse and returns that result.
     *
     * @return a CdpMgmtResponse resulting from this request
     */
    public T execute() {
        try {
            HttpURLConnection con = getConnection();
            handleConnection( con );
            return createResponse( con );
        } catch ( IOException e ) {
            throw new CdpException( "Error connecting to server: " + e.getMessage(), e );
        } catch ( URISyntaxException e ) {
            throw new CdpException( "Error building URI: " + e.getMessage(), e );
        }
    }

    /**
     * Creates an HttpURLConnection. default implementation uses info from the API configuration and <code>path</code>
     * and <code>query</code> to determine the complete URL.
     *
     * @return a new connection (from URL.openConnection)
     * @throws IOException
     * @throws URISyntaxException
     */
    protected HttpURLConnection getConnection()
            throws IOException, URISyntaxException {

        URI uri = new URI( api.getProto(), null, api.getHost(),
                api.getPort(), PATH_PREFIX + path, query, null );
        l4j.debug( "URI: " + uri );
        URL u = new URL( uri.toASCIIString() );
        l4j.debug( "URL: " + u );

        return (HttpURLConnection) u.openConnection();
    }

    /**
     * handles the case when a connection failed. you probably don't need to override this method.
     *
     * @param con a failed connection
     * @throws IOException
     */
    protected void handleError( HttpURLConnection con ) throws IOException {
        int httpCode = con.getResponseCode();
        String msg = con.getResponseMessage();
        byte[] response = HttpUtil.readResponse( con );
        l4j.debug( "Error response: " + new String( response, "UTF-8" ) );
        SAXBuilder sb = new SAXBuilder();

        Document d = null;
        try {
            d = sb.build( new ByteArrayInputStream( response ) );
        } catch ( JDOMException e ) {
            l4j.error( "Error parsing XML error response", e );
            throw new RuntimeException( e );
        }

        String code = d.getRootElement().getChildText( "code" );
        String message = d.getRootElement().getChildText( "message" );

        if ( code == null && message == null ) {
            // not an error from CDP
            throw new CdpException( msg, httpCode );
        }

        l4j.debug( "Error: " + code + " message: " + message );
        throw new CdpException( message, httpCode, code );
    }

    /**
     * returns the session query (key/value pair) with the session token for use in the URL querystring.
     *
     * @return "cdp_session=" + api.getSessionToken()
     */
    protected String getSessionQuery() {
        return "cdp_session=" + api.getSessionToken();
    }
}
