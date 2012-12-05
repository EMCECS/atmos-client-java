package com.emc.acdp.api;

import com.emc.acdp.AcdpConfig;

public class AcdpMgmtConfig extends AcdpConfig {
    public AcdpMgmtConfig() {
    }

    public AcdpMgmtConfig( String proto, String host, int port, String username, String password ) {
        super( proto, host, port, username, password );
    }

    @Override
    public String getLoginPath() {
        return "/cdp-rest/v1/login";
    }

    @Override
    public boolean isSecureRequest( String path, String method ) {
        if ( path.matches( "/cdp-rest/v1/identities" ) && "POST".equals( method ) )
            return false;
        return true;
    }
}
