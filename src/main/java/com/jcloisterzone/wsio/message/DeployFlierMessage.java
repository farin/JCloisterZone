package com.jcloisterzone.wsio.message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.wsio.WsMessageCommand;

/**
 * Because random dice is following deploy on flying machine seed should be also update.
 * For this reason just DeployMeepleMessage is not sufficient.
 * (DeployMeepleMessage would work but result of dice would be known before tile placement)
 */
@WsMessageCommand("DEPLOY_FLIER")
public class DeployFlierMessage extends DeployMeepleMessage implements WsInGameMessage, WsReplayableMessage, WsSeedMeesage {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private long seed;

    public DeployFlierMessage() {
    }

    public DeployFlierMessage(FeaturePointer pointer, String meepleId) {
        super(pointer, meepleId);
        assert pointer.getLocation() == Location.FLYING_MACHINE;

    }

    public long getSeed() {
        return seed;
    }

    public void setSeed(long seed) {
        this.seed = seed;
    }
}
