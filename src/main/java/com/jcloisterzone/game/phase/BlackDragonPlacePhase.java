package com.jcloisterzone.game.phase;

import com.jcloisterzone.Player;
import com.jcloisterzone.game.capability.BlackDragonCapability;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.random.RandomGenerator;
import io.vavr.collection.Array;

public class BlackDragonPlacePhase extends Phase {

    public BlackDragonPlacePhase(RandomGenerator random, Phase defaultNext) {
        super(random, defaultNext);
    }

    @Override
    public StepResult enter(GameState state) {
        Array<Integer> scoreOnStart = state.getCapabilityModel(BlackDragonCapability.class)._3;
        BlackDragonCapability blackdragonCap = state.getCapabilities().get(BlackDragonCapability.class);

        for(Player player : state.getPlayers().getPlayers() ) {
        	Integer scoreBefore = scoreOnStart.get(player.getIndex());
        	Integer scoreCurrent = state.getPlayers().getScore().get(player.getIndex());
        	Integer diff = scoreCurrent - scoreBefore;
        	if (diff>0 && ((scoreBefore % 50) + diff > 50)) {
        		state = blackdragonCap.moveBlackDragon(state, state.getLastPlaced().getPosition());
        		break;
        	}
        }

    	return next(state);
    }
}
