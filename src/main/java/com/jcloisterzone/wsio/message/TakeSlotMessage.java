package com.jcloisterzone.wsio.message;

public class TakeSlotMessage {
    String gameId;
    int number;
    String nickname;
    boolean ai;


    public TakeSlotMessage(String gameId, int number, String nickname) {
        super();
        this.gameId = gameId;
        this.number = number;
        this.nickname = nickname;
    }

    public String getGameId() {
        return gameId;
    }
    public void setGameId(String gameId) {
        this.gameId = gameId;
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


}
