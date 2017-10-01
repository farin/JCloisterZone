package com.jcloisterzone.ai.player;

import com.jcloisterzone.ai.RankingAiPlayer;

public class LegacyAiPlayer extends RankingAiPlayer {

    public LegacyAiPlayer() {
        super(new LegacyRanking());
    }
}

