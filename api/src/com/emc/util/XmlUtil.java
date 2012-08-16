package com.emc.util;

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
