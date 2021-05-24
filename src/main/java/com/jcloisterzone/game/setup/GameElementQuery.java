package com.jcloisterzone.game.setup;

import com.jcloisterzone.game.GameSetup;
import com.jcloisterzone.game.state.GameState;

public class GameElementQuery implements SetupQuery {

    private final String gameElement;

    public GameElementQuery(String gameElement) {
        this.gameElement = gameElement;
    }

    @Override
    public Boolean apply(GameState gameState) {
        return gameState.getElements().containsKey(gameElement);
    }
}
