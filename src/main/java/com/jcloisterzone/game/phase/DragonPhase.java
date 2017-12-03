package com.jcloisterzone.game.phase;

import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.TileTrigger;
import com.jcloisterzone.game.RandomGenerator;
import com.jcloisterzone.game.capability.DragonCapability;
import com.jcloisterzone.game.state.GameState;

@RequiredCapability(DragonCapability.class)
public class DragonPhase extends Phase {

    public DragonPhase(RandomGenerator random) {
        super(random);
    }

    @Override
    public StepResult enter(GameState state) {
        Tile tile = state.getLastPlaced().getTile();
        if (tile.getTrigger() == TileTrigger.DRAGON) {
            Position pos = state.getNeutralFigures().getDragonDeployment();
            if (pos != null) {
                return next(state, DragonMovePhase.class);
            }
        }
        return next(state);
    }
}
