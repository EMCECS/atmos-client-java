// Copyright (c) 2012, EMC Corporation.
// Redistribution and use in source and binary forms, with or without modification,
// are permitted provided that the following conditions are met:
//
//     + Redistributions of source code must retain the above copyright notice,
//       this list of conditions and the following disclaimer.
//     + Redistributions in binary form must reproduce the above copyright
//       notice, this list of conditions and the following disclaimer in the
//       documentation and/or other materials provided with the distribution.
//     + The name of EMC Corporation may not be used to endorse or promote
//       products derived from this software without specific prior written
//       permission.
//
//      THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
//      "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
//      TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
//      PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
//      BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
//      CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
//      SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
//      INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
//      CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
//      ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
//      POSSIBILITY OF SUCH DAMAGE.
package com.emc.atmos.sync.monitor;

public class DirectoryMonitorBean {
    private int atmosPort;
    private String atmosHost;
    private String atmosUid;
    private String atmosSecret;
    private String atmosDirectory;
    private String localDirectory;
    private boolean recursive;

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

    public boolean isRecursive() {
        return recursive;
    }

    public void setRecursive( boolean recursive ) {
        this.recursive = recursive;
    }
}