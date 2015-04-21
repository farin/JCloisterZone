package com.jcloisterzone.wsio.server;

import com.jcloisterzone.wsio.message.ClientUpdateMessage.ClientState;

public class RemoteClient {
    private String sessionId, name;
    private ClientState state;

    public RemoteClient(String sessionId, String name, ClientState state) {
        super();
        this.sessionId = sessionId;
        this.name = name;
        this.state = state;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }



	public ClientState getState() {
		return state;
	}

	public void setState(ClientState state) {
		this.state = state;
	}

	@Override
	public String toString() {
		return "RemoteClient("+sessionId+", "+name+", "+state+")";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((sessionId == null) ? 0 : sessionId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RemoteClient other = (RemoteClient) obj;
		if (sessionId == null) {
			if (other.sessionId != null)
				return false;
		} else if (!sessionId.equals(other.sessionId))
			return false;
		return true;
	}
}