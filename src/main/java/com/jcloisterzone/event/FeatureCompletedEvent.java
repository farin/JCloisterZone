package com.jcloisterzone.event;

import com.jcloisterzone.Player;
import com.jcloisterzone.feature.Completable;
import com.jcloisterzone.feature.visitor.score.CompletableScoreContext;

public class FeatureCompletedEvent extends PlayEvent {

    private final Completable feature;
    private final CompletableScoreContext scoreContent;

    public FeatureCompletedEvent(Player triggeringPlayer, Completable feature, CompletableScoreContext scoreContent) {
        super(triggeringPlayer, null);
        this.feature = feature;
        this.scoreContent = scoreContent;
    }

    public Completable getFeature() {
        return feature;
    }

    public CompletableScoreContext getScoreContent() {
        return scoreContent;
    }

    @Override
    public String toString() {
        return super.toString() + " feature:" + feature;
    }

}
