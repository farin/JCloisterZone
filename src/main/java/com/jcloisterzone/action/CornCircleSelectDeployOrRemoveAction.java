package com.jcloisterzone.action;

import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.io.message.CornCircleRemoveOrDeployMessage;
import com.jcloisterzone.io.message.CornCircleRemoveOrDeployMessage.CornCircleOption;
import com.jcloisterzone.io.message.Message;
import io.vavr.collection.HashSet;

import java.util.Arrays;

public class CornCircleSelectDeployOrRemoveAction extends AbstractPlayerAction<CornCircleOption> {

    private Class<? extends Feature> cornType;

    public CornCircleSelectDeployOrRemoveAction( Class<? extends Feature> cornType) {
        super(HashSet.ofAll(Arrays.asList(CornCircleOption.values())));
        this.cornType = cornType;
    }

    public Class<? extends Feature> getCornType() {
        return cornType;
    }

    @Override
    public Message select(CornCircleOption option) {
        return new CornCircleRemoveOrDeployMessage(option);
    }

    @Override
    public String toString() {
        return "DEPLOY or REMOVE";
    }

}
