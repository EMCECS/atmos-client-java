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
package com.emc.esu.sysmgmt;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;

import org.jdom.JDOMException;

import com.emc.esu.api.EsuException;

/**
 * @author cwikj
 *
 */
public class ListRmgRequest extends SysMgmtRequest<ListRmgResponse> {

	public ListRmgRequest(SysMgmtApi atmosSysMgmtApi) {
		super(atmosSysMgmtApi);
	}

	@Override
	public ListRmgResponse call() {
		HttpURLConnection con = null;
		try {
			con = getConnection("/sysmgmt/rmgs", null);
			
			con.connect();
			
			int code = con.getResponseCode();
			if(code != 200) {
				handleError(con);
			}
			
			return new ListRmgResponse(con);
			
		} catch (IOException e) {
			throw new EsuException("Error connecting to server: " + e.getMessage(), e);
		} catch (URISyntaxException e) {
			throw new EsuException("Error building URI: " + e.getMessage(), e);
		} catch (JDOMException e) {
			throw new EsuException("Error parsing XML response", e);
		}

	}
	
}
