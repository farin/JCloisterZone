package com.jcloisterzone.rmi;

import java.io.Serializable;

import com.jcloisterzone.game.Snapshot;


public class ControllMessage implements Serializable {

	private static final long serialVersionUID = 840173772004529053L;

	private long clientId;
	private int protocolVersion;
	private Snapshot snapshot;

	public ControllMessage(long clientId, int protocolVersion, Snapshot snapshot) {
		this.clientId = clientId;
		this.protocolVersion = protocolVersion;
		this.snapshot = snapshot;
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

}
