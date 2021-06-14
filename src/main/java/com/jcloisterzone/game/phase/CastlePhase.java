package com.jcloisterzone.game.phase;

import com.jcloisterzone.Player;
import com.jcloisterzone.action.CastleAction;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.CastleCreated;
import com.jcloisterzone.event.PlayEvent.PlayEventMeta;
import com.jcloisterzone.feature.Castle;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.game.capability.CastleCapability;
import com.jcloisterzone.game.capability.CastleCapability.CastleToken;
import com.jcloisterzone.game.state.ActionsState;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.PlacedTile;
import com.jcloisterzone.io.message.PlaceTokenMessage;
import com.jcloisterzone.random.RandomGenerator;
import io.vavr.Tuple2;
import io.vavr.collection.HashSet;
import io.vavr.collection.List;
import io.vavr.collection.Set;

public class CastlePhase extends Phase {

    public CastlePhase(RandomGenerator random, Phase defaultNext) {
        super(random, defaultNext);
    }

    private Set<FeaturePointer> getPlayerOptions(GameState state, Player player) {
        if (state.getPlayers().getPlayerTokenCount(player.getIndex(), CastleToken.CASTLE) == 0) {
            return HashSet.empty();
        }

        PlacedTile lastPlaced = state.getLastPlaced();
        Position pos = lastPlaced.getPosition();
        return state.getTileFeatures2(pos, City.class)
            .filter(t -> t._2.hasModifier(state, CastleCapability.CASTLE_BASE))
            .filter(t -> t._2.getPlaces().size() == 2)
            .filter(t -> {
                List<Follower> followers = t._2.getFollowers(state).toList();
                if (followers.size() != 1) return false;
                return followers.get().getPlayer().equals(player);
            })
            .map(Tuple2::_1)
            .toSet();
    }

    private StepResult prepareActions(GameState state, Player continueWith) {
        Player turnPlayer = state.getTurnPlayer();
        Player player = continueWith;

        do {
            Set<FeaturePointer> options = getPlayerOptions(state, player);
            if (!options.isEmpty()) {
                CastleAction action = new CastleAction(options);
                ActionsState as = new ActionsState(player, action, true);
                return promote(state.setPlayerActions(as));
            }

            player = player.getNextPlayer(state);
        } while (!player.equals(turnPlayer));

        //no castle action
        return next(state);
    }

    @Override
    public StepResult enter(GameState state) {
        return prepareActions(state, state.getTurnPlayer());
    }

    @PhaseMessageHandler
    public StepResult handlePlaceTokenMessage(GameState state, PlaceTokenMessage msg) {
        if (msg.getToken() != CastleToken.CASTLE) {
            throw new IllegalArgumentException();
        }
        Player player = state.getActivePlayer();
        City city = (City) state.getFeature((FeaturePointer) msg.getPointer());
        Castle castle = new Castle(city.getPlaces().map(fp -> fp.setFeature(Castle.class)));

        state = state.mapPlayers(ps ->
           ps.addTokenCount(player.getIndex(), CastleToken.CASTLE, -1)
        );
        state = state.mapFeatureMap(m -> {
            for (var fp : city.getPlaces()) {
                Position pos = fp.getPosition();
                m = m.put(pos, m.get(pos).get().remove(fp).put(fp.setFeature(Castle.class), castle));
            }
            return m;
        });
        state = state.appendEvent(new CastleCreated(
           PlayEventMeta.createWithPlayer(player),
           castle
        ));

        Player nextPlayer = player.getNextPlayer(state);
        state = clearActions(state);
        if (nextPlayer.equals(state.getTurnPlayer())) {
            return next(state);
        } else {
            //it is possible to deploy castle by another player
            return prepareActions(state, nextPlayer);
        }
    }
}
