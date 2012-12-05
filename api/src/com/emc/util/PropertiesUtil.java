package com.emc.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.TreeSet;

public class PropertiesUtil {
    private static Set<String> loadedFiles = new TreeSet<String>();

    private static void loadConfig( String fileName ) {
        InputStream in = ClassLoader.getSystemResourceAsStream( fileName );
        if ( in != null ) {
            try {
                System.getProperties().load( in );
            } catch ( IOException e ) {
                throw new RuntimeException( "Could not load " + fileName, e );
            }
        }
    }

    public static synchronized String getProperty( String fileName, String key ) {
        if ( !loadedFiles.contains( fileName ) ) {
            loadConfig( fileName );
            loadedFiles.add( fileName );
        }

        String value = System.getProperty( key );
        if ( value == null )
            throw new RuntimeException( key + " is null.  Set in " + fileName + " or on command line with -D" + key );
        return value;
    }

    private PropertiesUtil() {
    }
}
