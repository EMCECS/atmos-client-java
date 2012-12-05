package com.emc.acdp.api;

import com.emc.cdp.services.rest.model.Identity;

public interface AcdpMgmtApi {
    void createIdentity( Identity id );

    void createAccount( String serviceId );
}
