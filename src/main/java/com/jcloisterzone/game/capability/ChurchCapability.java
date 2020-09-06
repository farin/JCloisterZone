package com.jcloisterzone.game.capability;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.pointer.BoardPointer;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.PointsExpression;
import com.jcloisterzone.event.ScoreEvent.ReceivedPoints;
import com.jcloisterzone.feature.Cloister;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.Scoreable;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.state.GameState;
import io.vavr.Tuple2;
import io.vavr.collection.LinkedHashMap;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.Set;
import org.w3c.dom.Element;

import static com.jcloisterzone.XMLUtils.attributeBoolValue;

public class ChurchCapability extends Capability<Void> {

    private static final long serialVersionUID = 1L;

    public static final int CHURCH_TILES_BONUS = 3;

    @Override
    public Feature initFeature(GameState state, String tileId, Feature feature, Element xml) {
        if (feature instanceof Cloister && attributeBoolValue(xml, "church")) {
            feature = ((Cloister)feature).setChurch(true);
        }
        return feature;
    }

    @Override
    public List<ReceivedPoints> appendBonusPoints(GameState state, List<ReceivedPoints> bonusPoints, Scoreable feature, boolean isFinal) {
        if (isFinal || !(feature instanceof Cloister)) {
            return bonusPoints;
        }
        Cloister cloister = (Cloister) feature;
        if (!cloister.isChurch()) {
            return bonusPoints;
        }

        Position cloisterPosition = cloister.getPlace().getPosition();
        Set<Position> positions = Position.ADJACENT_AND_DIAGONAL.map(pt -> cloisterPosition.add(pt._2)).toSet().add(cloisterPosition);
        Map<Player, LinkedHashMap<Meeple, FeaturePointer>> adjacentMeepleCount = state.getDeployedMeeples()
                .filter(mt -> mt._1 instanceof  Follower)
                .filter(mt -> positions.contains(mt._2.getPosition()))
                .groupBy(mt -> mt._1.getPlayer());
                //.map((p, mts)-> new Tuple2<Player, Integer>(p, mts.size()));
        int max = adjacentMeepleCount.values().map(map -> map.size()).max().getOrElse(-1);
        Set<Player> players = adjacentMeepleCount.filter((p, map) -> map.size() == max).keySet();


        BoardPointer ptr = state.getNeutralFigures().getFairyDeployment();
        for (Player player: players) {
            LinkedHashMap<Meeple, FeaturePointer> followers = adjacentMeepleCount.get(player).get();
            Tuple2<Meeple, FeaturePointer> onTile = followers.filter(t -> t._2.getPosition().equals(cloisterPosition)).getOrNull();
            FeaturePointer fp;
            if (onTile == null) {
                // if user hasn't follower on cloister itself, use random follower from adjacent tile
                fp = followers.get()._2;
            } else {
                fp = onTile._2;
            }

            PointsExpression expr = new PointsExpression(CHURCH_TILES_BONUS, "cloister.church");
            bonusPoints = bonusPoints.append(new ReceivedPoints(expr, player, fp));
        }

        return bonusPoints;
    }
}
