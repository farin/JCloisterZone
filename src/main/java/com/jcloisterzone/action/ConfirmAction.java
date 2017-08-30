package com.jcloisterzone.action;

import com.jcloisterzone.wsio.message.CommitMessage;
import com.jcloisterzone.wsio.message.WsInGameMessage;

import io.vavr.collection.HashSet;

public class ConfirmAction extends PlayerAction<Boolean> {

    private static final long serialVersionUID = 1L;

    public ConfirmAction() {
        super(HashSet.empty());
    }

    @Override
    public WsInGameMessage select(Boolean target) {
        return new CommitMessage();
    }

    @Override
    public String toString() {
        return "confirm actions";
    }
}
