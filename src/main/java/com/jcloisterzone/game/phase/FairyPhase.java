package com.jcloisterzone.game.phase;

import com.jcloisterzone.PointCategory;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.ScoreEvent;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.CustomRule;
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
        FeaturePointer fp = fairyCap.getFairy().getFeaturePointer();
        if (fp != null && !getTilePack().isEmpty()) { //do not add 1 point in last additional abbey only round
            boolean onTileRule = game.getBooleanValue(CustomRule.FAIRY_ON_TILE);
            for (Meeple m : game.getDeployedMeeples()) {
                if (m.getPlayer() == getActivePlayer()) {
                    boolean match = onTileRule ?
                            m.at(fp.getPosition()) :
                            m.at(fp) && m == fairyCap.getFairy().getNextTo();
                    if (match) {
                        //always draw in center to now draw over meeples
                        m.getPlayer().addPoints(1, PointCategory.FAIRY);
                        game.post(new ScoreEvent(m.getPosition(), m.getPlayer(), 1, PointCategory.FAIRY));
                        break;
                    }
                }
            }
        }
        next();
    }
}
