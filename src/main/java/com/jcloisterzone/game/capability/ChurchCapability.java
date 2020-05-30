package com.jcloisterzone.game.capability;

import com.jcloisterzone.Player;
import com.jcloisterzone.PointCategory;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.pointer.BoardPointer;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.board.pointer.MeeplePointer;
import com.jcloisterzone.event.play.ScoreEvent;
import com.jcloisterzone.feature.Cloister;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.Scoreable;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.BonusPoints;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.ScoreFeatureReducer;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.reducers.AddPoints;
import io.vavr.Tuple2;
import io.vavr.collection.*;
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
    public List<BonusPoints> appendBonusPoints(GameState state, List<BonusPoints> bonusPoints, Scoreable feature, boolean isFinal) {
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
            Follower f = (Follower) followers.filter(t -> t._2.getPosition().equals(cloisterPosition)).keySet().getOrNull();
            List<Position> source = null;
            if (f == null) {
                // if user hasn't follower on cloister itself, use random follower from adjacent tile
                f = (Follower) followers.get()._1;
                source = List.of(cloisterPosition);
            }
            bonusPoints = bonusPoints.append(new BonusPoints(CHURCH_TILES_BONUS, PointCategory.CLOISTER, player, f, source));
        }

        return bonusPoints;
    }
}
