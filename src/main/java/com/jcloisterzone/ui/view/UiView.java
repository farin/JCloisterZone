package com.jcloisterzone.ui.view;

import java.awt.Container;
import java.awt.event.KeyEvent;

public interface UiView {

	void show(Container pane);
	void hide();
	//TODO vetoable hide - replace current Client.closeGame

	boolean dispatchKeyEvent(KeyEvent e);
}
