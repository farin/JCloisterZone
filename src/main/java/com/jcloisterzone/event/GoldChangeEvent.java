package com.jcloisterzone.event;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.capability.GoldminesCapability;

public class GoldChangeEvent extends PlayEvent implements Undoable {

    private final Position pos;
    private final int prevCount;
    private final int currCount;

    public GoldChangeEvent(Player triggeringPlayer, Position pos, int prevCount, int currCount) {
        super(triggeringPlayer, null);
        this.pos = pos;
        this.prevCount = prevCount;
        this.currCount = currCount;
    }

    public Position getPos() {
        return pos;
    }

    public int getCurrCount() {
        return currCount;
    }

    public int getPrevCount() {
        return prevCount;
    }

    @Override
    public void undo(Game game) {
        GoldminesCapability goCap = game.getCapability(GoldminesCapability.class);
        goCap.setGoldCount(pos, prevCount);
    }

    @Override
    public Event getInverseEvent() {
        return new GoldChangeEvent(null, pos, currCount, prevCount);
    }
}
