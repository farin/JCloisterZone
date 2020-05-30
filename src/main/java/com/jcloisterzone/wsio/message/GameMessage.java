package com.jcloisterzone.wsio.message;

import java.util.List;

import com.jcloisterzone.wsio.WsMessageCommand;

@WsMessageCommand("GAME")
public class GameMessage extends AbstractWsMessage implements WsInGameMessage {

    public enum GameStatus {
        OPEN, RUNNING, REMOVED, PAUSED
    }

    private String gameId;
    private String channel;
    private String name;
    private GameStatus status;
    private boolean passwordProtected;

    //TODO use structures instead of message?
    private GameSetupMessage gameSetup;
    private SlotMessage[] slots;

    private Long clockStart;
    private long initialSeed;
    private List<WsReplayableMessage> replay;


    public GameMessage() {
    }

    public GameMessage(String id, String name, GameStatus status, GameSetupMessage gameSetup) {
        this.gameId = id;
        this.name = name;
        this.status = status;
        this.gameSetup = gameSetup;
    }

    @Override
    public String getGameId() {
        return gameId;
    }

    @Override
    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public GameStatus getStatus() {
        return status;
    }

    public void setStatus(GameStatus state) {
        this.status = state;
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

    public boolean isPasswordProtected() {
        return passwordProtected;
    }

    public void setPasswordProtected(boolean passwordProtected) {
        this.passwordProtected = passwordProtected;
    }

    public long getInitialSeed() {
        return initialSeed;
    }

    public void setInitialSeed(long initialSeed) {
        this.initialSeed = initialSeed;
    }

    public List<WsReplayableMessage> getReplay() {
        return replay;
    }

    public void setReplay(List<WsReplayableMessage> replay) {
        this.replay = replay;
    }

    public Long getClockStart() {
        return clockStart;
    }

    public void setClockStart(Long clockStart) {
        this.clockStart = clockStart;
    }
}
