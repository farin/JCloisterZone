package com.jcloisterzone.wsio.message;

import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.wsio.WsMessageCommand;

@WsMessageCommand("PLACE_TILE")
public class PlaceTileMessage implements WsInGameMessage, WsReplayableMessage {

    private String gameId;

    private String tileId;
    private Rotation rotation;
    private Position position;

    public PlaceTileMessage(String gameId, String tileId, Rotation rotation, Position position) {
        this.gameId = gameId;
        this.tileId = tileId;
        this.rotation = rotation;
        this.position = position;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
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
