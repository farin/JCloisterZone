package com.jcloisterzone.ui;

public interface UIEventListener {

    default void registerTo(EventProxyUiController<?> gc) {
        gc.register(this);
    }

    default void unregisterFrom(EventProxyUiController<?> gc) {
        gc.unregister(this);
    }

}
