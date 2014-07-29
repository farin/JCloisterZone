package com.jcloisterzone.wsio.message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcloisterzone.figure.Meeple;

public class FlierDiceMessage {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private String gameId;
    private String meepleType;
    private int distance;

    public FlierDiceMessage(String gameId, String meepleType, int distance) {
        super();
        this.gameId = gameId;
        this.meepleType = meepleType;
        this.distance = distance;
    }

    @SuppressWarnings("unchecked")
    public Class<? extends Meeple> getMeepleTypeClass() {
        try {
            return (Class<? extends Meeple>) Class.forName("com.jcloisterzone.figure."+meepleType);
        } catch (ClassNotFoundException e) {
            logger.error(e.getMessage(), e);
            return null;
        }
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
    public int getDistance() {
        return distance;
    }
    public void setDistance(int distance) {
        this.distance = distance;
    }
}
