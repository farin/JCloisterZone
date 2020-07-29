package com.jcloisterzone.game.capability;

import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.Token;
import com.jcloisterzone.game.capability.SheepCapability.SheepToken;
import com.jcloisterzone.game.state.GameState;
import io.vavr.collection.*;

import java.util.function.Function;

/**
 * @model Map<Position, List<SheepToken>> - list of placed sheep tokens
 */
public class SheepCapability extends Capability<Map<FeaturePointer, List<SheepToken>>> {

	public static enum SheepToken implements Token {
	    SHEEP_1X,
	    SHEEP_2X,
	    SHEEP_3X,
	    SHEEP_4X,
	    WOLF;

	    public int sheepCount() {
	    	if (this == WOLF) {
	    		throw new IllegalStateException();
	    	}
	    	return this.ordinal() + 1;
	    }
	}

	private static final long serialVersionUID = 1L;

	// use sorted map (TreeMap) to same bag content on all clients!
	public static final Map<SheepToken, Integer> SHEEP_TOKEN_COUNT = TreeMap.of(
		SheepToken.SHEEP_1X, 4,
		SheepToken.SHEEP_2X, 5,
		SheepToken.SHEEP_3X, 5,
		SheepToken.SHEEP_4X, 2,
		SheepToken.WOLF, 2
	);
	public static final int TOKENS_COUNT = SHEEP_TOKEN_COUNT.values().sum().intValue();


	@Override
    public GameState onStartGame(GameState state) {
        return setModel(state, HashMap.empty());
    }

	public Vector<SheepToken> getBagConent(GameState state) {
		Map<SheepToken, Integer> bagCount = getModel(state)
			.values()
			.flatMap(Function.identity())
			.foldLeft(SHEEP_TOKEN_COUNT, (tokenCount, token) -> {
				int count = tokenCount.getOrElse(token, 0);
				if (count == 0) return tokenCount;
				if (count == 1) return tokenCount.remove(token);
				return tokenCount.put(token, count - 1);
			});

		// convert to flat array
		return bagCount.foldLeft(Vector.empty(), (acc, t) -> {
			return acc.appendAll(Vector.fill(t._2, t._1));
		});
	}
}
