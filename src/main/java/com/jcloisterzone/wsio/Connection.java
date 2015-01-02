package com.jcloisterzone.wsio;

import java.net.URI;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.exceptions.WebsocketNotConnectedException;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcloisterzone.bugreport.ReportingTool;
import com.jcloisterzone.config.Config;
import com.jcloisterzone.wsio.message.HelloMessage;
import com.jcloisterzone.wsio.message.RmiMessage;
import com.jcloisterzone.wsio.message.WelcomeMessage;
import com.jcloisterzone.wsio.message.WsMessage;

public class Connection {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());
    private ReportingTool reportingTool;

    private MessageParser parser = new MessageParser();
    private WebSocketClient ws;
    private final MessageListener listener;

    private String clientId;
    private String secret; //TODO will be used for message signing
    private String nickname;

    public Connection(final String username, Config config, URI uri, MessageListener _listener) {
        clientId = config.getClient_id();
        secret = config.getSecret();
        this.listener = _listener;
        ws = new WebSocketClient(uri) {
            @Override
            public void onClose(int code, String reason, boolean remote) {
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
                    if (!clientId.equals(welcome.getClientId())) {
                        clientId = welcome.getClientId();
                        logger.warn("ClientId changed by server!");
                    }
                    nickname = welcome.getNickname();
                }
                listener.onWebsocketMessage(msg);
            }

            @Override
            public void onOpen(ServerHandshake arg0) {
                Connection.this.send(new HelloMessage(username, clientId, secret));
            }
        };
        ws.connect();
    }


    public void send(WsMessage arg) {
        try {
            ws.send(parser.toJson(arg));
        } catch (WebsocketNotConnectedException ex) {
            listener.onWebsocketError(ex);
        }
    }

    public void close() {
        ws.close();
    }

    public String getClientId() {
        return clientId;
    }

    public String getNickname() {
        return nickname;
    }

    public ReportingTool getReportingTool() {
        return reportingTool;
    }

    public void setReportingTool(ReportingTool reportingTool) {
        this.reportingTool = reportingTool;
    }
}
