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
package com.emc.atmos.sync.plugins;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.log4j.Logger;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.emc.esu.api.EsuException;
import com.emc.esu.api.Identifier;
import com.emc.esu.api.Metadata;
import com.emc.esu.api.MetadataList;
import com.emc.esu.api.MetadataTag;
import com.emc.esu.api.MetadataTags;

/**
 * @author cwikj
 * 
 */
public class AtmosDeletePlugin extends SyncPlugin {
	private static final Logger l4j = Logger.getLogger(AtmosDeletePlugin.class);

	public static final String DELETE_OPT = "atmos-delete";
	public static final String DELETE_OPT_DESC = "Enables the AtmosDelete plugin.";

	public static final String DELETE_TAGS_OPT = "delete-listable-tags";
	public static final String DELETE_TAGS_DESC = "if set, any listable tags will be deleted first before deleting the object";

	private boolean deleteTags = false;

	private AtmosSource source;

	@Override
	public void filter(SyncObject obj) {
		Identifier id;
		SourceAtmosId idAnn = (SourceAtmosId) obj.getAnnotation(SourceAtmosId.class);
		if(idAnn.getId() != null) {
			id = idAnn.getId();
		} else {
			id = idAnn.getPath();
		}
		
		if(!deleteTags) {
			try {
				l4j.debug("Deleting " + id);
				source.getAtmos().deleteObject(id);
			} catch(EsuException e) {
				if(e.getHttpCode() == 404) {
					// Good (already deleted)
					l4j.debug("Object already deleted");
					getNext().filter(obj);
					return;
				} else {
					throw e;
				}
			}
		} else {
			try {
				MetadataList mlist = source.getAtmos().getUserMetadata(id, null);
				for(Metadata m : mlist) {
					if(m.isListable()) {
						MetadataTags mt = new MetadataTags();
						mt.addTag(new MetadataTag(m.getName(), true));
						l4j.debug("Deleting tag " + m.getName() + " from " + id);
						try {
							source.getAtmos().deleteUserMetadata(id, mt);
						} catch(EsuException e) {
							if(e.getAtmosCode() == 1005) {
								// Already deleted
								l4j.warn("Tag " + m.getName() + " already deleted (Atmos code 1005)");
							} else {
								throw e;
							}
						}
					}
					l4j.debug("Deleting " + id);
					source.getAtmos().deleteObject(id);
				}
			} catch(EsuException e) {
				if(e.getHttpCode() == 404) {
					// Good (already deleted)
					l4j.debug("Object already deleted");
					getNext().filter(obj);
					return;
				} else {
					throw e;
				}
			}
		}
		getNext().filter(obj);

	}

	@SuppressWarnings("static-access")
	@Override
	public Options getOptions() {
		Options opts = new Options();

		opts.addOption(OptionBuilder.withLongOpt(DELETE_OPT)
				.withDescription(DELETE_OPT_DESC).create());
		opts.addOption(OptionBuilder.withLongOpt(DELETE_TAGS_OPT)
				.withDescription(DELETE_TAGS_DESC).create());

		return opts;
	}

	@Override
	public boolean parseOptions(CommandLine line) {
		if (line.hasOption(DELETE_TAGS_OPT)) {
			deleteTags = true;
		}

		return line.hasOption(DELETE_OPT);
	}

	@Override
	public void validateChain(SyncPlugin first) {
		// Source must be AtmosSource and destination must be DummyDestination
		SyncPlugin source = first;
		SyncPlugin dest = null;
		while (first != null) {
			dest = first;
			first = first.getNext();
		}

		if (!(source instanceof AtmosSource)) {
			throw new IllegalArgumentException(
					"Source must be an AtmosSource to use AtmosDeletePlugin");
		}
		if (!(dest instanceof DummyDestination)) {
			throw new IllegalArgumentException(
					"Destination must be a DummyDestination to use AtmosDeletePlugin");
		}
		
		this.source = (AtmosSource) source;
	}

	@Override
	public String getName() {
		return "Atmos Delete";
	}

	@Override
	public String getDocumentation() {
		return "This plugin can be combined with an AtmosSource and a " +
				"DummyDestination to delete objects from Atmos.  It can handle " +
				"retries, extended timeouts, and can delete listable tags " +
				"before deleting the object.";
	}

	
	/**
	 * This app can be used to build a tracking database.  See the sample
	 * spring/atmos-delete.xml for the DDL and queries.
	 * @param args the first argument is the Spring configuration file.  We will
	 * get the DataSource from here.  The second argument is a list of
	 * ObjectIDs.
	 */
	public static void main(String[] args) {
		File springXml = new File(args[0]);
		if(!springXml.exists()) {
			System.err.println("The Spring XML file: " + springXml + " does not exist");
			System.exit(1);
		}
		
		l4j.info("Loading configuration from Spring XML file: " + springXml);
		FileSystemXmlApplicationContext ctx = 
				new FileSystemXmlApplicationContext(args[0]);
		
		if(!ctx.containsBean("dataSource")) {
			System.err.println("Your Spring XML file: " + springXml + 
					" must contain one bean named 'dataSource' that " +
					"initializes a DataSource object");
			System.exit(1);
		}
		
		try {
			DataSource ds = (DataSource) ctx.getBean("dataSource");
			File idFile = new File(args[1]);
			BufferedReader br = new BufferedReader(new FileReader(idFile));
			String id;
			int count = 0;
			Connection con = ds.getConnection();
			PreparedStatement ps = con.prepareStatement("INSERT INTO object_list(oid) VALUES(?)");
			while((id = br.readLine()) != null) {
				id = id.trim();
				if(id.length()<1) {
					continue;
				}
				ps.setString(1, id);
				try {
					ps.executeUpdate();
				} catch(SQLException e) {
					if(e.getErrorCode() == 1062) {
						// dupe
						continue;
					}
					l4j.warn("SQL Error: " + e.getErrorCode());
					throw e;
				}
				count++;
				if(count % 1000 == 0) {
					System.out.println("Inserted " + count + " records");
				}
			}
			System.out.println("COMPLETE. Inserted " + count + " records");
			System.exit(0);
		} catch(Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		

	}

	/**
	 * @return the deleteTags
	 */
	public boolean isDeleteTags() {
		return deleteTags;
	}

	/**
	 * @param deleteTags the deleteTags to set
	 */
	public void setDeleteTags(boolean deleteTags) {
		this.deleteTags = deleteTags;
	}
}
