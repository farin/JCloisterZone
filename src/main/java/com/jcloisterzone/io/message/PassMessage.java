package com.jcloisterzone.io.message;

import com.jcloisterzone.io.MessageCommand;

@MessageCommand("PASS")
public class PassMessage extends AbstractMessage implements ReplayableMessage {

    public PassMessage() { }
}