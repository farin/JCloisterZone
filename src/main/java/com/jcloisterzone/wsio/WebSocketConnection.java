package com.jcloisterzone.wsio;

import java.net.URI;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.exceptions.WebsocketNotConnectedException;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcloisterzone.bugreport.ReportingTool;
import com.jcloisterzone.config.Config;
import com.jcloisterzone.wsio.message.HelloMessage;
import com.jcloisterzone.wsio.message.JoinGameMessage;
import com.jcloisterzone.wsio.message.PingMessage;
import com.jcloisterzone.wsio.message.RmiMessage;
import com.jcloisterzone.wsio.message.WelcomeMessage;
import com.jcloisterzone.wsio.message.WsMessage;

public class WebSocketConnection implements Connection {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());
    private ReportingTool reportingTool;

    private MessageParser parser = new MessageParser();
    private WebSocketClientImpl ws;
    private URI uri;
    private final MessageListener listener;

    private String sessionId;
    private String clientId;
    private String secret; //TODO will be used for message signing
    private String nickname;

    private boolean closedByUser;
    private int pingInterval = 0;
    private String maintenance;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> pingFuture;
    private ScheduledFuture<?> reconnectFuture;

    class WebSocketClientImpl extends WebSocketClient {
    	private String username;
    	private String reconnectGameId;

		public WebSocketClientImpl(URI serverURI, String username, String reconnectGameId) {
			super(serverURI);
			this.username = username;
			this.reconnectGameId = reconnectGameId;
		}

		@Override
        public void onClose(int code, String reason, boolean remote) {
        	cancelPing();
            listener.onWebsocketClose(code, reason, remote && !closedByUser);
        }

        @Override
        public void onError(Exception ex) {
        	if (reconnectFuture != null) return; //don't handle connection refuse while trying to reconnect
        	if (ex instanceof WebsocketNotConnectedException) {
        		cancelPing();
                listener.onWebsocketClose(0, ex.getMessage(), true);
        	} else {
        		listener.onWebsocketError(ex);
        	}
        }

        @Override
        public void onMessage(String payload) {
            WsMessage msg = parser.fromJson(payload);
            if (logger.isInfoEnabled()) {
                if (msg instanceof RmiMessage) {
                    logger.info(((RmiMessage)msg).toString());
                } else {
                    logger.info(payload);
                }
            }
            if (reportingTool != null) {
                if (msg instanceof RmiMessage) {
                    reportingTool.report(((RmiMessage)msg).toString());
                } else {
                    reportingTool.report(payload);
                }
            }

            if (msg instanceof WelcomeMessage) {
                WelcomeMessage welcome = (WelcomeMessage) msg;
                sessionId = welcome.getSessionId();
                nickname = welcome.getNickname();
                pingInterval = welcome.getPingInterval();
                maintenance = welcome.getMaintenance();
            }
            schedulePing();
            listener.onWebsocketMessage(msg);
        }

        @Override
        public void onOpen(ServerHandshake arg0) {
            WebSocketConnection.this.send(new HelloMessage(username, clientId, secret));
            if (reconnectGameId != null) {
            	WebSocketConnection.this.send(new JoinGameMessage(reconnectGameId));
            }
        }
    }

    public WebSocketConnection(final String username, Config config, URI uri, MessageListener listener) {
        clientId = config.getClient_id();
        secret = config.getSecret();
        this.listener = listener;
        this.uri = uri;
        ws = new WebSocketClientImpl(uri, username, null);
        ws.connect();
    }

    @Override
    public void reconnect(final String gameId) {
    	reconnectFuture = scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
            	ws = new WebSocketClientImpl(uri, nickname, gameId);
                try {
					if (ws.connectBlocking()) {
						stopReconnecting();
					}
				} catch (InterruptedException e) {
				}
            }
        }, 1, 4, TimeUnit.SECONDS);
    }

    @Override
    public void stopReconnecting() {
    	if (reconnectFuture != null) {
    		reconnectFuture.cancel(false);
    		reconnectFuture = null;
    	}
    }

    private void cancelPing() {
    	if (pingFuture != null) {
            pingFuture.cancel(false);
            pingFuture = null;
        }
    }

    private void schedulePing() {
        if (pingInterval == 0) return;
        if (pingFuture != null) pingFuture.cancel(false);
        pingFuture = scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                WebSocketConnection.this.send(new PingMessage());
            }
        }, pingInterval, pingInterval, TimeUnit.SECONDS);
    }

    @Override
    public void send(WsMessage arg) {
        schedulePing();
        try {
            ws.send(parser.toJson(arg));
        } catch (WebsocketNotConnectedException ex) {
            listener.onWebsocketClose(0, ex.getMessage(), true);
        }
    }

    @Override
    public void close() {
    	closedByUser = true;
        ws.close();
    }

    @Override
    public boolean isClosed() {
    	return ws.isClosed() || ws.isClosing();
    }

    @Override
    public String getSessionId() {
        return sessionId;
    }

    @Override
    public String getNickname() {
        return nickname;
    }

    public ReportingTool getReportingTool() {
        return reportingTool;
    }

    public void setReportingTool(ReportingTool reportingTool) {
        this.reportingTool = reportingTool;
    }

    public MessageParser getParser() {
        return parser;
    }

    public String getMaintenance() {
        return maintenance;
    }
}
