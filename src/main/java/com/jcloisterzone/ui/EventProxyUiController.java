package com.jcloisterzone.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.jcloisterzone.EventBusExceptionHandler;
import com.jcloisterzone.EventProxy;
import com.jcloisterzone.config.Config;
import com.jcloisterzone.wsio.Connection;
import com.jcloisterzone.wsio.server.RemoteClient;

public class EventProxyUiController<T extends EventProxy> {

	protected final transient Logger logger = LoggerFactory.getLogger(getClass());

	protected final Client client;
    private final EventBus eventBus;
    private final T eventProxy;

    private InvokeInSwingUiAdapter invokeInSwingUiAdapter;

    private RemoteClient[] remoteClients;

    public EventProxyUiController(Client client, T eventProxy) {
    	this.client = client;
    	this.eventProxy = eventProxy;

    	eventBus = new EventBus(new EventBusExceptionHandler(getClass().getName() + " event bus"));
        eventBus.register(this);
        invokeInSwingUiAdapter = new InvokeInSwingUiAdapter(eventBus);
        eventProxy.getEventBus().register(invokeInSwingUiAdapter);
    }

    public void register(Object subscriber) {
        eventBus.register(subscriber);
    }

    public void unregister(Object subscriber) {
    	eventBus.unregister(subscriber);
    }

    public Client getClient() {
		return client;
	}

    public Config getConfig() {
    	return client.getConfig();
    }

    public T getEventProxy() {
		return eventProxy;
	}

    public Connection getConnection() {
        return client.getConnection();
    }

    public RemoteClient[] getRemoteClients() {
		return remoteClients;
	}

	public void setRemoteClients(RemoteClient[] remoteClients) {
		this.remoteClients = remoteClients;
	}

	protected InvokeInSwingUiAdapter getInvokeInSwingUiAdapter() {
		return invokeInSwingUiAdapter;
	}

}
