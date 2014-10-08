package com.jcloisterzone.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.jcloisterzone.EventBusExceptionHandler;
import com.jcloisterzone.EventBusProxy;
import com.jcloisterzone.wsio.Connection;
import com.jcloisterzone.wsio.server.RemoteClient;

public class AbstractController {

	protected final transient Logger logger = LoggerFactory.getLogger(getClass());

	protected final Client client;
    private final EventBus eventBus;

    private InvokeInSwingUiAdapter invokeInSwingUiAdapter;

    private RemoteClient[] remoteClients;

    public AbstractController(Client client, EventBusProxy eventBusProxy) {
    	this.client = client;

    	eventBus = new EventBus(new EventBusExceptionHandler(getClass().getName() + " event bus"));
        eventBus.register(this);
        invokeInSwingUiAdapter = new InvokeInSwingUiAdapter(eventBus);
        eventBusProxy.getEventBus().register(invokeInSwingUiAdapter);
    }

    public void register(Object subscriber) {
        eventBus.register(subscriber);
    }

    public Client getClient() {
		return client;
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
