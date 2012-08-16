package com.emc.util;

import javax.net.ssl.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class SslUtil {

    public static TrustManager[] gullibleManagers = new TrustManager[]{
            new X509TrustManager() {
                @Override
                public void checkClientTrusted( X509Certificate[] x509Certificates, String s ) throws CertificateException {
                }

                @Override
                public void checkServerTrusted( X509Certificate[] x509Certificates, String s ) throws CertificateException {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            }
    };

    public static HostnameVerifier gullibleVerifier = new HostnameVerifier() {
        @Override
        public boolean verify( String s, SSLSession sslSession ) {
            return true;
        }
    };

    public static SSLContext createGullibleSslContext() throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext ctx = SSLContext.getInstance( "SSL" );
        ctx.init( null, gullibleManagers, new SecureRandom() );
        return ctx;
    }
}
