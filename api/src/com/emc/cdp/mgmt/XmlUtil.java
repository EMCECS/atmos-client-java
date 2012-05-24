package com.emc.cdp.mgmt;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.io.StringWriter;

public class XmlUtil {
    private XmlUtil() {
    }

    private static Unmarshaller unmarshaller;
    private static Marshaller marshaller;

    static {
        try {
            JAXBContext context = JAXBContext.newInstance( "com.emc.cdp.mgmt.bean" );
            marshaller = context.createMarshaller();
            unmarshaller = context.createUnmarshaller();
        } catch ( JAXBException e ) {
            // unrecoverable
            throw new RuntimeException( e );
        }
    }

    public static String marshal( Object bean ) {
        try {
            StringWriter writer = new StringWriter();
            marshaller.marshal( bean, writer );
            return writer.toString();
        } catch ( JAXBException e ) {
            throw new RuntimeException( e );
        }
    }

    @SuppressWarnings( "unchecked" )
    public static <T> T unmarshal( Class<T> beanClass, String xml ) {
        try {
            StringReader reader = new StringReader( xml );
            return (T) unmarshaller.unmarshal( reader );
        } catch ( JAXBException e ) {
            throw new RuntimeException( e );
        }
    }
}
