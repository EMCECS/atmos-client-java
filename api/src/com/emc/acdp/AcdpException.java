package com.emc.acdp;

public class AcdpException extends RuntimeException {
    private int httpCode = 0;
    private String acdpCode;

    public AcdpException( String message ) {
        this( message, 0, null );
    }

    public AcdpException( String message, int httpCode ) {
        this( message, httpCode, null );
    }

    public AcdpException( String message, int httpCode, String acdpCode ) {
        super( message );
        this.httpCode = httpCode;
        this.acdpCode = acdpCode;
    }

    public AcdpException( String message, Throwable throwable ) {
        this( message, throwable, 0, null );
    }

    public AcdpException( String message, Throwable throwable, int httpCode ) {
        this( message, throwable, httpCode, null );
    }

    public AcdpException( String message, Throwable throwable, int httpCode, String acdpCode ) {
        super( message, throwable );
        this.httpCode = httpCode;
        this.acdpCode = acdpCode;
    }

    public int getHttpCode() {
        return httpCode;
    }

    public String getAcdpCode() {
        return acdpCode;
    }
}
