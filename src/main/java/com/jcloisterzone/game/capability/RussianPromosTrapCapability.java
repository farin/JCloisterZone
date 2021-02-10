package com.jcloisterzone.game.capability;

import com.jcloisterzone.Player;
import com.jcloisterzone.XMLUtils;
import com.jcloisterzone.action.ReturnMeepleAction;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.board.pointer.MeeplePointer;
import com.jcloisterzone.feature.CloisterLike;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.Road;
import com.jcloisterzone.feature.SoloveiRazboynik;
import com.jcloisterzone.figure.Abbot;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.state.ActionsState;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.io.message.ReturnMeepleMessage;
import com.jcloisterzone.reducers.DeployMeeple;
import io.vavr.Predicates;
import io.vavr.Tuple2;
import io.vavr.collection.HashSet;
import io.vavr.collection.List;
import io.vavr.collection.Vector;
import org.w3c.dom.Element;

public class RussianPromosTrapCapability extends Capability<Void> {

    @Override
    public Tile initTile(GameState state, Tile tile, Vector<Element> tileElements) {
        if (!XMLUtils.getElementStreamByTagName(tileElements, "razboynik").isEmpty()) {
            SoloveiRazboynik razboynik = new SoloveiRazboynik();
            tile = tile.setInitialFeatures(tile.getInitialFeatures().put(Location.TOWER, razboynik));
        }
        return tile;
    }

    @Override
    public GameState onActionPhaseEntered(GameState state) {
        ActionsState actions = state.getPlayerActions();
        HashSet places = HashSet.empty();
        Player active = state.getActivePlayer();
        for (Tuple2<Meeple, FeaturePointer> t : state.getDeployedMeeples()) {
            Meeple meeple = t._1;
            FeaturePointer fp = t._2;
            if (meeple.getPlayer().equals(active) && state.getFeature(fp) instanceof SoloveiRazboynik) {
                places = places.add(new MeeplePointer(fp, meeple.getId()));
            }
        }

        if (!places.isEmpty()){
            actions = actions.appendAction(new ReturnMeepleAction(places, ReturnMeepleMessage.ReturnMeepleSource.TRAP));
            state = state.setPlayerActions(actions);
        }
        return state;
    }

    public List<ExposedFollower> findExposedFollowers(GameState state) {
        List<ExposedFollower> result = List.empty();

        for (Feature razboynik : state.getFeatures().filter(Predicates.instanceOf(SoloveiRazboynik.class))) {
            Position pos = razboynik.getPlaces().get().getPosition();
            Road road = (Road) state.getFeatureMap().get(new FeaturePointer(pos, Location.WE)).getOrNull();
            if (road == null) {
                road = (Road) state.getFeatureMap().get(new FeaturePointer(pos, Location.NS)).getOrNull();
            }
            FeaturePointer trap = new FeaturePointer(pos, Location.TOWER);
            for (Tuple2<Follower, FeaturePointer> t : road.getFollowers2(state)) {
                Follower meeple = t._1;
                result = result.append(new ExposedFollower(meeple, trap));
            }
        };

        return result;
    }

    public GameState trapFollowers(GameState state, List<ExposedFollower> exposedFollowers) {
        for (ExposedFollower exposed : exposedFollowers) {
            state = (new DeployMeeple(exposed.getFollower(), exposed.getTrap())).apply(state);
        }
        return state;
    }

    public GameState trapFollowers(GameState state) {
        return trapFollowers(state, findExposedFollowers(state));
    }

    public static class ExposedFollower {
        private Follower follower;
        private FeaturePointer trap;

        public ExposedFollower(Follower follower, FeaturePointer trap) {
            this.follower = follower;
            this.trap = trap;
        }

        public Follower getFollower() {
            return follower;
        }

        public FeaturePointer getTrap() {
            return trap;
        }
    }
}
