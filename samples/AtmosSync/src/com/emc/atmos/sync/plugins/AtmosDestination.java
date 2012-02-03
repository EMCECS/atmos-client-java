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

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;

import com.emc.esu.api.EsuApi;
import com.emc.esu.api.EsuException;
import com.emc.esu.api.Metadata;
import com.emc.esu.api.MetadataList;
import com.emc.esu.api.ObjectId;
import com.emc.esu.api.ObjectMetadata;
import com.emc.esu.api.ObjectPath;
import com.emc.esu.api.ServiceInformation;
import com.emc.esu.api.rest.LBEsuRestApiApache;

/**
 * Stores objects into an Atmos system.
 * @author cwikj
 */
public class AtmosDestination extends DestinationPlugin {
	/**
	 * This pattern is used to activate this plugin.
	 */
	public static final String URI_PATTERN = "^(http|https)://([a-zA-Z0-9/\\-]+):([a-zA-Z0-9\\+/=]+)@([^/]*?)(:[0-9]+)?(?:/)?$";
	
	public static final String DEST_NAMESPACE_OPTION = "dest-namespace";
	public static final String DEST_NAMESPACE_DESC = "The destination within the Atmos namespace.  Note that a directory must end with a trailing slash (e.g. /dir1/dir2/) otherwise it will be interpreted as a single file (only useful for transferring a single file).";
	public static final String DEST_NAMESPACE_ARG_NAME = "atmos-path";
	
	public static final String ISO_8601 = "yyyy-MM-dd'T'HH:mm:ss'Z'";
	
	private static final Logger l4j = Logger.getLogger(AtmosDestination.class);

	private String destNamespace;
	private List<String> hosts;
	private String protocol;
	private int port;
	private String uid;
	private String secret;
	private EsuApi atmos;
	private boolean force;
	private DateFormat iso8601;
	
	public AtmosDestination() {
		super();
		iso8601 = new SimpleDateFormat(ISO_8601);
	}
	
	/**
	 * @see com.emc.atmos.sync.plugins.SyncPlugin#filter(com.emc.atmos.sync.plugins.SyncObject)
	 */
	@Override
	public void filter(SyncObject obj) {
		try {
			if(destNamespace != null) {
				// Determine a name for the object.
				ObjectPath destPath = null;
				if(!destNamespace.endsWith("/")) {
					// A specific file was mentioned.
					destPath = new ObjectPath(destNamespace);
				} else {
					destPath = new ObjectPath(destNamespace + obj.getRelativePath());
				}
				
				obj.setDestURI(new URI(protocol, uid + ":" + secret, 
						hosts.get(0), port, destPath.toString(), null, null));
				
				// See if the destination exists
				if(destPath.isDirectory()) {
					MetadataList smeta = getSystemMetadata(destPath);
					
					if(smeta != null && obj.getMetadata().getSystemMetadata() != null) {
						// See if a metadata update is required
						Date srcCtime = obj.getMetadata().getCtime();
						Date dstCtime = parseDate(smeta.getMetadata("ctime"));
						
						if((srcCtime != null && dstCtime != null && srcCtime.after(dstCtime)) || force) {
							if(obj.getMetadata().getMetadata() != null && obj.getMetadata().getMetadata().count()>0) {
								LogMF.debug(l4j, "Updating metadata on {0}", destPath);
								atmos.setUserMetadata(destPath, obj.getMetadata().getMetadata());
							}
							if(obj.getMetadata().getAcl() != null) {
								LogMF.debug(l4j, "Updating ACL on {0}", destPath);
								atmos.setAcl(destPath, obj.getMetadata().getAcl());
							}
						} else {
							LogMF.debug(l4j, "No changes from source {0} to dest {1}", 
									obj.getSourceURI(), 
									obj.getDestURI());
							return;
						}
					} else {
						// Directory does not exist on destination
						ObjectId id = atmos.createObjectOnPath(destPath, obj.getMetadata().getAcl(), obj.getMetadata().getMetadata(), null, null);
						DestinationAtmosId destId = new DestinationAtmosId();
						destId.setId(id);
						destId.setPath(destPath);
						obj.addAnnotation(destId);
					}
					
				} else {
					// File, not directory
					ObjectMetadata destMeta = getMetadata(destPath);
					if(destMeta == null) {
						// Destination does not exist.
						InputStream in = null;
						try {
							in = obj.getInputStream();
							ObjectId id;
							if(in == null) {
								// Create an empty object
								id = atmos.createObjectOnPath(destPath, 
										obj.getMetadata().getAcl(), obj.getMetadata().getMetadata(), 
										null, obj.getMetadata().getContentType());
							} else {
								id = atmos.createObjectFromStreamOnPath(destPath, 
									obj.getMetadata().getAcl(), obj.getMetadata().getMetadata(), in, 
									obj.getSize(), obj.getMetadata().getContentType());
							}
							
							DestinationAtmosId destId = new DestinationAtmosId();
							destId.setId(id);
							destId.setPath(destPath);
							obj.addAnnotation(destId);
						} finally {
							if(in != null) {
								in.close();
							}
						}
						
					} else {
						// Exists.  Check timestamps
						Date srcMtime = obj.getMetadata().getMtime();
						Date dstMtime = parseDate(destMeta.getMetadata().getMetadata("mtime"));
						Date srcCtime = obj.getMetadata().getMtime();
						Date dstCtime = parseDate(destMeta.getMetadata().getMetadata("ctime"));
						if((srcMtime != null && dstMtime != null && srcMtime.after(dstMtime)) || force) {
							// Update the object
							InputStream in = null;
							try {
								in = obj.getInputStream();
								if(in == null) {
									// Metadata only
									if(obj.getMetadata().getMetadata() != null && obj.getMetadata().getMetadata().count()>0) {
										LogMF.debug(l4j, "Updating metadata on {0}", destPath);
										atmos.setUserMetadata(destPath, obj.getMetadata().getMetadata());
									}
									if(obj.getMetadata().getAcl() != null) {
										LogMF.debug(l4j, "Updating ACL on {0}", destPath);
										atmos.setAcl(destPath, obj.getMetadata().getAcl());
									}
								} else {
									LogMF.debug(l4j, "Updating {0}", destPath);
									atmos.updateObjectFromStream(destPath, 
											obj.getMetadata().getAcl(), obj.getMetadata().getMetadata(), 
											null, in, obj.getSize(), 
											obj.getMetadata().getContentType());
								}
							} finally {
								if(in != null) {
									in.close();
								}
							}
							
						} else if(srcCtime != null && dstCtime != null && srcCtime.after(dstCtime)) {
							// Metadata update required.
							if(obj.getMetadata().getMetadata() != null && obj.getMetadata().getMetadata().count()>0) {
								LogMF.debug(l4j, "Updating metadata on {0}", destPath);
								atmos.setUserMetadata(destPath, obj.getMetadata().getMetadata());
							}
							if(obj.getMetadata().getAcl() != null) {
								LogMF.debug(l4j, "Updating ACL on {0}", destPath);
								atmos.setAcl(destPath, obj.getMetadata().getAcl());
							}
						} else {
							// No updates
							LogMF.debug(l4j, "No changes from source {0} to dest {1}", 
									obj.getSourceURI(), 
									obj.getDestURI());
							return;
						}
					}
				}
			} else {
				// Object Space
				InputStream in = null;
				try {
					ObjectId id = null;
					// Check and see if a destination ID was alredy computed
					Set<ObjectAnnotation> anns = obj.getAnnotations(DestinationAtmosId.class);
					if(anns.size()>0) {
						id = ((DestinationAtmosId)anns.iterator().next()).getId();
					}
					
					in = obj.getInputStream();
					if(in == null) {
						// Usually some sort of directory
						id = atmos.createObject(obj.getMetadata().getAcl(), 
								obj.getMetadata().getMetadata(), null, 
								obj.getMetadata().getContentType());
					} else {
						id = atmos.createObjectFromStream(obj.getMetadata().getAcl(), 
								obj.getMetadata().getMetadata(), 
								in, obj.getSize(), obj.getMetadata().getContentType());
					}
					obj.setDestURI(new URI(protocol, uid + ":"+ secret, 
							hosts.get(0), port, id.toString(), null, null));
					DestinationAtmosId destId = new DestinationAtmosId();
					destId.setId(id);
					obj.addAnnotation(destId);
				} finally {
					try {
						if(in != null) {
							in.close();
						}
					} catch (IOException e) {
						// Ignore
					}
				}
				
			}
			LogMF.debug(l4j, "Wrote source {0} to dest {1}", 
					obj.getSourceURI(), 
					obj.getDestURI());
		} catch(Exception e) {
			throw new RuntimeException(
					"Failed to store object: " + e.getMessage(), e);
		}
	}

	/**
	 * Gets the metadata for an object.  IFF the object does not exist, null
	 * is returned.  If any other error condition exists, the exception is
	 * thrown.
	 * @param destPath The object to get metadata for.
	 * @return the object's metadata or null.
	 */
	private ObjectMetadata getMetadata(ObjectPath destPath) {
		try {
			return atmos.getAllMetadata(destPath);
		} catch(EsuException e) {
			if(e.getHttpCode() == 404) {
				// Object not found
				return null;
			} else {
				// Some other error, rethrow it
				throw e;
			}
		}
	}

	/**
	 * Tries to parse an ISO-8601 date out of a metadata value.  If the value
	 * is null or the parse fails, null is returned.
	 * @param m the metadata value
	 * @return the Date or null if a date could not be parsed from the value.
	 */
	private Date parseDate(Metadata m) {
		if(m == null || m.getValue() == null) {
			return null;
		}
		try {
			synchronized(iso8601) {
				return iso8601.parse(m.getValue());
			}
		} catch(ParseException e) {
			LogMF.debug(l4j, "Failed to parse date {0}: {1}", m.getValue(), e.getMessage());
			return null;
		}
	}

	/**
	 * Get system metadata.  IFF the object doesn't exist, return null.  On any 
	 * other error (e.g. permission denied), throw exception.
	 * @param destPath
	 * @return
	 */
	private MetadataList getSystemMetadata(ObjectPath destPath) {
		try {
			return atmos.getSystemMetadata(destPath, null);
		} catch(EsuException e) {
			if(e.getAtmosCode() == 1003) {
				// Object not found --OK
				return null;
			} else {
				throw new RuntimeException(
						"Error checking for object existance: " + 
								e.getMessage(), e);
			}
		}
	}

	/**
	 * @see com.emc.atmos.sync.plugins.SyncPlugin#getOptions()
	 */
	@SuppressWarnings("static-access")
	@Override
	public Options getOptions() {
		Options opts = new Options();
		
		opts.addOption(OptionBuilder.withDescription(DEST_NAMESPACE_DESC)
				.withLongOpt(DEST_NAMESPACE_OPTION).hasArg()
				.withArgName(DEST_NAMESPACE_ARG_NAME).create());
		
		return opts;
	}

	/**
	 * @see com.emc.atmos.sync.plugins.SyncPlugin#parseOptions(org.apache.commons.cli.CommandLine)
	 */
	@Override
	public boolean parseOptions(CommandLine line) {
		if(line.hasOption(CommonOptions.DESTINATION_OPTION)) {
			Pattern p = Pattern.compile(URI_PATTERN);
			String source = line.getOptionValue(CommonOptions.DESTINATION_OPTION);
			Matcher m = p.matcher(source);
			if(!m.matches()) {
				LogMF.debug(l4j, "{0} does not match {1}", source, p);
				return false;
			}
			protocol = m.group(1);
			uid = m.group(2);
			secret = m.group(3);
			String sHost = m.group(4);
			String sPort = null;
			if(m.groupCount() == 5) {
				sPort = m.group(5);
			}
			hosts = Arrays.asList(sHost.split(","));
			if(sPort != null) {
				port = Integer.parseInt(sPort.substring(1));
			} else {
				if("https".equals(protocol)) {
					port = 443;
				} else {
					port = 80;
				}
			}
			
			if(line.hasOption(CommonOptions.FORCE_OPTION)) {
				setForce(true);
			}
			
			if(line.hasOption(DEST_NAMESPACE_OPTION)) {
				destNamespace = line.getOptionValue(DEST_NAMESPACE_OPTION);
			}
			
			// Create and verify Atmos connection
			afterPropertiesSet();
			
			return true;
		}
		
		return false;
	}

	/**
	 * Initialize the Atmos connection object and validate the credentials.
	 */
	private void afterPropertiesSet() {
		atmos = new LBEsuRestApiApache(hosts, port, uid, secret);
		
		ServiceInformation info = atmos.getServiceInformation();
		LogMF.info(l4j, "Connected to Atmos {0} on {1}", info.getAtmosVersion(),
				hosts);
		
		// Use unicode if available
		if(info.isUnicodeMetadataSupported()) {
			((LBEsuRestApiApache)atmos).setUnicodeEnabled(true);
			l4j.info("Unicode metadata enabled");
		} else {
			l4j.info("The destination Atmos server does not support unicode " +
					"metadata. You may encounter errors if metadata " +
					"contains extended characters");
		}
	}

	/**
	 * @see com.emc.atmos.sync.plugins.SyncPlugin#validateChain(com.emc.atmos.sync.plugins.SyncPlugin)
	 */
	@Override
	public void validateChain(SyncPlugin first) {
		// No known incompatible plugins
	}

	/**
	 * @see com.emc.atmos.sync.plugins.SyncPlugin#getName()
	 */
	@Override
	public String getName() {
		return "Atmos Destination";
	}

	/**
	 * @see com.emc.atmos.sync.plugins.SyncPlugin#getDocumentation()
	 */
	@Override
	public String getDocumentation() {
		return "The Atmos destination plugin is triggered by the destination pattern:\n" +
				"http://uid:secret@host[:port]  or\n" +
				"https://uid:secret@host[:port]\n" +
				"Note that the uid should be the 'full token ID' including the " +
				"subtenant ID and the uid concatenated by a slash\n" +
				"If you want to software load balance across multiple hosts, " +
				"you can provide a comma-delimited list of hostnames or IPs " +
				"in the host part of the URI.\n" +
				"By default, objects will be written to Atmos using the " +
				"object API unless --dest-namespace is specified.\n" +
				"When --dest-namespace is used, the --force flag may be used " +
				"to overwrite destination objects even if they exist.";
	}

	public String getDestNamespace() {
		return destNamespace;
	}

	public void setDestNamespace(String destNamespace) {
		this.destNamespace = destNamespace;
	}

	public List<String> getHosts() {
		return hosts;
	}

	public void setHosts(List<String> hosts) {
		this.hosts = hosts;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}

	/**
	 * @return the atmos
	 */
	public EsuApi getAtmos() {
		return atmos;
	}

	/**
	 * @param atmos the atmos to set
	 */
	public void setAtmos(EsuApi atmos) {
		this.atmos = atmos;
	}

	/**
	 * @return the force
	 */
	public boolean isForce() {
		return force;
	}

	/**
	 * @param force the force to set
	 */
	public void setForce(boolean force) {
		this.force = force;
	}

}
