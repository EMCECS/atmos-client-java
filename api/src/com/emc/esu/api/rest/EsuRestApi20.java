package com.emc.esu.api.rest;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;

import com.emc.esu.api.EsuApi20;
import com.emc.esu.api.EsuException;
import com.emc.esu.api.ObjectPath;

public class EsuRestApi20 extends EsuRestApi implements EsuApi20 {

	public EsuRestApi20(String host, int port, String uid, String sharedSecret) {
		super(host, port, uid, sharedSecret);
	}

	@Override
	public void hardLink(ObjectPath source, ObjectPath target) {
        try {
            String resource = getResourcePath(context, source);
            String query = "hardlink";
            URL u = buildUrl(resource, query);
            HttpURLConnection con = (HttpURLConnection) u.openConnection();

            // Build headers
            Map<String, String> headers = new HashMap<String, String>();

            headers.put("x-emc-uid", uid);

            String destPath = target.toString();
            if (destPath.startsWith("/"))
            {
                destPath = destPath.substring(1);
            }
            headers.put("x-emc-path", destPath);

            // Add date
            headers.put("Date", getDateHeader());
            
            // Compute checksum
            // Sign request
            signRequest("POST", resource, query, headers);
            configureRequest( con, "POST", headers );

            con.connect();

            // Check response
            if (con.getResponseCode() > 299) {
                handleError(con);
            }

            con.disconnect();

        } catch (MalformedURLException e) {
            throw new EsuException("Invalid URL", e);
        } catch (IOException e) {
            throw new EsuException("Error connecting to server", e);
        } catch (GeneralSecurityException e) {
            throw new EsuException("Error computing request signature", e);
        } catch (URISyntaxException e) {
            throw new EsuException("Invalid URL", e);
        }

	}

}
