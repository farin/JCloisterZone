package com.jcloisterzone.action;

import java.util.List;

import com.jcloisterzone.Player;
import com.jcloisterzone.PlayerRestriction;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.rmi.Client2ClientIF;

public abstract class SelectFollowerAction extends SelectFeatureAction {

    /** set null if anybody is allowed */
    private final PlayerRestriction players;

    public SelectFollowerAction(String name, PlayerRestriction players) {
        super(name);
        this.players = players;
    }

    public PlayerRestriction getPlayers() {
        return players;
    }

    @Override
    public final void perform(Client2ClientIF server, Position pos, Location loc) {
        List<Meeple> meeples = client.getGame().getBoard().get(pos).getFeature(loc).getMeeples();
        for (Meeple m : meeples) {
            if (players.isAllowed(m.getPlayer())) {
                perform(server, pos, loc, m.getClass(), m.getPlayer());
                return;
            }
        }
        throw new IllegalStateException("No legal meeple is placed on feature.");
    }

    public abstract void perform(Client2ClientIF server, Position pos, Location loc, Class<? extends Meeple> meepleType, Player owner);

}
