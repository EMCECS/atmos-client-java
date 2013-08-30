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
import java.util.ArrayList;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;

import com.emc.esu.api.EsuException;

/**
 * @author cwikj
 *
 */
public class ListHostsResponse extends SysMgmtResponse {
	private List<Host> hosts;

	public ListHostsResponse(HttpURLConnection con) throws IOException, JDOMException {
		super(con);
		
		// Parse response
		Document doc = SysMgmtUtils.parseResponseXml(con);
		
		Element root = doc.getRootElement(); //rmgList
		
		hosts = new ArrayList<ListHostsResponse.Host>();
		
		List<?> hostsXml = root.getChildren("node");
		for(Object o : hostsXml) {
			if(!(o instanceof Element)) {
				throw new EsuException("Expected XML Element got " + o.getClass());
			}
			
			Element e = (Element)o;
			
			Host h = new Host();
			
			h.setName(e.getAttributeValue("name"));
			h.setUp(Boolean.parseBoolean(e.getAttributeValue("up")));
			h.setLocation(e.getAttributeValue("location"));
			
			hosts.add(h);
		}

	}
	
	/**
	 * @return the hosts
	 */
	public List<Host> getHosts() {
		return hosts;
	}

	public class Host {
		private String name;
		private boolean up;
		private String location;
		/**
		 * @return the name
		 */
		public String getName() {
			return name;
		}
		/**
		 * @param name the name to set
		 */
		public void setName(String name) {
			this.name = name;
		}
		/**
		 * @return the up
		 */
		public boolean isUp() {
			return up;
		}
		/**
		 * @param up the up to set
		 */
		public void setUp(boolean up) {
			this.up = up;
		}
		/**
		 * @return the location
		 */
		public String getLocation() {
			return location;
		}
		/**
		 * @param location the location to set
		 */
		public void setLocation(String location) {
			this.location = location;
		}
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "Host [name=" + name + ", up=" + up + ", location="
					+ location + "]";
		}
		
	}

}
