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

package com.emc.acdp.api.request;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;

import com.emc.acdp.api.response.AcdpXmlResponse;
import com.emc.cdp.services.rest.model.Error;
import com.emc.esu.api.EsuException;

/**
 * Abstract class that uses AcdpXmlResponse to parse an XML object out of the
 * response body using JAXB.
 * 
 * @author cwikj
 *
 */
public abstract class AcdpXmlResponseRequest<T> extends AcdpRequest<AcdpXmlResponse<T>> {
    private static final Logger l4j = Logger.getLogger(AcdpXmlResponseRequest.class);

    /**
     * @see com.emc.acdp.api.request.AcdpRequest#hasResponseBody()
     */
    @Override
    public boolean hasResponseBody() {
        return true;
    }

    /**
     * @see com.emc.acdp.api.request.AcdpRequest#parseResponse(int, java.lang.String, java.util.Map, java.io.InputStream)
     */
    @Override
    public AcdpXmlResponse<T> parseResponse(int responseCode,
            String responseLine, Map<String, List<String>> headerFields,
            InputStream in) {
        
        String contentType = getContentType(headerFields);
        return new AcdpXmlResponse<T>(headerFields, contentType, responseCode, true, in);
    }

    /* (non-Javadoc)
     * @see com.emc.acdp.api.request.AcdpRequest#parseResponse(int, java.lang.String, java.util.Map)
     */
    @Override
    public AcdpXmlResponse<T> parseResponse(int responseCode,
            String responseMessage, Map<String, List<String>> headerFields) {
        throw new UnsupportedOperationException("Response body expected");
    }

    @Override
    public AcdpXmlResponse<T> parseError(int responseCode, String responseMessage,
            Map<String, List<String>> headerFields, byte[] errorBody) {
        try {
            JAXBContext jc = JAXBContext.newInstance(CDP_JAXB_CONTEXT);
            Unmarshaller u = jc.createUnmarshaller();
            Error err = (Error) u
                    .unmarshal(new ByteArrayInputStream(errorBody));
            EsuException ee = new EsuException(err.getMessage(), responseCode);
            AcdpXmlResponse<T> r = new AcdpXmlResponse<T>(ee);
            r.setResponseHeaders(headerFields);
            return r;
        } catch (Exception e) {
            l4j.debug("Failed to parse response", e);
            // Just do it without anything else
            EsuException ee = new EsuException(responseMessage, responseCode);
            AcdpXmlResponse<T> r = new AcdpXmlResponse<T>(ee);
            r.setResponseHeaders(headerFields);
            return r;
        }
    }

    @Override
    public AcdpXmlResponse<T> parseError(Throwable e) {
        return new AcdpXmlResponse<T>(e);
    }

}
