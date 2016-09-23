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
package com.emc.atmos.api.jersey;

import com.emc.atmos.api.AtmosConfig;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class ConfigProxySelector extends ProxySelector {
    private AtmosConfig config;

    public ConfigProxySelector(AtmosConfig config) {
        this.config = config;
    }

    /**
     * Very simple implementation that returns NO_PROXY for localhost and the configured proxy for anything else (no
     * nonProxyHosts-like support).
     */
    @Override
    public List<Proxy> select(URI uri) {
        List<Proxy> proxies = new ArrayList<Proxy>(1);

        if ("127.0.0.1".equals(uri.getHost()) || "localhost".equals(uri.getHost()) || config.getProxyUri() == null) {
            proxies.add(Proxy.NO_PROXY);
        } else {
            URI proxyUri = config.getProxyUri();
            SocketAddress address = InetSocketAddress.createUnresolved(proxyUri.getHost(), proxyUri.getPort());
            proxies.add(new Proxy(Proxy.Type.HTTP, address));
        }

        return proxies;
    }

    @Override
    public void connectFailed(URI uri, SocketAddress socketAddress, IOException e) {
        // ignored
    }
}
