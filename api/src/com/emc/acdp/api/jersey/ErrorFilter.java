// Copyright (c) 2012, EMC Corporation.
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
package com.emc.acdp.api.jersey;

import com.emc.acdp.AcdpException;
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
                throw new AcdpException( response.getClientResponseStatus().getReasonPhrase(), response.getStatus() );
            }

            String code = d.getRootElement().getChildText( "code" );
            if ( code == null )
                code = d.getRootElement()
                        .getChildText( "code", Namespace.getNamespace( "http://cdp.emc.com/services/rest/model" ) );
            String message = d.getRootElement().getChildText( "message" );
            if ( message == null )
                message = d.getRootElement()
                           .getChildText( "message",
                                          Namespace.getNamespace( "http://cdp.emc.com/services/rest/model" ) );

            if ( code == null && message == null ) {
                // not an error from CDP
                throw new AcdpException( response.getClientResponseStatus().getReasonPhrase(), response.getStatus() );
            }

            log.debug( "Error: " + code + " message: " + message );
            throw new AcdpException( message, response.getStatus(), code );
        }

        return response;
    }
}
