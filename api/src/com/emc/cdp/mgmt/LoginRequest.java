package com.emc.cdp.mgmt;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;

public class LoginRequest extends CdpMgmtRequest<LoginResponse> {
    public LoginRequest( CdpMgmtApi api ) {
        super( api );
        this.path = "/admin/login";
    }

    @Override
    protected void handleConnection( HttpURLConnection con ) throws IOException {
        con.setRequestMethod( "POST" );
        con.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded" );

        String content = "cdp-identity-id=" + api.getUsername() + "&cdp-password=" + api.getPassword();

        con.setDoOutput( true );
        con.setFixedLengthStreamingMode( content.length() );
        con.connect();

        OutputStreamWriter writer = new OutputStreamWriter( con.getOutputStream() );
        writer.write( content );
        writer.close();

        int responseCode = 0;
        try {
            responseCode = con.getResponseCode();
        } catch ( IOException e ) {
            // this might be ok
        }
        if ( responseCode > 0 ) {
            handleError( con );
        }
    }

    @Override
    protected LoginResponse createResponse( HttpURLConnection con ) throws IOException {
        return new LoginResponse( con );
    }
}
