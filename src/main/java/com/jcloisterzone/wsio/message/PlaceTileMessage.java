package com.jcloisterzone.wsio.message;

import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.wsio.WsMessageCommand;

@WsMessageCommand("PLACE_TILE")
public class PlaceTileMessage implements WsInGameMessage, WsReplayableMessage {

    private String gameId;
    private String messageId;

    private String tileId;
    private Rotation rotation;
    private Position position;

    public PlaceTileMessage() {
    }

    public PlaceTileMessage(String tileId, Rotation rotation, Position position) {
        this.tileId = tileId;
        this.rotation = rotation;
        this.position = position;
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

    public String getTileId() {
        return tileId;
    }

    public void setTileId(String tileId) {
        this.tileId = tileId;
    }

    public Rotation getRotation() {
        return rotation;
    }

    public void setRotation(Rotation rotation) {
        this.rotation = rotation;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }
}
