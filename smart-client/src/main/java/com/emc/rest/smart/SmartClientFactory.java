/*
 * Copyright (c) 2015 EMC Corporation
 * All Rights Reserved
 */
package com.emc.rest.smart;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandler;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.client.apache4.ApacheHttpClient4;
import com.sun.jersey.client.apache4.ApacheHttpClient4Handler;
import com.sun.jersey.client.apache4.config.ApacheHttpClient4Config;
import com.sun.jersey.core.impl.provider.entity.ByteArrayProvider;
import com.sun.jersey.core.impl.provider.entity.FileProvider;
import com.sun.jersey.core.impl.provider.entity.InputStreamProvider;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;

public final class SmartClientFactory {
    public static Client createSmartClient(SmartConfig smartConfig) {
        return createSmartClient(smartConfig, createApacheClientHandler(smartConfig));
    }

    public static Client createSmartClient(SmartConfig smartConfig,
                                           ClientHandler clientHandler) {
        Client client = createStandardClient(smartConfig, clientHandler);

        // inject SmartFilter (this is the Jersey integration point of the load balancer)
        client.addFilter(new SmartFilter(smartConfig.getLoadBalancer()));

        // set up polling for updated host list (if polling is disabled in smartConfig or there's no host list provider,
        // nothing will happen)
        PollingDaemon pollingDaemon = new PollingDaemon(smartConfig);
        pollingDaemon.start();

        return client;
    }

    /**
     * This creates a standard apache-based Jersey client, configured with a SmartConfig, but without any load balancing
     * or node polling.
     */
    public static Client createStandardClient(SmartConfig smartConfig) {
        return createStandardClient(smartConfig, createApacheClientHandler(smartConfig));
    }

    /**
     * This creates a standard apache-based Jersey client, configured with a SmartConfig, but without any load balancing
     * or node polling.
     */
    public static Client createStandardClient(SmartConfig smartConfig,
                                              ClientHandler clientHandler) {
        // init Jersey config
        ClientConfig clientConfig = new DefaultClientConfig();

        // pass in jersey parameters from calling code (allows customization of client)
        for (String propName : smartConfig.getProperties().keySet()) {
            clientConfig.getProperties().put(propName, smartConfig.property(propName));
        }

        // replace sized writers with override writers to allow dynamic content-length (i.e. for transformations)
        clientConfig.getClasses().remove(ByteArrayProvider.class);
        clientConfig.getClasses().remove(FileProvider.class);
        clientConfig.getClasses().remove(InputStreamProvider.class);
        clientConfig.getClasses().add(SizeOverrideWriter.ByteArray.class);
        clientConfig.getClasses().add(SizeOverrideWriter.File.class);
        clientConfig.getClasses().add(SizeOverrideWriter.InputStream.class);
        clientConfig.getClasses().add(ByteArrayProvider.class);
        clientConfig.getClasses().add(FileProvider.class);
        clientConfig.getClasses().add(InputStreamProvider.class);

        // build Jersey client
        Client client = new Client(clientHandler, clientConfig);

        // TODO: do we need a custom retry handler?

        return client;
    }

    static ApacheHttpClient4Handler createApacheClientHandler(SmartConfig smartConfig) {
        ClientConfig clientConfig = new DefaultClientConfig();

        // set up multi-threaded connection pool
        ThreadSafeClientConnManager connectionManager = new ThreadSafeClientConnManager();
        // 200 maximum active connections (should be more than enough for any JVM instance)
        connectionManager.setDefaultMaxPerRoute(200);
        connectionManager.setMaxTotal(200);
        clientConfig.getProperties().put(ApacheHttpClient4Config.PROPERTY_CONNECTION_MANAGER, connectionManager);

        // set proxy config
        if (smartConfig.getProxyUri() != null)
            clientConfig.getProperties().put(ApacheHttpClient4Config.PROPERTY_PROXY_URI, smartConfig.getProxyUri());
        if (smartConfig.getProxyUser() != null)
            clientConfig.getProperties().put(ApacheHttpClient4Config.PROPERTY_PROXY_USERNAME, smartConfig.getProxyUser());
        if (smartConfig.getProxyPass() != null)
            clientConfig.getProperties().put(ApacheHttpClient4Config.PROPERTY_PROXY_PASSWORD, smartConfig.getProxyPass());

        return ApacheHttpClient4.create(clientConfig).getClientHandler();
    }

    private SmartClientFactory() {
    }
}