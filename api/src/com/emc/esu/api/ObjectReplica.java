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
package com.emc.esu.api;

import java.util.List;

import org.jdom.Element;
import org.jdom.Namespace;

/**
 * Encapsulates the replica information on an object.
 */
public class ObjectReplica {
	private String id;
	private String location;
	private String replicaType;
	private boolean current;
	private String storageType;
	
	public ObjectReplica() {
	}
	
	@SuppressWarnings("rawtypes")
	public ObjectReplica(Element replica) {
        Namespace esuNs = Namespace.getNamespace( "http://www.emc.com/cos/" );
        
        // Parse ID
        List children = replica.getChildren( "id", esuNs );
        if( children == null || children.size() < 1 ) {
        	throw new EsuException( "id not found in replica" );
        }
        id = ((Element)children.get(0)).getTextTrim();

        // Parse location
        children = replica.getChildren( "location", esuNs );
        if( children == null || children.size() < 1 ) {
        	throw new EsuException( "location not found in replica" );
        }
        location = ((Element)children.get(0)).getTextTrim();
        
        // Parse replica type
        children = replica.getChildren( "type", esuNs );
        if( children == null || children.size() < 1 ) {
        	throw new EsuException( "type not found in replica" );
        }
        replicaType = ((Element)children.get(0)).getTextTrim();
        
        // Parse current flag 
        children = replica.getChildren( "current", esuNs );
        if( children == null || children.size() < 1 ) {
        	throw new EsuException( "current not found in replica" );
        }
        current = "true".equals(((Element)children.get(0)).getTextTrim());

        // Parse storage type
        children = replica.getChildren( "storageType", esuNs );
        if( children == null || children.size() < 1 ) {
        	throw new EsuException( "storageType not found in replica" );
        }
        storageType = ((Element)children.get(0)).getTextTrim();
	}
	
	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
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
	 * @return the replicaType
	 */
	public String getReplicaType() {
		return replicaType;
	}
	/**
	 * @param replicaType the replicaType to set
	 */
	public void setReplicaType(String replicaType) {
		this.replicaType = replicaType;
	}
	/**
	 * @return the current
	 */
	public boolean isCurrent() {
		return current;
	}
	/**
	 * @param current the current to set
	 */
	public void setCurrent(boolean current) {
		this.current = current;
	}
	/**
	 * @return the storageType
	 */
	public String getStorageType() {
		return storageType;
	}
	/**
	 * @param storageType the storageType to set
	 */
	public void setStorageType(String storageType) {
		this.storageType = storageType;
	}
	
	
}
