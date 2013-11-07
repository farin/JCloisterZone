package com.jcloisterzone.game.capability;

import com.jcloisterzone.Expansion;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.Game;

public class CountCapability extends Capability {

    public CountCapability(Game game) {
        super(game);
    }

    @Override
    public boolean isDeployAllowed(Tile tile, Class<? extends Meeple> meepleType) {
        return tile.getOrigin() != Expansion.COUNT;
    }



}
