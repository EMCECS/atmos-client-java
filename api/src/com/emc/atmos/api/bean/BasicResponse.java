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
package com.emc.atmos.api.bean;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import java.util.Date;
import java.util.List;
import java.util.Map;

@XmlTransient
@XmlAccessorType( XmlAccessType.NONE )
public class BasicResponse {
    protected int httpStatus;
    protected String httpMessage;
    protected Map<String, List<String>> headers;
    protected String contentType;
    protected long contentLength;
    protected String location;
    protected Date lastModified;
    protected Date date;

    public Date getDate() {
        return date;
    }

    public void setDate( Date date ) {
        this.date = date;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus( int httpStatus ) {
        this.httpStatus = httpStatus;
    }

    public String getHttpMessage() {
        return httpMessage;
    }

    public void setHttpMessage( String httpMessage ) {
        this.httpMessage = httpMessage;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public void setHeaders( Map<String, List<String>> headers ) {
        this.headers = headers;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType( String contentType ) {
        this.contentType = contentType;
    }

    public long getContentLength() {
        return contentLength;
    }

    public void setContentLength( long contentLength ) {
        this.contentLength = contentLength;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation( String location ) {
        this.location = location;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified( Date lastModified ) {
        this.lastModified = lastModified;
    }

    protected String getFirstHeader( String headerName ) {
        if ( headers != null ) {
            List<String> values = headers.get( headerName );
            if ( values != null && !values.isEmpty() ) {
                return values.get( 0 );
            }
        }
        return null;
    }
}
