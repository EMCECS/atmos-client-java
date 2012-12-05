package com.emc.atmos.api;

import com.emc.atmos.AtmosException;

public class ObjectPath implements ObjectIdentifier {
    private String path;

    public ObjectPath( String path ) {
        this.path = cleanPath( path );
    }

    public ObjectPath( ObjectPath parent, String path ) {
        if ( !parent.isDirectory() ) throw new AtmosException( "parent path must be a directory (end with a slash)" );
        // remove trailing slash from parent
        String parentPath = parent.getPath();
        this.path = parentPath.substring( 0, parentPath.length() - 1 ) + cleanPath( path );
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
     * Convenience method to determine whether this path represents a directory in Atmos.  Atmos uses a convention where
     * directory paths always end with a slash and object paths do not.
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
