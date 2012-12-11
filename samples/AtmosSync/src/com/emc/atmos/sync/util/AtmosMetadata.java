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
package com.emc.atmos.sync.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.TimeZone;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;

import com.emc.esu.api.Acl;
import com.emc.esu.api.Grant;
import com.emc.esu.api.Grantee;
import com.emc.esu.api.Grantee.GRANT_TYPE;
import com.emc.esu.api.Metadata;
import com.emc.esu.api.MetadataList;
import com.emc.esu.api.ObjectMetadata;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Similar to the Atmos API's ObjectMetadata, but it splits the system
 * metadata out into a separate collection and supports serializing to and 
 * from a standard JSON format.
 * @author cwikj
 */
public class AtmosMetadata {
	private static final String VALUE_PROP = "value";
	private static final String LISTABLE_PROP = "listable";
	private static final String PERMISSION_PROP = "permission";
	private static final String TYPE_PROP = "type";
	private static final String NAME_PROP = "name";
	private static final String GRANTEE_PROP = "grantee";
	private static final String SYSTEM_METADATA_PROP = "systemMetadata";
	private static final String CONTENT_TYPE_PROP = "contentType";
	private static final String ACL_PROP = "acl";
	private static final String METADATA_PROP = "metadata";
    private static final String RETENTION_END_PROP = "retentionEndDate";
    private static final String EXPIRATION_PROP = "expirationDate";
    private static final Logger l4j = Logger.getLogger(AtmosMetadata.class);
	
	private MetadataList metadata;
	private MetadataList systemMetadata;
	private Acl acl;
	private String contentType;
    private Date retentionEndDate;
    private Date expirationDate;
	
	public static final String META_DIR = ".atmosmeta"; // Special subdir for Atmos metadata
	public static final String DIR_META = ".dirmeta"; // Special file for directory-level metadata


	private static final String[] SYSTEM_METADATA_TAGS = new String[] {
		"atime",
		"ctime",
		"gid",
		"itime",
		"mtime",
		"nlink",
		"objectid",
		"objname",
		"policyname",
		"size",
		TYPE_PROP,
		"uid",
		"x-emc-wschecksum"
	};
	private static final Set<String> SYSTEM_TAGS = 
			Collections.unmodifiableSet(
					new HashSet<String>(Arrays.asList(SYSTEM_METADATA_TAGS)));

	/**
	 * Creates an instance of AtmosMetadata based on an ObjectMetadata
	 * retrieved through the Atmos API.  This separates the system metadata
	 * from the user metadata.
	 * 
	 * @param om the Object Metadata
	 * @return an AtmosMetadata
	 */
	public static AtmosMetadata fromObjectMetadata(ObjectMetadata om) {
		AtmosMetadata meta = new AtmosMetadata();
		
		MetadataList umeta = new MetadataList();
		MetadataList smeta = new MetadataList();
		for(Metadata m : om.getMetadata()) {
			if(SYSTEM_TAGS.contains(m.getName())) {
				smeta.addMetadata(m);
			} else {
				umeta.addMetadata(m);
			}
		}
		meta.setMetadata(umeta);
		meta.setSystemMetadata(smeta);
		meta.setContentType(om.getMimeType());
		meta.setAcl(om.getAcl());
		
		return meta;
	}
	
	/**
	 * For a given file, returns the appropriate file that should contain that
	 * file's AtmosMetadata.  This is a file with the same name inside the
	 * .atmosmeta subdirectory.  If the file is a directory, it's 
	 * ./.atmosmeta/.dirmeta.
	 * @param f the file to compute the metadata file name from
	 * @return the file that should contain this file's metadata.  The file
	 * may not exist.
	 */
	public static File getMetaFile(File f) {
		if(f.isDirectory()) {
			return new File(new File(f, META_DIR), DIR_META);
		} else {
			return new File(new File(f.getParentFile(), META_DIR), f.getName());
		}
	}
	
	/**
	 * Reads the given metadata file and builds an AtmosMetadata from the
	 * file's JSON contents.  
	 * @param metaFile the metadata file, see getMetaFile().
	 * @return the AtmosMetadata object.
	 * @throws IOException if reading the file fails
	 */
	public static AtmosMetadata fromFile(File metaFile) throws IOException {
		BufferedReader br = null;
		
		try {
			StringBuffer sb = new StringBuffer();
			br = new BufferedReader(new InputStreamReader(
				new FileInputStream(metaFile), "UTF-8"));
			String s = null;
			while((s = br.readLine()) != null) {
				sb.append(s);
				sb.append("\n");
			}
			return fromJson(sb.toString());
		} finally {
			if(br != null) {
				br.close();
			}
		}
	}
	

	public static AtmosMetadata fromJson(String json) {
		AtmosMetadata am = new AtmosMetadata();
		JsonParser jp = new JsonParser();
		
		JsonElement je = jp.parse(json);
		JsonObject mdata = (JsonObject)je;
		
		JsonObject jsonMetadata = (JsonObject) mdata.get(METADATA_PROP);
		JsonArray jsonAcl = (JsonArray) mdata.get(ACL_PROP);
		JsonElement jsonMime = mdata.get(CONTENT_TYPE_PROP);
		JsonObject jsonSysmeta = (JsonObject) mdata.get(SYSTEM_METADATA_PROP);
        JsonElement jsonRetentionEnd = mdata.get(RETENTION_END_PROP);
        JsonElement jsonExpiration = mdata.get(EXPIRATION_PROP);
		
		if(jsonMetadata != null) {
			am.setMetadata(decodeMetadata(jsonMetadata));
		} else {
			am.setMetadata(new MetadataList());
		}
		if(jsonAcl != null) {
			am.setAcl(decodeAcl(jsonAcl));
		} else {
			am.setAcl(null);
		}
		if(jsonMime != null) {
			am.setContentType(jsonMime.getAsString());
		}
		if(jsonSysmeta != null) {
			am.setSystemMetadata(decodeMetadata(jsonSysmeta));
		} else {
			am.setSystemMetadata(new MetadataList());
		}
        if (jsonRetentionEnd != null)
            am.setRetentionEndDate(Iso8601Util.parse(jsonRetentionEnd.getAsString()));
        if (jsonExpiration != null)
            am.setExpirationDate(Iso8601Util.parse(jsonExpiration.getAsString()));

        return am;
	}
	
	private static Acl decodeAcl(JsonArray jsonAcl) {
		Acl acl = new Acl();
		
		for(JsonElement ele : jsonAcl) {
			JsonObject jo = (JsonObject)ele;
			JsonObject jgrantee = (JsonObject) jo.get(GRANTEE_PROP);
			
			Grantee grantee = new Grantee(jgrantee.get(NAME_PROP).getAsString(), 
					GRANT_TYPE.valueOf(jgrantee.get(TYPE_PROP).getAsString()));
			Grant g = new Grant(grantee, jo.get(PERMISSION_PROP).getAsString());
			acl.addGrant(g);
		}
		
		return acl;
	}

	private static MetadataList decodeMetadata(JsonObject jsonMetadata) {
		MetadataList mlist = new MetadataList();
		
		for(Entry<String, JsonElement> ent : jsonMetadata.entrySet()) {
			String name = ent.getKey();
			JsonObject value = (JsonObject) ent.getValue();
			boolean listable = value.get(LISTABLE_PROP).getAsBoolean();
			String mvalue = value.get(VALUE_PROP).getAsString();
			Metadata m = new Metadata(name, mvalue, listable);
			mlist.addMetadata(m);
		}
		
		return mlist;
	}
	
	public AtmosMetadata() {
		metadata = new MetadataList();
		systemMetadata = new MetadataList();
	}


	/**
	 * @return the metadata
	 */
	public MetadataList getMetadata() {
		return metadata;
	}

	/**
	 * @param metadata the metadata to set
	 */
	public void setMetadata(MetadataList metadata) {
		this.metadata = metadata;
	}

	/**
	 * @return the systemMetadata
	 */
	public MetadataList getSystemMetadata() {
		return systemMetadata;
	}

	/**
	 * @param systemMetadata the systemMetadata to set
	 */
	public void setSystemMetadata(MetadataList systemMetadata) {
		this.systemMetadata = systemMetadata;
	}

	/**
	 * @return the acl
	 */
	public Acl getAcl() {
		return acl;
	}

	/**
	 * @param acl the acl to set
	 */
	public void setAcl(Acl acl) {
		this.acl = acl;
	}

	/**
	 * @return the contentType
	 */
	public String getContentType() {
		return contentType;
	}

	/**
	 * @param contentType the contentType to set
	 */
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

    public Date getRetentionEndDate() {
        return retentionEndDate;
    }

    public void setRetentionEndDate( Date retentionEndDate ) {
        this.retentionEndDate = retentionEndDate;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate( Date expirationDate ) {
        this.expirationDate = expirationDate;
    }

    /**
	 * Convenience method to locate and parse the mtime attribute into a 
	 * Java Date object.  If the mtime cannot be found or parsed, null will
	 * be returned.
	 */
	public Date getMtime() {
		if(systemMetadata.getMetadata("mtime") == null) {
			return null;
		}
		String mtime = systemMetadata.getMetadata("mtime").getValue();
		return Iso8601Util.parse( mtime );
	}
	
	public void setMtime(Date mtime) {
		String smtime = Iso8601Util.format(mtime);
		systemMetadata.addMetadata(new Metadata("mtime", smtime, false));
	}
	
	/**
	 * Convenience method to locate and parse the ctime attribute into a 
	 * Java Date object.  If the ctime cannot be found or parsed, null will
	 * be returned.
	 */
	public Date getCtime() {
		if(systemMetadata.getMetadata("ctime") == null) {
			return null;
		}
		String ctime = systemMetadata.getMetadata("ctime").getValue();
		return Iso8601Util.parse( ctime );
	}
	
	
	public String toJson() {
		JsonObject root = new JsonObject();
		JsonObject metadata = new JsonObject();
		root.add("metadata", metadata);
		JsonObject sysmeta = new JsonObject();
		root.add(SYSTEM_METADATA_PROP, sysmeta);
		JsonArray acl = new JsonArray();
		root.add(ACL_PROP, acl);
		root.addProperty(CONTENT_TYPE_PROP, contentType);
        if (retentionEndDate != null)
            root.addProperty(RETENTION_END_PROP, Iso8601Util.format(retentionEndDate));
		if (expirationDate != null)
            root.addProperty(EXPIRATION_PROP, Iso8601Util.format(expirationDate));

		writeMetadata(this.metadata, metadata);
		writeMetadata(this.systemMetadata, sysmeta);
		writeAcl(this.acl, acl);
		
		Gson gs = new Gson();
		return gs.toJson(root);
	}
	
	public void toFile(File metaFile) throws IOException {
		PrintWriter pw = null;
		
		try {
			pw = new PrintWriter(new OutputStreamWriter(
					new FileOutputStream(metaFile), "UTF-8"));
			pw.println(toJson());
		} finally {
			if(pw != null) {
				pw.close();
			}
		}
	}
	
	private void writeAcl(Acl acl, JsonArray jacl) {
		if(acl == null) {
			return;
		}
		for(Grant g : acl) {
			JsonObject jg = new JsonObject();
			jg.addProperty(PERMISSION_PROP, g.getPermission());
			JsonObject jgrantee = new JsonObject();
			jgrantee.addProperty(NAME_PROP, g.getGrantee().getName());
			jgrantee.addProperty(TYPE_PROP, g.getGrantee().getType().toString());
			jg.add(GRANTEE_PROP, jgrantee);
			jacl.add(jg);
		}
	}

	private void writeMetadata(MetadataList metadata, JsonObject jmetadata) {
		if(metadata == null) {
			return;
		}
		for(Metadata m : metadata) {
			JsonObject jm = new JsonObject();
			jm.addProperty(VALUE_PROP, m.getValue());
			jm.addProperty(LISTABLE_PROP, m.isListable());
			jmetadata.add(m.getName(), jm);	
		}
	}


}
