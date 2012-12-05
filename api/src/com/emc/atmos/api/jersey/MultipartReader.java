package com.emc.atmos.api.jersey;

import com.emc.atmos.api.RestUtil;
import com.emc.atmos.api.multipart.MultipartEntity;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

@Provider
public class MultipartReader implements MessageBodyReader<MultipartEntity> {
    @Override
    public boolean isReadable( Class<?> type, Type genericType, Annotation annotations[], MediaType mediaType ) {
        return MultipartEntity.class.isAssignableFrom( type )
               && RestUtil.TYPE_MULTIPART.equals( mediaType.getType() );
    }

    @Override
    public MultipartEntity readFrom( Class<MultipartEntity> type,
                                     Type genericType,
                                     Annotation annotations[],
                                     MediaType mediaType,
                                     MultivaluedMap<String, String> httpHeaders,
                                     InputStream entityStream ) throws IOException, WebApplicationException {
        return MultipartEntity.fromStream( entityStream,
                                           mediaType.getParameters().get( RestUtil.TYPE_PARAM_BOUNDARY ) );
    }
}
