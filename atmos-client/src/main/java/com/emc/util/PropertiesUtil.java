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
