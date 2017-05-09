package com.jcloisterzone.event;

import com.jcloisterzone.Player;
import com.jcloisterzone.event.play.PlayEvent;
import com.jcloisterzone.game.capability.CornCircleCapability.CornCicleOption;

public class CornCirclesOptionEvent extends PlayEvent {

    final CornCicleOption option;

    public CornCirclesOptionEvent(Player triggerPlayer, CornCicleOption option) {
        super(triggerPlayer, null);
        this.option = option;
    }

    public CornCicleOption getOption() {
        return option;
    }


}
