package com.jcloisterzone.game.capability;

import static com.jcloisterzone.XMLUtils.attributeBoolValue;

import org.w3c.dom.Element;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.feature.Castle;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Completable;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.game.Capability;
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

    @Override
    public GameState onStartGame(GameState state) {
        int tokens = state.getPlayers().length() < 5 ? 3 : 2;
        return state.mapPlayers(ps -> ps.setTokenCountForAllPlayers(Token.CASTLE, tokens));
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

    @Override
    public GameState onCompleted(GameState state, HashMap<Completable, Integer> completed) {
        java.util.Map<Castle, Integer> scoredCastles = new java.util.HashMap<>();
        Array<Tuple2<Completable, Integer>> scored = Array.ofAll(completed).sortBy(t -> -t._2);

        for (Castle castle : getOccupiedCastles(state)) {
            Set<Position> vicinity = castle.getVicinity();
            for (Tuple2<Completable, Integer> t : scored) {
                if (!vicinity.intersect(t._1.getTilePositions()).isEmpty()) {
                    state = (new ScoreCastle(castle, t._2)).apply(state);
                    state = (new UndeployMeeples(castle)).apply(state);
                    scoredCastles.put(castle, t._2);
                    break;
                }
            }
        }

        while (!scoredCastles.isEmpty()) {
            Map<Castle, Integer> scoredCastlesCpy = HashMap.ofAll(scoredCastles);
            scoredCastles.clear();

            //must call getOccupiedCastles each iteration to get fresh castles
            for (Castle castle : getOccupiedCastles(state)) {
                Set<Position> vicinity = castle.getVicinity();
                for (Tuple2<Castle, Integer> t : scoredCastlesCpy) {
                    if (!vicinity.intersect(t._1.getTilePositions()).isEmpty()) {
                        state = (new ScoreCastle(castle, t._2)).apply(state);
                        state = (new UndeployMeeples(castle)).apply(state);
                        scoredCastles.put(castle, t._2);
                        break;
                    }
                }
            }
        }

        return state;
    }

}
