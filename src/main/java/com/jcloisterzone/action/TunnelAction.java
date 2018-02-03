package com.jcloisterzone.action;

import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.game.Token;
import com.jcloisterzone.ui.annotations.LinkedGridLayer;
import com.jcloisterzone.ui.annotations.LinkedImage;
import com.jcloisterzone.ui.grid.layer.FeatureAreaLayer;
import com.jcloisterzone.wsio.message.PlaceTokenMessage;
import com.jcloisterzone.wsio.message.WsInGameMessage;

import io.vavr.collection.Set;


@LinkedImage("actions/tunnel")
@LinkedGridLayer(FeatureAreaLayer.class)
public class TunnelAction extends AbstractPlayerAction<FeaturePointer> implements SelectFeatureAction {

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
    public WsInGameMessage select(FeaturePointer ptr) {
        return new PlaceTokenMessage(token, ptr);
    }

    @Override
    public String toString() {
        return "place tunnel";
    }

}
