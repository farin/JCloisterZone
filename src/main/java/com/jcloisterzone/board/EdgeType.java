package com.jcloisterzone.board;

/**
 * Enumerates all possible edge types. An edge can contain a field, a river, a city or a road. An unknown type is also
 * provided.
 */
public enum EdgeType {
    ROAD(0b0001, 'R'),
    CITY(0b0010, 'C'),
    FIELD(0b0100, 'F'),
    RIVER(0b1000, 'I'),
    CITY_GATE(0b0101, 'G'),
    UNKNOWN(0b1111, '?');

    private int mask;
    private char ch;

    /**
     * Instantiates a new {@code EdgeType}.
     *
     * @param mask the mask for the new instance
     * @param ch an id for the new instance
     */
    EdgeType(int mask, char ch) {
        this.mask = mask;
        this.ch = ch;
    }

    /**
     * Gets the {@code mask} of the instance.
     * @return the {@code mask} of the instance
     */
    public int getMask() {
        return mask;
    }

    /**
     * Gets the {@code ch} (identifier) of the instance.
     * @return the {@code ch} (identifier) of the instance
     */
    public char asChar() {
        return ch;
    }

    /**
     * Gets the instance with the given {@code mask}.
     * @param mask the mask to search
     * @return the instance with the given {@code mask}
     * @throws IllegalArgumentException if {@code mask} does not match any instance
     */
    static EdgeType forMask(int mask) {
        for (EdgeType e : values()) {
            if (e.mask == mask) return e;
        }
        throw new IllegalArgumentException("Invalid Edge mask " + mask);
    }

    /**
     * Gets the instance with the given {@code ch} (identifier).
     * @param ch the ch to search
     * @return the instance with the given {@code ch} (identifier)
     * @throws IllegalArgumentException if {@code ch} does not match any instance
     */
    static EdgeType forChar(int ch) {
        for (EdgeType e : values()) {
            if (e.ch == ch) return e;
        }
        throw new IllegalArgumentException("Unknown edge " + ch);
    }
}