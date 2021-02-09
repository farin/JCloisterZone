package com.jcloisterzone.game.phase;

import com.jcloisterzone.action.TilePlacementAction;
import com.jcloisterzone.game.RandomGenerator;
import com.jcloisterzone.game.capability.*;
import com.jcloisterzone.game.capability.AbbeyCapability.AbbeyToken;
import com.jcloisterzone.game.state.ActionsState;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.io.message.PlaceTileMessage;

public class AbbeyPhase extends AbstractAbbeyPhase {

    private TilePhase tilePhase;
    private ActionPhase actionPhase;

    public AbbeyPhase(RandomGenerator random, Phase defaultNext) {
        super(random, defaultNext);
    }

    public void setTilePhase(TilePhase tilePhase) {
        this.tilePhase = tilePhase;
    }

    public void setActionPhase(ActionPhase actionPhase) {
        this.actionPhase = actionPhase;
    }

    @Override
    public StepResult enter(GameState state) {
        BazaarCapabilityModel bazaarModel = state.getCapabilityModel(BazaarCapability.class);
        BuilderState builderState = state.getCapabilityModel(BuilderCapability.class);
        boolean bazaarInProgress = bazaarModel != null &&  bazaarModel.getSupply() != null;
        boolean builderSecondTurnPart = builderState == BuilderState.SECOND_TURN;
        boolean hasAbbey = state.getPlayers().getPlayerTokenCount(state.getPlayers().getTurnPlayerIndex(), AbbeyToken.ABBEY_TILE) > 0;
        if (hasAbbey && (builderSecondTurnPart || !bazaarInProgress)) {
            TilePlacementAction action = createAbbeyAction(state);
            if (action != null) {
                state = state.setPlayerActions(new ActionsState(
                        state.getTurnPlayer(),
                        action,
                        true
                ));
                return promote(state);
            }

        }
        return next(state, tilePhase);
    }

    @PhaseMessageHandler
    public StepResult handlePlaceTile(GameState state, PlaceTileMessage msg) {
        state = applyPlaceTile(state, msg);
        return next(state, actionPhase);
    }
}
