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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import com.emc.acdp.api.request.AcdpRequest;
import com.emc.cdp.services.rest.model.Identity;
import com.emc.cdp.services.rest.model.ObjectFactory;
import com.emc.esu.api.EsuException;

/**
 * Templated response class that will use JAXB to parse an XML object out of
 * the response body.
 * 
 * @author cwikj
 */
public class AcdpXmlResponse<T> extends AcdpResponse {
    protected T response;

    /**
     * @param e
     */
    public AcdpXmlResponse(Throwable e) {
        super(e);
    }

    /**
     * @param responseHeaders
     * @param contentType
     * @param httpCode
     * @param successful
     */
    public AcdpXmlResponse(Map<String, List<String>> responseHeaders,
            String contentType, int httpCode, boolean successful, InputStream responseBody) {
        super(responseHeaders, contentType, httpCode, successful);
        
        try {
            response = deserialize(responseBody);
        } catch (JAXBException e) {
            this.successful = false;
            this.error = e;
            this.errorMessage = e.getMessage();
            this.httpCode = 0;            
        } catch(EsuException e) {
            this.successful = false;
            this.error = e;
            this.errorMessage = e.getMessage();
            this.httpCode = 0;            
        }
    }
    
    @SuppressWarnings("unchecked")
    protected T deserialize(InputStream data) throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance(AcdpRequest.CDP_JAXB_CONTEXT,
                ObjectFactory.class.getClassLoader());
        Unmarshaller u = jc.createUnmarshaller();
        try {
            return (T) u.unmarshal(data);
        } catch(ClassCastException e) {
            throw new EsuException("Error unmarshalilng XML: " + e.getMessage(), e);
        }
    }
    
    protected T deserialize(byte[] data) throws JAXBException {
        return deserialize(new ByteArrayInputStream(data));
    }

    public T getResponse() {
        return response;
    }
    

}
