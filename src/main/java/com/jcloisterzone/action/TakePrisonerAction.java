package com.jcloisterzone.action;

import com.jcloisterzone.Player;
import com.jcloisterzone.PlayerRestriction;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.rmi.Client2ClientIF;

public class TakePrisonerAction extends SelectFollowerAction {

    public TakePrisonerAction(PlayerRestriction players) {
        super("takeprisoner", players);
    }

    @Override
    public void perform(Client2ClientIF server, Position pos, Location loc, Class<? extends Meeple> meepleType, Player owner) {
        server.takePrisoner(pos, loc, meepleType, owner.getIndex());
    }

}
