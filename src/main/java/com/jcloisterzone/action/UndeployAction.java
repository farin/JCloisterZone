package com.jcloisterzone.action;

import com.jcloisterzone.Player;
import com.jcloisterzone.PlayerRestriction;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.rmi.Client2ClientIF;

public class UndeployAction extends SelectFollowerAction {

    public UndeployAction(String name, PlayerRestriction players) {
        super(name, players);
    }

    @Override
    public void perform(Client2ClientIF server, Position pos, Location loc, Class<? extends Meeple> meepleType, Player owner) {
        server.undeployMeeple(pos, loc, meepleType, owner.getIndex());
    }

}
