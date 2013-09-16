package com.jcloisterzone.game.phase;

import com.jcloisterzone.Expansion;
import com.jcloisterzone.PointCategory;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.expansion.PrincessAndDragonGame;


public class FairyPhase extends Phase {

    public FairyPhase(Game game) {
        super(game);
    }

    @Override
    public boolean isActive() {
        return game.hasExpansion(Expansion.PRINCESS_AND_DRAGON) && game.hasCapability(Capability.FAIRY);
    }

    @Override
    public void enter() {
        PrincessAndDragonGame pd = game.getPrincessAndDragonGame();
        Position fairyPos = pd.getFairyPosition();
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
