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

import io.vavr.collection.Array;
import io.vavr.collection.Vector;
import io.vavr.Tuple2;
import io.vavr.Tuple3;

/**
 * @model Tuple3<Vector<Position>,Integer,Array<Integer> : visited tiles by black dragon, count of finished features, score on start of turn
 */
@Immutable
public class BlackDragonCapability extends Capability<Tuple3<Vector<Position>,Integer,Array<Integer>>> {

    private static final long serialVersionUID = 1L;

    public static final Vector<Position> EMPTY_VISITED = Vector.empty();

    @Override
    public GameState onStartGame(GameState state) {
        state = state.mapNeutralFigures(nf -> nf.setBlackDragon(new BlackDragon("blackdragon.1")));
        state = setInitialTurnState(state);
        return state;
    }

    @Override
    public boolean isMeepleDeploymentAllowed(GameState state, Position pos) {
        return !pos.equals(state.getNeutralFigures().getBlackDragonDeployment());
    }

    @Override
    public GameState beforeCompletableScore(GameState state, java.util.Set<Completable> features) {
    	Tuple3<Vector<Position>,Integer,Array<Integer>> model = getModel(state);
    	state = setModel(state, new Tuple3<>(EMPTY_VISITED,features.size(),model._3));
        return state;
    }
    
    @Override
    public GameState onTurnPartCleanUp(GameState state) {
    	return setInitialTurnState(state);
    }

    public GameState setInitialTurnState(GameState state) {
        return setModel(state, new Tuple3<>(EMPTY_VISITED,0,state.getPlayers().getScore()));
    }

    public Vector<Position> getVisitedPositions(GameState state) {
        Vector<Position> visitedpositions = getModel(state)._1;
        return visitedpositions == null ? BlackDragonCapability.EMPTY_VISITED : visitedpositions;
    }

    public Integer getMoves(GameState state) {
    	Integer finishedfeatures = getModel(state)._2;
        return finishedfeatures == null ? 0 : finishedfeatures;
    }

    public Array<Integer> getScore(GameState state) {
    	return getModel(state)._3;
    }

    public GameState moveBlackDragon(GameState state, Position pos) {
        state = (
	        new MoveNeutralFigure<>(state.getNeutralFigures().getBlackDragon(), pos)
	    ).apply(state);
	
        state = blackDragonOnTile(state, pos);
	    return state;
    }
    
    public GameState blackDragonOnTile(GameState state, Position pos) {
	    for (Tuple2<Meeple, FeaturePointer> t: state.getDeployedMeeples()) {
	        Meeple m = t._1;
	        FeaturePointer fp = t._2;
	        if (pos.equals(fp.getPosition()) && m.canBeEatenByBlackDragon(state)) {
	            state = (new UndeployMeeple(m, true)).apply(state);
	        }
	    }
	    return state;
    }
}
