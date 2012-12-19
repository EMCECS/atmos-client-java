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

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;

/**
 * Encapsulates the information from the ObjectInfo call.  Contains replica,
 * retention, and expiration information.
 */
public class ObjectInfo {
	private String rawXml;
	private ObjectId objectId;
	private String selection;
	private List<ObjectReplica> replicas;
	private ObjectRetention retention;
	private ObjectExpiration expiration;
	
	public ObjectInfo() {
		replicas = new ArrayList<ObjectReplica>();
	}
	
	public ObjectInfo(String xml) {
		replicas = new ArrayList<ObjectReplica>();
		rawXml = xml;
		parse(xml);
	}

	@SuppressWarnings("rawtypes")
	public void parse(String xml) {
        // Use JDOM to parse the XML
        SAXBuilder sb = new SAXBuilder();
        try {
            Document d = sb.build( new StringReader(xml) );
            
            // The elements are part of a namespace so we need to use
            // the namespace to identify the elements.
            Namespace esuNs = Namespace.getNamespace( "http://www.emc.com/cos/" );
            
            List children = d.getRootElement().getChildren( "objectId", esuNs );
            if( children == null || children.size() < 1 ) {
            	throw new EsuException( "objectId not found in response" );
            }
            objectId = new ObjectId(((Element)children.get(0)).getTextTrim());
            
            // Parse selection
            children = d.getRootElement().getChildren( "selection", esuNs );
            if( children == null || children.size() < 1 ) {
            	throw new EsuException( "selection not found in response" );
            }
            selection = ((Element)children.get(0)).getTextTrim();
            
            // Parse replicas
            children = d.getRootElement().getChildren( "replicas", esuNs );
            if( children == null || children.size() < 1 ) {
            	throw new EsuException( "replicas not found in response" );
            }
            children = ((Element)children.get(0)).getChildren( "replica", esuNs );
            for( Iterator i = children.iterator(); i.hasNext(); ) {
            	Element replica = (Element)i.next();
            	
            	replicas.add( new ObjectReplica(replica) );
            }
            
            // Parse expiration
            children = d.getRootElement().getChildren( "expiration", esuNs );
            if( children == null || children.size() < 1 ) {
            	throw new EsuException( "expiration not found in response" );
            }
            expiration = new ObjectExpiration( (Element)children.get(0) );
            
            // Parse retention
            children = d.getRootElement().getChildren( "retention", esuNs );
            if( children == null || children.size() < 1 ) {
            	throw new EsuException( "retention not found in response" );
            }
            retention = new ObjectRetention( (Element)children.get(0) );
            
        } catch( JDOMException e ) {
        	throw new EsuException( "Error parsing object info", e );
        } catch( IOException e ) {
        	throw new EsuException( "Error parsing object info", e );
        }

	}

	/**
	 * @return the rawXml
	 */
	public String getRawXml() {
		return rawXml;
	}

	/**
	 * @param rawXml the rawXml to set
	 */
	public void setRawXml(String rawXml) {
		this.rawXml = rawXml;
	}

	/**
	 * @return the objectId
	 */
	public ObjectId getObjectId() {
		return objectId;
	}

	/**
	 * @param objectId the objectId to set
	 */
	public void setObjectId(ObjectId objectId) {
		this.objectId = objectId;
	}

	/**
	 * @return the selection
	 */
	public String getSelection() {
		return selection;
	}

	/**
	 * @param selection the selection to set
	 */
	public void setSelection(String selection) {
		this.selection = selection;
	}

	/**
	 * @return the replicas
	 */
	public List<ObjectReplica> getReplicas() {
		return replicas;
	}

	/**
	 * @param replicas the replicas to set
	 */
	public void setReplicas(List<ObjectReplica> replicas) {
		this.replicas = replicas;
	}

	/**
	 * @return the retention
	 */
	public ObjectRetention getRetention() {
		return retention;
	}

	/**
	 * @param retention the retention to set
	 */
	public void setRetention(ObjectRetention retention) {
		this.retention = retention;
	}

	/**
	 * @return the expiration
	 */
	public ObjectExpiration getExpiration() {
		return expiration;
	}

	/**
	 * @param expiration the expiration to set
	 */
	public void setExpiration(ObjectExpiration expiration) {
		this.expiration = expiration;
	}
	
}
