package com.jcloisterzone.game.capability;

import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.TileModifier;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.ExprItem;
import com.jcloisterzone.event.PointsExpression;
import com.jcloisterzone.event.ScoreEvent;
import com.jcloisterzone.feature.*;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.PlacedTile;
import io.vavr.Predicates;
import io.vavr.Tuple2;
import io.vavr.collection.List;
import io.vavr.collection.Stream;

import java.util.ArrayList;

public class WatchtowerCapability  extends Capability<Void> {

    public static class WatchtowerModifier extends TileModifier {
        private int points;
        private String subject;

        public WatchtowerModifier(String type) {
            super("Watchtower:" + type);
            String[] tokens = type.split("/", 2);
            this.points = Integer.parseInt(tokens[0]);
            this.subject = tokens[1];
        }
    }

    @Override
    public GameState beforeCompletableScore(GameState state, java.util.Set<Completable> features) {
        java.util.Map<Position, WatchtowerModifier> watchtowers = new java.util.HashMap<>();
        java.util.Map<Position, java.util.List<Tuple2<Meeple, FeaturePointer>>> watchtowerMeeples = new java.util.HashMap<>();

        state.getPlacedTiles().forEach((pos, pt) -> {
            pt.getTile().getTileModifiers().filter(Predicates.instanceOf(WatchtowerModifier.class)).forEach(mod -> {
                watchtowers.put(pos, (WatchtowerModifier) mod);
            });
        });

        if (watchtowers.isEmpty()) {
            return state;
        }

        for (var t : state.getDeployedMeeples()) {
            FeaturePointer fp = t._2;
            if (!watchtowers.containsKey(fp.getPosition())) {
                continue;
            }

            if (features.stream().filter(f -> f.getPlaces().contains(fp)).findAny().isPresent()) {
                var meeples = watchtowerMeeples.get(fp.getPosition());
                if (meeples == null) {
                    meeples = new ArrayList<>();
                    watchtowerMeeples.put(fp.getPosition(), meeples);
                }
                meeples.add(t);
            }
        }

        for (var entry : watchtowerMeeples.entrySet()) {
            Position pos = entry.getKey();
            WatchtowerModifier watchtower = watchtowers.get(pos);
            int count = 0;
            String exprName = null;

            switch (watchtower.subject) {
                case "coat-of-arms":
                    GameState _state = state;
                    count = getNeigbouringFeatures(state, pos).map(f -> {
                        if (f instanceof City) {
                            return ((City)f).getModifier(_state, City.PENNANTS, 0);
                        }
                        return 0;
                    }).sum().intValue();
                    exprName = "pennants";
                    break;
                case "monastery":
                    count = getNeigbouringFeatures(state, pos).filter(Predicates.instanceOf(Monastery.class)).length();
                    exprName = "monasteries";
                    break;
                case "city":
                    count = getNeigbouring(state, pos).filter(pt -> !pt.getTile().getInitialFeatures().values().filter(Predicates.instanceOf(City.class)).isEmpty()).length();
                    exprName = "cities";
                    break;
                case "road":
                    count = getNeigbouring(state, pos).filter(pt -> !pt.getTile().getInitialFeatures().values().filter(Predicates.instanceOf(Road.class)).isEmpty()).length();
                    exprName = "roads";
                    break;
                case "meeple":
                    count = state.getDeployedMeeples().values().filter(fp -> {
                        Position mpos = fp.getPosition();
                        return Math.abs(pos.x - mpos.x) <= 1 && Math.abs(pos.y - mpos.y) <= 1;
                    }).length();
                    exprName = "meeples";
            }

            if (count > 0) {
                PointsExpression expr = new PointsExpression("watchtower", new ExprItem(count, exprName, count * watchtower.points));
                var receivedPoints= List.ofAll(entry.getValue()).map(t -> new ScoreEvent.ReceivedPoints(expr, t._1.getPlayer(), t._2));
                state = state.appendEvent(new ScoreEvent(receivedPoints, true, false));
            }
        }

        return state;
    }

    private Stream<PlacedTile> getNeigbouring(GameState state, Position pos) {
        return state
                .getAdjacentAndDiagonalTiles(pos)
                .append(state.getPlacedTile(pos));
    }

    private Stream<Feature> getNeigbouringFeatures(GameState state, Position pos) {
        return getNeigbouring(state, pos).flatMap(pt -> pt.getTile().getInitialFeatures().values());
    }
}
