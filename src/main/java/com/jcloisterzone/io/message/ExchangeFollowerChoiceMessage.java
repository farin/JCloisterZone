package com.jcloisterzone.io.message;

import com.jcloisterzone.io.MessageCommand;

@MessageCommand("EXCHANGE_FOLLOWER")
public class ExchangeFollowerChoiceMessage extends AbstractMessage implements ReplayableMessage {

    private String meepleId;

    public ExchangeFollowerChoiceMessage() {
    }

    public ExchangeFollowerChoiceMessage(String meepleId) {
        this.meepleId = meepleId;
    }


    public String getMeepleId() {
        return meepleId;
    }

    public void setMeepleId(String meepleId) {
        this.meepleId = meepleId;
    }
}
