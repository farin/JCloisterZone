package com.jcloisterzone.action;

import com.jcloisterzone.feature.Feature;

public class CornCircleSelectDeployOrRemoveAction extends AbstractPlayerAction<Void> {

    private Class<? extends Feature> cornType;

    public CornCircleSelectDeployOrRemoveAction( Class<? extends Feature> cornType) {
        super(null);
        this.cornType = cornType;
    }

    public Class<? extends Feature> getCornType() {
        return cornType;
    }
}
