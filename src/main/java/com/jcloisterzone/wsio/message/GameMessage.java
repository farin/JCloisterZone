package com.jcloisterzone.wsio.message;

import com.jcloisterzone.wsio.WsMessageCommand;

@WsMessageCommand("GAME")
public class GameMessage implements WsMessage, WsInGameMessage {

    public enum GameState {
        OPEN, RUNNING
    }

    private String gameId;
    private String name;
    private GameState state;
    private String snapshot;
    private SlotMessage[] slots;
    private GameSetupMessage gameSetup;

    public GameMessage(String id, String name, GameState state, GameSetupMessage gameSetup) {
        this.gameId = id;
        this.name = name;
        this.state = state;
        this.gameSetup = gameSetup;
    }

    @Override
	public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public GameState getState() {
        return state;
    }

    public void setState(GameState state) {
        this.state = state;
    }

    public String getSnapshot() {
        return snapshot;
    }

    public void setSnapshot(String snapshot) {
        this.snapshot = snapshot;
    }

    public SlotMessage[] getSlots() {
        return slots;
    }

    public void setSlots(SlotMessage[] slots) {
        this.slots = slots;
    }

    public GameSetupMessage getGameSetup() {
        return gameSetup;
    }

    public void setGameSetup(GameSetupMessage gameSetup) {
        this.gameSetup = gameSetup;
    }
}
