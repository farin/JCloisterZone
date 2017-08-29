package com.jcloisterzone.action;

import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.game.Token;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.ui.annotations.LinkedImage;
import com.jcloisterzone.wsio.message.PlaceTokenMessage;

import io.vavr.collection.Set;


@LinkedImage("actions/tunnel")
public class TunnelAction extends SelectFeatureAction {

    private final Token token;

    public TunnelAction(Set<FeaturePointer> options, Token token) {
        super(options);
        assert token.isTunnel();
        this.token = token;
    }

    public Token getToken() {
        return token;
    }

    @Override
    public void perform(GameController gc, FeaturePointer ptr) {
        gc.getConnection().send(new PlaceTokenMessage(gc.getGameId(), token, ptr));
    }

    @Override
    public String toString() {
        return "place tunnel";
    }

}
