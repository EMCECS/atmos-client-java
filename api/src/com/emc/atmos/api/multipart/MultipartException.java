package com.emc.atmos.api.multipart;

public class MultipartException extends RuntimeException {
    private static final long serialVersionUID = 5446250195215514014L;

    public MultipartException() {
    }

    public MultipartException( String s ) {
        super( s );
    }

    public MultipartException( String s, Throwable throwable ) {
        super( s, throwable );
    }

    public MultipartException( Throwable throwable ) {
        super( throwable );
    }
}
