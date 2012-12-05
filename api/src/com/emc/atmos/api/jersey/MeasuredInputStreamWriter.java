package com.emc.atmos.api.jersey;

import com.emc.util.StreamUtil;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

@Provider
public class MeasuredInputStreamWriter implements MessageBodyWriter<MeasuredInputStream> {
    @Override
    public long getSize( MeasuredInputStream mis,
                         Class<?> type,
                         Type genericType,
                         Annotation[] annotations,
                         MediaType mediaType ) {
        return mis.getSize();
    }

    @Override
    public boolean isWriteable( Class<?> type, Type genericType, Annotation annotations[], MediaType mediaType ) {
        return MeasuredInputStream.class.isAssignableFrom( type );
    }

    @Override
    public void writeTo( MeasuredInputStream mis,
                         Class<?> type,
                         Type genericType,
                         Annotation annotations[],
                         MediaType mediaType,
                         MultivaluedMap<String, Object> httpHeaders,
                         OutputStream entityStream ) throws IOException {
        StreamUtil.copy( mis, entityStream, mis.getSize() );
    }
}
