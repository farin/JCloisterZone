package com.jcloisterzone.game.capability;

import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.PlacementOption;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.ExprItem;
import com.jcloisterzone.event.PointsExpression;
import com.jcloisterzone.event.ScoreEvent;
import com.jcloisterzone.event.ScoreEvent.ReceivedPoints;
import com.jcloisterzone.feature.Monastery;
import com.jcloisterzone.feature.Scoreable;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.ScoreFeatureReducer;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.reducers.UndeployMeeples;
import io.vavr.Predicates;
import io.vavr.Tuple2;
import io.vavr.collection.Array;
import io.vavr.collection.HashMap;
import io.vavr.collection.Set;


public final class ShrineCapability extends Capability<Void> {

	private static final long serialVersionUID = 1L;

    @Override
    public GameState onTurnScoring(GameState state, HashMap<Scoreable, ScoreFeatureReducer> completed) {
        GameState _state = state;

        Set<Monastery> completedMonasteries = completed
                .filterValues(r -> r.getOwners().nonEmpty())
                .keySet()
                .filter(Predicates.instanceOf(Monastery.class))
                .map(f -> (Monastery) f);

        Set<Monastery> challenged = completedMonasteries
            .flatMap(monastery -> {
                Position pos = monastery.getPlace().getPosition();
                return getAdjacentMonasteries(_state, pos)
                    .filter(c -> c.isShrine(_state) ^ monastery.isShrine(_state));
            })
            .filter(c -> c.isOpen(_state))
            .distinct();

        for (Monastery monastery : challenged) {
            Meeple meeple = monastery.getMeeples(state).getOrNull();
            if (meeple == null) {
                continue;
            }

            PointsExpression expr = new PointsExpression(monastery.isShrine(state) ? "shrine.challenged" : "monastery.challenged", new ExprItem("shrine-challenge", 0));
            ScoreEvent scoreEvent = new ScoreEvent(new ReceivedPoints(expr, meeple.getPlayer(), meeple.getDeployment(state)), true, false);
            state = state.appendEvent(scoreEvent);
            state = (new UndeployMeeples(monastery, true)).apply(state);
        }
        return state;
    }

    @Override
    public boolean isTilePlacementAllowed(GameState state, Tile tile, PlacementOption placement) {
        // monastery taken from not yet placed Tile (with initial position set to 0,0)
        // is enough, because just isShrine() is needed
        Monastery monastery = getMonastery(tile);
        if (monastery == null) {
            return true;
        }
        Array<Monastery> monasteries = getAdjacentMonasteries(state, placement.getPosition());
        Array<Monastery> oppositeMonasteries = monasteries.filter(c -> c.isShrine(state) ^ monastery.isShrine(state));
        if (oppositeMonasteries.size() > 1) {
            // Disallow placement next to more than one Monastery of opposite type.
            return false;
        }
        if (oppositeMonasteries.size() == 1) {
            // Also there must be check if this monastery is not the second one for opposite cloister.
            Monastery opposite = oppositeMonasteries.get();
            Position oppositePos = opposite.getPlace().getPosition();
            if (!getAdjacentMonasteries(state, oppositePos)
                .filter(c -> c.isShrine(state) == monastery.isShrine(state))
                .isEmpty()
            ) {
                return false;
            }
        }

        return true;
    }

    private Monastery getMonastery(Tile tile) {
        return (Monastery) tile.getInitialFeatures()
            .map(Tuple2::_2)
            .filter(Predicates.instanceOf(Monastery.class)) // filter out Yaga hut
            .getOrNull();
    }

    private Array<Monastery> getAdjacentMonasteries(GameState state, Position pos) {
        return state
            .getAdjacentAndDiagonalTiles(pos)
            .map(pt -> state.getFeature(
                new FeaturePointer(pt.getPosition(), Location.MONASTERY)
            ))
            .filter(Predicates.instanceOf(Monastery.class)) // filter out Yaga huts
            .map(f -> (Monastery) f)
            .toArray();
    }
}
