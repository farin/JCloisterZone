package com.jcloisterzone.game.capability;

import com.jcloisterzone.Player;
import com.jcloisterzone.XMLUtils;
import com.jcloisterzone.action.ReturnMeepleAction;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.board.pointer.MeeplePointer;
import com.jcloisterzone.event.ExprItem;
import com.jcloisterzone.event.PointsExpression;
import com.jcloisterzone.event.ScoreEvent;
import com.jcloisterzone.feature.*;
import com.jcloisterzone.figure.Abbot;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.state.ActionsState;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.io.message.ReturnMeepleMessage;
import com.jcloisterzone.reducers.AddPoints;
import com.jcloisterzone.reducers.DeployMeeple;
import io.vavr.Predicates;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.HashMap;
import io.vavr.collection.HashSet;
import io.vavr.collection.List;
import io.vavr.collection.Vector;
import org.w3c.dom.Element;

public class RussianPromosTrapCapability extends Capability<Void> {

    @Override
    public Tile initTile(GameState state, Tile tile, Vector<Element> tileElements) {
        if (!XMLUtils.getElementStreamByTagName(tileElements, "razboynik").isEmpty()) {
            SoloveiRazboynik razboynik = new SoloveiRazboynik();
            tile = tile.setInitialFeatures(tile.getInitialFeatures().put(razboynik.getPlace(), razboynik));
        }
        if (!XMLUtils.getElementStreamByTagName(tileElements, "vodyanoy").isEmpty()) {
            Vodyanoy vodyanoy = new Vodyanoy();
            tile = tile.setInitialFeatures(tile.getInitialFeatures().put(vodyanoy.getPlace(), vodyanoy));
        }
        return tile;
    }

    @Override
    public boolean isMeepleDeploymentAllowed(GameState state, Position pos) {
        return !state.getPlacedTile(pos).getTile().getInitialFeatures().keySet().filter(fp -> fp.getFeature().equals(Vodyanoy.class)).isEmpty();
    }

    @Override
    public GameState onActionPhaseEntered(GameState state) {
        ActionsState actions = state.getPlayerActions();
        HashSet places = HashSet.empty();
        Player active = state.getActivePlayer();
        for (Tuple2<Meeple, FeaturePointer> t : state.getDeployedMeeples()) {
            Meeple meeple = t._1;
            FeaturePointer fp = t._2;
            Feature feature = state.getFeature(fp);
            if (meeple.getPlayer().equals(active) && (feature instanceof SoloveiRazboynik || feature instanceof Vodyanoy)) {
                places = places.add(new MeeplePointer(fp, meeple.getId()));
            }
        }

        if (!places.isEmpty()){
            actions = actions.appendAction(new ReturnMeepleAction(places, ReturnMeepleMessage.ReturnMeepleSource.TRAP));
            state = state.setPlayerActions(actions);
        }
        return state;
    }

    @Override
    public GameState onFinalScoring(GameState state) {
        for (Vodyanoy vodyanoy : state.getFeatures(Vodyanoy.class)) {
            List<ScoreEvent.ReceivedPoints> receivedPoints = List.empty();
            HashMap<Player, Integer> counts = vodyanoy.getFollowers(state)
                    .foldLeft(HashMap.empty(), (acc, follower) -> {
                        Player player = follower.getPlayer();
                        return acc.put(player, acc.get(player).getOrElse(0) + 1);
                    });

            for (Tuple2<Player, Integer> t : counts) {
                Player player = t._1;
                int followersCount = t._2;
                PointsExpression expr = new PointsExpression( "vodyanoy", new ExprItem(followersCount, "meeples", -2 * followersCount));
                state = (new AddPoints(player, expr.getPoints())).apply(state);
                receivedPoints = receivedPoints.append(new ScoreEvent.ReceivedPoints(expr, player, vodyanoy.getSampleFollower2(state, player)._2));
            }

            if (!receivedPoints.isEmpty()) {
                state = state.appendEvent(new ScoreEvent(receivedPoints, true, true));
            }
        }
        return state;
    }

    public List<ExposedFollower> findExposedFollowers(GameState state) {
        List<ExposedFollower> result = List.empty();
        java.util.Set<String> alreadyExposed = new java.util.HashSet();

        for (Feature feature : state.getFeatures()) {
            if (feature instanceof SoloveiRazboynik) {
                Position pos = feature.getPlaces().get().getPosition();
                Road road = (Road) state.getFeatureMap().get(new FeaturePointer(pos, Road.class, Location.WE)).getOrNull();
                if (road == null) {
                    road = (Road) state.getFeatureMap().get(new FeaturePointer(pos, Road.class, Location.NS)).getOrNull();
                }
                FeaturePointer trap = new FeaturePointer(pos, SoloveiRazboynik.class, Location.I);
                for (Tuple2<Follower, FeaturePointer> t : road.getFollowers2(state)) {
                    Follower meeple = t._1;
                    if (!alreadyExposed.contains(meeple.getId())) {
                        result = result.append(new ExposedFollower(meeple, trap));
                        alreadyExposed.add(meeple.getId());
                    }
                }
            } else if (feature instanceof Vodyanoy) {
                Position pos = feature.getPlaces().get().getPosition();
                FeaturePointer trap = new FeaturePointer(pos, Vodyanoy.class, Location.I);

                for (Tuple2<Meeple, FeaturePointer> t : state.getDeployedMeeples()) {
                    Meeple meeple = t._1;
                    if (!(meeple instanceof Follower)) continue;
                    if (alreadyExposed.contains(meeple.getId())) continue;
                    FeaturePointer fp = t._2;
                    Position p = fp.getPosition();
                    Feature f = state.getFeature(fp);
                    if (fp.getLocation().isCityOfCarcassonneQuarter() || f instanceof Vodyanoy || f instanceof SoloveiRazboynik || f instanceof Castle) continue;
                    if (Math.abs(p.x - pos.x) <= 1 && Math.abs(p.y - pos.y) <= 1 && !pos.equals(p)) {
                        result = result.append(new ExposedFollower((Follower) t._1, trap));
                        alreadyExposed.add(meeple.getId());
                    }
                }
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
