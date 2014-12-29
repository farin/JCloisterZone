package com.jcloisterzone.ui.view;

import java.awt.event.KeyEvent;

import com.jcloisterzone.ui.Client;

public abstract class AbstractUiView implements UiView {

	protected final Client client;

	public AbstractUiView(Client client) {
		this.client = client;
	}

	@Override
	public boolean requestHide(Object ctx) {
		return true;
	}

	@Override
	public void hide() {
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent e) {
		return false;
	}

	@Override
	public void onWebsocketError(Exception ex) {
		client.onUnhandledWebsocketError(ex);
	}

	public Client getClient() {
		return client;
	}

}
