package com.jcloisterzone.action;

import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.ui.annotations.LinkedImage;

import io.vavr.collection.Set;


@LinkedImage("actions/tunnel")
public class TunnelAction extends SelectFeatureAction {

    private final boolean secondTunnelPiece;

    public TunnelAction(Set<FeaturePointer> ptrs, boolean secondTunnelPiece) {
        super(ptrs);
        this.secondTunnelPiece = secondTunnelPiece;
    }

    public boolean isSecondTunnelPiece() {
        return secondTunnelPiece;
    }

    @Override
    public void perform(GameController gc, FeaturePointer bp) {
        //server.placeTunnelPiece(bp, secondTunnelPiece);
        //TODO
    }

    @Override
    public String toString() {
        return "place tunnel";
    }

}
