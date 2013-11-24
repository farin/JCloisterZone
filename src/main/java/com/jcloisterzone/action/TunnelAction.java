package com.jcloisterzone.action;

import java.awt.Image;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.collection.LocationsMap;
import com.jcloisterzone.rmi.Client2ClientIF;

public class TunnelAction extends SelectFeatureAction {

    private final boolean secondTunnelPiece;

    public TunnelAction(boolean secondTunnelPiece, LocationsMap sites) {
        super("tunnel", sites);
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
    public void perform(Client2ClientIF server, Position p, Location d) {
        server.placeTunnelPiece(p, d, secondTunnelPiece);

    }

    @Override
    protected int getSortOrder() {
        return secondTunnelPiece ? 41 : 40;
    }

}
