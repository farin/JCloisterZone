package com.jcloisterzone.action;

import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.rmi.Client2ClientIF;

public class FestivalUndeployAction extends SelectFeatureAction {

    @Override
    public void perform(Client2ClientIF server, Position p, Location d) {
        server.undeployMeeple(p, d);
    }

    @Override
    public String getName() {
        return "festival";
    }
}
