package com.emc.cdp.mgmt;

import com.emc.cdp.mgmt.bean.Assignee;
import com.emc.util.HttpUtil;

import java.io.IOException;
import java.net.HttpURLConnection;

public class GetAccountAssigneeResponse extends CdpMgmtResponse {
    private Assignee assignee;

    public GetAccountAssigneeResponse( HttpURLConnection con ) throws IOException {
        this.assignee = XmlUtil.unmarshal( Assignee.class, HttpUtil.readResponseString( con ) );
    }

    public Assignee getAssignee() {
        return this.assignee;
    }
}
