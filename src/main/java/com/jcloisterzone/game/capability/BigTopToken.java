package com.jcloisterzone.game.capability;

import com.jcloisterzone.game.Token;

public enum BigTopToken implements Token {
    BIGTOP_1,
    BIGTOP_2,
    BIGTOP_3,
    BIGTOP_4,
    BIGTOP_5,
    BIGTOP_6,
    BIGTOP_7;
	
    public int getValue() {
        return this.ordinal() + 1;
    }
}
