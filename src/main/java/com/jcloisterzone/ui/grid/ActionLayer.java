package com.jcloisterzone.ui.grid;

import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.ui.controls.action.ActionWrapper;

public interface ActionLayer extends GridLayer {

    void setActionWrapper(boolean active, ActionWrapper actionWrapper);
    ActionWrapper getActionWrapper();

    default PlayerAction<?> getAction() {
        return getActionWrapper() == null ? null : getActionWrapper().getAction();
    }
}
