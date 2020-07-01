package com.jcloisterzone.action;

import com.jcloisterzone.wsio.message.FlockMessage;
import com.jcloisterzone.wsio.message.FlockMessage.FlockOption;
import com.jcloisterzone.wsio.message.WsInGameMessage;
import io.vavr.collection.HashSet;

import java.util.Arrays;

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
