package com.jcloisterzone;

import static com.jcloisterzone.game.Capability.*;
import static com.jcloisterzone.ui.I18nUtils._;

import com.jcloisterzone.game.Capability;

public enum Expansion {
    //Basic sets
    BASIC("BA", _("Basic game"), new Capability[] { FARM_PLACEMENT }),
    WINTER("WI", _("Winter Edition")),
    WHEEL_OF_FORTUNE("WF", _("Wheel of Fortune"), false),

    //Big expansions
    INNS_AND_CATHEDRALS("IC", _("Inns & Cathedrals"),
            new Capability[] { BIG_FOLLOWER ,INN, CATHEDRAL }),
    TRADERS_AND_BUILDERS("TB", _("Traders & Builders"),
            new Capability[] { PIG, BUILDER, CLOTH_WINE_GRAIN }),
    PRINCESS_AND_DRAGON("DG", _("The Princess & the Dragon"),
            new Capability[] { FAIRY, DRAGON, PORTAL, PRINCESS }),
    TOWER("TO", _("The Tower"),
            new Capability[] { Capability.TOWER }),
    ABBEY_AND_MAYOR("AM", _("Abbey & Mayor"),
            new Capability[] { ABBEY, WAGON, MAYOR, BARN }),
    CATAPULT("CA", _("The Catapult") + " (" + _("tiles only") + ")"),
    BRIDGES_CASTLES_AND_BAZAARS("BB", _("Bridges, Castles and Bazaars"),
            new Capability[] { BRIDGE, CASTLE, BAZAAR }),

    //Small expansion
    KING_AND_SCOUT("KS", _("King and Scout"), new Capability[] { KING_SCOUT}),
    RIVER("R1", _("The River"), new Capability[] { Capability.RIVER }),
    RIVER_II("R2", _("The River II"), new Capability[] { Capability.RIVER }),
    CATHARS("SI", _("The Cathars / Siege"), new Capability[] { SIEGE }),
    COUNT("CO", _("The Count of Carcassonne") + " (" + _("tiles only") + ")"),
    GQ11("GQ", _("The Mini Expansion (GQ11)")),
    CULT("CU", _("The Cult"),  new Capability[] { SHRINE }),
    TUNNEL("TU", _("The Tunnel"), new Capability[] { Capability.TUNNEL }),
    CORN_CIRCLES("CC", _("The Corn Circles"), new Capability[] { CORN_CIRCLE }),
    PLAGUE("PL", _("The Plague") + " (" + _("tiles only") + ")", new Capability[] { Capability.PLAGUE }),
    PHANTOM("PH", _("The Phantom"),  new Capability[] { Capability.PHANTOM }),
    FESTIVAL("FE", _("The Festival (10th an.)"), new Capability[] { Capability.FESTIVAL }),
    HOUSES("LB", _("Little Buildings"), false),
    WIND_ROSE("WR", _("The Wind Rose"), false),

    //minis expansion line
    FLIER("FL", "#1 - " + _("The Flier"), new Capability[] { Capability.FLIER }),
    MESSAGES("ME", "#2 - " + _("The Messages"), false),
    FERRIES("FR", "#3 - " + _("The Ferries"), false),
    GOLDMINES("GO", "#4 - " + _("The Goldmines"), false),
    MAGE_WITCH("MW", "#5 - " + _("Mage & Witch"), false),
    ROBBER("RO", "#6 - " + _("The Robber"), false),
    CORN_CIRCLES_II("C2", "#7 - " + _("The Corn circles II"), new Capability[] { CORN_CIRCLE });

    //promo/one tile expansions
    //LA_PORXADA("PX", _("La porxada"), false),
    //SCHOOL("SC", _("The school"), false);

    String code;
    String label;
    boolean enabled = true;
    Capability[] capabilities;

    Expansion(String code, String label) {
        this(code, label, null);
    }

    Expansion(String code, String label, boolean enabled) {
        this(code, label, null);
        this.enabled = enabled;
    }

    Expansion(String code, String label, Capability[] capabilities) {
        this.code = code;
        this.label = label;
        this.capabilities = capabilities == null ? new Capability[0] : capabilities;
    }

    public String getCode() {
        return code;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Capability[] getCapabilities() {
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