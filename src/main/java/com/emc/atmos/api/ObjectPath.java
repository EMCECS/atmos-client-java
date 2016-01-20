/*
 * Copyright (c) 2013-2016, EMC Corporation.
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * + Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 * + Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 * + The name of EMC Corporation may not be used to endorse or promote
 *   products derived from this software without specific prior written
 *   permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.emc.atmos.api;

import com.emc.atmos.AtmosException;
import com.emc.atmos.api.bean.DirectoryEntry;

/**
 * Represents the full path to an object within a subtenant namespace. Paths always start with a slash. Directories
 * always end with a slash and non-directories never end with a slash.
 */
public class ObjectPath implements ObjectIdentifier {
    private String path;

    public ObjectPath( String path ) {
        this.path = cleanPath( path );
    }

    /**
     * Constructs a new path underneath a parent directory.
     *
     * @param parent The parent directory under which this new path exists (must end with a slash).
     * @param path   The relative path to this object under the parent directory (may begin with a slash, but it will
     *               be ignored).
     */
    public ObjectPath( ObjectPath parent, String path ) {
        if ( !parent.isDirectory() ) throw new AtmosException( "parent path must be a directory (end with a slash)" );
        // remove trailing slash from parent
        String parentPath = parent.getPath();
        this.path = parentPath.substring( 0, parentPath.length() - 1 ) + cleanPath( path );
    }

    /**
     * Constructs a new path from a parent directory and (what is assumed to be) one of its directory entries. The
     * resulting path will be a directory or file consistent with the directoryEntry.
     *
     * @param parent         The parent directory under which this new path exists (must end with a slash).
     * @param directoryEntry A directory entry of an object (presumed to be under the parent path)
     */
    public ObjectPath( ObjectPath parent, DirectoryEntry directoryEntry ) {
        this( parent, directoryEntry.getFilename() + (directoryEntry.isDirectory() ? "/" : "") );
    }

    public String getPath() {
        return this.path;
    }

    @Override
    public String getRelativeResourcePath() {
        return "namespace" + path;
    }

    @Override
    public String toString() {
        return getPath();
    }

    @Override
    public boolean equals( Object o ) {
        if ( this == o ) return true;
        if ( o == null || getClass() != o.getClass() ) return false;

        ObjectPath that = (ObjectPath) o;

        if ( !path.equals( that.path ) ) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return path.hashCode();
    }

    /**
     * Convenience method to determine whether this path represents a directory in Atmos.  Atmos uses a convention
     * where directory paths always end with a slash and object paths do not.
     */
    public boolean isDirectory() {
        return path.charAt( path.length() - 1 ) == '/';
    }

    /**
     * Convenience method to return the filename of this path (the last token delimited by a slash)
     */
    public String getFilename() {
        String[] levels = path.split( "/" );
        if ( levels[levels.length - 1].length() == 0 )
            return levels[levels.length - 2];
        else
            return levels[levels.length - 1];
    }

    private String cleanPath( String path ) {
        // require beginning slash
        if ( path.charAt( 0 ) != '/' ) path = '/' + path;
        return path;
    }
}
