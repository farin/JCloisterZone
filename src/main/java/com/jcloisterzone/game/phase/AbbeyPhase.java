package com.jcloisterzone.game.phase;

import java.util.Arrays;

import com.jcloisterzone.Player;
import com.jcloisterzone.action.TilePlacementAction;
import com.jcloisterzone.board.EdgePattern;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.TileDefinition;
import com.jcloisterzone.board.TilePlacement;
import com.jcloisterzone.game.Token;
import com.jcloisterzone.game.capability.AbbeyCapability;
import com.jcloisterzone.game.capability.BazaarCapability;
import com.jcloisterzone.game.capability.BazaarCapabilityModel;
import com.jcloisterzone.game.capability.BazaarItem;
import com.jcloisterzone.game.capability.BuilderCapability;
import com.jcloisterzone.game.capability.BuilderState;
import com.jcloisterzone.game.state.ActionsState;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.reducers.PlaceTile;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.wsio.message.PlaceTileMessage;

import io.vavr.Tuple2;
import io.vavr.collection.Array;
import io.vavr.collection.Queue;
import io.vavr.collection.Stream;

@RequiredCapability(AbbeyCapability.class)
public class AbbeyPhase extends Phase {

    public AbbeyPhase(GameController gc) {
        super(gc);
    }

    @Override
    public StepResult enter(GameState state) {
        BazaarCapabilityModel bazaarModel = state.getCapabilityModel(BazaarCapability.class);
        BuilderState builderState = state.getCapabilityModel(BuilderCapability.class);
        boolean baazaarInProgress = bazaarModel != null &&  bazaarModel.getSupply() != null;
        boolean builderSecondTurnPart = builderState == BuilderState.SECOND_TURN;
        boolean hasAbbey = state.getPlayers().getPlayerTokenCount(state.getPlayers().getTurnPlayerIndex(), Token.ABBEY_TILE) > 0;
        if (hasAbbey && (builderSecondTurnPart || !baazaarInProgress)) {
            Stream<Tuple2<Position, EdgePattern>> holes = state.getHoles();
            if (!holes.isEmpty()) {
                TileDefinition abbey = state.getTilePack().findTile(TileDefinition.ABBEY_TILE_ID).get();

                TilePlacementAction action = new TilePlacementAction(
                    abbey,
                    holes.flatMap(t ->
                        Array.ofAll(Arrays.asList(Rotation.values()))
                            .map(r -> new TilePlacement(t._1, r, null))
                    ).toSet()
                );

                state = state.setPlayerActions(new ActionsState(
                    state.getTurnPlayer(),
                    action,
                    true
                ));

                return promote(state);
            }
        }
        return next(state);
    }

    @PhaseMessageHandler
    public StepResult handlePlaceTile(GameState state, PlaceTileMessage msg) {
        if (!msg.getTileId().equals(TileDefinition.ABBEY_TILE_ID)) {
            throw new IllegalArgumentException("Only abbey can be placed.");
        }

        Player player = state.getActivePlayer();
        state = state.mapPlayers(ps ->
            ps.addTokenCount(player.getIndex(), Token.ABBEY_TILE, -1)
        );

        TileDefinition abbey = state.getTilePack().findTile(TileDefinition.ABBEY_TILE_ID).get();
        state = (new PlaceTile(abbey, msg.getPosition(), msg.getRotation())).apply(state);
        state = clearActions(state);

        return next(state, ActionPhase.class);
    }
}
