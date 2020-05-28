package com.jcloisterzone.game.capability;

import static com.jcloisterzone.XMLUtils.attributeBoolValue;

import org.w3c.dom.Element;

import com.jcloisterzone.board.Position;
import com.jcloisterzone.feature.Castle;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Completable;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.ScoreFeatureReducer;
import com.jcloisterzone.game.Token;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.reducers.ScoreCastle;
import com.jcloisterzone.reducers.UndeployMeeples;

import io.vavr.Tuple2;
import io.vavr.collection.Array;
import io.vavr.collection.HashMap;
import io.vavr.collection.Map;
import io.vavr.collection.Set;
import io.vavr.collection.Stream;

public class CastleCapability extends Capability<Void> {

	public static enum CastleToken implements Token {
		CASTLE;
	}

	private static final long serialVersionUID = 1L;

    @Override
    public GameState onStartGame(GameState state) {
        int tokens = state.getPlayers().length() < 5 ? 3 : 2;
        return state.mapPlayers(ps -> ps.setTokenCountForAllPlayers(CastleToken.CASTLE, tokens));
    }

    @Override
    public Feature initFeature(GameState state, String tileId, Feature feature, Element xml) {
        if (feature instanceof City) {
            feature = ((City) feature).setCastleBase(attributeBoolValue(xml, "castle-base"));
        }
        return feature;
    }

    private Stream<Castle> getOccupiedCastles(GameState state) {
        return state.getFeatures(Castle.class).filter(c -> c.isOccupied(state));
    }

    public Tuple2<GameState, Map<Castle, ScoreFeatureReducer>> scoreCastles(GameState state, HashMap<Completable, ScoreFeatureReducer> completed) {
        java.util.Map<Castle, ScoreFeatureReducer> scoredCastles = new java.util.HashMap<>();
        Array<Tuple2<Completable, ScoreFeatureReducer>> scored = Array.ofAll(completed).sortBy(t -> -t._2.getFeaturePoints());
        HashMap<Castle, ScoreFeatureReducer> allScoredCastled = HashMap.empty();

        Position placedThisTurn = state.getLastPlaced().getPosition();

        for (Castle castle : getOccupiedCastles(state)) {
            if (castle.getTilePositions().contains(placedThisTurn)) {
                continue;
            }

            Set<Position> vicinity = castle.getVicinity();
            for (Tuple2<Completable, ScoreFeatureReducer> t : scored) {
                if (!vicinity.intersect(t._1.getTilePositions()).isEmpty()) {
                    ScoreCastle scoreReducer = new ScoreCastle(castle, t._2.getFeaturePoints(), false);
                    state = scoreReducer.apply(state);
                    state = (new UndeployMeeples(castle, false)).apply(state);
                    scoredCastles.put(castle, scoreReducer);
                    break;
                }
            }
        }

        while (!scoredCastles.isEmpty()) {
            Map<Castle, ScoreFeatureReducer> scoredCastlesCpy = HashMap.ofAll(scoredCastles);
            allScoredCastled = allScoredCastled.merge(scoredCastlesCpy);
            scoredCastles.clear();

            //must call getOccupiedCastles each iteration to get fresh castles
            for (Castle castle : getOccupiedCastles(state)) {
                Set<Position> vicinity = castle.getVicinity();
                for (Tuple2<Castle, ScoreFeatureReducer> t : scoredCastlesCpy) {
                    if (!vicinity.intersect(t._1.getTilePositions()).isEmpty()) {
                        ScoreCastle scoreReducer = new ScoreCastle(castle, t._2.getFeaturePoints(), false);
                        state = scoreReducer.apply(state);
                        state = (new UndeployMeeples(castle, false)).apply(state);
                        scoredCastles.put(castle, scoreReducer);
                        break;
                    }
                }
            }
        }

        return new Tuple2<>(state, allScoredCastled);
    }

}
