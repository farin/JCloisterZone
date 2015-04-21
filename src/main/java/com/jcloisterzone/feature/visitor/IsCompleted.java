package com.jcloisterzone.feature.visitor;

import com.jcloisterzone.feature.Completable;
import com.jcloisterzone.feature.Feature;

public class IsCompleted implements FeatureVisitor<Boolean> {

    private boolean isCompleted = true;

    @Override
    public boolean visit(Feature feature) {
        Completable completable = (Completable) feature;
        if (completable.isOpen()) {
            isCompleted = false;
            return false;
        }
        return true;
    }

    @Override
    public Boolean getResult() {
        return isCompleted;
    }

}
