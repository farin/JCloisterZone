package com.jcloisterzone;

import static com.jcloisterzone.ui.I18nUtils._;

import com.jcloisterzone.game.Capability;
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
import com.jcloisterzone.game.capability.CountCapability;
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

@SuppressWarnings("unchecked")
public enum Expansion {
    //Basic sets
    BASIC("BA", _("Basic game")),
    WINTER("WI", _("Winter Edition")),
    WHEEL_OF_FORTUNE("WF", _("Wheel of Fortune"), false),

    //Big expansions
    INNS_AND_CATHEDRALS("IC", _("Inns & Cathedrals"),
            new Class[] { BigFollowerCapability.class ,InnCapability.class, CathedralCapability.class}),
    TRADERS_AND_BUILDERS("TB", _("Traders & Builders"),
            new Class[] { PigCapability.class, BuilderCapability.class, ClothWineGrainCapability.class }),
    PRINCESS_AND_DRAGON("DG", _("The Princess & the Dragon"),
            new Class[] { FairyCapability.class, DragonCapability.class, PortalCapability.class, PrincessCapability.class }),
    TOWER("TO", _("The Tower"),
            new Class[] { TowerCapability.class }),
    ABBEY_AND_MAYOR("AM", _("Abbey & Mayor"),
            new Class[] { AbbeyCapability.class, WagonCapability.class, MayorCapability.class, BarnCapability.class }),
    CATAPULT("CA", _("The Catapult") + " (" + _("tiles only") + ")"),
    BRIDGES_CASTLES_AND_BAZAARS("BB", _("Bridges, Castles and Bazaars"),
            new Class[] { BridgeCapability.class, CastleCapability.class, BazaarCapability.class }),

    //Small expansion
    KING_AND_SCOUT("KS", _("King and Scout"), new Class[] { KingScoutCapability.class }),
    RIVER("R1", _("The River"), new Class[] { RiverCapability.class }),
    RIVER_II("R2", _("The River II"), new Class[] { RiverCapability.class }),
    CATHARS("SI", _("The Cathars / Siege"), new Class[] { SiegeCapability.class }),
    COUNT("CO", _("The Count of Carcassonne") + " (" + _("tiles only") + ")", new Class[] { CountCapability.class }),
    GQ11("GQ", _("The Mini Expansion (GQ11)")),
    CULT("CU", _("The Cult"),  new Class[] { ShrineCapability.class }),
    TUNNEL("TU", _("The Tunnel"), new Class[] { TunnelCapability.class }),
    CORN_CIRCLES("CC", _("The Corn Circles"), new Class[] { CornCircleCapability.class }),
    PLAGUE("PL", _("The Plague") + " (" + _("tiles only") + ")", new Class[] { /*PlagueCapability.class*/ }),
    PHANTOM("PH", _("The Phantom"),  new Class[] { PhantomCapability.class }),
    FESTIVAL("FE", _("The Festival (10th an.)"), new Class[] { FestivalCapability.class }),
    HOUSES("LB", _("Little Buildings"), false),
    WIND_ROSE("WR", _("The Wind Rose"), new Class[] { WindRoseCapability.class }),

    //minis expansion line
    FLIER("FL", "#1 - " + _("The Flier"), new Class[] { FlierCapability.class }),
    MESSAGES("ME", "#2 - " + _("The Messages"), false),
    FERRIES("FR", "#3 - " + _("The Ferries"), false),
    GOLDMINES("GO", "#4 - " + _("The Goldmines"), false),
    MAGE_WITCH("MW", "#5 - " + _("Mage & Witch"), false),
    ROBBER("RO", "#6 - " + _("The Robber"), false),
    CORN_CIRCLES_II("C2", "#7 - " + _("The Corn circles II"), new Class[] { CornCircleCapability.class });

    //promo/one tile expansions
    //LA_PORXADA("PX", _("La porxada"), false),
    //SCHOOL("SC", _("The school"), false);

    String code;
    String label;
    boolean enabled = true;
    Class<? extends Capability>[] capabilities;

    Expansion(String code, String label) {
        this(code, label, null);
    }

    Expansion(String code, String label, boolean enabled) {
        this(code, label, null);
        this.enabled = enabled;
    }

    Expansion(String code, String label, Class<? extends Capability>[] capabilities) {
        this.code = code;
        this.label = label;
        this.capabilities = capabilities == null ? new Class[0] : capabilities;
    }

    public String getCode() {
        return code;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Class<? extends Capability>[] getCapabilities() {
        return capabilities;
    }

    @Override
    public String toString() {
        return label;
    }

    public static Expansion valueOfCode(String code) {
        for (Expansion exp : values()) {
            if (exp.code.equals(code)) return exp;
        }
        return null;
    }
}