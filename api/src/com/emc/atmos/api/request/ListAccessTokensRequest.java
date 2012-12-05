package com.emc.atmos.api.request;

public class ListAccessTokensRequest extends ListRequest<ListAccessTokensRequest> {
    @Override
    public String getServiceRelativePath() {
        return "accesstokens";
    }

    @Override
    public String getMethod() {
        return "GET";
    }

    @Override
    protected ListAccessTokensRequest me() {
        return this;
    }
}
