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
