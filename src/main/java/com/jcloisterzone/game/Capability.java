package com.jcloisterzone.game;

import com.jcloisterzone.game.capability.AbbeyCapability;
import com.jcloisterzone.game.capability.BarnCapability;
import com.jcloisterzone.game.capability.BazaarCapability;
import com.jcloisterzone.game.capability.BigFollowerCapability;
import com.jcloisterzone.game.capability.BridgeCapability;
import com.jcloisterzone.game.capability.BuilderCapability;
import com.jcloisterzone.game.capability.CastleCapability;
import com.jcloisterzone.game.capability.CathedralCapability;
import com.jcloisterzone.game.capability.ClothWineGrainCapability;
import com.jcloisterzone.game.capability.CornCircleCapability;
import com.jcloisterzone.game.capability.DragonCapability;
import com.jcloisterzone.game.capability.FairyCapability;
import com.jcloisterzone.game.capability.FestivalCapability;
import com.jcloisterzone.game.capability.FlierCapability;
import com.jcloisterzone.game.capability.InnCapability;
import com.jcloisterzone.game.capability.KingScoutCapability;
import com.jcloisterzone.game.capability.MayorCapability;
import com.jcloisterzone.game.capability.PhantomCapability;
import com.jcloisterzone.game.capability.PigCapability;
import com.jcloisterzone.game.capability.PlagueCapability;
import com.jcloisterzone.game.capability.PortalCapability;
import com.jcloisterzone.game.capability.PrincessCapability;
import com.jcloisterzone.game.capability.RiverCapability;
import com.jcloisterzone.game.capability.ShrineCapability;
import com.jcloisterzone.game.capability.SiegeCapability;
import com.jcloisterzone.game.capability.TowerCapability;
import com.jcloisterzone.game.capability.TunnelCapability;
import com.jcloisterzone.game.capability.WagonCapability;
import com.jcloisterzone.game.capability.WindRoseCapability;

/* experimental - more granular game settings, now only for debug purposes */
public enum Capability {
    FARM_PLACEMENT,

    BIG_FOLLOWER(BigFollowerCapability.class),
    INN(InnCapability.class),
    CATHEDRAL(CathedralCapability.class),

    BUILDER(BuilderCapability.class),
    PIG(PigCapability.class),
    CLOTH_WINE_GRAIN(ClothWineGrainCapability.class),

    FAIRY(FairyCapability.class),
    DRAGON(DragonCapability.class),
    PORTAL(PortalCapability.class),
    PRINCESS(PrincessCapability.class),

    TOWER(TowerCapability.class),

    ABBEY(AbbeyCapability.class),
    WAGON(WagonCapability.class),
    MAYOR(MayorCapability.class),
    BARN(BarnCapability.class),

    BRIDGE(BridgeCapability.class),
    CASTLE(CastleCapability.class),
    BAZAAR(BazaarCapability.class),

    KING_SCOUT(KingScoutCapability.class),
    RIVER(RiverCapability.class),
    SIEGE(SiegeCapability.class),
    SHRINE(ShrineCapability.class),
    TUNNEL(TunnelCapability.class),
    CORN_CIRCLE(CornCircleCapability.class),
    PLAGUE(PlagueCapability.class),
    PHANTOM(PhantomCapability.class),
    FESTIVAL(FestivalCapability.class),
    WIND_ROSE(WindRoseCapability.class),

    FLIER(FlierCapability.class);


    Class<? extends CapabilityController> controller;

    private Capability() {
        this(null);
    }

    private Capability(Class<? extends CapabilityController> controller) {
        this.controller = controller;
    }

    public Class<? extends CapabilityController> getController() {
        return controller;
    }
}


