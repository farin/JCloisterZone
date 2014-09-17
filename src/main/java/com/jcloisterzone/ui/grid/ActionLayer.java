package com.jcloisterzone.ui.grid;

import com.jcloisterzone.action.PlayerAction;

public interface ActionLayer<T extends PlayerAction<?>> extends GridLayer {

    void setAction(boolean active, T action);
    T getAction();
}
