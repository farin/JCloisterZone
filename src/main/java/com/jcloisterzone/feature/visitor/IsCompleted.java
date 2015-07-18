package com.jcloisterzone.feature.visitor;

import com.jcloisterzone.feature.Completable;
import com.jcloisterzone.feature.Feature;

public class IsCompleted implements FeatureVisitor<Boolean> {

    private boolean isCompleted = true;

    @Override
    public VisitResult visit(Feature feature) {
        Completable completable = (Completable) feature;
        if (completable.isOpen()) {
            isCompleted = false;
            return VisitResult.STOP;
        }
        return VisitResult.CONTINUE;
    }

    @Override
    public Boolean getResult() {
        return isCompleted;
    }

}
