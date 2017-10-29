package com.jcloisterzone.figure.neutral;

import com.jcloisterzone.board.pointer.BoardPointer;

public class Fairy extends NeutralFigure<BoardPointer> {

    private static final long serialVersionUID = 4710402383462428260L;

    public Fairy(String id) {
        super(id);
    }

//    public Follower getNextTo() {
//        return nextTo;
//    }
//
//    public void setNextTo(Follower nextTo) {
//        this.nextTo = nextTo;
//    }
//
//    @Override
//    public void deploy(BoardPointer at) {
//        if (at instanceof MeeplePointer) {
//            //new rules
//            setNextTo((Follower) game.getMeeple((MeeplePointer) at));
//        } else if (at == null) {
//            setNextTo(null);;
//        }
//        super.deploy(at);
//    }
}
