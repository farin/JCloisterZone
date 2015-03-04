package com.jcloisterzone.game;

import java.io.Serializable;

import com.jcloisterzone.ai.AiPlayer;
import com.jcloisterzone.ui.PlayerColor;

public class PlayerSlot implements Serializable {

    public enum SlotState { OPEN, OWN, REMOTE };

    private static final long serialVersionUID = 6093356973595538191L;

    public static final int COUNT = 6;

    private final int number;
    private Integer serial; //server assign sequence number whgen type is occupied

    private String sessionId;
    private String nickname;
    private SlotState state = SlotState.OPEN;
    private String aiClassName;
    private boolean disconnected;

    private transient PlayerColor colors;
    private transient AiPlayer aiPlayer; //ai player instance, set only on onwner host

    public PlayerSlot(int number) {
        this.number = number;
    }

    public boolean isOccupied() {
        return state != SlotState.OPEN;
    }

    public boolean isAi() {
        return aiClassName != null;
    }

    public boolean isOwn() {
        return state == SlotState.OWN;
    }

    public int getNumber() {
        return number;
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

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public SlotState getState() {
        return state;
    }

    public void setState(SlotState state) {
        this.state = state;
    }

    public String getAiClassName() {
        return aiClassName;
    }

    public void setAiClassName(String aiClassName) {
        this.aiClassName = aiClassName;
    }

    public PlayerColor getColors() {
        return colors;
    }

    public void setColors(PlayerColor colors) {
        this.colors = colors;
    }

    @Override
    public String toString() {
        return "("+ number + ") " + state + (nickname == null ? "" : " " + nickname);
    }

    public boolean isDisconnected() {
        return disconnected;
    }

    public void setDisconnected(boolean disconnected) {
        this.disconnected = disconnected;
    }

    public AiPlayer getAiPlayer() {
        return aiPlayer;
    }

    public void setAiPlayer(AiPlayer aiPlayer) {
        this.aiPlayer = aiPlayer;
    }
}
