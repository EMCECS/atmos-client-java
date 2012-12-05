package com.emc.atmos.mgmt.api.jersey;

import com.emc.atmos.mgmt.AtmosMgmtConfig;
import com.emc.atmos.mgmt.api.TenantMgmtApi;

public class TenantMgmtApiClient implements TenantMgmtApi {
    private AtmosMgmtConfig config;

    public TenantMgmtApiClient( AtmosMgmtConfig config ) {
        this.config = config;
    }
}
