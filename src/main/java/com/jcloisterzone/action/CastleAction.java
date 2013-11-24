package com.jcloisterzone.action;

import java.util.Set;

import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.rmi.Client2ClientIF;

public class CastleAction extends SelectFeatureAction {

    public CastleAction( Position position, Set<Location> sites) {
        super("castle", position, sites);
    }

    public void perform(Client2ClientIF server, Position pos, Location loc) {
        server.deployCastle(pos, loc);
    }

}
