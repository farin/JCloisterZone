package com.jcloisterzone.game.phase;

import com.jcloisterzone.board.Position;
import com.jcloisterzone.feature.Circus;
import com.jcloisterzone.figure.neutral.BigTop;
import com.jcloisterzone.game.capability.AnimalToken;
import com.jcloisterzone.game.capability.BigTopCapability;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.PlacedTile;
import com.jcloisterzone.random.RandomGenerator;
import com.jcloisterzone.reducers.MoveNeutralFigure;

import java.util.ArrayList;

public class BigTopPhase extends Phase {

    public BigTopPhase(RandomGenerator random, Phase defaultNext) {
        super(random, defaultNext);
    }

    @Override
    public StepResult enter(GameState state) {
        PlacedTile pt = state.getLastPlaced();
        boolean hasCircus = pt.getTile().getInitialFeatures().filterValues(f -> f instanceof Circus).length() > 0;
        if (hasCircus) {
            state = moveBigTop(state, pt.getPosition());
        }
		return next(state);
    }

    public GameState moveBigTop(GameState state, Position pos) {
        BigTopCapability capability = state.getCapabilities().get(BigTopCapability.class);
        BigTop bigtop = state.getNeutralFigures().getBigTop();

        Position currentPos = bigtop.getPosition(state);
        if (currentPos != null) {
            ArrayList<AnimalToken> unusedTokens = capability.getUnusedTokens(state);
            AnimalToken token = unusedTokens.get(getRandom().getNextInt(unusedTokens.size()));
            state = capability.scoreBigTop(state, currentPos, token, false);
        }

        state = new MoveNeutralFigure(bigtop, pos).apply(state);
        return state;
    }
}
