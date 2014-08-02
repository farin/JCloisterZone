package com.jcloisterzone.wsio;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.UUID;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcloisterzone.rmi.RmiProxy;
import com.jcloisterzone.wsio.message.HelloMessage;
import com.jcloisterzone.wsio.message.WelcomeMessage;
import com.jcloisterzone.wsio.message.WsMessage;

public class Connection {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private WsBus wsBus = new WsBus();
    private WebSocketClient ws;

    private String clientId;
    private String sessionKey;

    //for legacy code, to be able pass connection only
    private RmiProxy rmiProxy;

    public Connection(URI uri, final WsReceiver receiver) {
        wsBus.register(this);
        wsBus.register(receiver);
        ws = new WebSocketClient(uri) {
            @Override
            public void onClose(int code, String reason, boolean remote) {
                receiver.onWebsocketClose(code, reason, remote);
            }

            @Override
            public void onError(Exception ex) {
                receiver.onWebsocketError(ex);
            }

            @Override
            public void onMessage(String payload) {
                logger.info(payload);
                WsMessage msg = wsBus.receive(Connection.this, payload);
                receiver.onWebsocketMessage(msg);
            }

            @Override
            public void onOpen(ServerHandshake arg0) {
                String name;
                try {
                    name = InetAddress.getLocalHost().getHostName();
                } catch (UnknownHostException e) {
                    logger.warn(e.getMessage(), e);
                    name = UUID.randomUUID().toString();
                }
                Connection.this.send(new HelloMessage(name));
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
        ws.send(wsBus.toJson(arg));
    }

    public void close() {
        ws.close();
    }

    public String getClientId() {
        return clientId;
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


}
