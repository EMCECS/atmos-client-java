package com.emc.atmos.mgmt;

import com.emc.atmos.AbstractConfig;

import java.net.URI;
import java.util.List;
import java.util.Map;

public abstract class AtmosMgmtConfig extends AbstractConfig {
    private static final String DEFAULT_CONTEXT = "/sysmgmt";

    private String username;
    private String password;

    public AtmosMgmtConfig() {
        super( DEFAULT_CONTEXT );
    }

    public AtmosMgmtConfig( String username, String password, URI... endpoints ) {
        super( DEFAULT_CONTEXT, endpoints );
        this.username = username;
        this.password = password;
    }

    public abstract Map<String, List<Object>> getAuthenticationHeaders();

    public String getUsername() {
        return username;
    }

    public void setUsername( String username ) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword( String password ) {
        this.password = password;
    }
}
