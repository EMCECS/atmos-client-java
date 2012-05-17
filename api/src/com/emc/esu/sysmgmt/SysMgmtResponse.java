package com.emc.esu.sysmgmt;

import java.net.HttpURLConnection;
import java.util.Date;

/**
 * Base class for system management responses.
 * @author cwikj
 *
 */
public class SysMgmtResponse {
	private static final String ATMOS_SYS_MGMT_VERSION = "x-atmos-sysmgmt-version";
	private static final String ATMOS_SYS_MGMT_VERSION_TYPO1 = "x-atoms-sysmgmt-version";
	private static final String ATMOS_SYS_MGMT_VERSION_TYPO2 = "x-atoms-sysmgnt-version";

	private String atmosSysMgmgtVersion;
	private Date serverDate;

	public SysMgmtResponse(HttpURLConnection response) {
		this.atmosSysMgmgtVersion = response.getHeaderField(ATMOS_SYS_MGMT_VERSION);
		if(this.atmosSysMgmgtVersion == null) {
			this.atmosSysMgmgtVersion = response.getHeaderField(ATMOS_SYS_MGMT_VERSION_TYPO1);
		}
		if(this.atmosSysMgmgtVersion == null) {
			this.atmosSysMgmgtVersion = response.getHeaderField(ATMOS_SYS_MGMT_VERSION_TYPO2);
		}
		this.serverDate = new Date(response.getDate());
	}
	
	public Date getServerDate() {
		return serverDate;
	}
	
	public String getAtmosSysMgmtVersion() {
		return atmosSysMgmgtVersion;
	}

}
