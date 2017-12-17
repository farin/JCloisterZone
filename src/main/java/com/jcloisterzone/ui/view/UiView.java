package com.jcloisterzone.ui.view;

import java.awt.Container;
import java.awt.event.KeyEvent;

public interface UiView {

    void show(Container pane);
    boolean requestHide(UiView nextView);
    void hide(UiView nextView);

    boolean dispatchKeyEvent(KeyEvent e);

    void onWebsocketError(Exception ex);
    void onWebsocketClose(int code, String reason, boolean remote);
}
