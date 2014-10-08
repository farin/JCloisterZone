package com.jcloisterzone.online;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.jcloisterzone.EventBusExceptionHandler;
import com.jcloisterzone.event.Event;
import com.jcloisterzone.wsio.server.RemoteClient;

public class Channel {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private String name;
    private RemoteClient[] remoteClients;

    private final EventBus eventBus;

    public Channel(String name) {
        this.name = name;
        eventBus = new EventBus(new EventBusExceptionHandler(name + " channel event bus"));
    }

    public void post(Event event) {
    	eventBus.post(event);
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public RemoteClient[] getRemoteClients() {
        return remoteClients;
    }

    public void setRemoteClients(RemoteClient[] remoteClients) {
        this.remoteClients = remoteClients;
    }

    @Override
    public String toString() {
        return name;
    }
}
