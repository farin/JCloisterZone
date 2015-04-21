package com.jcloisterzone.ai;

import com.google.common.eventbus.Subscribe;
import com.jcloisterzone.event.SelectActionEvent;
import com.jcloisterzone.event.SelectDragonMoveEvent;

public class DummyAiPlayer extends AiPlayer {

    @Subscribe
    public void selectAction(SelectActionEvent ev) {
    	if (isAiActive(ev)) {
    		selectDummyAction(ev.getActions(), ev.isPassAllowed());
    	}
    }

    @Subscribe
    public void selectDragonMove(SelectDragonMoveEvent ev) {
    	if (isAiActive(ev)) {
    		selectDummyDragonMove(ev.getPositions(), ev.getMovesLeft());
    	}
    }

}
