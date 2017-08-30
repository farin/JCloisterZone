package com.jcloisterzone.wsio.message;

import com.jcloisterzone.wsio.WsMessageCommand;

@WsMessageCommand("WELCOME")
public class WelcomeMessage implements WsMessage {

    private String sessionId;
    private String nickname;
    private Integer pingInterval;
    private String maintenance;
    private Long currentTime;

    public WelcomeMessage() {
    }

    public WelcomeMessage(String sessionId, String nickname, Integer pingInterval, Long currentTime) {
        this.sessionId = sessionId;
        this.nickname = nickname;
        this.pingInterval = pingInterval;
        this.currentTime = currentTime;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setPingInterval(Integer pingInterval) {
        this.pingInterval = pingInterval;
    }

    public Integer getPingInterval() {
        return pingInterval;
    }

    public String getMaintenance() {
        return maintenance;
    }

    public void setMaintenance(String maintenance) {
        this.maintenance = maintenance;
    }

    public Long getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(Long currentTime) {
        this.currentTime = currentTime;
    }
}
