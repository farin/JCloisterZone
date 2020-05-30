package com.jcloisterzone.wsio.message;

import com.jcloisterzone.wsio.WsMessageCommand;

public abstract class AbstractWsMessage implements WsMessage {

    private String messageId;

    @Override
    public String getMessageId() {
        return messageId;
    }

    @Override
    public void setMessageId(String messageId) {
        this.messageId = messageId;
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
