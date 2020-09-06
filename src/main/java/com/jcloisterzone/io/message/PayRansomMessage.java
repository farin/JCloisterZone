package com.jcloisterzone.io.message;

import com.jcloisterzone.io.MessageCommand;

@MessageCommand("PAY_RANSOM")
public class PayRansomMessage extends AbstractMessage implements ReplayableMessage {

    private String meepleId;

    public PayRansomMessage() {
    }

    public PayRansomMessage(String meepleId) {
        super();
        this.meepleId = meepleId;
    }

    public String getMeepleId() {
        return meepleId;
    }

    public void setMeepleId(String meepleId) {
        this.meepleId = meepleId;
    }
}