package com.jcloisterzone.wsio;

import java.net.URI;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.exceptions.WebsocketNotConnectedException;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcloisterzone.bugreport.ReportingTool;
import com.jcloisterzone.config.Config;
import com.jcloisterzone.wsio.message.HelloMessage;
import com.jcloisterzone.wsio.message.JoinGameMessage;
import com.jcloisterzone.wsio.message.PingMessage;
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
    private java.util.Set<String> alreadyReceived = new java.util.HashSet<>();

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> pingFuture;
    private ScheduledFuture<?> reconnectFuture;
    private String tryReconnectTo = null;

    class WebSocketClientImpl extends WebSocketClient {
        private String username;
        private String reconnectGameId;

        public WebSocketClientImpl(URI serverURI, String username, String reconnectGameId) {
            super(serverURI, new Draft_17(), null, 9000);
//            if (System.getProperty("hearthbeat") != null) {
//                setConnectionLostTimeout(Integer.parseInt(System.getProperty("hearthbeat")));
//            } else {
//                setConnectionLostTimeout(DEFAULT_HEARTHBEAT_INTERVAL);
//            }
            this.username = username;
            this.reconnectGameId = reconnectGameId;
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
            cancelPing();

            // workaround for https://github.com/TooTallNate/Java-WebSocket/issues/587
            //ws.setConnectionLostTimeout(0);

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
        synchronized public void onMessage(String payload) {
            if (isClosing()) {
                return;
            }

            WsMessage msg = null;
            try {
                msg = parser.fromJson(payload);
            } catch (Exception e) {
                logger.error("Can't parse message: " + payload);
                close(Connection.CLOSE_MESSAGE_LOST, "Can't parse message");
                return;
            }

            if (alreadyReceived.contains(msg.getMessageId())) {
                logger.info("Already received message dropped: " + payload);
                return;
            }

            if (logger.isInfoEnabled()) {
                logger.info(payload);
            }

//            if (msgSequence != msg.getSequenceNumber()) {
//                String errMessage = String.format("Message lost. Received #%s but expected #%s.", msg.getSequenceNumber(), msgSequence);
//                listener.onWebsocketError(new MessageLostException(errMessage));
//                close(Connection.CLOSE_MESSAGE_LOST, errMessage);
//                return;
//            }
//            msgSequence = msg.getSequenceNumber() + 1;

            if (msg instanceof WelcomeMessage) {
                WelcomeMessage welcome = (WelcomeMessage) msg;
                sessionId = welcome.getSessionId();
                nickname = welcome.getNickname();
                pingInterval = welcome.getPingInterval();
                maintenance = welcome.getMaintenance();
            }
            listener.onWebsocketMessage(msg);
        }

        @Override
        public void onOpen(ServerHandshake arg0) {
            WebSocketConnection.this.send(new HelloMessage(username, clientId, secret));
            if (reconnectGameId != null) {
                JoinGameMessage msg = new JoinGameMessage();
                msg.setGameId(reconnectGameId);
                WebSocketConnection.this.send(msg);
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

    private void doReconnectAttempt() {
        if (tryReconnectTo != null) {
            logger.info("Reconnection attempt.");
            ws = new WebSocketClientImpl(uri, nickname, tryReconnectTo);
            try {
                if (ws.connectBlocking()) {
                    stopReconnecting();
                }
            } catch (InterruptedException e) {
            }
        }
    }

    @Override
    public void reconnect(final String gameId, long initialDelay) {
        tryReconnectTo = gameId;
        reconnectFuture = scheduler.scheduleWithFixedDelay(this::doReconnectAttempt, initialDelay, 1000, TimeUnit.MILLISECONDS);
    }

    @Override
    public void stopReconnecting() {
        tryReconnectTo = null;
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
    public void send(WsMessage msg) {
        if (ws.isClosed() || ws.isClosing()) {
            return;
        }
        schedulePing();
        try {
            if (msg.getMessageId() == null) {
                msg.setMessageId(UUID.randomUUID().toString());
            }
            ws.send(parser.toJson(msg));
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

    public static class MessageLostException extends Exception {

        public MessageLostException(String message) {
            super(message);
        }
    }
}
