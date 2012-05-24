package com.emc.cdp.mgmt;

import java.io.IOException;
import java.net.HttpURLConnection;

public class GetAccountAssigneeRequest extends CdpMgmtRequest<GetAccountAssigneeResponse> {
    String accountId;
    String identityId;

    public GetAccountAssigneeRequest( CdpMgmtApi api, String accountId, String identityId ) {
        super( api );
        if ( accountId == null || identityId == null )
            throw new IllegalArgumentException( "all arguments are required" );
        this.accountId = accountId;
        this.identityId = identityId;
    }

    @Override
    protected String getPath() {
        return "/admin/accounts/" + accountId + "/identities/" + identityId;
    }

    @Override
    protected GetAccountAssigneeResponse createResponse( HttpURLConnection con ) throws IOException {
        return new GetAccountAssigneeResponse( con );
    }

    public void setShowProfile( Boolean showProfile ) {
        setQueryParameter( "show_profile", showProfile.toString() );
    }
}
