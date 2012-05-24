package com.emc.cdp.mgmt;

import com.emc.cdp.mgmt.bean.Account;
import com.emc.util.HttpUtil;

import java.io.IOException;
import java.net.HttpURLConnection;

public class GetAccountResponse extends CdpMgmtResponse {
    private Account account;

    public GetAccountResponse( HttpURLConnection con ) throws IOException {
        this.account = XmlUtil.unmarshal( Account.class, HttpUtil.readResponseString( con ) );
    }

    public Account getAccount() {
        return this.account;
    }
}
