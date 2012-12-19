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
package com.emc.atmos.sync.monitor;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

public class DirectoryHash {
    private static final String HASH_SEPARATOR = "|";

    private String hash = "";

    /**
     * updates the hash with the latest state of the directory
     *
     * @param directory the directory from which to update the hash
     * @return true if the hash has changed (the directory or its contents have changed), false if it's the same
     */
    public boolean update( File directory ) {
        if ( !directory.exists() )
            throw new IllegalArgumentException( directory.getAbsolutePath() + " does not exist" );
        if ( !directory.isDirectory() )
            throw new IllegalArgumentException( directory.getAbsolutePath() + " is not a directory" );
        if ( !directory.canRead() )
            throw new IllegalArgumentException( directory.getAbsolutePath() + " is not readable" );

        String hash = generateHash( directory );

        if ( !this.hash.equals( hash ) ) {
            this.hash = hash;
            return true;
        }
        return false;
    }

    /**
     * generates a hash of the specified file/directory
     *
     * @param file a file for which to generate a hash
     * @return the hash of the specified file (including all of its children if it's a directory)
     */
    private String generateHash( File file ) {
        String hash = file.getAbsolutePath() + HASH_SEPARATOR + file.length() + HASH_SEPARATOR + file.lastModified() + "\n";
        if ( file.isDirectory() ) {
            File[] list = file.listFiles();

            // sort alphabetically (String.compareTo)
            Arrays.sort( list, new Comparator<File>() {
                @Override
                public int compare( File fileA, File fileB ) {
                    return fileA.getName().compareTo( fileB.getName() );
                }
            } );
            for ( File child : list ) {
                hash += generateHash( child );
            }
        }
        return hash;
    }
}
