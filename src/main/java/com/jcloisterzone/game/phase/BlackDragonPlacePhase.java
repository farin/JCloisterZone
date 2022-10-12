package com.jcloisterzone.game.phase;

import com.jcloisterzone.Player;
import com.jcloisterzone.event.ScoreEvent;
import com.jcloisterzone.game.capability.BlackDragonCapability;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.random.RandomGenerator;
import io.vavr.Predicates;
import io.vavr.collection.Stream;

import java.util.HashMap;

public class BlackDragonPlacePhase extends Phase {

    public BlackDragonPlacePhase(RandomGenerator random, Phase defaultNext) {
        super(random, defaultNext);
    }

    @Override
    public StepResult enter(GameState state) {
        BlackDragonCapability cap = state.getCapabilities().get(BlackDragonCapability.class);

        java.util.Map<Player, Integer> receivedScore = new HashMap<>();
        for (Player player : state.getPlayers().getPlayers() ) {
            receivedScore.put(player, 0);
        }

        Stream.ofAll(state.getCurrentTurnPartEvents())
                .filter(Predicates.instanceOf(ScoreEvent.class))
                .map(ev -> (ScoreEvent) ev)
                .forEach(ev -> {
                    for (ScoreEvent.ReceivedPoints rp : ev.getPoints()) {
                        receivedScore.put(rp.getPlayer(), receivedScore.get(rp.getPlayer()) + rp.getPoints());
                    }
                });

        for (Player player : state.getPlayers().getPlayers() ) {
            int playerReceivedPoints = receivedScore.get(player);
            if (playerReceivedPoints > 0) {
                int scoreCurrent = state.getPlayers().getScore().get(player.getIndex());
                if (scoreCurrent / 50 > (scoreCurrent - playerReceivedPoints) / 50) {
                    state = cap.moveBlackDragon(state, state.getLastPlaced().getPosition());
                    break;
                }
            }
        }

    	return next(state);
    }
}
