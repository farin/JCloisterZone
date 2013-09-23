package com.jcloisterzone.game.phase;

import com.jcloisterzone.PointCategory;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.capability.FairyCapability;


public class FairyPhase extends Phase {

    public FairyPhase(Game game) {
        super(game);
    }

    @Override
    public boolean isActive() {
        return game.hasCapability(Capability.FAIRY);
    }

    @Override
    public void enter() {
        FairyCapability fairyCap = game.getFairyCapability();
        Position fairyPos = fairyCap.getFairyPosition();
        if (fairyPos != null) {
            for(Meeple m : game.getDeployedMeeples()) {
                if (m.getPosition().equals(fairyPos) && m.getPlayer() == getActivePlayer()) {
                    m.getPlayer().addPoints(1, PointCategory.FAIRY);
                    game.fireGameEvent().scored(m.getPosition(), m.getPlayer(), 1, "1", false);
                    break;
                }
            }
        }
        next();
    }


}
