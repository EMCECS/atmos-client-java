package com.emc.cdp.mgmt;

import java.io.IOException;
import java.net.HttpURLConnection;

public class GetAccountRequest extends CdpMgmtRequest<GetAccountResponse> {
    private String accountId;

    public GetAccountRequest( CdpMgmtApi api, String accountId ) {
        super( api );
        if ( accountId == null )
            throw new IllegalArgumentException( "all arguments are required" );
        this.accountId = accountId;
    }

    @Override
    protected String getPath() {
        return "/admin/accounts/" + accountId;
    }

    @Override
    protected GetAccountResponse createResponse( HttpURLConnection con ) throws IOException {
        return new GetAccountResponse( con );
    }

    public void setWithSubscriptions( Boolean withSubscriptions ) {
        setQueryParameter( "with_subscriptions", withSubscriptions.toString() );
    }
}
