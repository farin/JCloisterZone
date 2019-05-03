package com.jcloisterzone.integration.traders_and_builders;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.jcloisterzone.PlayerScore;
import com.jcloisterzone.PointCategory;
import com.jcloisterzone.game.capability.TradeGoodsCapability.TradeGoods;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.integration.IntegrationTest;

import io.vavr.collection.Array;

public class TradeGoodsScoringTest extends IntegrationTest {


    @Test
    public void testFinalScoring() {
        GameState state = createGameState("saved-games/traders_and_builders/tradeGoodsScoring.jcz");

        Array<PlayerScore> score = state.getPlayers().getScore();
        PlayerScore alice = score.get(0);
        PlayerScore bob = score.get(1);

        assertEquals(16, alice.getPoints());
        assertEquals(10, alice.getStats().get(PointCategory.TRADE_GOODS).getOrElse(0).intValue());
        assertEquals(1, state.getPlayers().getPlayerTokenCount(0, TradeGoods.CLOTH));
        assertEquals(0, state.getPlayers().getPlayerTokenCount(0, TradeGoods.GRAIN));
        assertEquals(0, state.getPlayers().getPlayerTokenCount(0, TradeGoods.WINE));

        assertEquals(20, bob.getPoints());
        assertEquals(20, bob.getStats().get(PointCategory.TRADE_GOODS).getOrElse(0).intValue());
        assertEquals(1, state.getPlayers().getPlayerTokenCount(1, TradeGoods.CLOTH));
        assertEquals(0, state.getPlayers().getPlayerTokenCount(1, TradeGoods.GRAIN));
        assertEquals(2, state.getPlayers().getPlayerTokenCount(1, TradeGoods.WINE));
    }
}
