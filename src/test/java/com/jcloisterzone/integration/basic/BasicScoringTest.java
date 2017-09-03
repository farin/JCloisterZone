package com.jcloisterzone.integration.basic;

import static org.junit.Assert.assertEquals;

import org.junit.Assert;
import org.junit.Test;

import com.jcloisterzone.PlayerScore;
import com.jcloisterzone.PointCategory;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.integration.IntegrationTest;

import io.vavr.collection.Array;

public class BasicScoringTest extends IntegrationTest {


    @Test
    public void test() throws Exception {
        GameState state = createGameState("saved-games/basic/basicScoring.jcz");

        Array<PlayerScore> score = state.getPlayers().getScore();
        PlayerScore alice = score.get(0);
        PlayerScore bob = score.get(0);

        assertEquals(14, alice.getPoints());
        assertEquals(6, alice.getStats().get(PointCategory.ROAD));
        assertEquals(0, alice.getStats().get(PointCategory.CITY));
        assertEquals(5, alice.getStats().get(PointCategory.CLOISTER));
        assertEquals(3, alice.getStats().get(PointCategory.FARM));

        assertEquals(11, bob.getPoints());
        assertEquals(0, bob.getStats().get(PointCategory.ROAD));
        assertEquals(11, bob.getStats().get(PointCategory.CITY));
        assertEquals(0, bob.getStats().get(PointCategory.CLOISTER));
        assertEquals(0, bob.getStats().get(PointCategory.FARM));
    }

}
