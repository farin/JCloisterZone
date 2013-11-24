package com.jcloisterzone.feature;

import java.util.Arrays;

import org.junit.After;
import org.junit.Before;

import com.jcloisterzone.Expansion;
import com.jcloisterzone.board.AbstractTileTest;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.game.Game;

public class AbstractScoringTest extends AbstractTileTest {

    protected void setUpGame(Game game) {
        game.getExpansions().add(Expansion.BASIC);
        game.getExpansions().add(Expansion.INNS_AND_CATHEDRALS);
        game.getExpansions().add(Expansion.TRADERS_AND_BUILDERS);
        game.getExpansions().add(Expansion.ABBEY_AND_MAYOR);
        game.getExpansions().add(Expansion.CATHARS);

        for (Expansion exp : game.getExpansions()) {
            game.getCapabilityClasses().addAll(Arrays.asList(exp.getCapabilities()));
        }
    }

    @Before
    public void setUp() {
        game.start();
    }

    @After
    public void tearDown() {
        game.getCustomRules().clear();
    }

    protected Tile putTile(Position pos, Rotation rot,  Expansion exp, String id) {
        Tile tile = createTile(exp, id);
        tile.setRotation(rot);
        game.getBoard().refreshAvailablePlacements(tile);
        game.getBoard().add(tile, pos, true);
        game.getBoard().mergeFeatures(tile);
        return tile;
    }

}
