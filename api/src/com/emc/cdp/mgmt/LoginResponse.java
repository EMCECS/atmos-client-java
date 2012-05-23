package com.emc.cdp.mgmt;

import com.emc.util.HttpUtil;

import java.io.IOException;
import java.net.HttpURLConnection;

public class LoginResponse extends CdpMgmtResponse {
    private String sessionToken;

    public LoginResponse( HttpURLConnection con ) throws IOException {
        sessionToken = HttpUtil.readResponseString( con );
    }

    public String getSessionToken() {
        return this.sessionToken;
    }
}
