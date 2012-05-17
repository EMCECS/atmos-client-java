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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;

import com.emc.esu.api.EsuException;
import com.emc.esu.sysmgmt.SysMgmtUtils;

/**
 * @author cwikj
 *
 */
public class ListRmgResponsePox extends PoxResponse {
	private List<Rmg> rmgs;
	
	public ListRmgResponsePox(HttpURLConnection con) throws IOException, JDOMException {
		
		// Parse response
		Document doc = SysMgmtUtils.parseResponseXml(con);
		
		Element root = doc.getRootElement(); //rmgList
		
		
		rmgs = new ArrayList<Rmg>();
		
		List<?> rmgsXml = root.getChildren("rmg");
		// Error check
		if(rmgsXml.size() < 1) {
			setSuccessful(false);
			setError(root.getTextTrim());
			return;
		}
		
		for(Object o : rmgsXml) {
			if(!(o instanceof Element)) {
				throw new EsuException("Expected XML Element got " + o.getClass());
			}
			
			Element e = (Element)o;
			
			Rmg r = new Rmg();
			r.setName(e.getChildText("name"));
			r.setId(Integer.parseInt(e.getChildText("id")));
			r.setLocation(e.getChildText("location"));
			r.setCapacity(e.getChildText("capacity"));
			r.setMulticastAddress(e.getChildText("multicast_address"));
			rmgs.add(r);
		}
	}


	public static class Rmg {
		private String name;
		private int id;
		private String location;
		private String capacity;
		private String multicastAddress;
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
		 * @return the id
		 */
		public int getId() {
			return id;
		}
		/**
		 * @param id the id to set
		 */
		public void setId(int id) {
			this.id = id;
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
		/**
		 * @return the capacity
		 */
		public String getCapacity() {
			return capacity;
		}
		/**
		 * @param capacity the capacity to set
		 */
		public void setCapacity(String capacity) {
			this.capacity = capacity;
		}
		/**
		 * @return the multicastAddress
		 */
		public String getMulticastAddress() {
			return multicastAddress;
		}
		/**
		 * @param multicastAddress the multicastAddress to set
		 */
		public void setMulticastAddress(String multicastAddress) {
			this.multicastAddress = multicastAddress;
		}
	}


	/**
	 * @return the rmgs
	 */
	public List<Rmg> getRmgs() {
		return rmgs;
	}
}
