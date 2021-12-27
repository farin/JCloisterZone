package com.jcloisterzone.game.capability;

import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.ExprItem;
import com.jcloisterzone.event.PointsExpression;
import com.jcloisterzone.event.ScoreEvent.ReceivedPoints;
import com.jcloisterzone.feature.*;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.figure.Ringmaster;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.state.GameState;
import io.vavr.Tuple2;
import io.vavr.collection.List;

public class RingmasterCapability extends Capability<Void> {

	private static final int RINGMASTER_BONUS = 2;
	
    @Override
    public List<ReceivedPoints> appendFiguresBonusPoints(GameState state, List<ReceivedPoints> bonusPoints, Scoreable feature, boolean isFinal) {
        if ((feature instanceof Castle) || (feature instanceof Vodyanoy)) {
            return bonusPoints;
        }
        for (Tuple2<Follower, FeaturePointer> t : feature.getFollowers2(state).filter(t -> t._1 instanceof Ringmaster)) {
            Meeple meeple = t._1;
            FeaturePointer fp = t._2;
            List<Feature> features = getNeigbouringFeatures(state, fp.getPosition());
            int circusCount = features.filter(f -> f instanceof Circus).length();
            int acrobatsCount = features.filter(f -> f instanceof Acrobats).length();
            List<ExprItem> exprItems = List.empty();
            if (circusCount > 0) {
                exprItems = exprItems.append(new ExprItem(circusCount, "circus", circusCount * RINGMASTER_BONUS));
            }
            if (acrobatsCount > 0) {
                exprItems = exprItems.append(new ExprItem(acrobatsCount, "acrobats", acrobatsCount * RINGMASTER_BONUS));
            }
            if (!exprItems.isEmpty()) {
                PointsExpression expr = new PointsExpression("ringmaster", exprItems);
                bonusPoints = bonusPoints.append(new ReceivedPoints(expr, meeple.getPlayer(), fp));
            }
        }
        return bonusPoints;
    }
    	
    private List<Feature> getNeigbouringFeatures(GameState state, Position pos) {
        return state
                .getAdjacentAndDiagonalTiles(pos)
                .append(state.getPlacedTile(pos))
                .flatMap(t -> t.getTile().getInitialFeatures().values())
                .toList();
    }
}
