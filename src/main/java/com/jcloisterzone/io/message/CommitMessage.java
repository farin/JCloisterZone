package com.jcloisterzone.io.message;

import com.jcloisterzone.io.MessageCommand;

@MessageCommand("COMMIT")
public class CommitMessage extends AbstractMessage implements ReplayableMessage, SaltMessage {

    private String salt;


    public CommitMessage() {
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
