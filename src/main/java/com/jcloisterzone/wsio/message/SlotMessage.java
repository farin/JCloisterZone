package com.jcloisterzone.wsio.message;

public class SlotMessage {
    String game;
    int number;
    String nickname;
    boolean ai;
    boolean own;

    public String getGame() {
        return game;
    }
    public void setGame(String game) {
        this.game = game;
    }
    public int getNumber() {
        return number;
    }
    public void setNumber(int number) {
        this.number = number;
    }
    public String getNickname() {
        return nickname;
    }
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    public boolean isAi() {
        return ai;
    }
    public void setAi(boolean ai) {
        this.ai = ai;
    }
    public boolean isOwn() {
        return own;
    }
    public void setOwn(boolean own) {
        this.own = own;
    }


}
