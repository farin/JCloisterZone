package com.jcloisterzone.wsio.message;

import com.jcloisterzone.wsio.WsMessageCommand;

public abstract class AbstractWsMessage implements WsMessage {

    private Long sequenceNumber;

    @Override
    public Long getSequenceNumber() {
        return sequenceNumber;
    }

    @Override
    public void setSequnceNumber(Long sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    @Override
    public String toString() {
    	try {
    		return getClass().getAnnotationsByType(WsMessageCommand.class)[0].value();
    	} catch (Exception e){
    		return getClass().getSimpleName();
    	}
    }

}
