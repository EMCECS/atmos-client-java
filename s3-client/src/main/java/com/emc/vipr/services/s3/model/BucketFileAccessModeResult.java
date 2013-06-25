package com.emc.vipr.services.s3.model;

import java.util.List;

import com.emc.vipr.services.s3.model.ViPRConstants.FileAccessMode;

public class BucketFileAccessModeResult {
	private FileAccessMode accessMode;
	private long duration;
	private List<String> hostList;
	private String uid;
	private String token;
	
	/**
	 * @return the accessMode
	 */
	public FileAccessMode getAccessMode() {
		return accessMode;
	}
	/**
	 * @param accessMode the accessMode to set
	 */
	public void setAccessMode(FileAccessMode accessMode) {
		this.accessMode = accessMode;
	}
	/**
	 * @return the duration
	 */
	public long getDuration() {
		return duration;
	}
	/**
	 * @param duration the duration to set
	 */
	public void setDuration(long duration) {
		this.duration = duration;
	}
	/**
	 * @return the hostList
	 */
	public List<String> getHostList() {
		return hostList;
	}
	/**
	 * @param hostList the hostList to set
	 */
	public void setHostList(List<String> hostList) {
		this.hostList = hostList;
	}
	/**
	 * @return the uid
	 */
	public String getUid() {
		return uid;
	}
	/**
	 * @param uid the uid to set
	 */
	public void setUid(String uid) {
		this.uid = uid;
	}
	/**
	 * @return the token
	 */
	public String getToken() {
		return token;
	}
	/**
	 * @param token the token to set
	 */
	public void setToken(String token) {
		this.token = token;
	}
	
	
}
