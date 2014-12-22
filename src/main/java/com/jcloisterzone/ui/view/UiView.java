package com.jcloisterzone.ui.view;

import java.awt.Container;

public interface UiView {

	void show(Container pane);
	void hide();

	//TODO vetoable hide - replace current Client.closeGame

}
