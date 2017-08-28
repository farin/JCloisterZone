package com.jcloisterzone.ui.controls.action;

import java.awt.Image;

import com.jcloisterzone.Player;
import com.jcloisterzone.action.TunnelAction;
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
        if (active && getAction().isSecondTunnelPiece()) {
            return getImage(rm, player.getColors().getTunnelBColor());
        } else {
            return super.getImage(rm, player, active);
        }
    }

}
