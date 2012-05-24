package com.emc.cdp.mgmt;

import com.emc.cdp.CdpException;
import org.apache.log4j.Logger;

public class CdpMgmtApi {
    private static final Logger l4j = Logger.getLogger( CdpMgmtApi.class );

    private String proto;
    private String host;
    private int port;
    private String username;
    private String password;
    private String sessionToken;

    public CdpMgmtApi() {
        this.proto = "http";
        this.port = 80;
    }

    public CdpMgmtApi( String proto, String host, int port, String username,
                       String password ) {
        this.proto = proto;
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    public void login() {
        LoginResponse response = new LoginRequest( this ).execute();
        this.sessionToken = response.getSessionToken();
    }

    public ListAccountsResponse listAccounts( boolean withSubscriptions ) {
        ListAccountsRequest request = new ListAccountsRequest( this );
        request.setWithSubscriptions( withSubscriptions );
        return executeWithRetry( request );
    }

    public GetAccountResponse getAccount( String accountId, boolean withSubscriptions ) {
        GetAccountRequest request = new GetAccountRequest( this, accountId );
        request.setWithSubscriptions( withSubscriptions );
        return executeWithRetry( request );
    }

    public GetAccountAssigneeResponse getAccountAssignee( String accountId, String identityId, boolean showProfile ) {
        GetAccountAssigneeRequest request = new GetAccountAssigneeRequest( this, accountId, identityId );
        request.setShowProfile( showProfile );
        return executeWithRetry( request );
    }

    public GetSubtenantResponse getSubtenant( String accountId, String subscriptionId ) {
        return executeWithRetry( new GetSubtenantRequest( this, accountId, subscriptionId ) );
    }

    // detects 401 response (unauthorized) and attempts to login and retry the request
    private <T extends CdpMgmtResponse> T executeWithRetry( CdpMgmtRequest<T> request ) {
        try {
            return request.execute();
        } catch ( CdpException e ) {
            if ( e.getHttpCode() == 401 ) {
                // try once more after login
                login();
                return request.execute();
            } else throw e;
        }
    }

    public String getProto() {
        return proto;
    }

    public void setProto( String proto ) {
        this.proto = proto;
    }

    public String getHost() {
        return host;
    }

    public void setHost( String host ) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort( int port ) {
        this.port = port;
    }

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

    public String getSessionToken() {
        return sessionToken;
    }

    public void setSessionToken( String sessionToken ) {
        this.sessionToken = sessionToken;
    }
}
