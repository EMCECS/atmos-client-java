package com.emc.cdp.mgmt;

import java.io.IOException;
import java.net.HttpURLConnection;

public class ListAccountsRequest extends CdpMgmtRequest<ListAccountsResponse> {
    public ListAccountsRequest( CdpMgmtApi api ) {
        super( api );
        path = "/admin/accounts";
        query = getSessionQuery();
    }

    @Override
    protected void handleConnection( HttpURLConnection con ) throws IOException {
        con.connect();
        if ( con.getResponseCode() != 200 ) {
            handleError( con );
        }
    }

    @Override
    protected ListAccountsResponse createResponse( HttpURLConnection con ) throws IOException {
        return new ListAccountsResponse( con );
    }
}
