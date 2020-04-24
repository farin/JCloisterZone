package com.jcloisterzone.game.capability;

import static com.jcloisterzone.XMLUtils.attributeBoolValue;

import org.w3c.dom.Element;

import com.jcloisterzone.PointCategory;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.PlacementOption;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.play.ScoreEvent;
import com.jcloisterzone.feature.Cloister;
import com.jcloisterzone.feature.Feature;
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
    public Feature initFeature(GameState settings, String tileId, Feature feature, Element xml) {
        if (feature instanceof Cloister) {
            return ((Cloister)feature).setShrine(attributeBoolValue(xml, "shrine"));
        }
        return feature;
    }

    @Override
    public GameState onTurnScoring(GameState state, HashMap<Scoreable, ScoreFeatureReducer> completed) {
        GameState _state = state;

        Set<Cloister> completedCloisters = completed
                .filterValues(r -> r.getOwners().nonEmpty())
                .keySet()
                .filter(Predicates.instanceOf(Cloister.class))
                .map(f -> (Cloister) f);

        Set<Cloister> challenged = completedCloisters
            .flatMap(cloister -> {
                Position pos = cloister.getPlace().getPosition();
                return getAdjacentCloisters(_state, pos)
                    .filter(c -> c.isShrine() ^ cloister.isShrine());
            })
            .filter(c -> c.isOpen(_state))
            .distinct();

        for (Cloister cloister : challenged) {
            Meeple meeple = cloister.getMeeples(state).getOrNull();
            if (meeple == null) {
                continue;
            }

            ScoreEvent scoreEvent = new ScoreEvent(0, PointCategory.CLOISTER, false, cloister.getPlace(), meeple);
            state = state.appendEvent(scoreEvent);
            state = (new UndeployMeeples(cloister, true)).apply(state);
        }
        return state;
    }

    @Override
    public boolean isTilePlacementAllowed(GameState state, Tile tile, PlacementOption placement) {
        // cloister taken from not yet placed Tile (with initial position set to 0,0)
        // is enough, because just isShrine() is needed
        Cloister cloister = getCloister(tile);
        if (cloister == null) {
            return true;
        }
        Array<Cloister> cloisters = getAdjacentCloisters(state, placement.getPosition());
        Array<Cloister> oppositeCloisters = cloisters.filter(c -> c.isShrine() ^ cloister.isShrine());
        if (oppositeCloisters.size() > 1) {
            // Disallow placement next to more than one Cloister of opposite type.
            return false;
        }
        if (oppositeCloisters.size() == 1) {
            // Also there must be check if this cloister is not the second one for opposite cloister.
            Cloister opposite = oppositeCloisters.get();
            Position oppositePos = opposite.getPlace().getPosition();
            if (!getAdjacentCloisters(state, oppositePos)
                .filter(c -> c.isShrine() == cloister.isShrine())
                .isEmpty()
            ) {
                return false;
            }
        }

        return true;
    }

    private Cloister getCloister(Tile tile) {
        return (Cloister) tile.getInitialFeatures()
            .map(Tuple2::_2)
            .filter(Predicates.instanceOf(Cloister.class)) // filter out Yaga hut
            .getOrNull();
    }

    private Array<Cloister> getAdjacentCloisters(GameState state, Position pos) {
        return state
            .getAdjacentAndDiagonalTiles(pos)
            .map(pt -> state.getFeature(
                new FeaturePointer(pt.getPosition(), Location.CLOISTER)
            ))
            .filter(Predicates.instanceOf(Cloister.class)) // filter out Yaga huts
            .map(f -> (Cloister) f)
            .toArray();
    }
}
