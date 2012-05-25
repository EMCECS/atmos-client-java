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
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;

import com.emc.acdp.api.response.CreateAccountResponse;
import com.emc.cdp.services.rest.model.Account;
import com.emc.cdp.services.rest.model.Error;
import com.emc.esu.api.EsuException;

/**
 * Creates a new account and returns the new account ID.  Note that after 
 * executing this request you'll likely want to follow it up with 
 * @author cwikj
 * 
 */
public class CreateAccountRequest extends AcdpRequest<CreateAccountResponse> {
    private static final Logger l4j = Logger
            .getLogger(CreateAccountRequest.class);

    public static final String WEB_ACCOUNT_TYPE = "web";

    private String adminSession;
    private byte[] data;

    public CreateAccountRequest(Account account, String adminSession) {
        this.adminSession = adminSession;

        try {
            data = serialize(account);
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
        return "/cdp-rest/v1/admin/accounts";
    }

    @Override
    public String getRequestQuery() {
        return "cdp_session=" + adminSession;
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

    @Override
    public CreateAccountResponse parseResponse(int responseCode, String responseLine,
            Map<String, List<String>> headerFields, InputStream in) {
        throw new UnsupportedOperationException("Unexpected response body");
    }

    @Override
    public CreateAccountResponse parseResponse(int responseCode, String responseMessage,
            Map<String, List<String>> headerFields) {
        
        List<String> location = headerFields.get(LOCATION_FIELD);
        if (location == null || location.size() < 1) {
            return parseError(new EsuException(
                    "Location header not found in response"));
        }

        String sloc = location.get(0);
        // Parse account ID from sloc
        // e.g.
        // http://localhost:8080/cdp-rest/v1/admin/accounts/A96342522441
        int last = sloc.lastIndexOf('/');
        if (last == -1) {
            return parseError(new EsuException("Could not parse account ID"));
        }

        String accountId = sloc.substring(last + 1);

        return new CreateAccountResponse(headerFields, "", responseCode, true,
                accountId);
    }

    @Override
    public CreateAccountResponse parseError(int responseCode, String responseMessage,
            Map<String, List<String>> headerFields, byte[] errorBody) {
        try {
            Error err = (Error) deserialize(new ByteArrayInputStream(errorBody));
            EsuException ee = new EsuException(err.getMessage(), responseCode);
            CreateAccountResponse r = new CreateAccountResponse(ee);
            r.setResponseHeaders(headerFields);
            return r;
        } catch (Exception e) {
            l4j.debug("Failed to parse response", e);
            // Just do it without anything else
            EsuException ee = new EsuException(responseMessage, responseCode);
            CreateAccountResponse r = new CreateAccountResponse(ee);
            r.setResponseHeaders(headerFields);
            return r;
        }
    }

    @Override
    public CreateAccountResponse parseError(Throwable e) {
        return new CreateAccountResponse(e);
    }

}
