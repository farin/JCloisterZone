package com.jcloisterzone.game.phase;

import com.jcloisterzone.PointCategory;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.capability.FairyCapability;


public class FairyPhase extends Phase {

    private final FairyCapability fairyCap;

    public FairyPhase(Game game) {
        super(game);
        fairyCap = game.getCapability(FairyCapability.class);
    }

    @Override
    public boolean isActive() {
        return game.hasCapability(FairyCapability.class);
    }

    @Override
    public void enter() {
        Position fairyPos = fairyCap.getFairyPosition();
        if (fairyPos != null) {
            for (Meeple m : game.getDeployedMeeples()) {
                if (m.at(fairyPos) && m.getPlayer() == getActivePlayer()) {
                    m.getPlayer().addPoints(1, PointCategory.FAIRY);
                    game.fireGameEvent().scored(m.getPosition(), m.getPlayer(), 1, "1", false);
                    break;
                }
            }
        }
        next();
    }


}
