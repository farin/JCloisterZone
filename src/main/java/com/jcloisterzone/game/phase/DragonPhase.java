package com.jcloisterzone.game.phase;

import java.util.Random;

import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.TileDefinition;
import com.jcloisterzone.board.TileTrigger;
import com.jcloisterzone.config.Config;
import com.jcloisterzone.game.capability.DragonCapability;
import com.jcloisterzone.game.state.GameState;

@RequiredCapability(DragonCapability.class)
public class DragonPhase extends Phase {

    public DragonPhase(Config config, Random random) {
        super(config, random);
    }

    @Override
    public StepResult enter(GameState state) {
        TileDefinition tile = state.getLastPlaced().getTile();
        if (tile.getTrigger() == TileTrigger.DRAGON) {
            Position pos = state.getNeutralFigures().getDragonDeployment();
            if (pos != null) {
                return next(state, DragonMovePhase.class);
            }
        }
        return next(state);
    }
}
