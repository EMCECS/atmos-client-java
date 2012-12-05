package com.emc.atmos.api.jersey;

import java.io.IOException;
import java.io.InputStream;

/**
 * A simple delegating class to attach a size to an input stream.  This class does not perform any calculation and
 * merely stores/returns the size specified in the constructor.
 */
public class MeasuredInputStream extends InputStream {
    private InputStream source;
    private long size;

    public MeasuredInputStream( InputStream source, long size ) {
        this.source = source;
        this.size = size;
    }

    public long getSize() {
        return size;
    }

    @Override
    public int read() throws IOException {
        return source.read();
    }

    @Override
    public int read( byte[] bytes ) throws IOException {
        return source.read( bytes );
    }

    @Override
    public int read( byte[] bytes, int i, int i1 ) throws IOException {
        return source.read( bytes, i, i1 );
    }

    @Override
    public long skip( long l ) throws IOException {
        return source.skip( l );
    }

    @Override
    public int available() throws IOException {
        return source.available();
    }

    @Override
    public void close() throws IOException {
        source.close();
    }

    @Override
    public void mark( int i ) {
        source.mark( i );
    }

    @Override
    public void reset() throws IOException {
        source.reset();
    }

    @Override
    public boolean markSupported() {
        return source.markSupported();
    }
}
