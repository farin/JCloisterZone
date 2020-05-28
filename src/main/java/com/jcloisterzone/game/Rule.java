package com.jcloisterzone.game;

import static com.jcloisterzone.ui.I18nUtils._tr;

import com.jcloisterzone.Expansion;

import io.vavr.collection.HashMap;
import io.vavr.collection.Map;

/*
 * TODO decouple this from single global just same as done for Tokens or TileModifier. Use just generic Rule interface and let
 * capabilities or expansion sets (some rules ale related to capabilities) to define their own rules
 *
 */
public enum Rule {
    RANDOM_SEATING_ORDER(null, Boolean.class,  _tr("Randomize seating order")),

    FARMERS(Expansion.BASIC, Boolean.class, _tr("Play farmers")),
    USE_PIG_HERDS_INDEPENDENTLY(Expansion.BASIC, Boolean.class, _tr("Use pig herds independently (without T&B expansion)")),

    PRINCESS_MUST_REMOVE_KNIGHT(Expansion.PRINCESS_AND_DRAGON, Boolean.class, _tr("Princess MUST remove a knight from city.") + " (RGG, ZMG)"),
    DRAGON_MOVE_AFTER_SCORING(Expansion.PRINCESS_AND_DRAGON, Boolean.class, _tr("Dragon movement occurs after scoring.") + " (RGG)"),
    FAIRY_ON_TILE(Expansion.PRINCESS_AND_DRAGON, Boolean.class, _tr("Place fairy on the tile. (instead of next to a follower)") + " (RGG, ZMG)"),

    MULTI_BARN_ALLOWED(Expansion.ABBEY_AND_MAYOR, Boolean.class, _tr("Allow direct barn placement on a farm where another barn is already placed.")),

    BAZAAR_NO_AUCTION(Expansion.BRIDGES_CASTLES_AND_BAZAARS, Boolean.class, _tr("No bazaar bidding. Each player just chooses one tile.")),

    ON_HILL_NUMBER_TIEBREAKER(Expansion.HILLS_AND_SHEEP, Boolean.class, _tr("Count number of followers on hill as tiebreaker.") + " [house rule]"),

    ESCAPE_RGG(Expansion.CATHARS, Boolean.class, _tr("Escape cloister can be placed adjacent to any tile of a besieged city.") + " (RGG)"),

    PIG_HERD_ON_GQ_FARM(Expansion.GQ11, Boolean.class, _tr("The Pig herd is present on the farm tile.") + " [house rule]"),

    TUNNELIZE_ALL_EXPANSIONS(Expansion.TUNNEL, Boolean.class, _tr("Apply tunnel rule on tunnels from other expansions.")),
    MORE_TUNNEL_TOKENS(Expansion.TUNNEL, Boolean.class, _tr("Assign 3/2 tunnel set in game of two/three players.")),

    FESTIVAL_FOLLOWER_ONLY(Expansion.FESTIVAL, Boolean.class, _tr("Only follower can be returned by festival (instead of any figure)") + " (RGG)"),

    KEEP_CLOISTERS(Expansion.GERMAN_MONASTERIES, Boolean.class, _tr("Keep basic cloisters in the game.")),

    BULDINGS_DIFFERENT_VALUE(Expansion.LITTLE_BUILDINGS, Boolean.class, _tr("Add 3/2/1 points for tower/house/shed.")),

    ADVANCED_LABYRINTH(Expansion.LABYRINTH, Boolean.class, _tr("Advanced labyrinth variant")),

    // legacy rules
    TINY_CITY_2_POINTS(Expansion.BASIC, Boolean.class, _tr("Tiny city is scored only for 2 points.")),

    CLOCK_PLAYER_TIME(null, Integer.class, null);


    String label;
    Class<?> type;
    Expansion expansion;

    private Rule(Expansion expansion, Class<?> type, String label) {
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
            return Double.valueOf(value).intValue();
        } else if (type.equals(String.class)) {
            return value;
        } else {
            throw new IllegalArgumentException("Unsupported type");
        }
    }

    public static Map<Rule, Object> getDefaultRules() {
        return HashMap.of(
            FARMERS, true,
            PIG_HERD_ON_GQ_FARM, true,
            TUNNELIZE_ALL_EXPANSIONS, true,
            MORE_TUNNEL_TOKENS, true,
            ADVANCED_LABYRINTH, true
        );
    }

}