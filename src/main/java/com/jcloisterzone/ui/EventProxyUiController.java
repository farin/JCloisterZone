package com.jcloisterzone.ui;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.jcloisterzone.EventBusExceptionHandler;
import com.jcloisterzone.EventProxy;
import com.jcloisterzone.config.Config;
import com.jcloisterzone.event.Event;
import com.jcloisterzone.wsio.Connection;
import com.jcloisterzone.wsio.server.RemoteClient;

public class EventProxyUiController<T extends EventProxy> {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    protected final Client client;
    private final EventBus eventBus;
    private final T eventProxy;
    private Connection connection;

    private InvokeInSwingUiAdapter invokeInSwingUiAdapter;

    private final List<RemoteClient> remoteClients = new ArrayList<RemoteClient>();

    public EventProxyUiController(Client client, T eventProxy) {
        this.client = client;
        this.eventProxy = eventProxy;

        eventBus = new EventBus(new EventBusExceptionHandler(getClass().getName() + " event bus"));
        eventBus.register(this);
        invokeInSwingUiAdapter = new InvokeInSwingUiAdapter(eventBus);
        eventProxy.getEventBus().register(invokeInSwingUiAdapter);
    }

    public void register(Object subscriber) {
        logger.debug(String.format("Registering %s to UI event bus", subscriber));
        eventBus.register(subscriber);
    }

    public void unregister(Object subscriber) {
        try {
            logger.debug(String.format("Unregistering %s to UI event bus", subscriber));
            eventBus.unregister(subscriber);
        } catch (IllegalArgumentException ex) {
            logger.warn("Subscriber not registered.");
        }
    }

    public Client getClient() {
        return client;
    }

    public Config getConfig() {
        return client == null ? null : client.getConfig();
    }

    public T getEventProxy() {
        return eventProxy;
    }

    public List<RemoteClient> getRemoteClients() {
        return remoteClients;
    }

    protected InvokeInSwingUiAdapter getInvokeInSwingUiAdapter() {
        return invokeInSwingUiAdapter;
    }

    public Connection getConnection() {
        //TODO fix this hack
        if (this.connection == null) {
            return client.getConnection();
        }
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }
}
