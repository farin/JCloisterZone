package com.jcloisterzone.game.phase;

import java.util.List;

import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.TileTrigger;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.capability.PlagueCapability;
import com.jcloisterzone.game.capability.PlagueCapability.PlagueSource;

public class PlaguePhase extends Phase {

    private final PlagueCapability plagueCap;

    public PlaguePhase(Game game) {
        super(game);
        plagueCap = game.getCapability(PlagueCapability.class);
    }

    @Override
    public boolean isActive() {
        return game.hasCapability(PlagueCapability.class);
    }

    @Override
    public void enter() {
        if (getTile().hasTrigger(TileTrigger.PLAGUE)) {
            PlagueSource source = new PlagueSource(getTile().getPosition());
            plagueCap.getPlagueSources().add(source);
        } else {
            List<Position> sources = plagueCap.getActiveSources();
            if (!sources.isEmpty()) {
                //TODO spread flea
            }
        }
        next();
    }

}
