package com.jcloisterzone.game.capability;

import com.jcloisterzone.Immutable;
import com.jcloisterzone.Player;
import com.jcloisterzone.action.FairyNextToAction;
import com.jcloisterzone.action.FairyOnTileAction;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.pointer.BoardPointer;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.board.pointer.MeeplePointer;
import com.jcloisterzone.event.PointsExpression;
import com.jcloisterzone.event.ScoreEvent.ReceivedPoints;
import com.jcloisterzone.feature.Scoreable;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.neutral.Fairy;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.Rule;
import com.jcloisterzone.game.state.GameState;
import io.vavr.Tuple2;
import io.vavr.collection.LinkedHashMap;
import io.vavr.collection.List;
import io.vavr.collection.Set;

@Immutable
public class FairyCapability extends Capability<Void> {

    private static final long serialVersionUID = 1L;

    public static final int FAIRY_POINTS_BEGINNING_OF_TURN = 1;
    public static final int FAIRY_POINTS_FINISHED_OBJECT = 3;

    @Override
    public GameState onStartGame(GameState state) {
        return state.mapNeutralFigures(nf -> nf.setFairy(new Fairy("fairy.1")));
    }

    @Override
    public GameState onActionPhaseEntered(GameState state) {
        boolean fairyOnTile = "on-tile".equals(state.getStringRule(Rule.FAIRY_PLACEMENT));
        Player activePlayer = state.getPlayerActions().getPlayer();


        LinkedHashMap<Follower, FeaturePointer> followers =
            state.getDeployedMeeples()
                .filter((m, fp) -> (m instanceof Follower) && m.getPlayer().equals(activePlayer))
                .mapKeys(m -> (Follower) m);

        Fairy fairy = state.getNeutralFigures().getFairy();

        if (fairyOnTile) {
            Set<Position> options = followers.values().map(fp -> fp.getPosition()).toSet();
            if (options.isEmpty()) {
                return state;
            }
            return state.appendAction(new FairyOnTileAction(fairy.getId(), options));
        } else {
            Set<MeeplePointer> options = followers.map(t -> new MeeplePointer(t)).toSet();
            if (options.isEmpty()) {
                return state;
            }
            return state.appendAction(new FairyNextToAction(fairy.getId(), options));
        }
    }

    @Override
    public List<ReceivedPoints> appendFiguresBonusPoints(GameState state, List<ReceivedPoints> bonusPoints, Scoreable feature, boolean isFinal) {
        BoardPointer ptr = state.getNeutralFigures().getFairyDeployment();

        if (ptr != null && !isFinal) {
            boolean onTileRule = ptr instanceof Position;

            for (Tuple2<Follower, FeaturePointer> t : feature.getFollowers2(state)) {
                Follower m = t._1;

                if (onTileRule && !ptr.getPosition().equals(t._2.getPosition())) continue;
                if (!onTileRule && !((MeeplePointer) ptr).getMeepleId().equals(m.getId())) continue;

                PointsExpression expr = new PointsExpression(FAIRY_POINTS_FINISHED_OBJECT, "fairy.completed");
                return bonusPoints.append(new ReceivedPoints(expr, m.getPlayer(), t._2));
            }
        }

        return bonusPoints;
    }
}
