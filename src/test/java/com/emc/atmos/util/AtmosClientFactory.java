/*
 * Copyright (c) 2013-2016, EMC Corporation.
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * + Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 * + Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 * + The name of EMC Corporation may not be used to endorse or promote
 *   products derived from this software without specific prior written
 *   permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.emc.atmos.util;

import com.emc.atmos.api.AtmosApi;
import com.emc.atmos.api.AtmosConfig;
import com.emc.atmos.api.jersey.AtmosApiClient;
import com.emc.util.TestConfig;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * This class looks on the classpath for a file named atmos.properties and uses it to
 * configure a connection to Atmos.  The supported properties are:
 * <dt>
 * <dl>atmos.uid</dl><dd>(Required) The full token ID</dd>
 * <dl>atmos.secret</dl><dd>(Required) The shared secret key</dd>
 * <dl>atmos.endpoints</dl><dd>(Required) Comma-delimited list of endpoint URIs</dd>
 * <dl>atmos.proxy</dl><dd>(Optional) proxy to use</dd>
 * </dt>
 *
 * @author cwikj
 */
public class AtmosClientFactory {
    private static final Logger l4j = Logger.getLogger(AtmosClientFactory.class);

    public static final String PROP_ATMOS_UID = "atmos.uid";
    public static final String PROP_ATMOS_SECRET = "atmos.secret";
    public static final String PROP_ATMOS_ENDPOINTS = "atmos.endpoints";
    public static final String PROP_ATMOS_IS_ECS = "atmos.is_ecs";
    public static final String PROP_PROXY = "http.proxyUri";

    public static AtmosApi getAtmosClient() {
        AtmosConfig config = getAtmosConfig();
        if(config == null) {
            return null;
        }
        return new AtmosApiClient(getAtmosConfig());
    }

    public static AtmosConfig getAtmosConfig() {
        try {
            Properties props = TestConfig.getProperties();

            String uid = TestConfig.getPropertyNotEmpty(props, PROP_ATMOS_UID);
            String secret = TestConfig.getPropertyNotEmpty(props, PROP_ATMOS_SECRET);
            String endpoints = TestConfig.getPropertyNotEmpty(props, PROP_ATMOS_ENDPOINTS);
            String proxyUrl = props.getProperty(PROP_PROXY);

            List<URI> endpointUris = new ArrayList<URI>();
            for (String endpoint : endpoints.split(",")) {
                endpointUris.add(new URI(endpoint));
            }

            AtmosConfig config = new AtmosConfig(uid, secret, endpointUris.toArray(new URI[endpointUris.size()]));
            if (proxyUrl != null) config.setProxyUri(new URI(proxyUrl));

            return config;
        } catch (IOException e) {
            l4j.info("Could not load properties file: " + e);
            return null;
        } catch (URISyntaxException e) {
           l4j.info("Invalid endpoint or proxy URI: " + e);
           return null;
        }
    }

    public static boolean atmosIsEcs() {
        try {
            Properties props = TestConfig.getProperties();

            return Boolean.parseBoolean(props.getProperty(PROP_ATMOS_IS_ECS, "false"));
            
        } catch (IOException e) {
            l4j.info("Could not load properties file: " + e);
            return false;
        } 
    }
}
