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

/**
 * @model Tuple2<Vector<Position>,Integer> : visited tiles, finished features
 */
@Immutable
public class BlackDragonCapability extends Capability<Tuple2<Vector<Position>,Integer>> {

    private static final long serialVersionUID = 1L;

    @Override
    public GameState onStartGame(GameState state) {
    	System.out.println("\n");
    	System.out.println("BlackDragon is running");
    	System.out.println("\n");
        state = state.mapNeutralFigures(nf -> nf.setBlackDragon(new BlackDragon("blackdragon.1")));
        state = setModel(state, new Tuple2<>(Vector.empty(),0));
        return state;
    }

    @Override
    public boolean isMeepleDeploymentAllowed(GameState state, Position pos) {
        return !pos.equals(state.getNeutralFigures().getBlackDragonDeployment());
    }

    @Override
    public GameState beforeCompletableScore(GameState state, java.util.Set<Completable> features) {
    	System.out.println("\n");
    	System.out.println("BeforeCompletable");
    	System.out.println(features.size());
    	System.out.println("\n");
        state = setModel(state, new Tuple2<>(Vector.empty(),features.size()));

//        Position pos = state.getLastPlaced().getPosition();
//        state = moveBlackDragon(state, pos);
        return state;
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
