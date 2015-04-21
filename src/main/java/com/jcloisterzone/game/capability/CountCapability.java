package com.jcloisterzone.game.capability;

import com.jcloisterzone.board.Tile;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.Game;

public class CountCapability extends Capability {

    private static final String[] FORBIDDEN_TILES = new String[] { "CO.6", "CO.7" };

    public CountCapability(Game game) {
        super(game);
    }

    public static boolean isTileForbidden(Tile tile) {
        String id = tile.getId();
        for (String forbidden : FORBIDDEN_TILES) {
            if (forbidden.equals(id)) return true;
        }
        return false;
    }

    @Override
    public boolean isDeployAllowed(Tile tile, Class<? extends Meeple> meepleType) {
        return !isTileForbidden(tile);
    }



}
