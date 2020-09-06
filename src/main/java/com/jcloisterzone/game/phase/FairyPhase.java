package com.jcloisterzone.game.phase;

import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.pointer.BoardPointer;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.board.pointer.MeeplePointer;
import com.jcloisterzone.event.PointsExpression;
import com.jcloisterzone.event.ScoreEvent;
import com.jcloisterzone.event.ScoreEvent.ReceivedPoints;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.RandomGenerator;
import com.jcloisterzone.game.capability.FairyCapability;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.reducers.AddPoints;
import io.vavr.Tuple2;


@RequiredCapability(FairyCapability.class)
public class FairyPhase extends Phase {

    public FairyPhase(RandomGenerator random) {
        super(random);
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
            if (!t._2.equals(fairyFp)) continue;

            if (!onTileRule) {
                if (!((MeeplePointer) ptr).getMeepleId().equals(m.getId())) continue;
            }

            state = new AddPoints(m.getPlayer(), FairyCapability.FAIRY_POINTS_BEGINNING_OF_TURN).apply(state);

            PointsExpression expr = new PointsExpression(FairyCapability.FAIRY_POINTS_BEGINNING_OF_TURN, "fairy.turn");
            state = state.appendEvent(new ScoreEvent(new ReceivedPoints(expr, m.getPlayer(), fairyFp), false, false));
        }

        return next(state);
    }
}
