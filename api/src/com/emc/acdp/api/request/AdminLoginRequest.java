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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.emc.acdp.api.response.AdminLoginResponse;
import com.emc.cdp.services.rest.model.Error;
import com.emc.esu.api.EsuException;

/**
 * @author cwikj
 * 
 */
public class AdminLoginRequest extends AcdpRequest<AdminLoginResponse> {
    private static final Logger l4j = Logger.getLogger(AdminLoginRequest.class);
    public byte[] data;

    public AdminLoginRequest(String userid, String password) {
        // Create post data
        try {
            String postdata = "cdp-identity-id="
                    + URLEncoder.encode(userid, "UTF-8") + "&cdp-password="
                    + URLEncoder.encode(password, "UTF-8");
            data = postdata.getBytes("US-ASCII");
        } catch (UnsupportedEncodingException e) {
            // Should never happen.
            throw new RuntimeException("Unable to encode data: "
                    + e.getMessage(), e);
        }
        requestHeaders.put(CONTENT_TYPE, FORM_CONTENT_TYPE);
    }

    @Override
    public String getRequestPath() {
        return "/cdp-rest/v1/admin/login";
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
        return true;
    }

    @Override
    public AdminLoginResponse parseResponse(int responseCode,
            String responseLine, Map<String, List<String>> headerFields,
            InputStream in) {
        try {
            // Should be one line of text.
            BufferedReader br = new BufferedReader(new InputStreamReader(in,
                    "UTF-8"));
            String token = br.readLine();
            if (token.length() < 5) {
                return parseError(new EsuException(
                        "Unable to read admin session ID from response"));
            }
            return new AdminLoginResponse(headerFields,
                    getContentType(headerFields), responseCode, true, token);
        } catch (UnsupportedEncodingException e) {
            return parseError(e);
        } catch (IOException e) {
            return parseError(e);
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                // Ignore
            }
        }
    }

    @Override
    public AdminLoginResponse parseResponse(int responseCode,
            String responseMessage, Map<String, List<String>> headerFields) {
        throw new UnsupportedOperationException("body required");
    }

    @Override
    public AdminLoginResponse parseError(int responseCode,
            String responseMessage, Map<String, List<String>> headerFields,
            byte[] errorBody) {
        try {
            Error err = (Error) deserialize(new ByteArrayInputStream(errorBody));
            EsuException ee = new EsuException(err.getMessage(), responseCode);
            AdminLoginResponse r = new AdminLoginResponse(ee);
            r.setResponseHeaders(headerFields);
            return r;
        } catch (Exception e) {
            l4j.debug("Failed to parse response", e);
            // Just do it without anything else
            EsuException ee = new EsuException(responseMessage, responseCode);
            AdminLoginResponse r = new AdminLoginResponse(ee);
            r.setResponseHeaders(headerFields);
            return r;
        }
    }

    @Override
    public AdminLoginResponse parseError(Throwable e) {
        return new AdminLoginResponse(e);
    }

}
