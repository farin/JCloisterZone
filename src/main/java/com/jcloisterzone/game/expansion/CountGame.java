package com.jcloisterzone.game.expansion;

import org.w3c.dom.Element;

import com.jcloisterzone.Expansion;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.game.GameExtension;

public class CountGame extends GameExtension {

    @Override
    public void begin() {
        game.getTilePack().activateGroup("count");
    }

    @Override
    public void initTile(Tile tile, Element xml) {
        if (tile.getId().startsWith(Expansion.COUNT.getCode())) {
            tile.setForbidden(true);
        }
    }

    @Override
    public String getTileGroup(Tile tile) {
        //special group needed to cooperate with river (which disabling default group at game start)
        return (tile.getId().startsWith(Expansion.COUNT.getCode())) ? "count" : null;
    }
}
