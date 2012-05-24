package com.emc.cdp.mgmt;

import java.io.IOException;
import java.net.HttpURLConnection;

public class ListAccountsRequest extends CdpMgmtRequest<ListAccountsResponse> {
    public ListAccountsRequest( CdpMgmtApi api ) {
        super( api );
    }

    @Override
    public String getPath() {
        return "/admin/accounts";
    }

    @Override
    protected ListAccountsResponse createResponse( HttpURLConnection con ) throws IOException {
        return new ListAccountsResponse( con );
    }

    public void setStart( Integer start ) {
        setQueryParameter( "start", start.toString() );
    }

    public void setCount( Integer count ) {
        setQueryParameter( "count", count.toString() );
    }

    public void setWithSubscriptions( Boolean withSubscriptions ) {
        setQueryParameter( "with_subscriptions", withSubscriptions.toString() );
    }
}
