package com.jcloisterzone.io.message;

import com.jcloisterzone.io.MessageCommand;

@MessageCommand("FLOCK_EXPAND_OR_SCORE")
public class FlockMessage extends AbstractMessage implements ReplayableMessage, RandomChangingMessage {

	public enum FlockOption { EXPAND, SCORE }

    private FlockOption value;

    private Double random;

    public FlockMessage() {
	}

    public FlockMessage(FlockOption value) {
		this.value = value;
	}

	public FlockOption getValue() {
		return value;
	}

	public void setValue(FlockOption value) {
		this.value = value;
	}

    @Override
    public Double getRandom() {
        return random;
    }

    @Override
    public void setRandom(Double random) {
        this.random = random;
    }
}
