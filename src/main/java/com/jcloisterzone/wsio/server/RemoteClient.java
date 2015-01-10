package com.jcloisterzone.wsio.server;

public class RemoteClient {
    private String sessionId, name, status;

    public RemoteClient(String sessionId, String name) {
        super();
        this.sessionId = sessionId;
        this.name = name;
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

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	@Override
	public String toString() {
		return "RemoteClient("+sessionId+", "+name+", "+status+")";
	}
}