package com.jcloisterzone.wsio.server;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcloisterzone.Expansion;
import com.jcloisterzone.game.CustomRule;
import com.jcloisterzone.game.GameSettings;
import com.jcloisterzone.game.PlayerSlot;
import com.jcloisterzone.game.PlayerSlot.SlotState;
import com.jcloisterzone.game.Snapshot;
import com.jcloisterzone.wsio.CmdHandler;
import com.jcloisterzone.wsio.MessageParser;
import com.jcloisterzone.wsio.MessageParser.Command;
import com.jcloisterzone.wsio.message.CreateGameMessage;
import com.jcloisterzone.wsio.message.GameMessage;
import com.jcloisterzone.wsio.message.HelloMessage;
import com.jcloisterzone.wsio.message.JoinGameMessage;
import com.jcloisterzone.wsio.message.LeaveSlotMessage;
import com.jcloisterzone.wsio.message.SlotMessage;
import com.jcloisterzone.wsio.message.TakeSlotMessage;
import com.jcloisterzone.wsio.message.WelcomeMessage;

public class SimpleServer extends WebSocketServer  {

//    static {
//        WebSocketImpl.DEBUG = true;
//    }

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    public static final String GAME_ID = "_1";

    private MessageParser parser = new MessageParser();

    private GameSettings game;
    protected final ServerPlayerSlot[] slots;
    protected int slotSerial;

    private Snapshot snapshot;
    private boolean gameStarted;

    private final Map<WebSocket, String> clientIds = new HashMap<>();

    public SimpleServer(InetSocketAddress address) {
        super(address);
        slots = new ServerPlayerSlot[PlayerSlot.COUNT];
        for (int i = 0; i < slots.length; i++) {
            slots[i] = new ServerPlayerSlot(i);
        }
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
        parser.delegate(this, ws, cmd);
    }

    @Override
    public void onOpen(WebSocket ws, ClientHandshake hs) {
    }

    private String getClientId(WebSocket ws) {
        return clientIds.get(ws);
    }

    private SlotMessage createSlotMessage(String clientId, ServerPlayerSlot slot) {
        SlotMessage msg = new SlotMessage(GAME_ID, slot.getNumber(), slot.getSerial(), slot.getNickname());
        msg.setAi(slot.isAi());
        if (slot.getOwner() == null) {
            msg.setState(SlotState.OPEN);
        } else if (clientId.equals(slot.getOwner())) {
            msg.setState(SlotState.OWN);
        } else {
            msg.setState(SlotState.REMOTE);
        }
        return msg;
    }

    private GameMessage createGameMessage(String clientId) {
        GameMessage gm = new GameMessage(GAME_ID, "open", game.getCustomRules(), game.getExpansions(), game.getCapabilityClasses());
        List<SlotMessage> slotMsgs = new ArrayList<>();
        for (ServerPlayerSlot slot : slots) {
            if (slot.isOccupied()) {
                SlotMessage sm = createSlotMessage(clientId, slot);
                slotMsgs.add(sm);
            }
        }
        gm.setSlots(slotMsgs.toArray(new SlotMessage[slotMsgs.size()]));
        return gm;
    }

    @CmdHandler("HELLO")
    public void handleHello(WebSocket ws, HelloMessage msg) {
        String clientId = UUID.randomUUID().toString();
        String sessionKey = UUID.randomUUID().toString();
        clientIds.put(ws, clientId);
        sendMessage(ws, "WELCOME", new WelcomeMessage(clientId, sessionKey));
    }

    @CmdHandler("CREATE_GAME")
    public void handleCreateGame(WebSocket ws, CreateGameMessage msg) {
        String clientId = getClientId(ws);
        createGame();
        sendMessage(ws, "GAME", createGameMessage(clientId));
    }


    @CmdHandler("JOIN_GAME")
    public void handleJoinGame(WebSocket ws, JoinGameMessage msg) {
        String clientId = getClientId(ws);
        if (this.game == null || !GAME_ID.equals(msg.getGameId())) {
            sendMessage(ws, "ERR", "Game doesn't exist.");
            return;
        }
        sendMessage(ws, "GAME", createGameMessage(clientId));
    }

    @CmdHandler("TAKE_SLOT")
    public void handleTakeSlot(WebSocket ws, TakeSlotMessage msg) {
        String clientId = getClientId(ws);
        if (gameStarted) {
            sendMessage(ws, "ERR", "Game is running.");
            return;
        }
        int number = msg.getNumber();
        if (number < 0 || number >= slots.length) {
            sendMessage(ws, "ERR", "Invalid slot number");
            return;

        }
        ServerPlayerSlot slot = slots[number];
        if (!slot.isOccupied()) {
            slot.setSerial(++slotSerial);
        }
        slot.setNickname(msg.getNickname());
        slot.setAi(msg.isAi());
        slot.setOwner(clientId);
        sendMessage(ws, "SLOT", createSlotMessage(clientId, slot));
    }

    @CmdHandler("LEAVE_SLOT")
    public void handleLeaveSlot(WebSocket ws, LeaveSlotMessage msg) {
         String clientId = getClientId(ws);
        //TODO DRY violation
        if (gameStarted) {
            sendMessage(ws, "ERR", "Game is running.");
            return;
        }
        int number = msg.getNumber();
        if (number < 0 || number >= slots.length) {
            sendMessage(ws, "ERR", "Invalid slot number");
            return;

        }
        ServerPlayerSlot slot = slots[number];
        slot.setNickname(null);
        slot.setSerial(null);
        slot.setOwner(null);
        slot.setAi(false);
        sendMessage(ws, "SLOT", createSlotMessage(clientId, slot));
    }


    public void sendMessage(WebSocket ws, String command, Object data) {
        ws.send(parser.toJson(command, data));
    }

//    private void sendToAll(String cmd, Object message) {
//
//    }

}
