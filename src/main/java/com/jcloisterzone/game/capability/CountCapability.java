package com.jcloisterzone.game.capability;

import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.RemoveTileException;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.Quarter;
import com.jcloisterzone.figure.neutral.Count;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.PlacedTile;
import com.jcloisterzone.reducers.MoveNeutralFigure;
import io.vavr.Tuple2;
import io.vavr.collection.Map;
import io.vavr.collection.Vector;
import org.w3c.dom.Element;

public class CountCapability extends Capability<CountCapabilityModel> {

	private static final long serialVersionUID = 1L;

    public static String QUARTER_ACTION_TILE_ID = "CO/7";
    private static final String[] FORBIDDEN_TILES = new String[] { "CO/6", "CO/7" };

    @Override
    public GameState onStartGame(GameState state) {
        Count count = new Count("count.1");
        state =  state.mapNeutralFigures(nf -> nf.setCount(count));

        Position quarterPosition = state.getPlacedTiles().filter(t -> t._2.getTile().getId().equals(QUARTER_ACTION_TILE_ID)).get()._1;
        state = setModel(state, new CountCapabilityModel(quarterPosition, null));
        state = (new MoveNeutralFigure<>(
                count,
                new FeaturePointer(quarterPosition, Location.QUARTER_CASTLE)
        )).apply(state);
        return state;
    }

    @Override
    public Tile initTile(GameState state, Tile tile, Vector<Element> tileElements) throws RemoveTileException {
        if (QUARTER_ACTION_TILE_ID.equals(tile.getId())) {
            Map<Location, Feature> features = tile.getInitialFeatures();
            features = features.merge(Location.QUARTERS.toMap(loc ->
                new Tuple2<>(loc, new Quarter(new FeaturePointer(Position.ZERO, loc)))
            ));
            return tile.setInitialFeatures(features);
        }
        return tile;
    }

    public static boolean isTileForbidden(Tile tile) {
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
}
