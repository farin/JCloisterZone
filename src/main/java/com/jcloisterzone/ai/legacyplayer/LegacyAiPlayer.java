package com.jcloisterzone.ai.legacyplayer;

import java.util.EnumSet;

import com.jcloisterzone.Expansion;
import com.jcloisterzone.ai.GameRanking;
import com.jcloisterzone.ai.RankingAiPlayer;

public class LegacyAiPlayer extends RankingAiPlayer {

    @Override
    protected GameRanking createGameRanking() {
        return new LegacyRanking(this);
    }

    public static EnumSet<Expansion> supportedExpansions() {
        return EnumSet.of(
            Expansion.BASIC,
            Expansion.WINTER,
            Expansion.ABBEY_AND_MAYOR,
            Expansion.INNS_AND_CATHEDRALS,
            Expansion.TRADERS_AND_BUILDERS,
            Expansion.PRINCESS_AND_DRAGON,
            Expansion.TOWER,
            Expansion.KING_AND_ROBBER_BARON,
            Expansion.RIVER,
            Expansion.RIVER_II,
            Expansion.GQ11,
            Expansion.CATAPULT,
            Expansion.WIND_ROSE,
            Expansion.CATHARS,
            Expansion.BESIEGERS,
            Expansion.PHANTOM,
            Expansion.FESTIVAL,
            Expansion.GERMAN_MONASTERIES,
            //Expansion.FLIER,
            //only tiles
            Expansion.COUNT
            //Expansion.PLAGUE
        );
    }
}
