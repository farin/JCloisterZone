package com.jcloisterzone.game.capability;

import com.jcloisterzone.XMLUtils;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.TileModifier;
import com.jcloisterzone.figure.neutral.Mage;
import com.jcloisterzone.figure.neutral.Witch;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.state.GameState;
import io.vavr.collection.Vector;
import org.w3c.dom.Element;

public class MageAndWitchCapability extends Capability<Void> {

	private static final long serialVersionUID = 1L;

	public static final TileModifier MAGE_TRIGGER = new TileModifier("MageTrigger");

    @Override
    public GameState onStartGame(GameState state) {
        return state.setNeutralFigures(
            state.getNeutralFigures()
                .setMage(new Mage("mage.1"))
                .setWitch(new Witch("witch.1"))
        );
    }

    @Override
    public Tile initTile(GameState state, Tile tile, Element tileElement) {
        if (!XMLUtils.getElementStreamByTagName(tileElement, "mage").isEmpty()) {
           tile = tile.addTileModifier(MAGE_TRIGGER);
        }
        return tile;
    }
}
