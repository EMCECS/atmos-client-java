package com.emc.atmos.api.bean;

import java.net.URL;

public class CreateAccessTokenResponse extends BasicResponse {
    private URL tokenUrl;

    public CreateAccessTokenResponse() {
    }

    public CreateAccessTokenResponse( URL tokenUrl ) {
        this.tokenUrl = tokenUrl;
    }

    public URL getTokenUrl() {
        return tokenUrl;
    }

    public void setTokenUrl( URL tokenUrl ) {
        this.tokenUrl = tokenUrl;
    }
}
