package com.jcloisterzone.integration.inns_and_cathedrals;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.jcloisterzone.PlayerScore;
import com.jcloisterzone.PointCategory;
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

        Array<PlayerScore> score = state.getPlayers().getScore();
        PlayerScore alice = score.get(0);
        PlayerScore bob = score.get(1);

        assertEquals(8, alice.getPoints());
        assertEquals(8, alice.getStats().get(PointCategory.ROAD).getOrElse(0).intValue());
        assertEquals(0, alice.getStats().get(PointCategory.CITY).getOrElse(0).intValue());
        assertEquals(0, alice.getStats().get(PointCategory.CLOISTER).getOrElse(0).intValue());
        assertEquals(0, alice.getStats().get(PointCategory.FARM).getOrElse(0).intValue());

        assertEquals(0, bob.getPoints());
        assertEquals(0, bob.getStats().get(PointCategory.ROAD).getOrElse(0).intValue());
        assertEquals(0, bob.getStats().get(PointCategory.CITY).getOrElse(0).intValue());
        assertEquals(0, bob.getStats().get(PointCategory.CLOISTER).getOrElse(0).intValue());
        assertEquals(0, bob.getStats().get(PointCategory.FARM).getOrElse(0).intValue());
    }

    /**
     * 	- completed cathedral scoring
     *  - unfinished inn final scoring (0 points)
     */
    @Test
    public void testCathedralsScoring() {
        GameState state = createGameState("saved-games/inns_and_cathedrals/cathedralsScoring.jcz");

        Array<PlayerScore> score = state.getPlayers().getScore();
        PlayerScore alice = score.get(0);
        PlayerScore bob = score.get(1);

        assertEquals(15, alice.getPoints());
        assertEquals(0, alice.getStats().get(PointCategory.ROAD).getOrElse(0).intValue());
        assertEquals(15, alice.getStats().get(PointCategory.CITY).getOrElse(0).intValue());
        assertEquals(0, alice.getStats().get(PointCategory.CLOISTER).getOrElse(0).intValue());
        assertEquals(0, alice.getStats().get(PointCategory.FARM).getOrElse(0).intValue());

        assertEquals(0, bob.getPoints());
        assertEquals(0, bob.getStats().get(PointCategory.ROAD).getOrElse(0).intValue());
        assertEquals(0, bob.getStats().get(PointCategory.CITY).getOrElse(0).intValue());
        assertEquals(0, bob.getStats().get(PointCategory.CLOISTER).getOrElse(0).intValue());
        assertEquals(0, bob.getStats().get(PointCategory.FARM).getOrElse(0).intValue());
    }
}
