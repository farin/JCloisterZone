package com.jcloisterzone.wsio.message;

import com.jcloisterzone.wsio.WsMessageCommand;

@WsMessageCommand("BAZAAR_BUY_OR_SELL")
public class BazaarBuyOrSellMessage extends AbstractWsMessage implements WsInGameMessage, WsReplayableMessage {

    //TODO move to own file (and probably action package)
    public enum BuyOrSellOption { BUY, SELL }

    private String gameId;
    private long clock;
    private String parentId;
    private BuyOrSellOption value;

    public BazaarBuyOrSellMessage() {
    }

    public BazaarBuyOrSellMessage(BuyOrSellOption value) {
        this.value = value;
    }

    @Override
    public String getGameId() {
        return gameId;
    }

    @Override
    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    @Override
    public long getClock() {
        return clock;
    }

    @Override
    public void setClock(long clock) {
        this.clock = clock;
    }

    @Override
    public String getParentId() {
        return parentId;
    }

    @Override
    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public BuyOrSellOption getValue() {
        return value;
    }

    public void setValue(BuyOrSellOption value) {
        this.value = value;
    }


}
