package com.jcloisterzone.ui.grid;

import com.jcloisterzone.action.PlayerAction;

public interface ActionLayer<T extends PlayerAction<?>> extends GridLayer {

    void setAction(T action);
    T getAction();
}
