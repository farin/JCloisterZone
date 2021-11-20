package com.jcloisterzone.game.capability;

import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.ExprItem;
import com.jcloisterzone.event.PointsExpression;
import com.jcloisterzone.event.ScoreEvent;
import com.jcloisterzone.event.ScoreEvent.ReceivedPoints;
import com.jcloisterzone.feature.*;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.figure.Ringmaster;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.Rule;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.PlacedTile;
import com.jcloisterzone.reducers.AddPoints;
import io.vavr.Predicates;
import io.vavr.Tuple2;
import io.vavr.collection.List;
import io.vavr.collection.Stream;

import java.util.ArrayList;

public class RingmasterCapability extends Capability<Void> {

	int RINGMASTER_BONUS = 2;
	
    @Override
    public List<ReceivedPoints> appendFiguresBonusPoints(GameState state, List<ReceivedPoints> bonusPoints, Scoreable feature, boolean isFinal) {
    	
        for (Tuple2<Meeple, FeaturePointer> t : state.getDeployedMeeples()) {
            Meeple m = t._1;
            FeaturePointer fp = t._2;
            if (!(m instanceof Ringmaster) || !feature.getPlaces().contains(fp)) {
                continue;
            }
            if (!("all-features".equals(state.getStringRule(Rule.RINGMASTER_VARIANT)))) {
            	if (fp.getFeature().equals(Monastery.class)) {
            		Monastery monastery = (Monastery) feature;
            		if (!(monastery.isMonastery(state))) {
            			continue;
            		}
            	} else if (!(fp.getFeature().equals(Road.class) || fp.getFeature().equals(City.class) || fp.getFeature().equals(Field.class))) {
            		continue;
            	}
            }
            GameState _state = state;
            int count = getNeigbouring(state, fp.getPosition()).filter(pt -> _state.getFeatureMap().get(pt.getPosition()).get().keySet().find(_fp -> _fp.getFeature().equals(Circus.class) || _fp.getFeature().equals(Acrobats.class)).isDefined()).length();
            if (count > 0) {
                PointsExpression expr = new PointsExpression("ringmaster", new ExprItem(count, "ringmaster", count * RINGMASTER_BONUS));
                bonusPoints = bonusPoints.append(new ReceivedPoints(expr, m.getPlayer(), fp));
            }
        }
        return bonusPoints;
    }
    	
    private Stream<PlacedTile> getNeigbouring(GameState state, Position pos) {
        return state
                .getAdjacentAndDiagonalTiles(pos)
                .append(state.getPlacedTile(pos));
    }
}
