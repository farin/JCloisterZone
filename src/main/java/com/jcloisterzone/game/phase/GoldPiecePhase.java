package com.jcloisterzone.game.phase;

import com.jcloisterzone.action.GoldPieceAction;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.TileTrigger;
import com.jcloisterzone.event.play.PlayEvent.PlayEventMeta;
import com.jcloisterzone.event.play.TokenPlacedEvent;
import com.jcloisterzone.game.RandomGenerator;
import com.jcloisterzone.game.Token;
import com.jcloisterzone.game.capability.GoldminesCapability;
import com.jcloisterzone.game.state.ActionsState;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.PlacedTile;
import com.jcloisterzone.wsio.message.PlaceTokenMessage;

import io.vavr.collection.Set;

@RequiredCapability(GoldminesCapability.class)
public class GoldPiecePhase extends Phase {

    public GoldPiecePhase(RandomGenerator random) {
        super(random);
    }

    @Override
    public StepResult enter(GameState state) {
        PlacedTile placedTile = state.getLastPlaced();
        if (placedTile.getTile().getTrigger() == TileTrigger.GOLDMINE) {
            Position pos = placedTile.getPosition();
            state = placeGoldToken(state, pos);
            Set<Position> options = state.getAdjacentAndDiagonalTiles(pos).map(PlacedTile::getPosition).toSet();
            switch (options.size()) {
            case 0:
                break;
            case 1:
                state = placeGoldToken(state, options.get());
                break;
            default:
                // player must choose second piece placement
                GoldPieceAction action = new GoldPieceAction(options);
                state = state.setPlayerActions(new ActionsState(state.getTurnPlayer(), action, false));
                return promote(state);
            }
        }
        return next(state);
    }

    private GameState placeGoldToken(GameState state, Position pos) {
        state = state.mapCapabilityModel(GoldminesCapability.class, placedGold -> {
            int currValue = placedGold.get(pos).getOrElse(0);
            return placedGold.put(pos, currValue + 1);
        });
        state = state.appendEvent(new TokenPlacedEvent(PlayEventMeta.createWithoutPlayer(), Token.GOLD, pos));
        return state;
    }


    @PhaseMessageHandler
    public StepResult handlePlaceToken(GameState state, PlaceTokenMessage msg) {
        Token token = msg.getToken();
        Position pos = (Position) msg.getPointer();

        if (token != Token.GOLD) {
            throw new IllegalArgumentException();
        }
        state = placeGoldToken(state, pos);
        state = clearActions(state);
        return next(state);
    }
}
