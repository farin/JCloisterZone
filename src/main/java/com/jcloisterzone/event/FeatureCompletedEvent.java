package com.jcloisterzone.event;

import com.jcloisterzone.Player;
import com.jcloisterzone.feature.Completable;
import com.jcloisterzone.feature.visitor.score.CompletableScoreContext;

public class FeatureCompletedEvent extends Event {

    private final Completable feature;
    private final CompletableScoreContext scoreContent;

    public FeatureCompletedEvent(Player player, Completable feature, CompletableScoreContext scoreContent) {
        super(player);
        this.feature = feature;
        this.scoreContent = scoreContent;
    }

    public Completable getFeature() {
        return feature;
    }

    public CompletableScoreContext getScoreContent() {
        return scoreContent;
    }

}
