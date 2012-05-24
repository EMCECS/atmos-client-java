package com.emc.cdp.mgmt;

import com.emc.cdp.mgmt.bean.Subtenant;
import com.emc.util.HttpUtil;

import java.io.IOException;
import java.net.HttpURLConnection;

public class GetSubtenantResponse extends CdpMgmtResponse {
    private Subtenant subtenant;

    public GetSubtenantResponse( HttpURLConnection con ) throws IOException {
        this.subtenant = XmlUtil.unmarshal( Subtenant.class, HttpUtil.readResponseString( con ) );
    }

    public Subtenant getSubtenant() {
        return this.subtenant;
    }
}
