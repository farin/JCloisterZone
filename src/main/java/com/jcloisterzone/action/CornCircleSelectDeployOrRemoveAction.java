package com.jcloisterzone.action;

import java.util.Arrays;

import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.ui.annotations.LinkedPanel;
import com.jcloisterzone.ui.grid.actionpanel.CornCirclesPanel;
import com.jcloisterzone.wsio.message.CornCircleRemoveOrDeployMessage;
import com.jcloisterzone.wsio.message.CornCircleRemoveOrDeployMessage.CornCicleOption;

import io.vavr.collection.HashSet;

@LinkedPanel(CornCirclesPanel.class)
public class CornCircleSelectDeployOrRemoveAction extends PlayerAction<CornCicleOption> {

    public CornCircleSelectDeployOrRemoveAction() {
        super(HashSet.ofAll(Arrays.asList(CornCicleOption.values())));
    }

    @Override
    public void perform(GameController gc, CornCicleOption option) {
        gc.getConnection().send(
            new CornCircleRemoveOrDeployMessage(gc.getGameId(), option)
        );
    }

    @Override
    public String toString() {
        return "DEPLOY or REMOVE";
    }

}
