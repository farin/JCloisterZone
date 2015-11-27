package com.jcloisterzone.event;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.DefaultTilePack;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.TilePack;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.capability.AbbeyCapability;
import com.jcloisterzone.game.capability.TowerCapability;

public class TileEvent extends PlayEvent implements Undoable {

    public static final int DRAW = 1;
    public static final int PLACEMENT = 2;
    public static final int DISCARD = 3;
    public static final int REMOVE = 4;

    private final Tile tile;
    private final Position position;


    public TileEvent(int type, Player player, Tile tile, Position position) {
        super(type, player, type == DRAW ? player : null);
        this.tile = tile;
        this.position = position;
    }

    public Tile getTile() {
        return tile;
    }

    public Position getPosition() {
        return position;
    }

    @Override
    public void undo(Game game) {
        switch (getType()) {
        case PLACEMENT:
            game.getBoard().unmergeFeatures(tile);
            game.getBoard().remove(tile);
            if (tile.isAbbeyTile()) {
                tile.setRotation(Rotation.R0);
                game.setCurrentTile(null);
                ((DefaultTilePack)game.getTilePack()).addTile(tile, TilePack.INACTIVE_GROUP);
                game.getCapability(AbbeyCapability.class).undoUseAbbey(getTriggeringPlayer());
            }
            if (tile.getTower() != null) {
                game.getCapability(TowerCapability.class).unregisterTower(position);
            }
            break;
        default:
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public Event getInverseEvent() {
        switch (getType()) {
        case PLACEMENT:
            return new TileEvent(TileEvent.REMOVE, getTriggeringPlayer(), tile, getPosition());
        default:
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public String toString() {
        return super.toString() + " tile:" + tile.getId() + " position:" + position;
    }

}
