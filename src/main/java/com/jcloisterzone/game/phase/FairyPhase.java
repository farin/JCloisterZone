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
            } else {
                // Acrobats has special handling, assign points when fairy is assigned to ANY follower in pyramid
                // Wikicarpedia:
                //   You may assign the fairy to an acrobat, in which case it counts for all acrobats in the pyramid
                //   (regardless of color). If the fairy is still there at the beginning of your turn, you score 1 point
                //   for each of your meeples in the pyramid.
                if (fairyFp.getFeature().equals(Acrobats.class)) {
                    if (!fairyFp.getPosition().equals(t._2.getPosition())) continue;
                    if (!fairyFp.getLocation().equals(t._2.getLocation())) continue;
                } else {
                    if (!t._2.equals(fairyFp)) continue;
                    if (!((MeeplePointer) ptr).match(m)) continue;
                }
            }

            PointsExpression expr = new PointsExpression("fairy.turn", new ExprItem("fairy", FairyCapability.FAIRY_POINTS_BEGINNING_OF_TURN));
            state = (new AddPoints(new ReceivedPoints(expr, m.getPlayer(), fairyFp), false)).apply(state);
            break;
        }

        return next(state);
    }
}
