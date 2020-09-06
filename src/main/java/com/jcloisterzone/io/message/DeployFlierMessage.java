package com.jcloisterzone.io.message;

import com.jcloisterzone.io.MessageCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.pointer.FeaturePointer;

/**
 * Because random dice is following deploy on flying machine seed should be also update.
 * For this reason just DeployMeepleMessage is not sufficient.
 * (DeployMeepleMessage would work but result of dice would be known before tile placement)
 */
@MessageCommand("DEPLOY_FLIER")
public class DeployFlierMessage extends DeployMeepleMessage implements ReplayableMessage, SaltMessage {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private String salt;

    public DeployFlierMessage() {
    }

    public DeployFlierMessage(FeaturePointer pointer, String meepleId) {
        super(pointer, meepleId);
        assert pointer.getLocation() == Location.FLYING_MACHINE;
    }

    @Override
    public String getSalt() {
        return salt;
    }

    @Override
    public void setSalt(String salt) {
        this.salt = salt;
    }
}
