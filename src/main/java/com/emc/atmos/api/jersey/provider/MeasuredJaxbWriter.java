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
package com.emc.atmos.api.jersey.provider;

import com.sun.jersey.core.impl.provider.entity.XMLRootElementProvider;
import com.sun.jersey.spi.inject.Injectable;

import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Providers;
import javax.xml.parsers.SAXParserFactory;

public class MeasuredJaxbWriter extends MeasuredMessageBodyWriter<Object> {
    public MeasuredJaxbWriter( MessageBodyWriter<Object> wrapped ) {
        super( wrapped );
    }

    @Produces( "application/xml" )
    public static final class App extends MeasuredJaxbWriter {
        public App( @Context Injectable<SAXParserFactory> spf, @Context Providers ps ) {
            super( new XMLRootElementProvider.App( spf, ps ) );
        }
    }

    @Produces( "text/xml" )
    public static final class Text extends MeasuredJaxbWriter {
        public Text( @Context Injectable<SAXParserFactory> spf, @Context Providers ps ) {
            super( new XMLRootElementProvider.Text( spf, ps ) );
        }
    }

    @Produces( "*/*" )
    public static final class General extends MeasuredJaxbWriter {
        public General( @Context Injectable<SAXParserFactory> spf, @Context Providers ps ) {
            super( new XMLRootElementProvider.General( spf, ps ) );
        }
    }
}
