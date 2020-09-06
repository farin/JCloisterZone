package com.jcloisterzone.io.message;

import com.jcloisterzone.io.MessageCommand;

@MessageCommand("FLOCK_EXPAND_OR_SCORE")
public class FlockMessage extends AbstractMessage implements ReplayableMessage, SaltMessage {

	public enum FlockOption { EXPAND, SCORE }

    private FlockOption value;

    private String salt;

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
    public String getSalt() {
        return salt;
    }

    @Override
    public void setSalt(String salt) {
        this.salt = salt;
    }


}
