package com.jcloisterzone.action;

import com.jcloisterzone.game.Game;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.wsio.message.CommitMessage;

import io.vavr.collection.HashSet;

public class ConfirmAction extends PlayerAction<Boolean> {

    private static final long serialVersionUID = 1L;

    public ConfirmAction() {
        super(HashSet.empty());
    }

    @Override
    public void perform(GameController gc, Boolean target) {
        Game game = gc.getGame();
        gc.getConnection().send(new CommitMessage());
    }

    @Override
    public String toString() {
        return "confirm actions";
    }
}
