package com.jcloisterzone.game.capability;

import org.w3c.dom.Element;

import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.RemoveTileException;
import com.jcloisterzone.board.TileDefinition;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.Quarter;
import com.jcloisterzone.feature.Scoreable;
import com.jcloisterzone.figure.neutral.Count;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.ScoringResult;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.PlacedTile;
import com.jcloisterzone.reducers.MoveNeutralFigure;

import io.vavr.Tuple2;
import io.vavr.collection.HashMap;
import io.vavr.collection.Map;

/** model is Position od CO.7 tile */
public class CountCapability extends Capability<Position> {

    public static String QUARTER_ACTION_TILE_ID = "CO.7";
    private static final String[] FORBIDDEN_TILES = new String[] { "CO.6", "CO.7" };

//
//    // active player for pre score phase
//    private Player moveOutPlayer;

    @Override
    public GameState onStartGame(GameState state) {
        return state.mapNeutralFigures(nf -> nf.setCount(new Count("count.1")));
    }

    @Override
    public TileDefinition initTile(GameState state, TileDefinition tile, Element xml) throws RemoveTileException {
        if (QUARTER_ACTION_TILE_ID.equals(tile.getId())) {
            Map<Location, Feature> features = tile.getInitialFeatures();
            features = features.merge(Location.QUARTERS.toMap(loc ->
                new Tuple2<>(loc, new Quarter(new FeaturePointer(Position.ZERO, loc)))
            ));
            return tile.setInitialFeatures(features);
        }
        return tile;
    }


    public static boolean isTileForbidden(TileDefinition tile) {
        String id = tile.getId();
        for (String forbidden : FORBIDDEN_TILES) {
            if (forbidden.equals(id)) return true;
        }
        return false;
    }

    @Override
    public boolean isMeepleDeploymentAllowed(GameState state, Position pos) {
        PlacedTile pt = state.getPlacedTiles().get(pos).getOrNull();
        return pt == null || !isTileForbidden(pt.getTile());
    }

    @Override
    public GameState onTilePlaced(GameState state, PlacedTile pt) {
        if (!pt.getTile().getId().equals(QUARTER_ACTION_TILE_ID)) {
            return state;
        }

        Position quarterPosition = pt.getPosition();
        state = setModel(state, quarterPosition);
        Count count = state.getNeutralFigures().getCount();
        state = (new MoveNeutralFigure<>(
            count,
            new FeaturePointer(quarterPosition, Location.QUARTER_CASTLE)
        )).apply(state);
        return state;
    }
//
//    public Quarter getQuarter(Location loc) {
//        return (Quarter) game.getBoard().get(quarterPosition).getFeature(loc);
//    }
//
//    public Quarter getQuarterFor(Feature f) {
//        if (f instanceof City) return getQuarter(Location.QUARTER_CASTLE);
//        if (f instanceof Road) return getQuarter(Location.QUARTER_BLACKSMITH);
//        if (f instanceof Cloister) return getQuarter(Location.QUARTER_CATHEDRAL);
//        if (f instanceof Farm) return getQuarter(Location.QUARTER_MARKET);
//        throw new IllegalArgumentException("Illegal feature " + f);
//    }
//
//    public Player getMoveOutPlayer() {
//        return moveOutPlayer;
//    }
//
//    public void setMoveOutPlayer(Player moveOutPlayer) {
//        this.moveOutPlayer = moveOutPlayer;
//    }
}
