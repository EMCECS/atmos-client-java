package com.emc.atmos.sync.monitor;

public class DirectoryMonitorBean {
    private int atmosPort;
    private String atmosHost;
    private String atmosUid;
    private String atmosSecret;
    private String atmosDirectory;
    private String localDirectory;

    public DirectoryMonitorBean() {
    }

    public int getAtmosPort() {
        return atmosPort;
    }

    public void setAtmosPort( final int atmosPort ) {
        this.atmosPort = atmosPort;
    }

    public String getAtmosHost() {
        return atmosHost;
    }

    public void setAtmosHost( final String atmosHost ) {
        this.atmosHost = atmosHost;
    }

    public String getAtmosUid() {
        return atmosUid;
    }

    public void setAtmosUid( final String atmosUid ) {
        this.atmosUid = atmosUid;
    }

    public String getAtmosSecret() {
        return atmosSecret;
    }

    public void setAtmosSecret( final String atmosSecret ) {
        this.atmosSecret = atmosSecret;
    }

    public String getAtmosDirectory() {
        return atmosDirectory;
    }

    public void setAtmosDirectory( final String atmosDirectory ) {
        this.atmosDirectory = atmosDirectory;
        if ( !this.atmosDirectory.startsWith( "/" ) ) this.atmosDirectory = "/" + this.atmosDirectory;
        if ( !this.atmosDirectory.endsWith( "/" ) ) this.atmosDirectory += "/";
    }

    public String getLocalDirectory() {
        return localDirectory;
    }

    public void setLocalDirectory( String localDirectory ) {
        this.localDirectory = localDirectory;
    }
}