package com.jcloisterzone.game;

import com.jcloisterzone.Expansion;

import io.vavr.collection.HashMap;
import io.vavr.collection.Map;

/*
 * TODO decouple this from single global just same as done for Tokens or TileModifier. Use just generic Rule interface and let
 * capabilities or expansion sets (some rules ale related to capabilities) to define their own rules
 *
 */
public enum Rule {
    // RANDOM_SEATING_ORDER(null, Boolean.class,  "Randomize seating order"),

    FARMERS(null, Boolean.class, null),
    ESCAPE(null, Boolean.class, null),

    // USE_PIG_HERDS_INDEPENDENTLY(Expansion.BASIC, Boolean.class, "Use pig herds independently (without T&B expansion)"),
    // TODO set with capability

    PRINCESS_ACTION("princess-action", String.class, new Object[] { "may", "must" }),
    FAIRY_PLACEMENT("fairy-placement", String.class, new Object[] { "next-follower", "on-tile" }),
    DRAGON_MOVEMENT("dragon-move", String.class, new Object[] { "before-scoring", "after-scoring" }),
    BARN_PLACEMENT("barn-placement", String.class, new Object[] { "not-occupied", "occupied" }),
    BAZAAR_NO_AUCTION("bazaar-no-auction", Boolean.class, null),
    HILL_TIEBREAKER("hill-tiebreaker", String.class, new Object[] { "at-least-one-follower", "number-of-followers" }),
    ESCAPE_VARIANT("espace-variant", String.class, new Object[] { "any-tile", "siege-tile" }),
    // TODO what about use second tile set?
    GQ11_PIG_HERD("gq11-pig-herd", String.class, new Object[] { "pig", "nothing" }),
    TUNNELIZE_OTHER_EXPANSIONS("tunnelize-other-expansions", Boolean.class, null),
    MORE_TUNNEL_TOKENS("more-tunnel-tokens", String.class, new Object[] { "3/2", "2/1", "1/1" }),
    FESTIVAL_RETURN("festival-return", String.class, new Object[] { "meeple", "follower" }),
    KEEP_MONASTERIES("keep-monasteries", String.class, new Object[] { "replace", "add" }),
    LABYRINTH_VARIANT("labyrinth-variant", String.class, new Object[] { "basic", "advanced" }),
    LITTLE_BUILDINGS_SCORING("little-buildings-scoring", String.class, new Object[] { "1/1/1", "3/2/1" }),
    KING_AND_ROBBER_BARON_SCORING("king-and-robber-baron-scoring", String.class, new Object[] { "default", "10/20", "15/40", "continuosly" }),
    TINY_CITY_SCORING("tiny-city-scoring", String.class, new Object[] { "4", "2" });


    String key;
    Class<?> type;
    Object[] allowedValues;

    private Rule(String key, Class<?> type, Object[] allowedValues) {
        this.key = key;
        this.type = type;
        this.allowedValues = allowedValues;
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

    public static Rule byKey(String key) {
        assert key != null;
        for (Rule r : Rule.values()) {
            if (key.equals(r.key)) {
                return r;
            }
        }
        throw new IllegalArgumentException("Unknow key");
    }

//    public static Map<Rule, Object> getDefaultRules() {
//        return HashMap.of(
//            FARMERS, true,
//            PIG_HERD_ON_GQ_FARM, true,
//            TUNNELIZE_ALL_EXPANSIONS, true,
//            MORE_TUNNEL_TOKENS, true,
//            ADVANCED_LABYRINTH, true
//        );
//    }

}