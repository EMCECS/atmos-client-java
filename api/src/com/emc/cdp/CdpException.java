package com.emc.cdp;

/**
 * Base CDP exception class that is thrown from the API methods.  Contains an
 * error code that can be mapped to the standard CDP error codes.  See the
 * Atmos programmer's guide for a list of server error codes.
 */
public class CdpException extends RuntimeException {

    private int httpCode = 0;
    private String cdpCode;

    /**
     * Creates a new CDP Exception with the given message.  The error code
     * will be set to 0.
     *
     * @param message the error message
     */
    public CdpException( String message ) {
        super( message );
    }

    /**
     * Creates a new CDP exception with the given message and HTTP error code.
     *
     * @param message  the error message
     * @param httpCode code the error code
     */
    public CdpException( String message, int httpCode ) {
        super( message );
        this.httpCode = httpCode;
    }

    /**
     * Creates a new CDP exception with the given message and HTTP error code.
     *
     * @param message  the error message
     * @param httpCode code the error code
     * @param cdpCode  detailed code the error code
     */
    public CdpException( String message, int httpCode, String cdpCode ) {
        super( message );
        this.httpCode = httpCode;
        this.cdpCode = cdpCode;
    }

    /**
     * Creates a new CDP exception with the given message and cause.
     *
     * @param message the error message
     * @param cause   the exception that caused the failure
     */
    public CdpException( String message, Exception cause ) {
        super( message, cause );
    }

    /**
     * Creates a new CDP exception with the given message, code, and cause
     *
     * @param message  the error message
     * @param cause    the exception that caused the failure
     * @param httpCode code the error code
     */
    public CdpException( String message, Exception cause, int httpCode ) {
        super( message, cause );
        this.httpCode = httpCode;
    }

    /**
     * Creates a new CDP exception with the given message, code, and cause
     *
     * @param message  the error message
     * @param cause    the exception that caused the failure
     * @param httpCode the error code
     * @param cdpCode  detailed code the error code
     */
    public CdpException( String message, Exception cause, int httpCode, String cdpCode ) {
        super( message, cause );
        this.httpCode = httpCode;
        this.cdpCode = cdpCode;
    }

    /**
     * Returns the HTTP error code associated with the exception.  If unknown
     * (the error did not originate inside the CDP server), the code will be zero.
     *
     * @return the error code
     */
    public int getHttpCode() {
        return httpCode;
    }

    /**
     * Returns the CDP internal error code associated with the exception.
     *
     * @return the error code
     */
    public String getAtmosCode() {
        return cdpCode;
    }
}
