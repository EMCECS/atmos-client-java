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
package com.emc.acdp.util;

import com.emc.cdp.services.rest.model.ObjectFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;

public class XmlUtil {
    private XmlUtil() {
    }

    private static JAXBContext jaxbContext;
    private static ThreadLocal<Marshaller> marshaller = new ThreadLocal<Marshaller>();
    private static ThreadLocal<Unmarshaller> unmarshaller = new ThreadLocal<Unmarshaller>();

    static {
        try {
            jaxbContext = JAXBContext.newInstance( "com.emc.cdp.services.rest.model",
                                                   ObjectFactory.class.getClassLoader() );
        } catch ( JAXBException e ) {
            // unrecoverable
            throw new RuntimeException( e );
        }
    }

    public static String marshal( Object bean ) throws JAXBException {
        StringWriter writer = new StringWriter();
        getMarshaller().marshal( bean, writer );
        return writer.toString();
    }

    public static void marshal( Object bean, OutputStream os ) throws JAXBException {
        getMarshaller().marshal( bean, os );
    }

    @SuppressWarnings( "unchecked" )
    public static <T> T unmarshal( Class<T> beanClass, String xml ) throws JAXBException {
        StringReader reader = new StringReader( xml );
        return (T) getUnmarshaller().unmarshal( reader );
    }

    @SuppressWarnings( "unchecked" )
    public static <T> T unmarshal( Class<T> beanClass, InputStream is ) throws JAXBException {
        return (T) getUnmarshaller().unmarshal( is );
    }

    private static Marshaller getMarshaller() throws JAXBException {
        Marshaller m = marshaller.get();
        if ( m == null ) {
            m = jaxbContext.createMarshaller();
            marshaller.set( m );
        }
        return m;
    }

    private static Unmarshaller getUnmarshaller() throws JAXBException {
        Unmarshaller um = unmarshaller.get();
        if ( um == null ) {
            um = jaxbContext.createUnmarshaller();
            unmarshaller.set( um );
        }
        return um;
    }
}
