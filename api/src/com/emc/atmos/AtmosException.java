package com.emc.atmos;

public class AtmosException extends RuntimeException {
    private int errorCode;
    private int httpCode;

    public AtmosException() {
    }

    public AtmosException( String message ) {
        super( message );
    }

    public AtmosException( String message, Throwable throwable ) {
        super( message, throwable );
    }

    public AtmosException( Throwable throwable ) {
        super( throwable );
    }

    public AtmosException( String message, int httpCode ) {
        super( message );
        this.httpCode = httpCode;
    }

    public AtmosException( String message, int httpCode, int errorCode ) {
        super( message );
        this.errorCode = errorCode;
        this.httpCode = httpCode;
    }

    public AtmosException( String message, int httpCode, int errorCode, Throwable throwable ) {
        super( message, throwable );
        this.errorCode = errorCode;
        this.httpCode = httpCode;
    }

    @Override
    public String toString() {
        return "AtmosException{" +
               "errorCode=" + errorCode +
               ", httpCode=" + httpCode +
               "} " + super.toString();
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode( int errorCode ) {
        this.errorCode = errorCode;
    }

    public int getHttpCode() {
        return httpCode;
    }

    public void setHttpCode( int httpCode ) {
        this.httpCode = httpCode;
    }
}
