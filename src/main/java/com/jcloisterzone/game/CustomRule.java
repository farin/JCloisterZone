package com.jcloisterzone.game;

import static com.jcloisterzone.ui.I18nUtils._;

import java.util.EnumMap;

import com.jcloisterzone.Expansion;



public enum CustomRule {
    RANDOM_SEATING_ORDER(null, Boolean.class,  _("Randomize seating order")),

    TINY_CITY_2_POINTS(Expansion.BASIC, Boolean.class, _("Tiny city is scored only for 2 points.")),

    PRINCESS_MUST_REMOVE_KNIGHT(Expansion.PRINCESS_AND_DRAGON, Boolean.class, _("Princess MUST remove a knight from city.") + "(RGG, ZMG)"),
    DRAGON_MOVE_AFTER_SCORING(Expansion.PRINCESS_AND_DRAGON, Boolean.class, _("Dragon movement occurs after scoring.") + " (RGG)"),

    ESCAPE_RGG(Expansion.CATHARS, Boolean.class, _("Escape cloister can be placed adjacent to any tile of a besieged city.") + " (RGG)"),

    PIG_HERD_ON_GQ_FARM(Expansion.GQ11, Boolean.class, _("The Pig herd is present on the farm tile.")),

    MULTI_BARN_ALLOWED(Expansion.ABBEY_AND_MAYOR, Boolean.class, _("Allow direct barn placement on a farm where another barn is already placed.")),

    TUNNELIZE_ALL_EXPANSIONS(Expansion.TUNNEL, Boolean.class, _("Apply tunnel rule on tunnels from other expansions.")),

    BAZAAR_NO_AUCTION(Expansion.BRIDGES_CASTLES_AND_BAZAARS, Boolean.class, _("No bazaar bidding. Each players just choose one tile.")),

    KEEP_CLOISTERS(Expansion.GERMAN_MONASTERIES, Boolean.class, _("Keep basic cloisters in the game.")),

    BULDINGS_DIFFERENT_VALUE(Expansion.LITTLE_BUILDINGS, Boolean.class, _("Add 3/2/1 points for tower/house/shed.")),

    CLOCK_PLAYER_TIME(null, Integer.class, null);


    String label;
    Class<?> type;
    Expansion expansion;

    private CustomRule(Expansion expansion, Class<?> type, String label) {
        this.expansion = expansion;
        this.type = type;
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public Expansion getExpansion() {
        return expansion;
    }

    public Class<?> getType() {
        return type;
    }

    public Object unpackValue(String value) {
        if (type.equals(Boolean.class)) {
            return Boolean.valueOf(value);
        } else if (type.equals(Integer.class)) {
            return Integer.valueOf(value);
        } else if (type.equals(String.class)) {
            return value;
        } else {
            throw new IllegalArgumentException("Unsupported type");
        }
    }

    public static EnumMap<CustomRule, Object> getDefaultRules() {
        EnumMap<CustomRule, Object> defaultRules = new EnumMap<>(CustomRule.class);
        defaultRules.put(PIG_HERD_ON_GQ_FARM, true);
        defaultRules.put(TUNNELIZE_ALL_EXPANSIONS, true);
        return defaultRules;
    }

}