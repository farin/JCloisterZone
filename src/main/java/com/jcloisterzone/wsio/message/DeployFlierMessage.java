package com.jcloisterzone.wsio.message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcloisterzone.board.Position;
import com.jcloisterzone.wsio.WsMessageCommand;

@WsMessageCommand("DEPLOY_FLIER")
public class DeployFlierMessage implements WsInGameMessage, WsReplayableMessage, WsSeedMeesage {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private String gameId;
    private Position position;
    private String meepleId;
    private long seed;

    public DeployFlierMessage() {
    }

    public DeployFlierMessage(Position position, String meepleId) {
        this.position = position;
        this.meepleId = meepleId;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public String getMeepleId() {
        return meepleId;
    }

    public void setMeepleId(String meepleId) {
        this.meepleId = meepleId;
    }

    public long getSeed() {
        return seed;
    }

    public void setSeed(long seed) {
        this.seed = seed;
    }
}
