package com.jcloisterzone.integration.traders_and_builders;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.jcloisterzone.game.capability.TradeGoodsCapability.TradeGoods;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.integration.IntegrationTest;

import io.vavr.collection.Array;

public class TradeGoodsScoringTest extends IntegrationTest {


    @Test
    public void testFinalScoring() {
        GameState state = createGameState("saved-games/traders_and_builders/tradeGoodsScoring.jcz");

        Array<Integer> score = state.getPlayers().getScore();
        int alice = score.get(0);
        int bob = score.get(1);

        assertEquals(16, alice);
        assertEquals(1, state.getPlayers().getPlayerTokenCount(0, TradeGoods.CLOTH));
        assertEquals(0, state.getPlayers().getPlayerTokenCount(0, TradeGoods.GRAIN));
        assertEquals(0, state.getPlayers().getPlayerTokenCount(0, TradeGoods.WINE));

        assertEquals(20, bob);
        assertEquals(1, state.getPlayers().getPlayerTokenCount(1, TradeGoods.CLOTH));
        assertEquals(0, state.getPlayers().getPlayerTokenCount(1, TradeGoods.GRAIN));
        assertEquals(2, state.getPlayers().getPlayerTokenCount(1, TradeGoods.WINE));
    }
}
