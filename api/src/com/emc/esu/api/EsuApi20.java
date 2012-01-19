package com.emc.esu.api;

import java.net.URL;
import java.util.Date;

public interface EsuApi20 extends EsuApi {

	void hardLink(ObjectPath source, ObjectPath target);
	
	URL getShareableUrl(Identifier id, Date expiration, String disposition);
}
