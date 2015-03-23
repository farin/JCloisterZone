package com.jcloisterzone.wsio;

import com.jcloisterzone.wsio.message.WsMessage;

public class MutedConnection implements Connection {

	private final Connection conn;

	public MutedConnection(Connection conn) {
		this.conn = conn;
	}

	@Override
	public void send(WsMessage arg) {
	}

	@Override
	public void close() {
	}

	@Override
	public String getSessionId() {
		return conn.getSessionId();
	}

	@Override
	public String getNickname() {
		return conn.getNickname();
	}

	public Connection getConnection() {
		return conn;
	}

	@Override
	public void reconnect(String gameId) {
		conn.reconnect(gameId);
	}

	@Override
	public void stopReconnecting() {
		conn.stopReconnecting();
	}

	@Override
	public boolean isClosed() {
		return conn.isClosed();
	}

}
