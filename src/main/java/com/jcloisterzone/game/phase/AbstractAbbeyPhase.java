package com.jcloisterzone.game.phase;

import com.jcloisterzone.Player;
import com.jcloisterzone.action.TilePlacementAction;
import com.jcloisterzone.board.PlacementOption;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.capability.AbbeyCapability;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.io.message.PlaceTileMessage;
import com.jcloisterzone.random.RandomGenerator;
import com.jcloisterzone.reducers.PlaceTile;
import io.vavr.collection.Array;
import io.vavr.collection.Stream;

import java.util.Arrays;

public abstract class AbstractAbbeyPhase extends Phase {

    public AbstractAbbeyPhase(RandomGenerator random, Phase defaultNext) {
        super(random, defaultNext);
    }

    protected TilePlacementAction createAbbeyAction(GameState state) {
        Stream<PlacementOption> options = state.getHoles()
                .flatMap(t ->
                        Array.ofAll(Arrays.asList(Rotation.values()))
                                .map(r -> new PlacementOption(t._1, r, null))
                )
                .filter(tp -> {
                    for (Capability<?> cap : state.getCapabilities().toSeq()) {
                        if (!cap.isTilePlacementAllowed(state, AbbeyCapability.ABBEY_TILE, tp)) return false;
                    }
                    return true;
                });

        if (!options.isEmpty()) {
            return new TilePlacementAction(
                    AbbeyCapability.ABBEY_TILE,
                    options.toSet()
            );
        }
        return null;
    }

    protected GameState applyPlaceTile(GameState state, PlaceTileMessage msg) {
        if (!msg.getTileId().equals(AbbeyCapability.ABBEY_TILE_ID)) {
            throw new IllegalArgumentException("Only abbey can be placed.");
        }

        Player player = state.getActivePlayer();
        state = state.mapPlayers(ps ->
                ps.addTokenCount(player.getIndex(), AbbeyCapability.AbbeyToken.ABBEY_TILE, -1)
        );

        state = (new PlaceTile(AbbeyCapability.ABBEY_TILE, msg.getPosition(), msg.getRotation())).apply(state);
        state = clearActions(state);
        return state;
    }
}
