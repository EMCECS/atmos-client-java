package com.emc.acdp.api.jaxrs;

import com.emc.acdp.api.AcdpException;
import com.emc.util.StreamUtil;
import com.emc.util.XmlUtil;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

public class AcdpReader implements MessageBodyReader<Object> {
    @Override
    public boolean isReadable( Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType ) {
        return mediaType.isCompatible( RestUtil.MEDIA_TYPE_XML );
    }

    @Override
    public Object readFrom( Class<Object> objectClass,
                            Type type,
                            Annotation[] annotations,
                            MediaType mediaType,
                            MultivaluedMap<String, String> stringStringMultivaluedMap,
                            InputStream inputStream ) throws IOException, WebApplicationException {
        String xmlString = null;
        try {
            xmlString = StreamUtil.readAsString( inputStream );
            return XmlUtil.unmarshal( objectClass, xmlString );
        } catch ( JAXBException e ) {
            throw new AcdpException( "Error parsing XML.  Raw content:\n" + xmlString, e );
        }
    }
}
