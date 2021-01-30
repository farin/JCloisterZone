package com.jcloisterzone.game.phase;

import com.jcloisterzone.Player;
import com.jcloisterzone.action.TilePlacementAction;
import com.jcloisterzone.board.PlacementOption;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.RandomGenerator;
import com.jcloisterzone.game.capability.*;
import com.jcloisterzone.game.capability.AbbeyCapability.AbbeyToken;
import com.jcloisterzone.game.state.ActionsState;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.reducers.PlaceTile;
import com.jcloisterzone.io.message.PlaceTileMessage;
import io.vavr.collection.Array;
import io.vavr.collection.Stream;

import java.util.Arrays;

@RequiredCapability(AbbeyCapability.class)
public class AbbeyPhase extends AbstractAbbeyPhase {

    public AbbeyPhase(RandomGenerator random) {
        super(random);
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
        return next(state, TilePhase.class);
    }

    @PhaseMessageHandler
    public StepResult handlePlaceTile(GameState state, PlaceTileMessage msg) {
        state = applyPlaceTile(state, msg);
        return next(state, ActionPhase.class);
    }
}
