package com.jcloisterzone.rmi;

import java.io.Serializable;

import com.jcloisterzone.game.PlayerSlot;
import com.jcloisterzone.game.Snapshot;


public class ControllMessage implements Serializable {

    private static final long serialVersionUID = 840173772004529053L;

    private long clientId;
    private int protocolVersion;
    private Snapshot snapshot;
    private PlayerSlot[] slots;

    public ControllMessage(long clientId, int protocolVersion, Snapshot snapshot, PlayerSlot[] slots) {
        this.clientId = clientId;
        this.protocolVersion = protocolVersion;
        this.snapshot = snapshot;
        this.slots = slots;
    }

    public long getClientId() {
        return clientId;
    }

    public int getProtocolVersion() {
        return protocolVersion;
    }

    public Snapshot getSnapshot() {
        return snapshot;
    }

    public PlayerSlot[] getSlots() {
        return slots;
    }

}
