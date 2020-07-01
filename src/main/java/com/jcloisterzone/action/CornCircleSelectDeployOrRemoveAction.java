package com.jcloisterzone.action;

import com.jcloisterzone.wsio.message.CornCircleRemoveOrDeployMessage;
import com.jcloisterzone.wsio.message.CornCircleRemoveOrDeployMessage.CornCircleOption;
import com.jcloisterzone.wsio.message.WsInGameMessage;
import io.vavr.collection.HashSet;

import java.util.Arrays;

public class CornCircleSelectDeployOrRemoveAction extends AbstractPlayerAction<CornCircleOption> {

    public CornCircleSelectDeployOrRemoveAction() {
        super(HashSet.ofAll(Arrays.asList(CornCircleOption.values())));
    }

    @Override
    public WsInGameMessage select(CornCircleOption option) {
        return new CornCircleRemoveOrDeployMessage(option);
    }

    @Override
    public String toString() {
        return "DEPLOY or REMOVE";
    }

}
