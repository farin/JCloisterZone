package com.jcloisterzone.reducers;

import com.jcloisterzone.Player;
import com.jcloisterzone.event.PointsExpression;
import com.jcloisterzone.feature.Field;
import com.jcloisterzone.game.state.GameState;
import io.vavr.collection.HashMap;
import io.vavr.collection.Map;

public class ScoreField extends ScoreFeature {

    protected Map<Player, PointsExpression> playerPoints = HashMap.empty();
    private final String exprSubtitle;

    public ScoreField(Field feature, boolean isFinal, String exprSubtitle) {
        super(feature, isFinal);
        this.exprSubtitle = exprSubtitle;
    }

    public ScoreField(Field feature, boolean isFinal) {
        this(feature, isFinal, null);
    }

    @Override
    protected PointsExpression getFeaturePoints(GameState state, Player player) {
        PointsExpression value = getFeature().getPoints(state, exprSubtitle, player);
        playerPoints = playerPoints.put(player, value);
        return value;
    }

    @Override
    public PointsExpression getFeaturePoints() {
        throw new UnsupportedOperationException("Call getFeaturePoints() with player argument");
    }

    @Override
    public PointsExpression getFeaturePoints(Player player) {
        return playerPoints.get(player).getOrNull();
    }

    @Override
    public Field getFeature() {
        return (Field) super.getFeature();
    }

}
