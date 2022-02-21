package com.jcloisterzone.game.phase;

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

        for (Tuple2<Meeple, FeaturePointer> t : state.getDeployedMeeples()) {
            Meeple m = t._1;
            if (!m.getPlayer().equals(state.getTurnPlayer())) continue;
            if (onTileRule) {
                if (!t._2.getPosition().equals(fairyFp.getPosition())) continue;
            } else if (!(m.getFeature(state) instanceof Acrobats)) {
                if (!t._2.equals(fairyFp)) continue;
               	if (!((MeeplePointer) ptr).match(m)) continue;
            }

            PointsExpression expr = new PointsExpression("fairy.turn", new ExprItem("fairy", FairyCapability.FAIRY_POINTS_BEGINNING_OF_TURN));
            state = (new AddPoints(new ReceivedPoints(expr, m.getPlayer(), fairyFp), false)).apply(state);
            if (!(m.getFeature(state) instanceof Acrobats)) {
                break;
            }
        }

        return next(state);
    }
}
