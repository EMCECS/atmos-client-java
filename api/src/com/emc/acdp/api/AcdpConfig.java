package com.emc.acdp.api;

public class AcdpConfig {
    private String proto;
    private boolean disableSslValidation = false;
    private String host;
    private int port;
    private String username;
    private String password;
    private String sessionToken;

    public AcdpConfig() {
        this( "http", null, 80, null, null );
    }

    public AcdpConfig( String proto, String host, int port, String username, String password ) {
        this.proto = proto;
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    public String getProto() {
        return proto;
    }

    public void setProto( String proto ) {
        this.proto = proto;
    }

    public boolean isDisableSslValidation() {
        return disableSslValidation;
    }

    public void setDisableSslValidation( boolean disableSslValidation ) {
        this.disableSslValidation = disableSslValidation;
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

    public String getBaseUri() {
        String url = proto + "://" + host;
        if (port > 0) url += ":" + port;
        return url;
    }
}
