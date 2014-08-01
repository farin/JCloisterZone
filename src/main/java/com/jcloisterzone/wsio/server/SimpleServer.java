package com.jcloisterzone.wsio.server;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
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
import com.jcloisterzone.wsio.WsSubscribe;
import com.jcloisterzone.wsio.WsBus;
import com.jcloisterzone.wsio.message.CreateGameMessage;
import com.jcloisterzone.wsio.message.ErrorMessage;
import com.jcloisterzone.wsio.message.FlierDiceMessage;
import com.jcloisterzone.wsio.message.GameMessage;
import com.jcloisterzone.wsio.message.GameMessage.GameState;
import com.jcloisterzone.wsio.message.GameSetupMessage;
import com.jcloisterzone.wsio.message.GetRandSampleMessage;
import com.jcloisterzone.wsio.message.HelloMessage;
import com.jcloisterzone.wsio.message.JoinGameMessage;
import com.jcloisterzone.wsio.message.LeaveSlotMessage;
import com.jcloisterzone.wsio.message.RandSampleMessage;
import com.jcloisterzone.wsio.message.RmiMessage;
import com.jcloisterzone.wsio.message.RollFlierDiceMessage;
import com.jcloisterzone.wsio.message.SetExpansionMessage;
import com.jcloisterzone.wsio.message.SetRuleMessage;
import com.jcloisterzone.wsio.message.SlotMessage;
import com.jcloisterzone.wsio.message.StartGameMessage;
import com.jcloisterzone.wsio.message.TakeSlotMessage;
import com.jcloisterzone.wsio.message.WelcomeMessage;
import com.jcloisterzone.wsio.message.WsMessage;
import com.jcloisterzone.wsio.server.checks.CheckGameId;
import com.jcloisterzone.wsio.server.checks.CheckGameRunning;

public class SimpleServer extends WebSocketServer  {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    public static final String GAME_ID = "_1";

    private WsBus wsBus = new WsBus();

    private GameSettings game;
    protected final ServerPlayerSlot[] slots;
    protected int slotSerial;

    private Snapshot snapshot;
    private boolean gameStarted;

    private final Map<WebSocket, String> clientIds = new HashMap<>();

    private Random random = new Random();

    public SimpleServer(InetSocketAddress address) {
        super(address);
        wsBus.register(this);
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
    public void onMessage(WebSocket ws, String payload) {
        logger.info(payload);
        wsBus.receive(ws, payload);
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
        msg.setSupportedExpansions(slot.getSupportedExpansions());
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
        GameSetupMessage gsm = new GameSetupMessage(GAME_ID, game.getCustomRules(), game.getExpansions(), game.getCapabilityClasses());
        GameMessage gm = new GameMessage(GAME_ID,  gameStarted ? GameState.RUNNING : GameState.OPEN, gsm);
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

    @WsSubscribe
    @CheckGameRunning(false)
    public void handleHello(WebSocket ws, HelloMessage msg) {
        String clientId = UUID.randomUUID().toString();
        String sessionKey = UUID.randomUUID().toString();
        clientIds.put(ws, clientId);
        send(ws,  new WelcomeMessage(clientId, sessionKey));
    }

    @WsSubscribe
    @CheckGameRunning(false)
    public void handleCreateGame(WebSocket ws, CreateGameMessage msg) {
        String clientId = getClientId(ws);
        createGame();
        send(ws, createGameMessage(clientId));
    }


    @WsSubscribe
    @CheckGameRunning(false)
    @CheckGameId
    public void handleJoinGame(WebSocket ws, JoinGameMessage msg) {
        String clientId = getClientId(ws);
        if (this.game == null || !GAME_ID.equals(msg.getGameId())) {
            send(ws, new ErrorMessage("JOIN_GAME", "Game doesn't exist."));
            return;
        }
        send(ws, createGameMessage(clientId));
    }

    @WsSubscribe
    @CheckGameId
    public void handleGameSetupMessage(WebSocket ws, GameSetupMessage msg) {
        game.getExpansions().clear();
        game.getExpansions().addAll(msg.getExpansions());
        game.getCustomRules().clear();
        game.getCustomRules().addAll(msg.getCustomRules());
        broadcast(msg);
    }


    @WsSubscribe
    @CheckGameRunning(false)
    @CheckGameId
    public void handleTakeSlot(WebSocket ws, TakeSlotMessage msg) {
        String clientId = getClientId(ws);
        int number = msg.getNumber();
        if (number < 0 || number >= slots.length) {
            send(ws, new ErrorMessage("TAKE_SLOT", "Invalid slot number"));
            return;

        }
        ServerPlayerSlot slot = slots[number];
        if (!slot.isOccupied()) {
            slot.setSerial(++slotSerial);
        }
        slot.setNickname(msg.getNickname());
        slot.setAi(msg.isAi());
        slot.setOwner(clientId);
        slot.setSupportedExpansions(msg.getSupportedExpansions());
        broadcast(createSlotMessage(clientId, slot));
    }

    @WsSubscribe
    @CheckGameRunning(false)
    @CheckGameId
    public void handleLeaveSlot(WebSocket ws, LeaveSlotMessage msg) {
        String clientId = getClientId(ws);
        int number = msg.getNumber();
        if (number < 0 || number >= slots.length) {
            send(ws, new ErrorMessage("LEAVE_SLOT", "Invalid slot number"));
            return;

        }
        ServerPlayerSlot slot = slots[number];
        slot.setNickname(null);
        slot.setSerial(null);
        slot.setOwner(null);
        slot.setAi(false);
        slot.setSupportedExpansions(null);
        broadcast(createSlotMessage(clientId, slot));
    }

    @WsSubscribe
    @CheckGameRunning(false)
    @CheckGameId
    public void handleSetExpansion(WebSocket ws, SetExpansionMessage msg) {
        Expansion expansion = msg.getExpansion();
        if (!expansion.isImplemented() || expansion == Expansion.BASIC) {
            logger.error("Invalid expansion {}", expansion);
            return;
        }
        if (msg.isEnabled()) {
            game.getExpansions().add(msg.getExpansion());
        } else {
            game.getExpansions().remove(msg.getExpansion());
        }
        broadcast(msg);
    }

    @WsSubscribe
    @CheckGameRunning(false)
    @CheckGameId
    public void handleSetRule(WebSocket ws, SetRuleMessage msg) {
        CustomRule rule = msg.getRule();
        if (msg.isEnabled()) {
            game.getCustomRules().add(rule);
        } else {
            game.getCustomRules().remove(rule);
        }
        broadcast(msg);
    }

    @WsSubscribe
    @CheckGameRunning(false)
    @CheckGameId
    public void handleStartGame(WebSocket ws, StartGameMessage msg) {
        gameStarted = true;
        for (Entry<WebSocket, String> entry : clientIds.entrySet()) {
            send(entry.getKey(), createGameMessage(entry.getValue()));
        }
    }

    @WsSubscribe
    @CheckGameRunning(true)
    @CheckGameId
    public void handleGetRandSample(WebSocket ws, GetRandSampleMessage msg) {
        int[] result = new int[msg.getK()];
        int n = msg.getPopulation();
        for (int i = 0; i < msg.getK(); i++) {
            result[i] = i + random.nextInt(n--);
        }
        broadcast(new RandSampleMessage(msg.getGameId(), msg.getName(), msg.getPopulation(), result));
    }

    @WsSubscribe
    @CheckGameRunning(true)
    @CheckGameId
    public void handleRollFlierDice(WebSocket ws, RollFlierDiceMessage msg) {
        broadcast(new FlierDiceMessage(msg.getGameId(),msg.getMeepleType(), 1+random.nextInt(3)));
    }

    @WsSubscribe
    @CheckGameRunning(true)
    @CheckGameId
    public void handleRmi(WebSocket ws, RmiMessage msg) {
        broadcast(msg);
    }


    public void send(WebSocket ws, WsMessage message) {
        ws.send(wsBus.toJson(message));
    }

    public void broadcast(WsMessage data) {
        String payload = wsBus.toJson(data);
        for (WebSocket ws : clientIds.keySet()) {
            ws.send(payload);
        }
    }
}
