package com.jcloisterzone.game.capability;

import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.ExprItem;
import com.jcloisterzone.event.PointsExpression;
import com.jcloisterzone.event.ScoreEvent;
import com.jcloisterzone.feature.Circus;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.figure.neutral.BigTop;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.PlacedTile;
import com.jcloisterzone.reducers.AddPoints;
import com.jcloisterzone.reducers.MoveNeutralFigure;
import io.vavr.Tuple2;
import io.vavr.collection.*;

import java.util.Random;
import java.util.function.Function;

/**
 * @model BigTopCapabilityModel> - all placed BigTop tokens and if showed value
 */
public class BigTopCapability extends Capability<BigTopCapabilityModel> {

	private static final long serialVersionUID = 1L;

	// use sorted map (TreeMap) to same bag content on all clients!
	public static final Map<BigTopToken, Integer> BIGTOP_TOKEN_COUNT = TreeMap.of(
		BigTopToken.BIGTOP_1, 1,
		BigTopToken.BIGTOP_2, 0,
		BigTopToken.BIGTOP_3, 4,
		BigTopToken.BIGTOP_4, 5,
		BigTopToken.BIGTOP_5, 3,
		BigTopToken.BIGTOP_6, 2,
		BigTopToken.BIGTOP_7, 1
	);

	@Override
    public GameState onStartGame(GameState state) {
		state = state.mapNeutralFigures(nf -> nf.setBigTop(new BigTop("bigtop.1")));
		state = setModel(state, new BigTopCapabilityModel(HashMap.empty()));
		return state;
	}
    
    @Override
    public GameState onFinalScoring(GameState state) {
        return bigTopScore(state);
    }
    
    public GameState bigTopMove(GameState state) {
        PlacedTile pt = state.getLastPlaced();
    	FeaturePointer circus = pt.getTile().getInitialFeatures().keySet().filter(fp -> fp.getFeature().equals(Circus.class)).getOrNull();
    	if (circus != null) {
    		state = bigTopScore(state);
    		BigTop bigtop = state.getNeutralFigures().getBigTop();
            state = (
                new MoveNeutralFigure<FeaturePointer>(bigtop, circus.setPosition(pt.getPosition()))
            ).apply(state);
        }
        return state;
    }
    
    public GameState bigTopScore(GameState state) {
		BigTop bigtop = state.getNeutralFigures().getBigTop();
		Position position = bigtop.getPosition(state);
		if (position != null) {
			LinkedHashMap<Meeple, FeaturePointer> affectedMeeples = state.getDeployedMeeples().filter((m, fp) -> {
	            if (!(m instanceof Follower)) return false;
	            Position mpos = fp.getPosition();
	            return Math.abs(position.x - mpos.x) <= 1 && Math.abs(position.y - mpos.y) <= 1;
	        });
	        if (affectedMeeples.length()>0) {
	    		Vector<BigTopToken> stack = state.getCapabilities().get(BigTopCapability.class).getStack(state);
	    		Random rand = new Random();
	    		BigTopToken token = stack.get(rand.nextInt(stack.size()));
	            for (Tuple2<Meeple, FeaturePointer> t : affectedMeeples) {
	                PointsExpression expr = new PointsExpression("bigtop", new ExprItem(1, "bigtop." + token.name(), 1 * token.getValue()));
	                var receivedPoints= new ScoreEvent.ReceivedPoints(expr, t._1.getPlayer(), t._2);
	                state = (new AddPoints(receivedPoints, true)).apply(state);
	        	}
	        }
		}
        return state;
    }

	public Vector<BigTopToken> getStack(GameState state) {
		Map<BigTopToken, Integer> stackCount = getModel(state).getPlacedTokens()
			.values()
			.flatMap(Function.identity())
			.foldLeft(BIGTOP_TOKEN_COUNT, (tokenCount, token) -> {
				int count = tokenCount.getOrElse(token, 0);
				if (count == 0) return tokenCount;
				if (count == 1) return tokenCount.remove(token);
				return tokenCount.put(token, count - 1);
			});
	
		// convert to flat array
		return stackCount.foldLeft(Vector.empty(), (acc, t) -> {
			return acc.appendAll(Vector.fill(t._2, t._1));
		});
	}
}
