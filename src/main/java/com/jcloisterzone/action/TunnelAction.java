package com.jcloisterzone.action;

import java.awt.Image;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.rmi.Client2ClientIF;

public class TunnelAction extends SelectFeatureAction {

    private final boolean secondTunnelPiece;

    public TunnelAction(boolean secondTunnelPiece) {
        super("tunnel");
        this.secondTunnelPiece = secondTunnelPiece;
    }

    @Override
    public Image getImage(Player player, boolean active) {
        if (active && isSecondTunnelPiece()) {        	
            return getImage(client.getPlayerSecondTunelColor(player));
        } else {
            return super.getImage(player, active);
        }
    }

    public boolean isSecondTunnelPiece() {
        return secondTunnelPiece;
    }

    @Override
    public void perform(Client2ClientIF server, FeaturePointer bp) {
        server.placeTunnelPiece(bp.getPosition(), bp.getLocation(), secondTunnelPiece);

    }

    @Override
    protected int getSortOrder() {
        return secondTunnelPiece ? 41 : 40;
    }

}
