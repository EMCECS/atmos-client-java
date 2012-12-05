package com.emc.atmos.api;

import org.concord.security.ccjce.cryptix.jce.provider.CryptixCrypto;

import java.security.Security;

public enum ChecksumAlgorithm {
    SHA0( "SHA-0" ),
    SHA1( "SHA-1" ),
    MD5( "MD5" );

    // Register SHA-0 provider
    static {
        Security.addProvider( new CryptixCrypto() );
    }

    private String digestName;

    private ChecksumAlgorithm( String digestName ) {
        this.digestName = digestName;
    }

    public String getDigestName() {
        return this.digestName;
    }
}
