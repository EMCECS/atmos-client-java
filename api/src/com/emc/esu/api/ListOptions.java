// Copyright (c) 2008, EMC Corporation.
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
package com.emc.esu.api;

import java.util.List;

/**
 * Allows you to specify extended options when listing directories or listing
 * objects.  When using paged directory responses (limit > 0), the token
 * used for subsequent responses will be returned through this object.
 * @since 1.4.1
 */
public class ListOptions {
	private int limit;
	private String token;
	private List<String> userMetadata;
	private List<String> systemMetadata;
	private boolean includeMetadata;
	
	/**
	 * Returns the current results limit.  Zero indicates all results.
	 * 
	 * @return the limit
	 */
	public int getLimit() {
		return limit;
	}
	
	/**
	 * Sets the maximum number of results to fetch.  Set to zero to fetch all
	 * remaining results.
	 */
	public void setLimit(int limit) {
		this.limit = limit;
	}
	
	/**
	 * Returns the token used to request more results.  If no more results
	 * are available, the token will be null.
	 * 
	 * @return the token
	 */
	public String getToken() {
		return token;
	}
	
	/**
	 * Sets the token to request more results.  Normally, this will only
	 * be called internally by the API.
	 * 
	 * @param token the token to set
	 */
	public void setToken(String token) {
		this.token = token;
	}
	
	/**
	 * Returns true if metadata is included in the response.
	 * @return the includeMetadata
	 */
	public boolean isIncludeMetadata() {
		return includeMetadata;
	}
	
	/**
	 * Set to true if you want object metadata included in the response
	 * @param includeMetadata the includeMetadata to set
	 */
	public void setIncludeMetadata(boolean includeMetadata) {
		this.includeMetadata = includeMetadata;
	}

	/**
	 * When includeMetadata is true, returns the requested set of user metadata
	 * values to include in the result.  A null list requests all 
	 * results.
	 * @return the userMetadata requested
	 */
	public List<String> getUserMetadata() {
		return userMetadata;
	}

	/**
	 * When includeMetadata is true, sets the list of user metadata values to 
	 * include in the results.  Set to null to request all metadata.	 
	 * @param userMetadata the userMetadata to set
	 */
	public void setUserMetadata(List<String> userMetadata) {
		this.userMetadata = userMetadata;
	}

	/**
	 * When includeMetadata is true, returns the requested set of user metadata
	 * values to include in the result.  A null list requests all 
	 * results.
	 * @return the systemMetadata requested
	 */
	public List<String> getSystemMetadata() {
		return systemMetadata;
	}

	/**
	 * When includeMetadata is true, sets the list of system metadata values to 
	 * include in the results.  Set to null to request all metadata.	 
	 * @param systemMetadata the systemMetadata to set
	 */
	public void setSystemMetadata(List<String> systemMetadata) {
		this.systemMetadata = systemMetadata;
	}
	
}
