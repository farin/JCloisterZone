package com.jcloisterzone.wsio.server;

import com.jcloisterzone.Expansion;

//TODO use slot messge directly
public class ServerPlayerSlot {

    private final int number;
    private Integer serial; // server assign sequence number when type is occupied
    private String nickname;
    private String owner;
    private boolean ai;
    private Expansion[] supportedExpansions;

    public ServerPlayerSlot(int number) {
        this.number = number;
    }

    public boolean isOccupied() {
        return nickname != null;
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

    public boolean isAi() {
        return ai;
    }

    public void setAi(boolean ai) {
        this.ai = ai;
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

}
