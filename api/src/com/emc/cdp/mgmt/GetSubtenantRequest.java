package com.emc.cdp.mgmt;

import java.io.IOException;
import java.net.HttpURLConnection;

public class GetSubtenantRequest extends CdpMgmtRequest<GetSubtenantResponse> {
    private String accountId;
    private String subscriptionId;

    public GetSubtenantRequest( CdpMgmtApi api, String accountId, String subscriptionId ) {
        super( api );
        if ( accountId == null || subscriptionId == null )
            throw new IllegalArgumentException( "all arguments are required" );
        this.accountId = accountId;
        this.subscriptionId = subscriptionId;
    }

    @Override
    protected String getPath() {
        return "/admin/accounts/" + accountId + "/storage/" + subscriptionId + "/subtenant";
    }

    @Override
    protected GetSubtenantResponse createResponse( HttpURLConnection con ) throws IOException {
        return new GetSubtenantResponse( con );
    }
}
