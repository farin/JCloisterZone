package com.jcloisterzone.ui.view;

import java.awt.Container;
import java.awt.event.KeyEvent;

public interface UiView {

	void show(Container pane, Object ctx);
	boolean requestHide(UiView nextView, Object nextCtx);
	void hide(UiView nextView, Object nextCtx);

	boolean dispatchKeyEvent(KeyEvent e);

	void onWebsocketError(Exception ex);
	void onWebsocketClose(int code, String reason, boolean remote);
}
