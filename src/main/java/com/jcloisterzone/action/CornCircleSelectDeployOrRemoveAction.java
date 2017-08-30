package com.jcloisterzone.action;

import java.util.Arrays;

import com.jcloisterzone.ui.annotations.LinkedPanel;
import com.jcloisterzone.ui.grid.actionpanel.CornCirclesPanel;
import com.jcloisterzone.wsio.message.CornCircleRemoveOrDeployMessage;
import com.jcloisterzone.wsio.message.CornCircleRemoveOrDeployMessage.CornCicleOption;
import com.jcloisterzone.wsio.message.WsInGameMessage;

import io.vavr.collection.HashSet;

@LinkedPanel(CornCirclesPanel.class)
public class CornCircleSelectDeployOrRemoveAction extends PlayerAction<CornCicleOption> {

    public CornCircleSelectDeployOrRemoveAction() {
        super(HashSet.ofAll(Arrays.asList(CornCicleOption.values())));
    }

    @Override
    public WsInGameMessage select(CornCicleOption option) {
        return new CornCircleRemoveOrDeployMessage(option);
    }

    @Override
    public String toString() {
        return "DEPLOY or REMOVE";
    }

}
