package com.jcloisterzone.game.capability;

import com.jcloisterzone.Immutable;
import com.jcloisterzone.XMLUtils;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.TileModifier;
import com.jcloisterzone.figure.neutral.Dragon;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.PlacedTile;
import com.jcloisterzone.reducers.MoveNeutralFigure;
import io.vavr.collection.Vector;
import org.w3c.dom.Element;

/**
 * @model Vector<Position> : visited tiles
 */
@Immutable
public class DragonCapability extends Capability<Vector<Position>> {

    private static final long serialVersionUID = 1L;

    public static final TileModifier VOLCANO = new TileModifier("Volcano");
    public static final TileModifier DRAGON_TRIGGER = new TileModifier("DragonTrigger");

    public static final int DRAGON_MOVES = 6;
    public static final String TILE_GROUP_DRAGON = "dragon";

    @Override
    public Tile initTile(GameState state, Tile tile, Vector<Element> tileElements) {
        if (!XMLUtils.getElementStreamByTagName(tileElements, "volcano").isEmpty()) {
            tile = tile.addTileModifier(VOLCANO);
        }
        if (!XMLUtils.getElementStreamByTagName(tileElements, "dragon").isEmpty()) {
            tile = tile.addTileModifier(DRAGON_TRIGGER);
        }
        return tile;
    }

    @Override
    public String getTileGroup(Tile tile) {
        return tile.hasModifier(DRAGON_TRIGGER) ? TILE_GROUP_DRAGON : null;
    }


    @Override
    public GameState onStartGame(GameState state) {
        state = state.mapNeutralFigures(nf -> nf.setDragon(new Dragon("dragon.1")));
        state = state.mapTilePack(pack -> pack.deactivateGroup(TILE_GROUP_DRAGON));
        state = setModel(state, Vector.empty());
        return state;
    }

    @Override
    public GameState onTilePlaced(GameState state, PlacedTile pt) {
        if (pt.getTile().hasModifier(VOLCANO)) {
            state = state.mapTilePack(pack -> pack.activateGroup(TILE_GROUP_DRAGON));
            state = (
                new MoveNeutralFigure<>(state.getNeutralFigures().getDragon(), pt.getPosition())
            ).apply(state);
        }
        return state;
    }

    @Override
    public boolean isMeepleDeploymentAllowed(GameState state, Position pos) {
        return !pos.equals(state.getNeutralFigures().getDragonDeployment());
    }
}
