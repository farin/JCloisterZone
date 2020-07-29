package com.jcloisterzone.integration.basic;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.integration.IntegrationTest;

import io.vavr.collection.Array;

public class BasicScoringTest extends IntegrationTest {

    /**
     *  - road, city (both completed and final) scoring
     *  - farm scoring
     */
    @Test
    public void testBasicScoring() {
        GameState state = createGameState("saved-games/basic/scoring.jcz");

        Array<Integer> score = state.getPlayers().getScore();
        int alice = score.get(0);
        int bob = score.get(1);

        assertEquals(14, alice);
        assertEquals(11, bob);
    }

    /**
     * 	- features (road and city) with two parts on same tiles (should be score for one points only)
     *  - completed cloister
     *  - if two followers are on one feature - player still gets points once
     */
    @Test
    public void testScoringMultitileFeatures() {
        GameState state = createGameState("saved-games/basic/scoringMultiTiles.jcz");

        Array<Integer> score = state.getPlayers().getScore();
        int alice = score.get(0);
        int bob = score.get(1);

        assertEquals(28, alice);
        assertEquals(13, bob);

    }

    /**
     * 	- farm scoring, one city on multiple farms
     *  - tie on one farm
     */
    @Test
    public void testFarmScoring() {
        GameState state = createGameState("saved-games/basic/scoringFarms.jcz");

        Array<Integer> score = state.getPlayers().getScore();
        int alice = score.get(0);
        int bob = score.get(1);

        assertEquals(9, alice);
        assertEquals(15, bob);
    }

}
