package com.jcloisterzone.game.capability;

import org.w3c.dom.Element;

import com.jcloisterzone.Immutable;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.TileDefinition;
import com.jcloisterzone.board.TileGroupState;
import com.jcloisterzone.board.TileTrigger;
import com.jcloisterzone.figure.neutral.Dragon;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.PlacedTile;
import com.jcloisterzone.reducers.MoveNeutralFigure;

import io.vavr.collection.Vector;

/**
 * @model Vector<Position> : visited tiles
 */
@Immutable
public class DragonCapability extends Capability<Vector<Position>> {

    private static final long serialVersionUID = 1L;

    public static final int DRAGON_MOVES = 6;
    public static final String TILE_GROUP_DRAGON = "dragon";

    @Override
    public TileDefinition initTile(TileDefinition tile, Element xml) {
        if (xml.getElementsByTagName("volcano").getLength() > 0) {
            tile = tile.setTileTrigger(TileTrigger.VOLCANO);
        }
        if (xml.getElementsByTagName("dragon").getLength() > 0) {
            tile = tile.setTileTrigger(TileTrigger.DRAGON);
        }
        return tile;
    }

    @Override
    public String getTileGroup(TileDefinition tile) {
        return tile.getTrigger() == TileTrigger.DRAGON ? TILE_GROUP_DRAGON : null;
    }


    @Override
    public GameState onStartGame(GameState state) {
        state = state.mapNeutralFigures(nf -> nf.setDragon(new Dragon("dragon.1")));
        state = state.mapTilePack(pack -> pack.deactivateGroup(TILE_GROUP_DRAGON));
        state = setModel(state, Vector.empty());
        return state;
    }

    @Override
    public GameState onTilePlaced(GameState state) {
        PlacedTile pt = state.getLastPlaced();
        if (pt.getTile().getTrigger() == TileTrigger.VOLCANO) {
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
