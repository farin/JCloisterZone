package com.jcloisterzone.wsio.message;

import com.jcloisterzone.Application;

public class HelloMessage {

    private String appVersion = Application.VERSION;
    private String protocolVersion = "" + Application.PROTCOL_VERSION;
    private String nickname;

    public HelloMessage(String nickname) {
        this.nickname = nickname;
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

}
