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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Contains information from the GetServiceInformation call
 * @author jason
 */
public class ServiceInformation {
	public static final String FEATURE_OBJECT = "object";
	public static final String FEATURE_NAMESPACE = "namespace";
	public static final String FEATURE_UTF_8 = "utf-8";
	public static final String BROWSER_COMPAT = "browser-compat";
	public static final String KEY_VALUE = "key-value";
	public static final String HARDLINK = "hardlink";
	public static final String QUERY = "query";
	public static final String VERSIONING = "versioning";
	
	private String atmosVersion;
	private boolean unicodeMetadataSupported = false;
	private Set<String> features;
	
	public ServiceInformation() {
		features = new HashSet<String>();
	}
	
	/**
	 * Adds a feature to the list of supported features.
	 */
	public void addFeature(String feature) {
		features.add(feature);
	}
	
	/**
	 * Checks to see if a feature is supported.
	 */
	public boolean hasFeature(String feature) {
		return features.contains(feature);
	}
	
	/**
	 * Gets the features advertised by the service
	 */
	public Set<String> getFeatures() {
		return Collections.unmodifiableSet(features);
	}

	/**
	 * @return the atmosVersion
	 */
	public String getAtmosVersion() {
		return atmosVersion;
	}

	/**
	 * @param atmosVersion the atmosVersion to set
	 */
	public void setAtmosVersion(String atmosVersion) {
		this.atmosVersion = atmosVersion;
	}

	public boolean isUnicodeMetadataSupported() {
		return unicodeMetadataSupported;
	}

	public void setUnicodeMetadataSupported(boolean unicodeMetadataSupported) {
		this.unicodeMetadataSupported = unicodeMetadataSupported;
	}

	
}
