package com.jcloisterzone.wsio.server;

import java.net.InetSocketAddress;
import java.util.UUID;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.jcloisterzone.Expansion;
import com.jcloisterzone.game.CustomRule;
import com.jcloisterzone.game.GameSettings;
import com.jcloisterzone.game.Snapshot;
import com.jcloisterzone.wsio.MessageParser;
import com.jcloisterzone.wsio.MessageParser.Command;
import com.jcloisterzone.wsio.message.CreateGameMessage;
import com.jcloisterzone.wsio.message.GameMessage;
import com.jcloisterzone.wsio.message.HelloMessage;
import com.jcloisterzone.wsio.message.JoinGameMessage;
import com.jcloisterzone.wsio.message.SlotMessage;
import com.jcloisterzone.wsio.message.WelcomeMessage;

public class SimpleServer extends WebSocketServer  {

//    static {
//        WebSocketImpl.DEBUG = true;
//    }

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    public static final String GAME_ID = "_1";

    private MessageParser parser = new MessageParser();

    private GameSettings game;
    private Snapshot snapshot;

    public SimpleServer(InetSocketAddress address) {
        super(address);
    }


    public void createGame() {
        game = new GameSettings();
        game.getExpansions().add(Expansion.BASIC);
        for (CustomRule cr : CustomRule.defaultEnabled()) {
            game.getCustomRules().add(cr);
        }
    }

    public void createGame(Snapshot snapshot) {
        this.snapshot =  snapshot;
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void onClose(WebSocket ws, int code, String reason, boolean remote) {

    }

    @Override
    public void onError(WebSocket ws, Exception ex) {
        logger.error(ex.getMessage(), ex);
    }

    @Override
    public void onMessage(WebSocket ws, String message) {
        logger.info(message);
        Command cmd = parser.fromJson(message);
        switch (cmd.command) {
        case "HELLO": handleHello(ws, (HelloMessage) cmd.arg); break;
        case "CREATE_GAME": handleCreateGame(ws, (CreateGameMessage) cmd.arg); break;
        case "JOIN_GAME": handleJoinGame(ws, (JoinGameMessage) cmd.arg); break;
        default:
            logger.error("Unknown command " + cmd.command);
        }
    }

    private GameMessage createGameMessage() {
        GameMessage gm = new GameMessage(GAME_ID, "open", game.getCustomRules(), game.getExpansions(), game.getCapabilityClasses());
        gm.setSlots(new SlotMessage[0]);
        return gm;
    }

    public void handleHello(WebSocket ws, HelloMessage msg) {
        String clientId = UUID.randomUUID().toString();
        String sessionKey = UUID.randomUUID().toString();
        sendMessage(ws, "WELCOME", new WelcomeMessage(clientId, sessionKey));
    }

    public void handleCreateGame(WebSocket ws, CreateGameMessage msg) {
        createGame();
        sendMessage(ws, "GAME", createGameMessage());
    }


    public void handleJoinGame(WebSocket ws, JoinGameMessage msg) {
        if (this.game == null || !GAME_ID.equals(msg.getId())) {
            sendMessage(ws, "ERR", "Game doesn't exist.");
            return;
        }
        sendMessage(ws, "GAME", createGameMessage());
    }


    @Override
    public void onOpen(WebSocket ws, ClientHandshake hs) {

    }

    public void sendMessage(WebSocket ws, String command, Object data) {
        ws.send(parser.toJson(command, data));
    }

//    private void sendToAll(String cmd, Object message) {
//
//    }

}
