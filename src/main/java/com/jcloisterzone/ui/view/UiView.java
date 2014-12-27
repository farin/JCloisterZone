package com.jcloisterzone.ui.view;

import java.awt.Container;
import java.awt.event.KeyEvent;

public interface UiView {

	void show(Container pane, Object ctx);
	boolean requestHide(Object ctx);
	void hide();

	boolean dispatchKeyEvent(KeyEvent e);

	void onWebsocketError(Exception ex);
}
