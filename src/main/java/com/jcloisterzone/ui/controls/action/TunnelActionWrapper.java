package com.jcloisterzone.ui.controls.action;

import java.awt.Image;

import com.jcloisterzone.Player;
import com.jcloisterzone.action.TunnelAction;
import com.jcloisterzone.game.Token;
import com.jcloisterzone.ui.resources.ResourceManager;

public class TunnelActionWrapper extends ActionWrapper {

    public TunnelActionWrapper(TunnelAction action) {
        super(action);
    }

    @Override
    public TunnelAction getAction() {
        return (TunnelAction) super.getAction();
    }

    @Override
    public Image getImage(ResourceManager rm, Player player, boolean active) {
        Token token = getAction().getToken();
        return getImage(rm, active ? player.getColors().getTunnelColors().get(token) : INACTIVE_COLOR);
    }

}
