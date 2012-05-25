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

import com.emc.acdp.api.response.AcdpResponse;
import com.emc.cdp.services.rest.model.Error;
import com.emc.esu.api.EsuException;

/**
 * This is a base class for basic ACDP requests that have empty responses, i.e.
 * a response that's basically "HTTP/200 OK" or "HTTP/201 Created", etc.
 * @author cwikj
 */
public abstract class BasicAcdpRequest extends AcdpRequest<AcdpResponse> {
    private static final Logger l4j = Logger.getLogger(BasicAcdpRequest.class);
    
    @Override
    public AcdpResponse parseResponse(int responseCode, String responseLine,
            Map<String, List<String>> headerFields, InputStream in) {
        throw new UnsupportedOperationException(
                "Base AcdpResponse can't parse bodies");
    }

    @Override
    public AcdpResponse parseResponse(int responseCode, String responseMessage,
            Map<String, List<String>> headerFields) {
        return new AcdpResponse(headerFields, null, responseCode, true);
    }

    @Override
    public AcdpResponse parseError(int responseCode, String responseMessage,
            Map<String, List<String>> headerFields, byte[] errorBody) {
        try {
            JAXBContext jc = JAXBContext.newInstance(CDP_JAXB_CONTEXT);
            Unmarshaller u = jc.createUnmarshaller();
            Error err = (Error) u
                    .unmarshal(new ByteArrayInputStream(errorBody));
            EsuException ee = new EsuException(err.getMessage(), responseCode);
            AcdpResponse r = new AcdpResponse(ee);
            r.setResponseHeaders(headerFields);
            return r;
        } catch (Exception e) {
            l4j.debug("Failed to parse response", e);
            // Just do it without anything else
            EsuException ee = new EsuException(responseMessage, responseCode);
            AcdpResponse r = new AcdpResponse(ee);
            r.setResponseHeaders(headerFields);
            return r;
        }
    }

    @Override
    public AcdpResponse parseError(Throwable e) {
        return new AcdpResponse(e);
    }

  
}
