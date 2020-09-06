package com.jcloisterzone.io.message;

import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.io.MessageCommand;

@MessageCommand("PLACE_TILE")
public class PlaceTileMessage extends AbstractMessage implements ReplayableMessage {

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
