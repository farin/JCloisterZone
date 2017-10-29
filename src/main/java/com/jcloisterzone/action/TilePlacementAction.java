package com.jcloisterzone.action;

import com.jcloisterzone.board.PlacementOption;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.ui.annotations.LinkedGridLayer;
import com.jcloisterzone.ui.grid.layer.TilePlacementLayer;
import com.jcloisterzone.wsio.message.PlaceTileMessage;
import com.jcloisterzone.wsio.message.WsInGameMessage;

import io.vavr.collection.Set;
import io.vavr.collection.Stream;


@LinkedGridLayer(TilePlacementLayer.class)
public class TilePlacementAction extends PlayerAction<PlacementOption> {

    private static final long serialVersionUID = 1L;

    private final Tile tile;

    public TilePlacementAction(Tile tile, Set<PlacementOption> options) {
        super(options);
        this.tile = tile;
    }

    public Tile getTile() {
        return tile;
    }

    @Deprecated
    public Set<Rotation> getRotations(Position pos) {
        return Stream.ofAll(getOptions())
            .filter(tp -> tp.getPosition().equals(pos))
            .map(tp -> tp.getRotation())
            .toSet();
    }

    @Override
    public WsInGameMessage select(PlacementOption tp) {
        return new PlaceTileMessage(tile.getId(), tp.getRotation(), tp.getPosition());
    }

    @Override
    public String toString() {
        return "place tile " + tile.getId();
    }
}
