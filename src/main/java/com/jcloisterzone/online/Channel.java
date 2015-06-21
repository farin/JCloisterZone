package com.jcloisterzone.online;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.jcloisterzone.EventBusExceptionHandler;
import com.jcloisterzone.EventProxy;
import com.jcloisterzone.event.Event;

public class Channel implements EventProxy {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private String name;

    private final EventBus eventBus;

    public Channel(String name) {
        this.name = name;
        eventBus = new EventBus(new EventBusExceptionHandler(name + " channel event bus"));
    }

    public void post(Event event) {
    	eventBus.post(event);
    }

    @Override
	public EventBus getEventBus() {
        return eventBus;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    @Override
    public String toString() {
        return name;
    }
}
