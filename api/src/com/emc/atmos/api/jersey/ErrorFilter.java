package com.emc.atmos.api.jersey;

import com.emc.atmos.AtmosException;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;

public class ErrorFilter extends ClientFilter {
    private static final Logger log = Logger.getLogger( ErrorFilter.class );

    @Override
    public ClientResponse handle( ClientRequest clientRequest ) throws ClientHandlerException {
        ClientResponse response = getNext().handle( clientRequest );

        if ( response.getStatus() > 299 ) {

            // JAXB will expect a namespace if we try to unmarshall, but some error responses don't include
            // a namespace. In lieu of writing a SAXFilter to apply a default namespace in-line, this works just as well.
            SAXBuilder sb = new SAXBuilder();

            Document d = null;
            try {
                d = sb.build( response.getEntityInputStream() );
            } catch ( Exception e ) {
                throw new AtmosException( response.getClientResponseStatus().getReasonPhrase(), response.getStatus() );
            }

            String code = d.getRootElement().getChildText( "Code" );
            if ( code == null )
                code = d.getRootElement()
                        .getChildText( "Code", Namespace.getNamespace( "http://www.emc.com/cos/" ) );
            String message = d.getRootElement().getChildText( "Message" );
            if ( message == null )
                message = d.getRootElement()
                           .getChildText( "Message",
                                          Namespace.getNamespace( "http://www.emc.com/cos/" ) );

            if ( code == null && message == null ) {
                // not an error from Atmos
                throw new AtmosException( response.getClientResponseStatus().getReasonPhrase(), response.getStatus() );
            }

            log.debug( "Error: " + code + " message: " + message );
            throw new AtmosException( message, response.getStatus(), Integer.parseInt( code ) );
        }

        return response;
    }
}
