package com.jcloisterzone.reducers;

import com.jcloisterzone.board.Position;
import com.jcloisterzone.event.play.PlayEvent.PlayEventMeta;
import com.jcloisterzone.event.play.TokenPlacedEvent;
import com.jcloisterzone.game.Token;
import com.jcloisterzone.game.capability.LittleBuildingsCapability;
import com.jcloisterzone.game.state.GameState;

public class PlaceLittleBuilding implements Reducer {

    private final Token token;
    private final Position pos;

    public PlaceLittleBuilding(Token token, Position pos) {
        assert token.isLittleBuilding();
        this.token = token;
        this.pos = pos;

    }

    @Override
    public GameState apply(GameState state) {
        state = state.mapCapabilityModel(LittleBuildingsCapability.class, placedTokens ->
            placedTokens.put(pos, token)
        );
        state = state.appendEvent(new TokenPlacedEvent(PlayEventMeta.createWithActivePlayer(state), token, pos));
        return state;
    }
}
