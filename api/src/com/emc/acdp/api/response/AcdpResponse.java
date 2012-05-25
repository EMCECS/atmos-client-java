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

package com.emc.acdp.api.response;

import java.util.List;
import java.util.Map;

/**
 * @author cwikj
 *
 */
public class AcdpResponse {
    protected int httpCode;
    protected Throwable error;
    protected Map<String, List<String>> responseHeaders;
    protected String contentType;
    protected boolean successful;
    protected String errorMessage;
    
    /**
     * @return the responseHeaders
     */
    public Map<String, List<String>> getResponseHeaders() {
        return responseHeaders;
    }
    
    public List<String> getResponseHeaders(String name) {
        if (responseHeaders == null) return null;
        return responseHeaders.get(name);
    }
    
    public String getResponseHeader(String name) {
        List<String> list = getResponseHeaders(name);
        if (list == null || list.isEmpty()) return null;
        return list.get(0);
    }

    
    /**
     * @return the contentType
     */
    public String getContentType() {
        return contentType;
    }
    
    /**
     * Exception constructor used when a general failure occurs outside of
     * normal Atmos (HTTP) errors.
     * @param e the exception
     */
    public AcdpResponse(Throwable e) {
        this.successful = false;
        this.error = e;
        this.errorMessage = e.getMessage();
        this.httpCode = 0;
    }

    public AcdpResponse(Map<String, List<String>> responseHeaders, 
            String contentType, int httpCode, 
            boolean successful) {
        this.responseHeaders = responseHeaders;
        this.contentType = contentType;
        this.httpCode = httpCode;
        this.successful = successful;
    }
    
    /**
     * @return the HTTP response code
     */
    public int getHttpCode() {
        return httpCode;
    }
    /**
     * Sets the HTTP response code
     * @param httpCode the httpCode to set
     */
    public void setHttpCode(int httpCode) {
        this.httpCode = httpCode;
    }
    /**
     * @return the successful
     */
    public boolean isSuccessful() {
        return successful;
    }
    /**
     * @return the errorMessage
     */
    public String getErrorMessage() {
        return errorMessage;
    }
    /**
     * @param errorMessage the errorMessage to set
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Throwable getError() {
        return error;
    }
    
    public void setError(Throwable e) {
        this.error = e;
    }

    public void setResponseHeaders(Map<String, List<String>> headerFields) {
        this.responseHeaders = headerFields;
    }
    
}
