package com.jcloisterzone.action;

import java.util.List;

import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.figure.Meeple;

public abstract class SelectFollowerAction extends SelectFeatureAction {

    public SelectFollowerAction(String name) {
        super(name);
    }

    protected Class<? extends Meeple> getMeepleType(Position pos, Location loc) {
        List<Meeple> meeples = client.getGame().getBoard().get(pos).getFeature(loc).getMeeples();
        if (meeples.isEmpty()) return null;
        if (meeples.size() == 1) return meeples.get(0).getClass();

        //TODO meeple type dialog
        return meeples.get(0).getClass();
    }

}
