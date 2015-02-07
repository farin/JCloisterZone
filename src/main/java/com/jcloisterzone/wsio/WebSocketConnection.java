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
import com.jcloisterzone.wsio.message.PingMessage;
import com.jcloisterzone.wsio.message.RmiMessage;
import com.jcloisterzone.wsio.message.WelcomeMessage;
import com.jcloisterzone.wsio.message.WsMessage;

public class WebSocketConnection implements Connection {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());
    private ReportingTool reportingTool;

    private MessageParser parser = new MessageParser();
    private WebSocketClient ws;
    private final MessageListener listener;

    private String sessionId;
    private String clientId;
    private String secret; //TODO will be used for message signing
    private String nickname;

    private int pingInterval = 0;
    private String maintenance;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> pingFuture;

    public WebSocketConnection(final String username, Config config, URI uri, MessageListener _listener) {
        clientId = config.getClient_id();
        secret = config.getSecret();
        this.listener = _listener;
        ws = new WebSocketClient(uri) {
            @Override
            public void onClose(int code, String reason, boolean remote) {
                if (pingFuture != null) {
                    pingFuture.cancel(false);
                    pingFuture = null;
                }
                listener.onWebsocketClose(code, reason, remote);
            }

            @Override
            public void onError(Exception ex) {
                listener.onWebsocketError(ex);
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
            }
        };
        ws.connect();
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
            listener.onWebsocketError(ex);
        }
    }

    @Override
	public void close() {
        ws.close();
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
