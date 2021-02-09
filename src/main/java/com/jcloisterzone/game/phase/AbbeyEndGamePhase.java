package com.jcloisterzone.game.phase;

import com.jcloisterzone.Player;
import com.jcloisterzone.action.TilePlacementAction;
import com.jcloisterzone.game.RandomGenerator;
import com.jcloisterzone.game.capability.AbbeyCapability;
import com.jcloisterzone.game.state.ActionsState;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.io.message.PassMessage;
import com.jcloisterzone.io.message.PlaceTileMessage;
import com.jcloisterzone.reducers.SetNextPlayer;

@RequiredCapability(AbbeyCapability.class)
public class AbbeyEndGamePhase extends AbstractAbbeyPhase {

    private ActionPhase actionPhase;

    public AbbeyEndGamePhase(RandomGenerator random, Phase defaultNext) {
        super(random, defaultNext);
    }

    public void setActionPhase(ActionPhase actionPhase) {
        this.actionPhase = actionPhase;
    }

    @Override
    public StepResult enter(GameState state) {
        Integer endPlayerIdx = state.getCapabilityModel(AbbeyCapability.class);
        Player player = state.getTurnPlayer();

        if (endPlayerIdx == null) {
            endPlayerIdx = player.getIndex();
            state = state.setCapabilityModel(AbbeyCapability.class, endPlayerIdx);
        }

        return findNextAbbeyAction(state);
    }

    private StepResult findNextAbbeyAction(GameState state) {
        Integer endPlayerIdx = state.getCapabilityModel(AbbeyCapability.class);
        Player player = state.getTurnPlayer();
        player = player.getNextPlayer(state);

        while (true) {
            boolean hasAbbey = state.getPlayers().getPlayerTokenCount(player.getIndex(), AbbeyCapability.AbbeyToken.ABBEY_TILE) > 0;
            if (hasAbbey) {
                TilePlacementAction action = createAbbeyAction(state);
                if (action != null) {
                    state = (new SetNextPlayer(player)).apply(state);
                    state = state.setPlayerActions(new ActionsState(player, action, true));
                    return promote(state);
                }
            }

            if (endPlayerIdx == player.getIndex()) {
                break;
            }
            player = player.getNextPlayer(state);
        }

        return next(state);
    }

    @PhaseMessageHandler
    public StepResult handlePass(GameState state, PassMessage msg) {
        state = clearActions(state);
        return findNextAbbeyAction(state);
    }

    @PhaseMessageHandler
    public StepResult handlePlaceTile(GameState state, PlaceTileMessage msg) {
        state = applyPlaceTile(state, msg);
        return next(state, actionPhase);
    }
}
