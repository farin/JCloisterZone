package com.jcloisterzone.integration.inns_and_cathedrals;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.integration.IntegrationTest;

import io.vavr.collection.Array;

public class InnsAndCathedralsScoringTest extends IntegrationTest {

    /**
     * 	- completed inns scoring
     *  - unfinished inn final scoring (0 points)
     */
    @Test
    public void testInnsScoring() {
        GameState state = createGameState("saved-games/inns_and_cathedrals/innsScoring.jcz");

        Array<Integer> score = state.getPlayers().getScore();
        int alice = score.get(0);
        int bob = score.get(1);

        assertEquals(8, alice);
        assertEquals(0, bob);
    }

    /**
     * 	- completed cathedral scoring
     *  - unfinished inn final scoring (0 points)
     */
    @Test
    public void testCathedralsScoring() {
        GameState state = createGameState("saved-games/inns_and_cathedrals/cathedralsScoring.jcz");

        Array<Integer> score = state.getPlayers().getScore();
        int alice = score.get(0);
        int bob = score.get(1);

        assertEquals(15, alice);
        assertEquals(0, bob);

    }
}
