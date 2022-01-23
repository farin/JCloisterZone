package com.jcloisterzone.io.message;

import com.jcloisterzone.io.MessageCommand;

@MessageCommand("COMMIT")
public class CommitMessage extends AbstractMessage implements ReplayableMessage, RandomChangingMessage {

    private Double random;

    public CommitMessage() {
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
