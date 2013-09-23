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
import com.jcloisterzone.game.capability.DragonCapability;
import com.jcloisterzone.game.capability.FairyCapability;
import com.jcloisterzone.game.capability.InnCapability;
import com.jcloisterzone.game.capability.MayorCapability;
import com.jcloisterzone.game.capability.PigCapability;
import com.jcloisterzone.game.capability.PortalCapability;
import com.jcloisterzone.game.capability.PrincessCapability;
import com.jcloisterzone.game.capability.TowerCapability;
import com.jcloisterzone.game.capability.WagonCapability;

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
    BAZAAR(BazaarCapability.class);


    Class<? extends GameExtension> impl;

    private Capability() {
        this(null);
    }

    private Capability(Class<? extends GameExtension> impl) {
        this.impl = impl;
    }

    public Class<? extends GameExtension> getImplementedBy() {
        return impl;
    }
}


