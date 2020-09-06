package com.jcloisterzone.io.message;

import com.jcloisterzone.io.MessageCommand;

public abstract class AbstractMessage implements Message {

    @Override
    public String toString() {
    	try {
    		return getClass().getAnnotationsByType(MessageCommand.class)[0].value();
    	} catch (Exception e){
    		return getClass().getSimpleName();
    	}
    }

}
