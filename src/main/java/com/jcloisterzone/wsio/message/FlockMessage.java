package com.jcloisterzone.wsio.message;

import com.jcloisterzone.wsio.WsMessageCommand;

@WsMessageCommand("FLOCK_EXPAND_OR_SCORE")
public class FlockMessage extends AbstractWsMessage implements WsInGameMessage, WsReplayableMessage, WsSaltMessage {

	public enum FlockOption { EXPAND, SCORE }

    private String gameId;
    private String messageId;
    private FlockOption value;

    private long salt;

    public FlockMessage() {
	}

    public FlockMessage(FlockOption value) {
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
    public String getMessageId() {
        return messageId;
    }

    @Override
    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

	public FlockOption getValue() {
		return value;
	}

	public void setValue(FlockOption value) {
		this.value = value;
	}

	@Override
    public long getSalt() {
        return salt;
    }

    @Override
    public void setSalt(long salt) {
        this.salt = salt;
    }


}
