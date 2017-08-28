package com.jcloisterzone;

import com.jcloisterzone.game.Token;

public enum TradeGoods {

    WINE(Token.WINE),
    CLOTH(Token.CLOTH),
    GRAIN(Token.GRAIN);

    Token token;

    private TradeGoods(Token token) {
        this.token = token;
    }

    public Token getToken() {
        return token;
    }

}
