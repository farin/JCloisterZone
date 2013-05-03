package com.jcloisterzone.action;

import java.util.Set;

import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.rmi.Client2ClientIF;

public abstract class SelectFollowerAction extends SelectFeatureAction {

    public SelectFollowerAction(String name) {
        super(name);
    }

    @Override
    public final void perform(Client2ClientIF server, Position pos, Location loc) {
        perform(server, pos, loc, getMeepleType(pos, loc));
    }

    public abstract void perform(Client2ClientIF server, Position pos, Location loc, Class<? extends Meeple> meepleType);

    protected Class<? extends Meeple> getMeepleType(Position pos, Location loc) {
        Set<Class<? extends Meeple>> meeples = client.getGame().getBoard().get(pos).getFeature(loc).getMeepleTypes();
        if (meeples.isEmpty()) return null;
        return meeples.iterator().next();
    }

}
