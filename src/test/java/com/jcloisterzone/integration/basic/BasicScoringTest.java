package com.jcloisterzone.integration.basic;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.jcloisterzone.PlayerScore;
import com.jcloisterzone.PointCategory;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.integration.IntegrationTest;

import io.vavr.collection.Array;

public class BasicScoringTest extends IntegrationTest {

    /**
     * Covers basic road / city / farm scoring
     */
    @Test
    public void basicScoring() {
        GameState state = createGameState("saved-games/basic/scoring.jcz");

        Array<PlayerScore> score = state.getPlayers().getScore();
        PlayerScore alice = score.get(0);
        PlayerScore bob = score.get(1);

        assertEquals(14, alice.getPoints());
        assertEquals(6, alice.getStats().get(PointCategory.ROAD).getOrElse(0).intValue());
        assertEquals(0, alice.getStats().get(PointCategory.CITY).getOrElse(0).intValue());
        assertEquals(5, alice.getStats().get(PointCategory.CLOISTER).getOrElse(0).intValue());
        assertEquals(3, alice.getStats().get(PointCategory.FARM).getOrElse(0).intValue());

        assertEquals(11, bob.getPoints());
        assertEquals(0, bob.getStats().get(PointCategory.ROAD).getOrElse(0).intValue());
        assertEquals(11, bob.getStats().get(PointCategory.CITY).getOrElse(0).intValue());
        assertEquals(0, bob.getStats().get(PointCategory.CLOISTER).getOrElse(0).intValue());
        assertEquals(0, bob.getStats().get(PointCategory.FARM).getOrElse(0).intValue());
    }

}
