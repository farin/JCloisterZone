package com.jcloisterzone.game.capability;

import com.jcloisterzone.Immutable;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.feature.Completable;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.figure.neutral.BlackDragon;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.reducers.MoveNeutralFigure;
import com.jcloisterzone.reducers.UndeployMeeple;

import io.vavr.collection.Vector;
import io.vavr.Tuple2;


@Immutable
public class BlackDragonCapability extends Capability<BlackDragonCapabilityModel> {

    private static final long serialVersionUID = 1L;

    public static final Vector<Position> EMPTY_VISITED = Vector.empty();

    @Override
    public GameState onStartGame(GameState state) {
        state = state.mapNeutralFigures(nf -> nf.setBlackDragon(new BlackDragon("blackdragon.1")));
        state = setModel(state, new BlackDragonCapabilityModel(EMPTY_VISITED, 0));
        return state;
    }

    @Override
    public boolean isMeepleDeploymentAllowed(GameState state, Position pos) {
        return !pos.equals(state.getNeutralFigures().getBlackDragonDeployment());
    }

    @Override
    public GameState beforeCompletableScore(GameState state, java.util.Set<Completable> features) {
        if (features.size() > 0) {
    	    state = setModel(state, new BlackDragonCapabilityModel(EMPTY_VISITED, features.size()));
        }
        return state;
    }

    public GameState moveBlackDragon(GameState state, Position pos) {
        state = (
	        new MoveNeutralFigure<>(state.getNeutralFigures().getBlackDragon(), pos)
	    ).apply(state);
	
        state = clearTile(state, pos);
	    return state;
    }
    
    public GameState clearTile(GameState state, Position pos) {
	    for (Tuple2<Meeple, FeaturePointer> t: state.getDeployedMeeples()) {
	        Meeple m = t._1;
	        FeaturePointer fp = t._2;
	        if (pos.equals(fp.getPosition()) && m.canBeEatenByDragon(state)) {
	            state = (new UndeployMeeple(m, true)).apply(state);
	        }
	    }

	    return state;
    }
}
