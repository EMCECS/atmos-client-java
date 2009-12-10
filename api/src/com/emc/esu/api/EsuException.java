// Copyright (c) 2008, EMC Corporation.
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
package com.emc.esu.api;

/**
 * Base ESU exception class that is thrown from the API methods.  Contains an
 * error code that can be mapped to the standard ESU error codes.
 */
public class EsuException extends RuntimeException {
    private static final long serialVersionUID = -6765742140810819241L;
    
    private int code = 0;

    /**
     * Creates a new ESU Exception with the given message.  The error code
     * will be set to 0.
     * @param message the error message
     */
    public EsuException( String message ) {
        super( message );
    }
    
    /**
     * Creates a new ESU exception with the given message and error code.
     * @param message the error message
     * @param code the error code
     */
    public EsuException( String message, int code ) {
        super( message );
        this.code = code;
    }
    
    /**
     * Creates a new ESU exception with the given message and cause.
     * @param message the error message
     * @param cause the exception that caused the failure
     */
    public EsuException( String message, Exception cause ) {
        super( message, cause );
    }
    
    /**
     * Creates a new ESU exception with the given message, code, and cause
     * @param message the error message
     * @param cause the exception that caused the failure
     * @param code the error code
     */
    public EsuException( String message, Exception cause, int code ) {
        super( message, cause );
        this.code = code;
    }

    /**
     * Returns the error code associated with the exception.  If unknown (the
     * error did not originate inside the ESU server), the code will be zero.
     * @return the error code
     */
    public int getCode() {
        return code;
    } 
}
