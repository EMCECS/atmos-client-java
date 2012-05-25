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

import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import javax.xml.bind.JAXBException;

import com.emc.cdp.services.rest.model.Identity;
import com.emc.esu.api.EsuException;

/**
 * Creates an Identity in CDP
 * 
 * @author cwikj
 */
public class CreateIdentityRequest extends BasicAcdpRequest {
    private Identity ident;
    private byte[] data;

    public CreateIdentityRequest(Identity ident) {
        this.ident = ident;

        // Build request headers
        requestHeaders = new HashMap<String, String>();

        // Serialize the body
        try {
            data = serialize(ident);
        } catch (JAXBException e) {
            throw new EsuException("Error marshalling XML: " + e.getMessage(),
                    e);
        } catch (UnsupportedEncodingException e) {
            // Should never happen
            throw new EsuException("Error marshalling XML: " + e.getMessage(),
                    e);
        }

        requestHeaders.put(CONTENT_TYPE, XML_CONTENT_TYPE);

    }

    @Override
    public String getRequestPath() {
        return "/cdp-rest/v1/identities";
    }

    @Override
    public String getRequestQuery() {
        return null;
    }

    @Override
    public String getMethod() {
        return POST_METHOD;
    }

    @Override
    public long getRequestSize() {
        return data.length;
    }

    @Override
    public byte[] getRequestData() {
        return data;
    }

    @Override
    public boolean hasResponseBody() {
        return false;
    }

    /**
     * @return the ident
     */
    public Identity getIdentity() {
        return ident;
    }

  

}
