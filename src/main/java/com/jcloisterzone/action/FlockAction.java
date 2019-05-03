package com.jcloisterzone.action;

import java.util.Arrays;

import com.jcloisterzone.ui.annotations.LinkedPanel;
import com.jcloisterzone.ui.grid.actionpanel.FlockPanel;
import com.jcloisterzone.wsio.message.FlockMessage;
import com.jcloisterzone.wsio.message.FlockMessage.FlockOption;
import com.jcloisterzone.wsio.message.WsInGameMessage;

import io.vavr.collection.HashSet;

@LinkedPanel(FlockPanel.class)
public class FlockAction extends AbstractPlayerAction<FlockOption> {

	public FlockAction() {
		 super(HashSet.ofAll(Arrays.asList(FlockOption.values())));
	}

	@Override
	public WsInGameMessage select(FlockOption option) {
		return new FlockMessage(option);
	}

	@Override
    public String toString() {
        return "EXPAND or SCORE";
    }
}
