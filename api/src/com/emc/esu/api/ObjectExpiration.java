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

import java.util.Date;
import java.util.List;

import org.jdom.Element;
import org.jdom.Namespace;

/**
 * Encapsulates the object's expriation information
 */
public class ObjectExpiration {
	private boolean enabled;
	private Date endAt;
	
	public ObjectExpiration() {
	}
	
	@SuppressWarnings("rawtypes")
	public ObjectExpiration(Element element) {
        Namespace esuNs = Namespace.getNamespace( "http://www.emc.com/cos/" );
        
        // Parse Enabled flag
        List children = element.getChildren( "enabled", esuNs );
        if( children == null || children.size() < 1 ) {
        	throw new EsuException( "id not found in replica" );
        }
        enabled = "true".equals(((Element)children.get(0)).getTextTrim());
        
        children = element.getChildren( "endAt", esuNs );
        if( children == null || children.size() < 1 ) {
        	throw new EsuException( "endAt not found in replica" );
        }
        endAt = ObjectId.parseXmlDate(((Element)children.get(0)).getTextTrim());
	}
	/**
	 * @return the enabled
	 */
	public boolean isEnabled() {
		return enabled;
	}
	/**
	 * @param enabled the enabled to set
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	/**
	 * @return the endAt
	 */
	public Date getEndAt() {
		return endAt;
	}
	/**
	 * @param endAt the endAt to set
	 */
	public void setEndAt(Date endAt) {
		this.endAt = endAt;
	}
}
