package com.jcloisterzone.wsio.message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.wsio.WsMessageCommand;

@WsMessageCommand("DEPLOY_FLIER")
public class DeployFlierMessage implements WsInGameMessage {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private String gameId;
    private String meepleType;
    private long currentTime;

    public DeployFlierMessage(String gameId, Class<? extends Meeple> meepleType) {
        this.gameId = gameId;
        this.meepleType = meepleType.getSimpleName();
        this.currentTime = System.currentTimeMillis();
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public String getMeepleType() {
        return meepleType;
    }

    public void setMeepleType(String meepleType) {
        this.meepleType = meepleType;
    }

    public long getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(long currentTime) {
        this.currentTime = currentTime;
    }

    @SuppressWarnings("unchecked")
    public Class<? extends Meeple> getMeepleTypeClass() {
        try {
            return (Class<? extends Meeple>) Class.forName("com.jcloisterzone.figure." + meepleType);
        } catch (ClassNotFoundException e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }
}
