package com.jcloisterzone.feature.visitor;

import com.jcloisterzone.Player;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.figure.Builder;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.figure.Pig;
import com.jcloisterzone.figure.Special;

public class RemoveLonelyBuilderAndPig implements FeatureVisitor<Special> {

    Player player;
    Special toRemove = null;

    public RemoveLonelyBuilderAndPig(Player player) {
        this.player = player;
    }

    @Override
    public boolean visit(Feature feature) {
        for (Meeple m : feature.getMeeples()) {
            if (m.getPlayer() != player) continue;
            if (m instanceof Builder || m instanceof Pig) {
                toRemove = (Special) m;
                continue;
            }
            if (m instanceof Follower) {
                //another follower exists
                toRemove = null;
                return false; //can stop immediately
            }
        }
        return true;
    }

    @Override
    public Special getResult() {
        return toRemove;
    }


}