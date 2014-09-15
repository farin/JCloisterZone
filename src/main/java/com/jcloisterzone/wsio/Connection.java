package com.jcloisterzone.wsio;

import java.net.URI;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.exceptions.WebsocketNotConnectedException;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcloisterzone.bugreport.ReportingTool;
import com.jcloisterzone.rmi.RmiProxy;
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
    private String sessionKey;
    private String nickname;

    //for legacy code, to be able pass connection only
    private RmiProxy rmiProxy;

    public Connection(final String username, URI uri, MessageListener _listener) {
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
                    clientId = welcome.getClientId();
                    sessionKey = welcome.getSessionKey();
                    nickname = welcome.getNickname();
                }
                listener.onWebsocketMessage(msg);
            }

            @Override
            public void onOpen(ServerHandshake arg0) {
                Connection.this.send(new HelloMessage(username));
            }
        };
        ws.connect();
    }


    @WsSubscribe
    public void handleWelcome(Connection conn, WelcomeMessage msg) {
        clientId = msg.getClientId();
        sessionKey = msg.getSessionKey();
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

    public String getSessionKey() {
        return sessionKey;
    }

    public RmiProxy getRmiProxy() {
        return rmiProxy;
    }

    public void setRmiProxy(RmiProxy rmiProxy) {
        this.rmiProxy = rmiProxy;
    }


    public ReportingTool getReportingTool() {
        return reportingTool;
    }


    public void setReportingTool(ReportingTool reportingTool) {
        this.reportingTool = reportingTool;
    }


}
