package com.jcloisterzone.reducers;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.PointsExpression;
import com.jcloisterzone.event.ScoreEvent.ReceivedPoints;
import com.jcloisterzone.feature.Field;
import com.jcloisterzone.feature.Scoreable;
import com.jcloisterzone.figure.Barn;
import com.jcloisterzone.figure.Special;
import com.jcloisterzone.game.ScoreFeatureReducer;
import com.jcloisterzone.game.state.GameState;
import io.vavr.Tuple2;
import io.vavr.collection.*;

public class ScoreFieldBarn implements ScoreFeatureReducer {

    private final Field field;
    private final boolean isFinal;

    // "out" variable - computed owners are store to instance
    // to be available to reducer caller
    private Map<Player, PointsExpression> playerPoints = HashMap.empty();

    public ScoreFieldBarn(Field field, boolean isFinal) {
        this.field = field;
        this.isFinal = isFinal;
    }

    @Override
    public Scoreable getFeature() {
        return field;
    }

    @Override
    public GameState apply(GameState state) {
        Stream<Tuple2<Special, FeaturePointer>> barns = field.getSpecialMeeples2(state)
            .filter(t -> t._1 instanceof Barn);

        PointsExpression expr = field.getBarnPoints(state);
        List<ReceivedPoints> receivedPoints = List.empty();

        for (Tuple2<Special, FeaturePointer> t : barns) {
            Barn barn = (Barn) t._1;
            playerPoints = playerPoints.put(barn.getPlayer(), expr);
            receivedPoints = receivedPoints.append(new ReceivedPoints(expr, barn.getPlayer(), t._2));
        }

        return (new AddPoints(receivedPoints, true, isFinal)).apply(state);
    }

    @Override
    public Set<Player> getOwners() {
        return playerPoints.keySet();
    }

    @Override
    public PointsExpression getFeaturePoints() {
        throw new UnsupportedOperationException("Call getFeaturePoints() with player argument");
    }

    @Override
    public PointsExpression getFeaturePoints(Player player) {
        return playerPoints.get(player).getOrNull();
    }
}
