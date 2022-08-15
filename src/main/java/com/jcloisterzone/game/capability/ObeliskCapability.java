package com.jcloisterzone.game.capability;

import com.jcloisterzone.Player;
import com.jcloisterzone.action.MeepleAction;
import com.jcloisterzone.board.Corner;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.ExprItem;
import com.jcloisterzone.event.PointsExpression;
import com.jcloisterzone.event.ScoreEvent;
import com.jcloisterzone.event.ScoreEvent.ReceivedPoints;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.Field;
import com.jcloisterzone.feature.Scoreable;
import com.jcloisterzone.figure.Obelisk;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.ScoreFeatureReducer;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.PlacedTile;
import com.jcloisterzone.reducers.AddPoints;
import com.jcloisterzone.reducers.UndeployMeeple;

import io.vavr.Predicates;
import io.vavr.Tuple2;
import io.vavr.collection.HashMap;
import io.vavr.collection.Map;
import io.vavr.collection.Set;
import io.vavr.collection.HashSet;
import io.vavr.collection.Stream;

/**
 * @model FeaturePointer: ptr to just placed Obelisk
 */
public final class ObeliskCapability extends Capability<FeaturePointer> {

	private static final long serialVersionUID = 1L;

    public static final Set<Position> TILES_ARROUND_OBELISK = HashSet.of(
        new Position(-2, -2),
        new Position(-2, -1),
        new Position(-2,  0),
        new Position(-2,  1),
        new Position(-1, -2),
        new Position(-1, -1),
        new Position(-1,  0),
        new Position(-1,  1),
        new Position( 0, -2),
        new Position( 0, -1),
        new Position( 0,  0),
        new Position( 0,  1),
        new Position( 1, -2),
        new Position( 1, -1),
        new Position( 1,  0),
        new Position( 1,  1)
	).toSet();

	@Override
    public GameState onActionPhaseEntered(GameState state) {
        Player player = state.getPlayerActions().getPlayer();

        Obelisk obelisk = player.getMeepleFromSupply(state, Obelisk.class);
        if (obelisk == null) {
            return state;
        }

        Position pos = state.getLastPlaced().getPosition();

        // By convention obelisk action contains feature pointer which points to
        // left top corner of tile intersection
        //      |
        //      |
        //  ----+----
        //      | XX
        //      | XX
        Set<FeaturePointer> options = Stream.of(
            pos,
            new Position(pos.x + 1, pos.y),
            new Position(pos.x, pos.y + 1),
            new Position(pos.x + 1, pos.y + 1)
        )
            .map(p -> getCornerFeature(state, p))
            .filter(Predicates.isNotNull())
            .map(Tuple2::_1)
            .toSet();

        if (options.isEmpty()) {
            return state;
        }

        return state.appendAction(new MeepleAction(obelisk, options));
    }

    @Override
    public GameState onTurnPartCleanUp(GameState state) {
        return setModel(state, null);
    }

    private boolean containsCorner(Tuple2<FeaturePointer, Feature> t, Corner c) {
        return t != null && t._1.getLocation().getCorners().contains(c);
    }

    private Tuple2<FeaturePointer, Feature> getCornerFeature(GameState state, Position pos) {
        Tuple2<FeaturePointer, Feature> t;
        t = state.getFeaturePartOf2(new FeaturePointer(new Position(pos.x - 1, pos.y - 1), Field.class, Location.SL));
        if (!containsCorner(t, Corner.SE)) return null;
        t = state.getFeaturePartOf2(new FeaturePointer(new Position(pos.x, pos.y - 1), Field.class, Location.WL));
        if (!containsCorner(t, Corner.SW)) return null;
        t = state.getFeaturePartOf2(new FeaturePointer(new Position(pos.x - 1, pos.y), Field.class, Location.EL));
        if (!containsCorner(t, Corner.NE)) return null;
        t = state.getFeaturePartOf2(new FeaturePointer(pos, Field.class, Location.NL));
        if (!containsCorner(t, Corner.NW)) return null;
        return t;
    }

    @Override
    public GameState onFinalScoring(GameState state) {
	    state = scoreObelisks(state, true);
        return state;
    }
    
    @Override
    public GameState onTurnScoring(GameState state, HashMap<Scoreable, ScoreFeatureReducer> completed) {
	    state = scoreObelisks(state, false);
        return state;
    }

    public GameState scoreObelisks(GameState state, Boolean isFinal) {
        Set<Obelisk> deployedObelisks = getDeployedObelisks(state).keySet();
        
        int tilesRequired = TILES_ARROUND_OBELISK.length();
        
        for(Obelisk obelisk : deployedObelisks) {
        	Position position = obelisk.getPosition(state);
        	Set<PlacedTile> tiles = getObeliskTiles(state,position);
        	if (isFinal) {
            	ReceivedPoints rp = new ReceivedPoints(new PointsExpression("obelisk.incomplete", new ExprItem(tiles.length(), "tiles", tiles.length())), obelisk.getPlayer() , obelisk.getDeployment(state));
                state = (new AddPoints(rp, true, true)).apply(state);
        	} else if (tiles.length() == tilesRequired) {
                PointsExpression expr = new PointsExpression("obelisk", new ExprItem(tiles.length(), "tiles", tiles.length()));
            	ScoreEvent scoreEvent = new ScoreEvent(new ReceivedPoints(expr, obelisk.getPlayer(), obelisk.getDeployment(state)), true, false);
            	state = state.appendEvent(scoreEvent);
            	state = (new UndeployMeeple(obelisk, false)).apply(state);
        	}
        }
        return state;
    	
    }

    private Map<Obelisk, FeaturePointer> getDeployedObelisks(GameState state) {
        return state.getDeployedMeeples()
           .filter((m, fp) -> m instanceof Obelisk)
           .mapKeys(m -> (Obelisk) m);
    }
    
    private Set<PlacedTile> getObeliskTiles(GameState state, Position pos) {
        return TILES_ARROUND_OBELISK
            .map(
                offset -> state.getPlacedTile(pos.add(offset))
            )
            .filter(locTile -> locTile != null);
    }
}