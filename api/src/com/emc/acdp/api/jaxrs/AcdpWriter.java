package com.emc.acdp.api.jaxrs;

import com.emc.util.XmlUtil;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

public class AcdpWriter implements MessageBodyWriter<Object> {
    @Override
    public long getSize( Object o, Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType ) {
        try {
            return XmlUtil.marshal( o ).getBytes( "UTF-8" ).length;
        } catch ( Exception e ) {
            throw new RuntimeException( e );
        }
    }

    @Override
    public boolean isWriteable( Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType ) {
        return mediaType.isCompatible( RestUtil.MEDIA_TYPE_XML );
    }

    @Override
    public void writeTo( Object o,
                         Class<?> aClass,
                         Type type,
                         Annotation[] annotations,
                         MediaType mediaType,
                         MultivaluedMap<String, Object> stringObjectMultivaluedMap,
                         OutputStream outputStream ) throws IOException, WebApplicationException {
        try {
            XmlUtil.marshal( o, outputStream );
        } catch ( JAXBException e ) {
            throw new RuntimeException( e );
        }
    }
}
