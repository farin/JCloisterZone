package com.jcloisterzone.wsio.message;

import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.wsio.WsMessageCommand;

@WsMessageCommand("ROLL_FLIER_DICE")
public class RollFlierDiceMessage implements WsMessage {

    private String gameId;
    private String meepleType;

    public RollFlierDiceMessage(String gameId, Class<? extends Meeple> meepleType) {
        this.gameId = gameId;
        this.meepleType = meepleType.getSimpleName();
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

}
