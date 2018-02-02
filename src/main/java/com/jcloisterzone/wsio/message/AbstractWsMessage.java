package com.jcloisterzone.wsio.message;

public abstract class AbstractWsMessage implements WsMessage {

    private Long sequenceNumber;

    @Override
    public Long getSequenceNumber() {
        // TODO Auto-generated method stub
        return sequenceNumber;
    }

    @Override
    public void setSequnceNumber(Long sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

}
