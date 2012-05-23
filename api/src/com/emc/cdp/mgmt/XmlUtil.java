package com.emc.cdp.mgmt;

import javax.xml.bind.*;
import java.io.StringReader;
import java.io.StringWriter;

public class XmlUtil {
    private XmlUtil() {
    }

    private static Unmarshaller unmarshaller;
    private static Marshaller marshaller;

    static {
        try {
            JAXBContext context = JAXBContext.newInstance( "com.emd.cdp.mgmt.bean" );
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

    public static Object unmarshal( String xml ) {
        try {
            StringReader reader = new StringReader( xml );
            JAXBElement<?> element = (JAXBElement<?>) unmarshaller.unmarshal( reader );
            return element.getValue();
        } catch ( JAXBException e ) {
            throw new RuntimeException( e );
        }
    }
}
