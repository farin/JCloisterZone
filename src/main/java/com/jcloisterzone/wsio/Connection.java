package com.jcloisterzone.wsio;

import java.net.URI;
import java.util.UUID;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcloisterzone.wsio.message.HelloMessage;
import com.jcloisterzone.wsio.message.WelcomeMessage;
import com.jcloisterzone.wsio.message.WsMessage;

public class Connection {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private WsBus wsBus = new WsBus();
    private WebSocketClient ws;

    private String clientId;
    private String sessionKey;

    public Connection(URI uri, final Object receiver) {
        wsBus.register(this);
        wsBus.register(receiver);
        ws = new WebSocketClient(uri) {
            @Override
            public void onClose(int code, String reason, boolean remote) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onError(Exception ex) {
                logger.error(ex.getMessage(), ex);
            }

            @Override
            public void onMessage(String payload) {
                logger.info(payload);
                wsBus.receive(Connection.this, payload);
            }

            @Override
            public void onOpen(ServerHandshake arg0) {
                Connection.this.send(new HelloMessage(UUID.randomUUID().toString()));
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
}
