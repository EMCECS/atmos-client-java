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

import java.text.MessageFormat;

/**
 * Unassigns an identity from an account
 * DELETE
 * /cdp-rest/v1/admin/accounts/{account_id}/identities/{identity_id}
 * 
 * @author cwikj
 */
public class UnassignAccountIdentityRequest extends BasicAcdpRequest {
	private String accountId;
	private String identityId;
	private String adminSessionId;

	public UnassignAccountIdentityRequest(String accountId, String identityId,
			String adminSessionId) {
		this.accountId = accountId;
		this.identityId = identityId;
		this.adminSessionId = adminSessionId;
	}

	/**
	 * @see com.emc.acdp.api.request.AcdpRequest#getRequestPath()
	 */
	@Override
	public String getRequestPath() {
		return MessageFormat.format(
				"/cdp-rest/v1/admin/accounts/{0}/identities/{1}", accountId,
				identityId);
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
		return -1;
	}

	/**
	 * @see com.emc.acdp.api.request.AcdpRequest#getRequestData()
	 */
	@Override
	public byte[] getRequestData() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see com.emc.acdp.api.request.AcdpRequest#hasResponseBody()
	 */
	@Override
	public boolean hasResponseBody() {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * @return the accountId
	 */
	public String getAccountId() {
		return accountId;
	}

	/**
	 * @param accountId
	 *            the accountId to set
	 */
	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}

	/**
	 * @return the identityId
	 */
	public String getIdentityId() {
		return identityId;
	}

	/**
	 * @param identityId
	 *            the identityId to set
	 */
	public void setIdentityId(String identityId) {
		this.identityId = identityId;
	}

	/**
	 * @return the adminSessionId
	 */
	public String getAdminSessionId() {
		return adminSessionId;
	}

	/**
	 * @param adminSessionId
	 *            the adminSessionId to set
	 */
	public void setAdminSessionId(String adminSessionId) {
		this.adminSessionId = adminSessionId;
	}

}
