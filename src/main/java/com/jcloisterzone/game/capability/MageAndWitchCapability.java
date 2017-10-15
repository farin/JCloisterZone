package com.jcloisterzone.game.capability;

import org.w3c.dom.Element;

import com.jcloisterzone.XMLUtils;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.TileTrigger;
import com.jcloisterzone.figure.neutral.Mage;
import com.jcloisterzone.figure.neutral.Witch;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.state.GameState;

import io.vavr.collection.Vector;

public class MageAndWitchCapability extends Capability<Void> {

    @Override
    public GameState onStartGame(GameState state) {
        return state.setNeutralFigures(
            state.getNeutralFigures()
                .setMage(new Mage("mage.1"))
                .setWitch(new Witch("witch.1"))
        );
    }

    @Override
    public Tile initTile(GameState state, Tile tile, Vector<Element> tileElements) {
        if (!XMLUtils.getElementStreamByTagName(tileElements, "mage").isEmpty()) {
           tile = tile.setTileTrigger(TileTrigger.MAGE);
        }
        return tile;
    }
}
