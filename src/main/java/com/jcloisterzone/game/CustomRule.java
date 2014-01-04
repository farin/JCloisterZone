package com.jcloisterzone.game;

import static com.jcloisterzone.ui.I18nUtils._;

import com.jcloisterzone.Expansion;



public enum CustomRule {
    RANDOM_SEATING_ORDER(Expansion.BASIC, _("Randomize seating order")),

    TINY_CITY_2_POINTS(Expansion.BASIC, _("Tiny city is scored only for 2 points.")),

    PRINCESS_MUST_REMOVE_KNIGHT(Expansion.PRINCESS_AND_DRAGON, _("Princess MUST remove a knight from city.") + "(RGG, ZMG)"),
    DRAGON_MOVE_AFTER_SCORING(Expansion.PRINCESS_AND_DRAGON, _("Dragon movement occurs after scoring.") + " (RGG)"),

    ESCAPE_RGG(Expansion.CATHARS, _("Escape cloister can be placed adjacent to any tile of a besieged city.") + " (RGG)"),

    PIG_HERD_ON_GQ_FARM(Expansion.GQ11, _("The Pig herd is present on the farm tile.")),

    MULTI_BARN_ALLOWED(Expansion.ABBEY_AND_MAYOR, _("Allow direct barn placement on a farm where another barn is already placed.")),

    TUNNELIZE_ALL_EXPANSIONS(Expansion.TUNNEL, _("Apply tunnel rule on tunnels from other expansions.")),

    BAZAAR_NO_AUCTION(Expansion.BRIDGES_CASTLES_AND_BAZAARS, _("No bazaar bidding. Each players just choose one tile."));

    String label;
    Expansion expansion;

    private CustomRule(Expansion expansion, String label) {
        this.expansion = expansion;
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public Expansion getExpansion() {
        return expansion;
    }

    @Override
    public String toString() {
        return label;
    }

    public static CustomRule[] defaultEnabled() {
        return new CustomRule[] { PIG_HERD_ON_GQ_FARM, TUNNELIZE_ALL_EXPANSIONS };
    }

}