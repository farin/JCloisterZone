package com.jcloisterzone.game.capability;

import org.w3c.dom.Element;

import com.jcloisterzone.board.TileDefinition;
import com.jcloisterzone.board.TileTrigger;
import com.jcloisterzone.figure.neutral.Mage;
import com.jcloisterzone.figure.neutral.Witch;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.state.GameState;

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
    public TileDefinition initTile(GameState state, TileDefinition tile, Element xml) {
        if (xml.getElementsByTagName("mage").getLength() > 0) {
           tile = tile.setTileTrigger(TileTrigger.MAGE);
        }
        return tile;
    }
}
