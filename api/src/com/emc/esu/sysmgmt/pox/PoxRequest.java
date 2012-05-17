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
package com.emc.esu.sysmgmt.pox;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import com.emc.esu.api.EsuException;
import com.emc.esu.sysmgmt.SysMgmtApi;
import com.emc.esu.sysmgmt.SysMgmtUtils;

/**
 * @author cwikj
 *
 */
public abstract class PoxRequest<T extends PoxResponse> implements Callable<T> {
	private static final Logger l4j = Logger.getLogger(PoxRequest.class);
	
	public static final String ACCEPT_HEADER = "Accept";
	public static final String POX_MIME = "application/xml";
	public static final String COOKIE_HEADER = "Cookie";
	public static final String SESSION_NAME = "_gui_session_id";
	
	private SysMgmtApi api;

	public PoxRequest(SysMgmtApi api) {
		this.api = api;
	}
	
	protected HttpURLConnection getConnection(String path, String query) 
			throws IOException, URISyntaxException {
		
        URI uri = new URI( api.getProto(), null, api.getHost(), 
        		api.getPort(), path, query, null );
        l4j.debug("URI: " + uri);
        URL u = new URL(uri.toASCIIString());
        l4j.debug( "URL: " + u );

        HttpURLConnection con = (HttpURLConnection) u.openConnection();
        
        con.addRequestProperty(ACCEPT_HEADER, POX_MIME);
        con.addRequestProperty(COOKIE_HEADER, SESSION_NAME + "=" + 
        		api.getPoxCookie());
        
        return con;
	}
	
	protected void handleError(HttpURLConnection con) throws IOException, JDOMException {
		int httpCode = con.getResponseCode();
		String msg = con.getResponseMessage();
		
        byte[] response = SysMgmtUtils.readResponse(con);
        l4j.debug("Error response: " + new String(response, "UTF-8"));
        SAXBuilder sb = new SAXBuilder();

        Document d = sb.build(new ByteArrayInputStream(response));

        String code = d.getRootElement().getChildText("Code");
        String message = d.getRootElement().getChildText("Message");

        if (code == null && message == null) {
            // not an error from ESU
            throw new EsuException(msg, httpCode);
        }

        l4j.debug("Error: " + code + " message: " + message);
        throw new EsuException(message, httpCode, Integer.parseInt(code));
	}

}
