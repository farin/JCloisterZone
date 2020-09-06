package com.jcloisterzone.game;

/*
 * TODO decouple this from single global just same as done for Tokens or TileModifier. Use just generic Rule interface and let
 * capabilities or expansion sets (some rules ale related to capabilities) to define their own rules
 *
 */
public enum Rule {
    FARMERS(null, Boolean.class, null),
    ESCAPE(null, Boolean.class, null),

    PRINCESS_ACTION("princess-action", String.class, new Object[] { "may", "must" }),
    FAIRY_PLACEMENT("fairy-placement", String.class, new Object[] { "next-follower", "on-tile" }),
    DRAGON_MOVEMENT("dragon-move", String.class, new Object[] { "before-scoring", "after-scoring" }),
    WAGON_MOVE("wagon-move", String.class, new Object[] { "C1", "C2" }),
    BARN_PLACEMENT("barn-placement", String.class, new Object[] { "not-occupied", "occupied" }),
    BAZAAR_NO_AUCTION("bazaar-no-auction", Boolean.class, null),
    HILL_TIEBREAKER("hill-tiebreaker", String.class, new Object[] { "at-least-one-follower", "number-of-followers" }),
    ESCAPE_VARIANT("espace-variant", String.class, new Object[] { "any-tile", "siege-tile" }),
    GQ11_PIG_HERD("gq11-pig-herd", String.class, new Object[] { "pig", "nothing" }),
    TUNNELIZE_OTHER_EXPANSIONS("tunnelize-other-expansions", Boolean.class, null),
    MORE_TUNNEL_TOKENS("more-tunnel-tokens", String.class, new Object[] { "3/2", "2/1", "1/1" }),
    FESTIVAL_RETURN("festival-return", String.class, new Object[] { "meeple", "follower" }),
    KEEP_MONASTERIES("keep-monasteries", String.class, new Object[] { "replace", "merge" }),
    LABYRINTH_VARIANT("labyrinth-variant", String.class, new Object[] { "basic", "advanced" }),
    LITTLE_BUILDINGS_SCORING("little-buildings-scoring", String.class, new Object[] { "1/1/1", "3/2/1" }),
    KING_AND_ROBBER_SCORING("king-and-robber-scoring", String.class, new Object[] { "default", "10/20", "15/40", "continuously" }),
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
}