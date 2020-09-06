package com.jcloisterzone.action;

import com.jcloisterzone.board.PlacementOption;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.io.message.PlaceTileMessage;
import com.jcloisterzone.io.message.Message;
import io.vavr.collection.Set;
import io.vavr.collection.Stream;

public class TilePlacementAction extends AbstractPlayerAction<PlacementOption> {

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
}
