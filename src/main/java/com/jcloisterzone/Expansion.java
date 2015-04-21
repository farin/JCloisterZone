package com.jcloisterzone;

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
import com.jcloisterzone.game.capability.GermanMonasteriesCapability;
import com.jcloisterzone.game.capability.InnCapability;
import com.jcloisterzone.game.capability.KingAndRobberBaronCapability;
import com.jcloisterzone.game.capability.LittleBuildingsCapability;
import com.jcloisterzone.game.capability.MageAndWitchCapability;
import com.jcloisterzone.game.capability.MayorCapability;
import com.jcloisterzone.game.capability.PhantomCapability;
import com.jcloisterzone.game.capability.PigCapability;
import com.jcloisterzone.game.capability.PortalCapability;
import com.jcloisterzone.game.capability.PrincessCapability;
import com.jcloisterzone.game.capability.RiverCapability;
import com.jcloisterzone.game.capability.ShrineCapability;
import com.jcloisterzone.game.capability.SiegeCapability;
import com.jcloisterzone.game.capability.TowerCapability;
import com.jcloisterzone.game.capability.TunnelCapability;
import com.jcloisterzone.game.capability.WagonCapability;
import com.jcloisterzone.game.capability.WindRoseCapability;

import static com.jcloisterzone.ui.I18nUtils._;

@SuppressWarnings("unchecked")
public enum Expansion {
    //Basic sets
    BASIC("BA", _("Basic game")),
    WHEEL_OF_FORTUNE("WF", _("Wheel of Fortune"), false),

    //Winter branch
    WINTER("WI", _("Winter Edition")),
    GINGERBREAD_MAN("GM", _("The Gingerbread man"), false),

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
    HILLS_AND_SHEEP("HS", _("Hills and sheep"), false),

    //Small expansion
    KING_AND_ROBBER_BARON("KR", _("King and Robber Baron"), new Class[] { KingAndRobberBaronCapability.class }),
    RIVER("R1", _("The River"), new Class[] { RiverCapability.class }),
    RIVER_II("R2", _("The River II"), new Class[] { RiverCapability.class }),
    CATHARS("SI", _("The Cathars / Siege"), new Class[] { SiegeCapability.class }),
    BESIEGERS("BE", _("The Besiegers"), new Class[] { SiegeCapability.class }),
    COUNT("CO", _("The Count of Carcassonne") + " (" + _("tiles only") + ")", new Class[] { CountCapability.class }),
    GQ11("GQ", _("The Mini Expansion (GQ11)")),
    CULT("CU", _("The Cult"),  new Class[] { ShrineCapability.class }),
    TUNNEL("TU", _("The Tunnel"), new Class[] { TunnelCapability.class }),
    CORN_CIRCLES("CC", _("The Corn Circles"), new Class[] { CornCircleCapability.class }),
    //PLAGUE("PL", _("The Plague") + " (" + _("tiles only") + ")", new Class[] { PlagueCapability.class }),
    PLAGUE("PL", _("The Plague"), false),
    PHANTOM("PH", _("The Phantom"),  new Class[] { PhantomCapability.class }),
    FESTIVAL("FE", _("The Festival (10th an.)"), new Class[] { FestivalCapability.class }),
    LITTLE_BUILDINGS("LB", _("Little Buildings"), new Class[] { LittleBuildingsCapability.class }),
    WIND_ROSE("WR", _("The Wind Rose"), new Class[] { WindRoseCapability.class }),
    GERMAN_MONASTERIES("GM", _("The German Monasteries"), new Class[] { GermanMonasteriesCapability.class }),

    //minis expansion line
    FLIER("FL", "#1 - " + _("The Flier"), new Class[] { FlierCapability.class }),
    MESSAGES("ME", "#2 - " + _("The Messages"), false),
    FERRIES("FR", "#3 - " + _("The Ferries"), false),
    GOLDMINES("GO", "#4 - " + _("The Goldmines"), false),
    MAGE_AND_WITCH("MW", "#5 - " + _("Mage & Witch"), new Class[] { MageAndWitchCapability.class }),
    ROBBER("RO", "#6 - " + _("The Robber"), false),
    CORN_CIRCLES_II("C2", "#7 - " + _("The Corn circles II"), new Class[] { CornCircleCapability.class }),

    //promo/one tile expansions
    SCHOOL("SC", _("The school"), false),
    LA_PORXADA("PX", _("La porxada"), false);

    String code;
    String label;
    boolean implemented = true;
    Class<? extends Capability>[] capabilities;

    Expansion(String code, String label) {
        this(code, label, null);
    }

    Expansion(String code, String label, boolean implemented) {
        this(code, label, null);
        this.implemented = implemented;
    }

    Expansion(String code, String label, Class<? extends Capability>[] capabilities) {
        this.code = code;
        this.label = label;
        this.capabilities = capabilities == null ? new Class[0] : capabilities;
    }

    public String getCode() {
        return code;
    }

    public boolean isImplemented() {
        return implemented;
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
