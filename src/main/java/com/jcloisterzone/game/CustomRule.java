package com.jcloisterzone.game;

import static com.jcloisterzone.ui.I18nUtils._;

import com.jcloisterzone.Expansion;



public enum CustomRule {
    TINY_CITY_2_POINTS(Expansion.BASIC, _("Tiny city is scored only for 2 points.")),
    FARM_CITY_SCORED_ONCE(Expansion.BASIC, _("Each city is scored with one farm only.")),   //each city can be scored only once

    CANNOT_PLACE_BUILDER_ON_VOLCANO(Expansion.PRINCESS_AND_DRAGON, _("The Builder and the pig cannot be placed on a volcano.")),
    PRINCESS_MUST_REMOVE_KNIGHT(Expansion.PRINCESS_AND_DRAGON, _("Princess MUST remove a knight from city.")),
    //DRAGON_MOVE_AFTER_SCORING(Expansion.PRINCESS_AND_DRAGON, _("Dragon movement after scoring.")),

    PIG_HERD_ON_GQ_FARM(Expansion.GQ11, _("The Pig herd is present on the farm tile.")),

    MULTI_BARN_ALLOWED(Expansion.ABBEY_AND_MAYOR, _("Allow direct barn placement on a farm where another barn is already placed.")),

    TUNNELIZE_ALL_EXPANSIONS(Expansion.TUNNEL, _("Apply tunnel rule on tunnel from other expansions.")),

    BAZAAR_NO_AUCTION(Expansion.BRIDGES_CASTLES_AND_BAZAARS, _("No bidding on bazaar tile. Each players just choose one tile."));

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

}