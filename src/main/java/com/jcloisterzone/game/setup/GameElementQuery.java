package com.jcloisterzone.game.setup;

import com.jcloisterzone.game.GameSetup;

public class GameElementQuery implements SetupQuery {

    private final String mechanics;

    public GameElementQuery(String mechanics) {
        this.mechanics = mechanics;
    }

    @Override
    public Boolean apply(GameSetup gameSetup) {
        return true;
    }
}
