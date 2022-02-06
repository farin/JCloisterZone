package com.jcloisterzone.game.capability;

import com.jcloisterzone.game.Token;

public enum SheepToken implements Token {
    SHEEP_1X,
    SHEEP_2X,
    SHEEP_3X,
    SHEEP_4X,
    WOLF;

    public int sheepCount() {
        if (this == WOLF) {
            throw new IllegalStateException();
        }
        return this.ordinal() + 1;
    }
}
