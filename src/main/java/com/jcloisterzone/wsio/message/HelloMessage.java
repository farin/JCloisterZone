package com.jcloisterzone.wsio.message;

import com.jcloisterzone.wsio.WsMessageCommand;

@WsMessageCommand("HELLO")
public class HelloMessage extends AbstractWsMessage {

    private String appVersion = "XXX";  // JCloisterZone.VERSION;
    private String protocolVersion = "XXX";  // + JCloisterZone.PROTCOL_VERSION;
    private String nickname;
    private String clientId;
    private String secret;

    public HelloMessage() {
    }

    public HelloMessage(String nickname, String clientId, String secret) {
        this.nickname = nickname;
        this.clientId = clientId;
        this.secret = secret;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getProtocolVersion() {
        return protocolVersion;
    }

    public void setProtocolVersion(String protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }
}
