package com.emc.atmos.api.jersey;

import com.emc.atmos.api.BufferSegment;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

@Provider
public class BufferSegmentWriter implements MessageBodyWriter<BufferSegment> {
    @Override
    public long getSize( BufferSegment bufferSegment,
                         Class<?> type,
                         Type genericType,
                         Annotation[] annotations,
                         MediaType mediaType ) {
        return bufferSegment.getSize();
    }

    @Override
    public boolean isWriteable( Class<?> type, Type genericType, Annotation annotations[], MediaType mediaType ) {
        return BufferSegment.class.isAssignableFrom( type );
    }

    @Override
    public void writeTo( BufferSegment bufferSegment,
                         Class<?> type,
                         Type genericType,
                         Annotation annotations[],
                         MediaType mediaType,
                         MultivaluedMap<String, Object> httpHeaders,
                         OutputStream entityStream ) throws IOException {
        entityStream.write( bufferSegment.getBuffer(), bufferSegment.getOffset(), bufferSegment.getSize() );
    }
}
