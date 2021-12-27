package com.jcloisterzone.game.capability;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.*;
import com.jcloisterzone.feature.Circus;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.figure.neutral.BigTop;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.reducers.AddPoints;
import io.vavr.collection.*;

import java.util.ArrayList;

/**
 * @model BigTopCapabilityModel> - all placed BigTop tokens and if showed value
 */
public class BigTopCapability extends Capability<Integer> {

	private static final long serialVersionUID = 1L;

	@Override
    public GameState onStartGame(GameState state) {
		state = state.mapNeutralFigures(nf -> nf.setBigTop(new BigTop("bigtop.1")));
        int circusCount = state.getTilePack().getGroups().toStream().flatMap(t -> t._2.getTiles()).filter(tile -> tile.getInitialFeatures().exists(t -> t._2 instanceof Circus)).size();
        int tokensPerSet = 0;
        for (AnimalToken t : AnimalToken.values()) {
            tokensPerSet += t.count;
        }
        state = setModel(state, (int) Math.ceil(circusCount / (double) tokensPerSet));
		return state;
	}
    
    @Override
    public GameState onFinalScoring(GameState state) {
        BigTop bigtop = state.getNeutralFigures().getBigTop();
        Position pos = bigtop.getPosition(state);
        if (pos != null) {
            var tokens = getUnusedTokens(state);
            int idx = state.getPhase().getRandom().getNextInt(tokens.size());
            return scoreBigTop(state, pos, tokens.get(idx), true);
        }
        return state;
    }

    public GameState scoreBigTop(GameState state, Position pos, AnimalToken token, boolean isFinal) {
        // add token event even if nobody scoring it
        state = state.appendEvent(new TokenPlacedEvent(PlayEvent.PlayEventMeta.createWithoutPlayer(), token, pos));

        List<ScoreEvent.ReceivedPoints> points = List.empty();
        Set<Position> positions = Position.ADJACENT_AND_DIAGONAL.map(pt -> pos.add(pt._2)).toSet().add(pos);
        Map<Player, LinkedHashMap<Meeple, FeaturePointer>> adjacentMeepleCount = state.getDeployedMeeples()
                .filter(mt -> mt._1 instanceof  Follower)
                .filter(mt -> positions.contains(mt._2.getPosition()))
                .groupBy(mt -> mt._1.getPlayer());

        for (var t : adjacentMeepleCount) {
            int followers = t._2.size();
            ExprItem expr = new ExprItem(followers, "meeples", token.points * followers);
            points = points.append(new ScoreEvent.ReceivedPoints(new PointsExpression("bigtop", expr), t._1, pos));
        }

        if (!points.isEmpty()) {
            state = (new AddPoints(points, false)).apply(state);
        }
        return state;
    }

    public ArrayList<AnimalToken> getUnusedTokens(GameState state) {
        int tokenSetCount = state.getCapabilityModel(BigTopCapability.class);
        ArrayList<AnimalToken> unusedTokens = new ArrayList<>();
        var placedTokens = state.getEvents()
                .filter(ev -> (ev instanceof TokenPlacedEvent) && ((TokenPlacedEvent) ev).getToken() instanceof AnimalToken)
                .map(ev -> ((TokenPlacedEvent)ev).getToken())
                .groupBy(t -> ((AnimalToken)t).points);

        for (AnimalToken t : AnimalToken.values()) {
            int count = t.count * tokenSetCount;
            count -= placedTokens.get(t.points).getOrElse(Queue.empty()).size();
            for (int i = 0; i < count; i++) {
                unusedTokens.add(t);
            }
        }
        return unusedTokens;
    }
}
