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

/**
 * Deletes an ACDP account.
 * @author cwikj
 *
 */
public class DeleteAccountRequest extends BasicAcdpRequest {
    private String accountId;
    private String adminSessionId;

    public DeleteAccountRequest(String accountId, String adminSessionId) {
        this.accountId = accountId;
        this.adminSessionId = adminSessionId;
    }

    /**
     * @see com.emc.acdp.api.request.AcdpRequest#getRequestPath()
     */
    @Override
    public String getRequestPath() {
        return "/cdp-rest/v1/admin/accounts/" + accountId;
    }

    /**
     * @see com.emc.acdp.api.request.AcdpRequest#getRequestQuery()
     */
    @Override
    public String getRequestQuery() {
        return CDP_SESSION_PARAM + "=" + adminSessionId;
    }

    /**
     * @see com.emc.acdp.api.request.AcdpRequest#getMethod()
     */
    @Override
    public String getMethod() {
        return DELETE_METHOD;
    }

    /**
     * @see com.emc.acdp.api.request.AcdpRequest#getRequestSize()
     */
    @Override
    public long getRequestSize() {
        // -1 means no body at all (e.g. DELETE, GET, HEAD)
        return -1;
    }

    /**
     * @see com.emc.acdp.api.request.AcdpRequest#getRequestData()
     */
    @Override
    public byte[] getRequestData() {
        return null;
    }

    /**
     * @see com.emc.acdp.api.request.AcdpRequest#hasResponseBody()
     */
    @Override
    public boolean hasResponseBody() {
        return false;
    }

}
