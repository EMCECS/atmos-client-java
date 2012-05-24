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
import java.util.Map;
import java.util.TreeMap;

public abstract class CdpMgmtRequest<T extends CdpMgmtResponse> {
    private static final Logger l4j = Logger.getLogger( CdpMgmtRequest.class );

    public static final String PATH_PREFIX = "/cdp-rest/v1";

    protected CdpMgmtApi api;

    private Map<String, String> queryParameters = new TreeMap<String, String>();

    /**
     * main constructor
     *
     * @param api the management API object that contains configuration properties
     */
    public CdpMgmtRequest( CdpMgmtApi api ) {
        this.api = api;
    }

    /**
     * returns the URL path to use for the request. this allows implementations to dynamically generate the path based
     * on arbitrary criteria immediately before creating the connection.
     *
     * @return the URL path for this request
     */
    protected abstract String getPath();

    /**
     * returns the URL querystring to use for the request. override to dynamically generate the query
     * based on arbitraty criteria immediately before creating the connection. default implementation marshals the
     * query parameter map (see setQueryParameter()).
     *
     * @return the URL querystring for this request
     */
    protected String getQuery() {
        String query = "";
        for ( Map.Entry<String, String> entry : queryParameters.entrySet() ) {
            query += entry.getKey() + "=" + entry.getValue() + "&";
        }

        if ( query.length() == 0 ) return null;

        return query.substring( 0, query.length() - 1 );
    }

    /**
     * handles the details of the request. default implementation calls con.connect() and if a response code other than
     * 200 is found, calls handleError(). override for any other behavior.
     *
     * @param con a new connection that has not been sent (connect() has not been called)
     * @throws IOException
     */
    protected void handleConnection( HttpURLConnection con ) throws IOException {
        con.connect();
        if ( con.getResponseCode() != 200 ) {
            handleError( con );
        }
    }

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
        String query = getQuery();

        // prepend the session token if available
        String sessionQuery = getSessionQuery();
        if ( sessionQuery != null ) {
            if ( query == null )
                query = sessionQuery;
            else
                query = sessionQuery + "&" + query;
        }

        URI uri = new URI( api.getProto(), null, api.getHost(), api.getPort(), PATH_PREFIX + getPath(), query, null );
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
        if ( api.getSessionToken() == null ) return null;
        return "cdp_session=" + api.getSessionToken();
    }

    /**
     * Sets a parameter in the map of parameters (used to generate the querystring)
     *
     * @param name  the name of the parameter to set
     * @param value the value of the parameter to set
     */
    protected void setQueryParameter( String name, String value ) {
        queryParameters.put( name, value );
    }
}
