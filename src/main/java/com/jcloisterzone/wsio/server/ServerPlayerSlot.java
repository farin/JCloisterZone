package com.jcloisterzone.wsio.server;

import com.jcloisterzone.Expansion;

//TODO use slot messge directly
public class ServerPlayerSlot {

    private final int number;
    private Integer serial; // server assign sequence number when type is occupied
    private String nickname;
    private String owner;
    private String aiClassName;
    private Expansion[] supportedExpansions;

    private String clientId;
    private String secret;

    public ServerPlayerSlot(int number) {
        this.number = number;
    }

    public boolean isOccupied() {
        return owner != null;
    }

    public Integer getSerial() {
        return serial;
    }

    public void setSerial(Integer serial) {
        this.serial = serial;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getAiClassName() {
        return aiClassName;
    }

    public void setAiClassName(String aiClassName) {
        this.aiClassName = aiClassName;
    }

    public int getNumber() {
        return number;
    }

    public Expansion[] getSupportedExpansions() {
        return supportedExpansions;
    }

    public void setSupportedExpansions(Expansion[] supportedExpansions) {
        this.supportedExpansions = supportedExpansions;
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
