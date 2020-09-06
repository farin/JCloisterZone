package com.jcloisterzone.game;

import com.jcloisterzone.Player;
import com.jcloisterzone.event.PointsExpression;
import com.jcloisterzone.feature.Scoreable;
import com.jcloisterzone.reducers.Reducer;
import io.vavr.collection.Set;

public interface ScoreFeatureReducer extends Reducer {

    Scoreable getFeature();
    Set<Player> getOwners();

    PointsExpression getFeaturePoints();

    default PointsExpression getFeaturePoints(Player player) {
        return getOwners().contains(player) ? getFeaturePoints() : null;
    }
}
