package com.jcloisterzone.game.phase;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.pointer.BoardPointer;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.board.pointer.MeeplePointer;
import com.jcloisterzone.event.ExprItem;
import com.jcloisterzone.event.PointsExpression;
import com.jcloisterzone.event.ScoreEvent.ReceivedPoints;
import com.jcloisterzone.feature.Acrobats;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.capability.FairyCapability;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.random.RandomGenerator;
import com.jcloisterzone.reducers.AddPoints;
import io.vavr.Tuple2;
import io.vavr.collection.LinkedHashMap;

import java.util.HashMap;


public class FairyPhase extends Phase {

    public FairyPhase(RandomGenerator random, Phase defaultNext) {
        super(random, defaultNext);
    }

    @Override
    public StepResult enter(GameState state) {
        BoardPointer ptr = state.getNeutralFigures().getFairyDeployment();
        if (ptr == null) {
            return next(state);
        }

        boolean onTileRule = ptr instanceof Position;
        FeaturePointer fairyFp = ptr.asFeaturePointer();
        boolean fairyOnAcrobats = fairyFp.getFeature().equals(Acrobats.class);

        var points = 0;


        for (Tuple2<Meeple, FeaturePointer> t : state.getDeployedMeeples()) {
            Meeple m = t._1;
            if (!m.getPlayer().equals(state.getTurnPlayer())) continue;
            if (onTileRule) {
                if (!t._2.getPosition().equals(fairyFp.getPosition())) continue;
            } else {
                if (!t._2.equals(fairyFp)) continue;
                if (!((MeeplePointer) ptr).match(m) && !fairyOnAcrobats) continue;
            }
            points += 1;
            if (!onTileRule && !fairyOnAcrobats) break; // little opt, only one can exist
        }

        if (points > 0) {
            PointsExpression expr = new PointsExpression("fairy.turn", new ExprItem( "fairy", points * FairyCapability.FAIRY_POINTS_BEGINNING_OF_TURN));
            state = (new AddPoints(new ReceivedPoints(expr, state.getTurnPlayer(), fairyFp), false)).apply(state);
        }

        return next(state);
    }
}
