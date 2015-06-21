package com.jcloisterzone.game.phase;

import com.jcloisterzone.PointCategory;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.event.ScoreEvent;
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
        Position fairyPos = fairyCap.getFairy().getPosition();
        if (fairyPos != null && !getTilePack().isEmpty())  { //do not add 1 point in last additional abbey only round
            for (Meeple m : game.getDeployedMeeples()) {
                if (m.at(fairyPos) && m.getPlayer() == getActivePlayer()) {
                    m.getPlayer().addPoints(1, PointCategory.FAIRY);
                    game.post(new ScoreEvent(m.getPosition(), m.getPlayer(), 1, PointCategory.FAIRY));
                    break;
                }
            }
        }
        next();
    }


}
