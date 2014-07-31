package com.jcloisterzone.wsio;

import java.net.URI;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcloisterzone.wsio.message.HelloMessage;
import com.jcloisterzone.wsio.message.WelcomeMessage;
import com.jcloisterzone.wsio.message.WsMessage;

public class Connection {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private WsUtils parser = new WsUtils();
    private WebSocketClient ws;

    private String clientId;
    private String sessionKey;

    public Connection(URI uri, final Object receiver) {
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
            public void onMessage(String message) {
                logger.info(message);
                WsMessage msg = parser.fromJson(message);
                if (msg instanceof WelcomeMessage) {
                    WelcomeMessage welcomeMsg = (WelcomeMessage) msg;
                    clientId = welcomeMsg.getClientId();
                    sessionKey = welcomeMsg.getSessionKey();
                }
                parser.delegate(receiver, Connection.this, msg);
            }

            @Override
            public void onOpen(ServerHandshake arg0) {
                Connection.this.send(new HelloMessage("WsFarin"));
            }
        };
        ws.connect();
    }

    public void send(WsMessage arg) {
        ws.send(parser.toJson(arg));
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
