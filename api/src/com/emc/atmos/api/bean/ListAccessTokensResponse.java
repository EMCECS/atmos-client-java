package com.emc.atmos.api.bean;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement( namespace = "", name = "list-access-tokens-result" )
public class ListAccessTokensResponse extends BasicResponse {
    private List<AccessToken> tokens;

    @XmlElementWrapper( namespace = "", name = "access-tokens-list" )
    @XmlElement( namespace = "", name = "access-token" )
    public List<AccessToken> getTokens() {
        return tokens;
    }

    public void setTokens( List<AccessToken> tokens ) {
        this.tokens = tokens;
    }
}
