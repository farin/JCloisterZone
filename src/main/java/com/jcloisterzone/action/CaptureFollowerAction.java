package com.jcloisterzone.action;

import com.jcloisterzone.board.pointer.MeeplePointer;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.ui.annotations.LinkedImage;
import com.jcloisterzone.wsio.message.CaptureFollowerMessage;

import io.vavr.collection.Set;

@LinkedImage("actions/takeprisoner")
public class CaptureFollowerAction extends SelectFollowerAction {

    public CaptureFollowerAction(Set<MeeplePointer> options) {
        super(options);
    }

    @Override
    public void perform(GameController gc, MeeplePointer mp) {
        gc.getConnection().send(new CaptureFollowerMessage(gc.getGameId(), mp));
    }

    @Override
    public String toString() {
        return "take prisoner";
    }


}
