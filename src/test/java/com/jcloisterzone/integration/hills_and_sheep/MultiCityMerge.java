package com.jcloisterzone.integration.hills_and_sheep;

import com.jcloisterzone.PlayerScore;
import com.jcloisterzone.PointCategory;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.integration.IntegrationTest;
import io.vavr.collection.Array;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MultiCityMerge extends IntegrationTest {

    /**
     *  HS.CC!.v merging
     */
    @Test
    public void testMultiCityMerge() {
        GameState state = createGameState("saved-games/hills_and_sheep/multi_city_merge.jcz");

        Array<PlayerScore> score = state.getPlayers().getScore();
        PlayerScore alice = score.get(0);
        PlayerScore bob = score.get(1);

        assertEquals(20, alice.getPoints());
        assertEquals(0, bob.getPoints());
    }

    /**
     *  HS.CC!.v merging
     */
    @Test
    public void testMultiCityMerge2() {
        GameState state = createGameState("saved-games/hills_and_sheep/multi_city_merge2.jcz");

        Array<PlayerScore> score = state.getPlayers().getScore();
        PlayerScore alice = score.get(0);
        PlayerScore bob = score.get(1);

        assertEquals(8, alice.getPoints());
        assertEquals(8, bob.getPoints());
    }

    /**
     *  HS.CC!.v merging
     */
    @Test
    public void testMultiCityMergeOpen() {
        GameState state = createGameState("saved-games/hills_and_sheep/multi_city_merge_open.jcz");

        Array<PlayerScore> score = state.getPlayers().getScore();
        PlayerScore alice = score.get(0);
        PlayerScore bob = score.get(1);

        assertEquals(0, alice.getPoints());
        assertEquals(0, bob.getPoints());
    }

    /**
     *  HS.CC!.v merging
     */
    @Test
    public void testMultiCityMergeAbbey() {
        GameState state = createGameState("saved-games/hills_and_sheep/multi_city_merge_abbey.jcz");

        Array<PlayerScore> score = state.getPlayers().getScore();
        PlayerScore alice = score.get(0);
        PlayerScore bob = score.get(1);

        assertEquals(4, alice.getPoints());
        assertEquals(4, bob.getPoints());
    }

    /**
     *  HS.CC!.v merging
     */
    @Test
    public void testMultiCityMergeAbbey2() {
        GameState state = createGameState("saved-games/hills_and_sheep/multi_city_merge_abbey2.jcz");

        Array<PlayerScore> score = state.getPlayers().getScore();
        PlayerScore alice = score.get(0);
        PlayerScore bob = score.get(1);

        assertEquals(24, alice.getPoints());
        assertEquals(0, bob.getPoints());
    }
}
