package com.jcloisterzone.wsio.message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.wsio.WsMessageCommand;

@WsMessageCommand("DEPLOY_FLIER")
public class DeployFlierMessage implements WsInGameMessage, WsReplayableMessage, WsSeedMeesage {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private String gameId;
    private String meepleType;
    private long seed;

    public DeployFlierMessage() {
    }

    public DeployFlierMessage(Class<? extends Meeple> meepleType) {
        this.meepleType = meepleType.getSimpleName();
    }

    @Override
    public String getGameId() {
        return gameId;
    }

    @Override
    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public String getMeepleType() {
        return meepleType;
    }

    public void setMeepleType(String meepleType) {
        this.meepleType = meepleType;
    }

    public long getSeed() {
        return seed;
    }

    public void setSeed(long seed) {
        this.seed = seed;
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
