package com.jcloisterzone.board;

public enum EdgeType {
    ROAD(1, 'R'),
    CITY(2, 'C'),
    FARM(4, 'F'),
    RIVER(8, 'I'),
    UNKNOWN(15, '?');

    private int mask;
    private char ch;

    EdgeType(int mask, char ch) {
        this.mask = mask;
        this.ch = ch;
    }

    public int getMask() {
        return mask;
    }

    public char asChar() {
        return ch;
    }

    static EdgeType forMask(int mask) {
        for (EdgeType e : values()) {
            if (e.mask == mask) return e;
        }
        throw new IllegalArgumentException("Invalid Edge mask " + mask);
    }

    static EdgeType forChar(int ch) {
        for (EdgeType e : values()) {
            if (e.ch == ch) return e;
        }
        throw new IllegalArgumentException("Unknow edge " + ch);
    }
}
