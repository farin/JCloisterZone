package com.jcloisterzone.event;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.capability.BridgeCapability;

public class BridgeEvent extends FeatureEvent implements Undoable {

    public static final int DEPLOY = 1;
    public static final int REMOVE = 2;

    boolean forced; //if force by tile placement

    public BridgeEvent(int type, Player triggeringPlayer, Position position, Location location) {
        super(type, triggeringPlayer, new FeaturePointer(position, location));
    }

    public boolean isForced() {
        return forced;
    }

    public void setForced(boolean forced) {
        this.forced = forced;
    }

    @Override
    public void undo(Game game) {
        switch (getType()) {
        case DEPLOY:
            BridgeCapability bCap = game.getCapability(BridgeCapability.class);
            if (getTriggeringPlayer() != null) {
                bCap.increaseBridges(getTriggeringPlayer());
            }
            bCap.undoDeployBridge(getPosition(), getLocation());
            break;
        default:
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public Event getInverseEvent() {
        switch (getType()) {
        case DEPLOY:
            return new BridgeEvent(REMOVE, getTriggeringPlayer(), getPosition(), getLocation());
        default:
            throw new UnsupportedOperationException();
        }
    }

}
