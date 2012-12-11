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

import com.emc.atmos.AtmosException;
import com.emc.atmos.sync.util.Iso8601Util;
import com.emc.esu.api.*;
import com.emc.esu.api.Checksum.Algorithm;
import com.emc.esu.api.rest.LBEsuRestApiApache;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Stores objects into an Atmos system.
 * @author cwikj
 */
public class AtmosDestination extends DestinationPlugin implements InitializingBean {
	/**
	 * This pattern is used to activate this plugin.
	 */
	public static final String URI_PATTERN = "^(http|https)://([a-zA-Z0-9/\\-]+):([a-zA-Z0-9\\+/=]+)@([^/]*?)(:[0-9]+)?(?:/)?$";
	
	public static final String DEST_NAMESPACE_OPTION = "dest-namespace";
	public static final String DEST_NAMESPACE_DESC = "The destination within the Atmos namespace.  Note that a directory must end with a trailing slash (e.g. /dir1/dir2/) otherwise it will be interpreted as a single file (only useful for transferring a single file).";
	public static final String DEST_NAMESPACE_ARG_NAME = "atmos-path";
	
	public static final String DEST_NO_UPDATE_OPTION = "no-update";
	public static final String DEST_NO_UPDATE_DESC = "If specified, no updates will be applied to the destination";

    public static final String RETENTION_DELAY_WINDOW_OPTION = "retention-delay-window";
    public static final String RETENTION_DELAY_WINDOW_DESC = "If include-retention-expiration is set, use this option to specify the Start Delay Window in the retention policy.  Default is 1 second (the minimum).";
    public static final String RETENTION_DELAY_WINDOW_ARG_NAME = "seconds";

	
	public static final String DEST_CHECKSUM_OPT = "atmos-dest-checksum";
	public static final String DEST_CHECKSUM_DESC = "If specified, the atmos wschecksum feature will be applied to uploads.  Valid algorithms are SHA0 for Atmos < 2.1 and SHA0, SHA1, or MD5 for 2.1+";
	public static final String DEST_CHECKSUM_ARG_NAME = "checksum-alg";
	
	private static final Logger l4j = Logger.getLogger(AtmosDestination.class);

	private String destNamespace;
	private List<String> hosts;
	private String protocol;
	private int port;
	private String uid;
	private String secret;
	private EsuApi atmos;
	private boolean force;
	private boolean noUpdate;
    private boolean includeRetentionExpiration;
    private long retentionDelayWindow = 1; // 1 second by default
    private String checksum;

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
							ObjectId id = null;
							if(in == null) {
								// Create an empty object
								id = atmos.createObjectOnPath(destPath, 
										obj.getMetadata().getAcl(), obj.getMetadata().getMetadata(), 
										null, obj.getMetadata().getContentType());
							} else {
								if(checksum != null) {
									Checksum ck = new Checksum(Algorithm.valueOf(checksum));
									byte[] buffer = new byte[1024*1024];
									long read = 0;
									int c = 0;
									while((c = in.read(buffer)) != -1) {
										BufferSegment bs = new BufferSegment(buffer, 0, c);
										if(read == 0) {
											// Create
											id = atmos.createObjectFromSegmentOnPath(
													destPath,
													obj.getMetadata().getAcl(), 
													obj.getMetadata().getMetadata(), 
													bs, 
													obj.getMetadata().getContentType(), 
													ck);
										} else {
											// Append
											Extent e = new Extent(read, c);
											atmos.updateObjectFromSegment(id, 
													obj.getMetadata().getAcl(), 
													obj.getMetadata().getMetadata(), 
													e, bs, 
													obj.getMetadata().getContentType(), 
													ck);
										}
										read += c;
									}
								} else {
									id = atmos.createObjectFromStreamOnPath(destPath, 
										obj.getMetadata().getAcl(), obj.getMetadata().getMetadata(), in, 
										obj.getSize(), obj.getMetadata().getContentType());
								}
							}

                            updateRetentionExpiration(obj, id);
							
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
						checkUpdate(obj, destPath, destMeta);
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
					
					if(id != null) {
						ObjectMetadata destMeta = getMetadata(id);
						if(destMeta == null) {
							// Destination ID not found!
							throw new RuntimeException("The destination object ID " + id + " was not found!");
						}
						obj.setDestURI(new URI(protocol, uid + ":"+ secret, 
								hosts.get(0), port, "/"+id.toString(), null, null));
						checkUpdate(obj, id, destMeta);
					} else {					
						in = obj.getInputStream();
						if(in == null) {
							// Usually some sort of directory
							id = atmos.createObject(obj.getMetadata().getAcl(), 
									obj.getMetadata().getMetadata(), null, 
									obj.getMetadata().getContentType());
						} else {
							if(checksum != null) {
								Checksum ck = new Checksum(Algorithm.valueOf(checksum));
								byte[] buffer = new byte[1024*1024];
								long read = 0;
								int c = 0;
								while((c = in.read(buffer)) != -1) {
									BufferSegment bs = new BufferSegment(buffer, 0, c);
									if(read == 0) {
										// Create
										id = atmos.createObjectFromSegment(
												obj.getMetadata().getAcl(), 
												obj.getMetadata().getMetadata(), 
												bs, 
												obj.getMetadata().getContentType(), 
												ck);
									} else {
										// Append
										Extent e = new Extent(read, c);
										atmos.updateObjectFromSegment(id, 
												obj.getMetadata().getAcl(), 
												obj.getMetadata().getMetadata(), 
												e, bs, 
												obj.getMetadata().getContentType(), 
												ck);
									}
									read += c;
								}
							} else {
								id = atmos.createObjectFromStream(obj.getMetadata().getAcl(), 
										obj.getMetadata().getMetadata(), 
										in, obj.getSize(), obj.getMetadata().getContentType());
							}
						}

                        updateRetentionExpiration(obj, id);

                        obj.setDestURI(new URI(protocol, uid + ":"+ secret,
								hosts.get(0), port, "/"+id.toString(), null, null));
						DestinationAtmosId destId = new DestinationAtmosId();
						destId.setId(id);
						obj.addAnnotation(destId);
					}
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
	 * If the destination exists, we perform some checks and update only what
	 * needs to be updated (metadata and/or content)
	 * @param obj
	 * @param destId
	 * @param destMeta
	 * @throws IOException
	 */
	private void checkUpdate(SyncObject obj, Identifier destId, ObjectMetadata destMeta) throws IOException {
		// Exists.  Check timestamps
		Date srcMtime = obj.getMetadata().getMtime();
		Date dstMtime = parseDate(destMeta.getMetadata().getMetadata("mtime"));
		Date srcCtime = obj.getMetadata().getMtime();
		Date dstCtime = parseDate(destMeta.getMetadata().getMetadata("ctime"));
		if((srcMtime != null && dstMtime != null && srcMtime.after(dstMtime)) || force) {
			if(noUpdate) {
				LogMF.debug(l4j, "Skipping {0}, updates disabled.", 
						obj.getSourceURI(), 
						obj.getDestURI());
				return;
			}
			// Update the object
			InputStream in = null;
			try {
				in = obj.getInputStream();
				if(in == null) {
					// Metadata only
					if(obj.getMetadata().getMetadata() != null && obj.getMetadata().getMetadata().count()>0) {
						LogMF.debug(l4j, "Updating metadata on {0}", destId);
						atmos.setUserMetadata(destId, obj.getMetadata().getMetadata());
					}
					if(obj.getMetadata().getAcl() != null) {
						LogMF.debug(l4j, "Updating ACL on {0}", destId);
						atmos.setAcl(destId, obj.getMetadata().getAcl());
					}
				} else {
					LogMF.debug(l4j, "Updating {0}", destId);
					if(checksum != null) {
						try {
							Checksum ck = new Checksum(Algorithm.valueOf(checksum));
							byte[] buffer = new byte[1024*1024];
							long read = 0;
							int c = 0;
							while((c = in.read(buffer)) != -1) {
								BufferSegment bs = new BufferSegment(buffer, 0, c);
								if(read == 0) {
									// You cannot update a checksummed object.
									// Delete and replace.
									if(destId instanceof ObjectId) {
										throw new RuntimeException(
												"Cannot update checksummed " +
												"object by ObjectID, only " +
												"namespace objects are " +
												"supported");
									}
									atmos.deleteObject(destId);
									atmos.createObjectFromSegmentOnPath(
											(ObjectPath)destId, 
											obj.getMetadata().getAcl(), 
											obj.getMetadata().getMetadata(), 
											bs,
											obj.getMetadata().getContentType(), 
											ck);
								} else {
									// Append
									Extent e = new Extent(read, c);
									atmos.updateObjectFromSegment(destId, 
											obj.getMetadata().getAcl(), 
											obj.getMetadata().getMetadata(), 
											e, bs, 
											obj.getMetadata().getContentType(), 
											ck);
								}
								read += c;
							}
						} catch (NoSuchAlgorithmException e) {
							throw new RuntimeException(
									"Incorrect checksum method: " + checksum, 
									e);
						}
					} else {
						atmos.updateObjectFromStream(destId, 
								obj.getMetadata().getAcl(), obj.getMetadata().getMetadata(), 
								null, in, obj.getSize(), 
								obj.getMetadata().getContentType());
					}
				}
			} finally {
				if(in != null) {
					in.close();
				}
			}
			
		} else if(srcCtime != null && dstCtime != null && srcCtime.after(dstCtime)) {
			if(noUpdate) {
				LogMF.debug(l4j, "Skipping {0}, updates disabled.", 
						obj.getSourceURI(), 
						obj.getDestURI());
				return;
			}
			// Metadata update required.
			if(obj.getMetadata().getMetadata() != null && obj.getMetadata().getMetadata().count()>0) {
				LogMF.debug(l4j, "Updating metadata on {0}", destId);
				atmos.setUserMetadata(destId, obj.getMetadata().getMetadata());
			}
			if(obj.getMetadata().getAcl() != null) {
				LogMF.debug(l4j, "Updating ACL on {0}", destId);
				atmos.setAcl(destId, obj.getMetadata().getAcl());
			}
		} else {
			// No updates
			LogMF.debug(l4j, "No changes from source {0} to dest {1}", 
					obj.getSourceURI(), 
					obj.getDestURI());
			return;
		}
	}

    private void updateRetentionExpiration(SyncObject obj, Identifier destId) {
        if (includeRetentionExpiration) {
            MetadataList metaList = new MetadataList();

            Date retentionEnd = obj.getMetadata().getRetentionEndDate();
            Date expiration = obj.getMetadata().getExpirationDate();

            if (retentionEnd != null) {
                try {
                    // wait for retention to kick in (it must be enabled before we can update the end-date)
                    Thread.sleep((retentionDelayWindow * 1000) + 300);
                } catch (InterruptedException e) {
                    LogMF.warn(l4j, "Interrupted while waiting for retention delay window (destId {0})", destId);
                }
                LogMF.debug(l4j, "Retention enabled (destId {0} is retained until {1})", destId, Iso8601Util.format(retentionEnd));
                metaList.addMetadata(new Metadata("user.maui.retentionEnd", Iso8601Util.format(retentionEnd), false));
            }

            if (expiration != null) {
                LogMF.debug(l4j, "Expiration enabled (destId {0} will expire at {1})", destId, Iso8601Util.format(expiration));
                metaList.addMetadata(new Metadata("user.maui.expirationEnd", Iso8601Util.format(expiration), false));
            }

            if (metaList.count() > 0) {
                try {
                    atmos.setUserMetadata(destId, metaList); // manually set retention/expiration end-dates
                } catch (AtmosException e) {
                    LogMF.error( l4j, "Failed to manually set retention/expiration\n" +
                                      "(destId: {0}, retentionEnd: {1}, expiration: {2})\n" +
                                      "[http: {3}, atmos: {4}, msg: {5}]", new Object[]{
                            destId, Iso8601Util.format( retentionEnd ), Iso8601Util.format( expiration ),
                            e.getHttpCode(), e.getErrorCode(), e.getMessage()} );
                } catch (Exception e) {
                    LogMF.error( l4j, "Failed to manually set retention/expiration\n" +
                                      "(destId: {0}, retentionEnd: {1}, expiration: {2})\n[error: {3}]", new Object[]{
                            destId, Iso8601Util.format( retentionEnd ), Iso8601Util.format( expiration ), e.getMessage()
                    } );
                }
            }
        }
    }

	/**
	 * Gets the metadata for an object.  IFF the object does not exist, null
	 * is returned.  If any other error condition exists, the exception is
	 * thrown.
	 * @param destId The object to get metadata for.
	 * @return the object's metadata or null.
	 */
	private ObjectMetadata getMetadata(Identifier destId) {
		try {
			return atmos.getAllMetadata(destId);
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
		return Iso8601Util.parse( m.getValue() );
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
		
		opts.addOption(OptionBuilder.withDescription(DEST_NO_UPDATE_DESC)
				.withLongOpt(DEST_NO_UPDATE_OPTION).create());

        opts.addOption(OptionBuilder.withDescription(RETENTION_DELAY_WINDOW_DESC)
                .withLongOpt(RETENTION_DELAY_WINDOW_OPTION).hasArg()
                .withArgName(RETENTION_DELAY_WINDOW_ARG_NAME).create());
		
		opts.addOption(OptionBuilder.withLongOpt(DEST_CHECKSUM_OPT)
				.withDescription(DEST_CHECKSUM_DESC)
				.hasArg().withArgName(DEST_CHECKSUM_ARG_NAME).create());
		
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
			
			if(line.hasOption(DEST_NO_UPDATE_OPTION)) {
				noUpdate = true;
				l4j.info("Overwrite/update destination objects disabled");
			}
			
			if(line.hasOption(DEST_CHECKSUM_OPT)) {
				checksum = line.getOptionValue(DEST_CHECKSUM_OPT);
			} else {
				checksum = null;
			}

            includeRetentionExpiration = line.hasOption(CommonOptions.INCLUDE_RETENTION_EXPIRATION_OPTION);

            if (line.hasOption(RETENTION_DELAY_WINDOW_OPTION)) {
                retentionDelayWindow = Long.parseLong(line.getOptionValue(RETENTION_DELAY_WINDOW_OPTION));
                l4j.info("Retention start delay window set to " + retentionDelayWindow);
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
	public void afterPropertiesSet() {
		atmos = new LBEsuRestApiApache(hosts, port, uid, secret);
		
		ServiceInformation info = atmos.getServiceInformation();
		LogMF.info(l4j, "Connected to Atmos {0} on {1}", info.getAtmosVersion(),
				hosts);
		
		// Use unicode if available
		if(info.isUnicodeMetadataSupported()) {
			atmos.setUnicodeEnabled(true);
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
