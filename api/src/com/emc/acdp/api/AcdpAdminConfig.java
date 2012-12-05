package com.emc.acdp.api;

import com.emc.acdp.AcdpConfig;

public class AcdpAdminConfig extends AcdpConfig {
    public AcdpAdminConfig() {
    }

    public AcdpAdminConfig( String proto, String host, int port, String username, String password ) {
        super( proto, host, port, username, password );
    }

    @Override
    public String getLoginPath() {
        return "/cdp-rest/v1/admin/login";
    }

    @Override
    public boolean isSecureRequest( String path, String method ) {
        return true;
    }
}
