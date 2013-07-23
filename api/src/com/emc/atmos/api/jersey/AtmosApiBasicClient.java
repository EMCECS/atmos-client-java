package com.emc.atmos.api.jersey;

import com.emc.atmos.api.AtmosConfig;

import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import java.util.List;

/**
 * Extension of AtmosApiClient that does *not* use the commons-http client and instead uses the default Jersey client
 * (URLConnection).  Note that this implementation does not support the Expect: 100-continue header.
 */
public class AtmosApiBasicClient extends AtmosApiClient {
    public AtmosApiBasicClient( AtmosConfig config ) {
        this( config, null, null );
    }

    public AtmosApiBasicClient( AtmosConfig config,
                                List<Class<MessageBodyReader<?>>> readers,
                                List<Class<MessageBodyWriter<?>>> writers ) {
        super( config, JerseyUtil.createClient( config, readers, writers ), null );
    }
}
