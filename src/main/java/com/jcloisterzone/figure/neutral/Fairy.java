package com.jcloisterzone.figure.neutral;

import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.board.pointer.MeeplePointer;
import com.jcloisterzone.event.NeutralFigureMoveEvent;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.game.Game;

public class Fairy extends NeutralFigure<Position> {

    private static final long serialVersionUID = 4710402383462428260L;

    private Follower nextTo;

    public Fairy(Game game) {
        super(game);
    }

    public Follower getNextTo() {
        return nextTo;
    }

    public void setNextTo(Follower nextTo) {
        this.nextTo = nextTo;
    }

    public void deploy(MeeplePointer at) {
        FeaturePointer origin = getFeaturePointer();
        setFeaturePointer(at.asFeaturePointer());
        setNextTo((Follower) game.getMeeple(at));
        game.post(new NeutralFigureMoveEvent(game.getActivePlayer(), this, origin, at));
    }


}
