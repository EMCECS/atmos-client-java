package com.emc.cdp.mgmt;

import com.emc.cdp.mgmt.bean.AccountList;
import com.emc.util.HttpUtil;

import java.io.IOException;
import java.net.HttpURLConnection;

public class ListAccountsResponse extends CdpMgmtResponse {
    private AccountList accountList;

    public ListAccountsResponse( HttpURLConnection con ) throws IOException {
        this.accountList = (AccountList) XmlUtil.unmarshal( HttpUtil.readResponseString( con ) );
    }

    public AccountList getAccountList() {
        return this.accountList;
    }
}
