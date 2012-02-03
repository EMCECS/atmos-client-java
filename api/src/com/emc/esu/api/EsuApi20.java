package com.emc.esu.api;

import java.net.URL;
import java.util.Date;

/**
 * Features supported by the Atmos 2.0 REST API.
 * @author cwikj
 */
public interface EsuApi20 extends EsuApi {

	/**
	 * Unsupported.
	 */
	void hardLink(ObjectPath source, ObjectPath target);
	
	/**
	 * Creates a shareable URL that sets the Content-Disposition header when
	 * streaming the content.  Requires the browser-compat feature, check 
	 * ServiceInformation for supported features.
	 * @param id the ID to create the Shareable URL for.
	 * @param expiration The expration timestamp for the URL
	 * @param disposition the value of the Content-Disposition header, e.g.
	 * "attachment; filename=\"filename.txt\""
	 * @return the new shareable URL.
	 */
	URL getShareableUrl(Identifier id, Date expiration, String disposition);
}
