package com.jcloisterzone.action;

import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.rmi.Client2ClientIF;

/** Escape from The Cathars expansion */
public class EscapeAction extends SelectFeatureAction {

    @Override
    public void perform(Client2ClientIF server, Position p, Location loc) {
        server.undeployMeeple(p, loc);
    }

}
