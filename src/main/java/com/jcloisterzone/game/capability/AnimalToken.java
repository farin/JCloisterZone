package com.jcloisterzone.game.capability;

import com.jcloisterzone.game.Token;

public enum AnimalToken implements Token {
    BIGTOP_1(1, 1),
    BIGTOP_3(3, 4),
    BIGTOP_4(4, 5),
    BIGTOP_5(5, 3),
    BIGTOP_6(6, 2),
    BIGTOP_7(7, 1);

    public int points;
    public int count;

    AnimalToken(int points, int count) {
        this.points = points;
        this.count = count;
    }
}
