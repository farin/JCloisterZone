package com.jcloisterzone.feature.visitor;

import com.jcloisterzone.feature.Completable;
import com.jcloisterzone.feature.Feature;

public class IsOccupiedAndUncompleted extends IsOccupied {

    private boolean isCompleted = true;

    @Override
    public VisitResult visit(Feature feature) {
        Completable completable = (Completable) feature;
        if (completable.isOpen()) {
            isCompleted = false;
        }
        super.visit(feature);
        return VisitResult.CONTINUE;
    }

    @Override
    public Boolean getResult() {
        return !isCompleted && super.getResult();
    }
}
