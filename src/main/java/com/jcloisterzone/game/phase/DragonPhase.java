package com.jcloisterzone.game.phase;

import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.game.capability.DragonCapability;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.random.RandomGenerator;

public class DragonPhase extends Phase {

    private DragonMovePhase dragonMovePhase;

    public DragonPhase(RandomGenerator random, Phase defaultNext) {
        super(random, defaultNext);
        dragonMovePhase = new DragonMovePhase(random, defaultNext);
    }

    @Override
    public StepResult enter(GameState state) {
        Tile tile = state.getLastPlaced().getTile();
        if (tile.hasModifier(DragonCapability.DRAGON_TRIGGER)) {
            Position pos = state.getNeutralFigures().getDragonDeployment();
            if (pos != null) {
                return next(state, dragonMovePhase);
            }
        }
        return next(state);
    }
}
