package com.jcloisterzone.game.phase;

import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.TileDefinition;
import com.jcloisterzone.board.TileTrigger;
import com.jcloisterzone.game.capability.DragonCapability;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.ui.GameController;

@RequiredCapability(DragonCapability.class)
public class DragonPhase extends Phase {

    public DragonPhase(GameController gc) {
        super(gc);
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
