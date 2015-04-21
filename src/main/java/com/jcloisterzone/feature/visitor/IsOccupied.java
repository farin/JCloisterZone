package com.jcloisterzone.feature.visitor;

import java.util.List;

import com.jcloisterzone.Player;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.figure.Meeple;

public class IsOccupied implements FeatureVisitor<Boolean> {

    private Player player;
    private Class<? extends Meeple> clazz;

    private boolean isOccupied = false;

    public IsOccupied with(Player player) {
        this.player = player;
        return this;
    }

    public IsOccupied with(Class<? extends Meeple> clazz) {
        this.clazz = clazz;
        return this;
    }

    @Override
    public boolean visit(Feature feature) {
        List<Meeple> meeples = feature.getMeeples();
        for (Meeple m : meeples) {
            if (player != null && m.getPlayer() != player) continue;
            if (clazz != null && !clazz.isInstance(m)) continue;
            isOccupied = true;
            return false;
        }
        return true;
    }

    @Override
    public Boolean getResult() {
        return isOccupied;
    }
}
