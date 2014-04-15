/*
 * Copyright 2013 EMC Corporation. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.emc.vipr.services.s3;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.internal.StaticCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3EncryptionClient;
import com.amazonaws.services.s3.S3ClientOptions;
import com.amazonaws.services.s3.model.EncryptionMaterials;
import com.emc.vipr.services.lib.ViprConfig;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Properties;

/**
 * This class looks on the classpath for a file named viprs3.properties and uses it to
 * configure a connection to ViPR.  The supported properties are:
 * <dt>
 * <dl>vipr.s3.access_key_id</dl><dd>(Required) The access key (user ID)</dd>
 * <dl>vipr.s3.secret_key</dl><dd>(Required) The shared secret key</dd>
 * <dl>vipr.s3.endpoint</dl><dd>(Required) The endpoint hostname or IP address of the ViPR
 * data service server to use</dd>
 * <dl>vipr.s3.endpoints</dl><dd>(Optional) The endpoints (plural) to use in the smart client</dd>
 * <dl>vipr.namespace</dl><dd>(Optional) The ViPR namespace to connect to.  Generally
 * this is required if your endpoint is an IP or not in the format of {namespace}.company.com</dd>
 * <dl>vipr.s3.virtual_host</dl><dd>(Optional) The root virtual host used for vhost requests (also used by the smart
 * client as the load-balanced address.</dd>
 * </dt>
 *
 * @author cwikj
 */
public class S3ClientFactory {
    private static Log log = LogFactory.getLog(S3ClientFactory.class);


    public static ViPRS3Client getS3Client() {
        return getS3Client(false);
    }

    public static ViPRS3Client getS3Client(boolean setNamespace) {
        return getS3Client(false, setNamespace);
    }

    public static ViPRS3Client getSmartS3Client(boolean setNamespace) {
        return getS3Client(true, setNamespace);
    }

    private static ViPRS3Client getS3Client(boolean smart, boolean setNamespace) {
        try {
            Properties props = ViprConfig.getProperties();

            String accessKey = ViprConfig.getPropertyNotEmpty(props, ViprConfig.PROP_S3_ACCESS_KEY_ID);
            String secretKey = ViprConfig.getPropertyNotEmpty(props, ViprConfig.PROP_S3_SECRET_KEY);
            String endpoint = ViprConfig.getPropertyNotEmpty(props, ViprConfig.PROP_S3_ENDPOINT);
            String endpoints = props.getProperty(ViprConfig.PROP_S3_ENDPOINTS);
            String virtualHost = props.getProperty(ViprConfig.PROP_S3_VIRTUAL_HOST);

            BasicAWSCredentials creds = new BasicAWSCredentials(accessKey, secretKey);

            ViPRS3Config viprConfig = new ViPRS3Config().withCredentialsProvider(new StaticCredentialsProvider(creds));
            if (endpoints != null) viprConfig.withS3Endpoints(endpoints);
            else viprConfig.withS3Endpoints(endpoint);
            if (virtualHost != null) viprConfig.setVirtualHost(virtualHost);

            ViPRS3Client client = smart ? new ViPRS3Client(viprConfig) : new ViPRS3Client(endpoint, creds);

            if (smart) {
                S3ClientOptions s3ClientOptions = new S3ClientOptions();
                s3ClientOptions.setPathStyleAccess(true);
                client.setS3ClientOptions(s3ClientOptions);
            }

            String namespace = props.getProperty(ViprConfig.PROP_NAMESPACE);
            if (namespace != null && setNamespace) {
                client.setNamespace(namespace);
            }
            checkProxyConfig(client, props);

            return client;
        } catch (IOException e) {
            log.info("Failed to load properties: " + e);
            return null;
        }
    }

    /**
     * Creates an EncryptionClient for testing.  Loads the public and private keys from
     * the properties file (not suitable for production).
     *
     * @return
     * @throws IOException
     */
    public static AmazonS3EncryptionClient getEncryptionClient() throws IOException {
        try {
            Properties props = ViprConfig.getProperties();

            String accessKey = ViprConfig.getPropertyNotEmpty(props, ViprConfig.PROP_S3_ACCESS_KEY_ID);
            String secretKey = ViprConfig.getPropertyNotEmpty(props, ViprConfig.PROP_S3_SECRET_KEY);
            String endpoint = ViprConfig.getPropertyNotEmpty(props, ViprConfig.PROP_S3_ENDPOINT);
            String publicKey = ViprConfig.getPropertyNotEmpty(props, ViprConfig.PROP_PUBLIC_KEY);
            String privateKey = ViprConfig.getPropertyNotEmpty(props, ViprConfig.PROP_PRIVATE_KEY);

            byte[] pubKeyBytes = Base64.decodeBase64(publicKey.getBytes("US-ASCII"));
            byte[] privKeyBytes = Base64.decodeBase64(privateKey.getBytes("US-ASCII"));

            X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(pubKeyBytes);
            PKCS8EncodedKeySpec privKeySpec = new PKCS8EncodedKeySpec(privKeyBytes);

            PublicKey pubKey;
            PrivateKey privKey;
            try {
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                pubKey = keyFactory.generatePublic(pubKeySpec);
                privKey = keyFactory.generatePrivate(privKeySpec);
            } catch (GeneralSecurityException e) {
                throw new RuntimeException("Could not load key pair: " + e, e);
            }

            EncryptionMaterials keys = new EncryptionMaterials(new KeyPair(pubKey, privKey));

            BasicAWSCredentials creds = new BasicAWSCredentials(accessKey, secretKey);
            AmazonS3EncryptionClient client = new AmazonS3EncryptionClient(creds, keys);
            client.setEndpoint(endpoint);

            checkProxyConfig(client, props);

            return client;
        } catch (Exception e) {
            log.info("Could not load configuration: " + e);
            return null;
        }
    }

    private static void checkProxyConfig(AmazonS3Client client, Properties props) {
        String proxyHost = props.getProperty(ViprConfig.PROP_PROXY_HOST);
        if (proxyHost != null && !proxyHost.isEmpty()) {
            int proxyPort = Integer.parseInt(props.getProperty(ViprConfig.PROP_PROXY_PORT));
            ClientConfiguration config = new ClientConfiguration();
            config.setProxyHost(proxyHost);
            config.setProxyPort(proxyPort);
            client.setConfiguration(config);
        }
    }

    // Generates a RSA key pair for testing.
    public static void main(String[] args) {
        try {
            KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("RSA");
            keyGenerator.initialize(1024, new SecureRandom());
            KeyPair myKeyPair = keyGenerator.generateKeyPair();

            // Serialize.
            byte[] pubKeyBytes = myKeyPair.getPublic().getEncoded();
            byte[] privKeyBytes = myKeyPair.getPrivate().getEncoded();

            String pubKeyStr = new String(Base64.encodeBase64(pubKeyBytes, false), "US-ASCII");
            String privKeyStr = new String(Base64.encodeBase64(privKeyBytes, false), "US-ASCII");

            System.out.println("Public Key: " + pubKeyStr);
            System.out.println("Private Key: " + privKeyStr);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
