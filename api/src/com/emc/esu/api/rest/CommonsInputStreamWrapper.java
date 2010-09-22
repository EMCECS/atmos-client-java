package com.emc.esu.api.rest;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpResponse;
import org.apache.log4j.Logger;

public class CommonsInputStreamWrapper extends InputStream {
    private static final Logger l4j = Logger.getLogger( CommonsInputStreamWrapper.class );
    
    private InputStream in;
    private HttpResponse response;

    public CommonsInputStreamWrapper(HttpResponse response) throws IllegalStateException, IOException {
        this.in = response.getEntity().getContent();
        this.response = response;
    }

    @Override
    public int available() throws IOException {
        return in.available();
    }

    @Override
    public void close() throws IOException {
        in.close();
        if( response != null ) {
            response.getEntity().consumeContent();
            response = null;
        }
    }

    @Override
    public synchronized void mark(int readlimit) {
        in.mark(readlimit);
    }

    @Override
    public boolean markSupported() {
        return in.markSupported();
    }

    @Override
    public int read() throws IOException {
        return in.read();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return in.read(b, off, len);
    }

    @Override
    public int read(byte[] b) throws IOException {
        return in.read(b);
    }

    @Override
    public synchronized void reset() throws IOException {
        in.reset();
    }

    @Override
    public long skip(long n) throws IOException {
        return in.skip(n);
    }

    @Override
    protected void finalize() throws Throwable {
        if( response != null ) {
            l4j.warn( "Warning: connection was not closed!" );
            try {
                response.getEntity().consumeContent();
                response = null;
            } catch( Exception e ) {
                // Ignore
            }
        }
        super.finalize();
    }

    
}
