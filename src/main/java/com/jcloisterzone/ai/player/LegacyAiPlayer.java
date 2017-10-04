package com.jcloisterzone.ai.player;

import com.jcloisterzone.ai.RankingAiPlayer;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.capability.AbbeyCapability;
import com.jcloisterzone.game.capability.BarnCapability;
import com.jcloisterzone.game.capability.BigFollowerCapability;
import com.jcloisterzone.game.capability.BuilderCapability;
import com.jcloisterzone.game.capability.CathedralCapability;
import com.jcloisterzone.game.capability.CornCircleCapability;
import com.jcloisterzone.game.capability.DragonCapability;
import com.jcloisterzone.game.capability.FairyCapability;
import com.jcloisterzone.game.capability.FestivalCapability;
import com.jcloisterzone.game.capability.InnCapability;
import com.jcloisterzone.game.capability.KingAndRobberBaronCapability;
import com.jcloisterzone.game.capability.MayorCapability;
import com.jcloisterzone.game.capability.PhantomCapability;
import com.jcloisterzone.game.capability.PigCapability;
import com.jcloisterzone.game.capability.PigHerdCapability;
import com.jcloisterzone.game.capability.PortalCapability;
import com.jcloisterzone.game.capability.PrincessCapability;
import com.jcloisterzone.game.capability.RiverCapability;
import com.jcloisterzone.game.capability.ShrineCapability;
import com.jcloisterzone.game.capability.SiegeCapability;
import com.jcloisterzone.game.capability.TowerCapability;
import com.jcloisterzone.game.capability.TradeGoodsCapability;
import com.jcloisterzone.game.capability.TunnelCapability;
import com.jcloisterzone.game.capability.WagonCapability;
import com.jcloisterzone.game.capability.WindRoseCapability;

import io.vavr.collection.HashSet;
import io.vavr.collection.Set;

public class LegacyAiPlayer extends RankingAiPlayer {

    public LegacyAiPlayer() {
        super(new LegacyRanking());
    }

    @Override
    public Set<Class<? extends Capability<?>>> supportedCapabilities() {
        return HashSet.of(
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
            WindRoseCapability.class
        );
    }
}

