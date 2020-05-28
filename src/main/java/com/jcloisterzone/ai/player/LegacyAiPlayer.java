package com.jcloisterzone.ai.player;

import java.util.Set;

import com.jcloisterzone.Expansion;
import com.jcloisterzone.Player;
import com.jcloisterzone.ai.GameStateRanking;
import com.jcloisterzone.ai.RankingAiPlayer;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.SupportedSetup;
import com.jcloisterzone.game.capability.*;

import io.vavr.collection.HashSet;

public class LegacyAiPlayer extends RankingAiPlayer {

    @Override
    protected GameStateRanking createStateRanking(Player me) {
        return new LegacyRanking(me);
    }

    private Set<Class<? extends Capability<?>>> getSupportedCapabilities() {
        HashSet<Class<? extends Capability<?>>> value = HashSet.of(
            StandardGameCapability.class,
            InnCapability.class,
            BigFollowerCapability.class,
            CathedralCapability.class,
            PigCapability.class,
            BuilderCapability.class,
            TradeGoodsCapability.class,
            PigHerdCapability.class,
            FairyCapability.class,
            DragonCapability.class,
            PortalCapability.class,
            PrincessCapability.class,
            TowerCapability.class,
            AbbeyCapability.class,
            WagonCapability.class,
            MayorCapability.class,
            BarnCapability.class,
            KingAndRobberBaronCapability.class,
            RiverCapability.class,
            SiegeCapability.class,
            ShrineCapability.class,
            TunnelCapability.class,
            CornCircleCapability.class,
            PhantomCapability.class,
            FestivalCapability.class,
            WindRoseCapability.class,
            LabyrinthCapability.class
        );
        return value.toJavaSet();
    }

    @Override
    public SupportedSetup supportedSetup() {
        return new SupportedSetup(
            getSupportedCapabilities(),
            Expansion.values().toJavaSet()
        );
    }
}

