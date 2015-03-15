package com.jcloisterzone.wsio.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.xml.transform.TransformerException;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcloisterzone.Application;
import com.jcloisterzone.Expansion;
import com.jcloisterzone.KeyUtils;
import com.jcloisterzone.Player;
import com.jcloisterzone.VersionComparator;
import com.jcloisterzone.config.ConfigLoader;
import com.jcloisterzone.game.CustomRule;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.GameSettings;
import com.jcloisterzone.game.PlayerSlot;
import com.jcloisterzone.game.PlayerSlot.SlotState;
import com.jcloisterzone.game.Snapshot;
import com.jcloisterzone.wsio.MessageDispatcher;
import com.jcloisterzone.wsio.MessageParser;
import com.jcloisterzone.wsio.WsSubscribe;
import com.jcloisterzone.wsio.message.ChatMessage;
import com.jcloisterzone.wsio.message.ClientUpdateMessage;
import com.jcloisterzone.wsio.message.ClientUpdateMessage.ClientState;
import com.jcloisterzone.wsio.message.ClockMessage;
import com.jcloisterzone.wsio.message.EndTurnMessage;
import com.jcloisterzone.wsio.message.ErrorMessage;
import com.jcloisterzone.wsio.message.FlierDiceMessage;
import com.jcloisterzone.wsio.message.GameMessage;
import com.jcloisterzone.wsio.message.GameMessage.GameState;
import com.jcloisterzone.wsio.message.GameOverMessage;
import com.jcloisterzone.wsio.message.GameSetupMessage;
import com.jcloisterzone.wsio.message.HelloMessage;
import com.jcloisterzone.wsio.message.LeaveSlotMessage;
import com.jcloisterzone.wsio.message.PingMessage;
import com.jcloisterzone.wsio.message.PongMessage;
import com.jcloisterzone.wsio.message.PostChatMessage;
import com.jcloisterzone.wsio.message.RmiMessage;
import com.jcloisterzone.wsio.message.RollFlierDiceMessage;
import com.jcloisterzone.wsio.message.SetExpansionMessage;
import com.jcloisterzone.wsio.message.SetRuleMessage;
import com.jcloisterzone.wsio.message.SlotMessage;
import com.jcloisterzone.wsio.message.StartGameMessage;
import com.jcloisterzone.wsio.message.TakeSlotMessage;
import com.jcloisterzone.wsio.message.ToggleClockMessage;
import com.jcloisterzone.wsio.message.UndoMessage;
import com.jcloisterzone.wsio.message.WelcomeMessage;
import com.jcloisterzone.wsio.message.WsMessage;

public class SimpleServer extends WebSocketServer  {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private String HOST_SESSION_PLACEHOLDER = "!host";

    private final SimpleServerErrorHandler errHandler;

    private MessageParser parser = new MessageParser();
    private MessageDispatcher dispatcher = new MessageDispatcher();

    private GameSettings game;
    protected final ServerPlayerSlot[] slots;
    protected int slotSerial;

    private Snapshot snapshot;
    private boolean gameStarted;

    private long[] clocks;
    private int runningClock;
    private long runningSince;

    protected final Map<WebSocket, RemoteClient> connections = new HashMap<>();
    private String hostClientId;

    private Random random = new Random();

    public static interface SimpleServerErrorHandler {
        public void onError(WebSocket ws, final Exception ex);
    }

    public SimpleServer(InetSocketAddress address, SimpleServerErrorHandler errHandler) {
        super(address);
        this.errHandler = errHandler;
        slots = new ServerPlayerSlot[PlayerSlot.COUNT];
    }

    public void createGame(Snapshot snapshot, Game settings, String hostClientId) {
        slotSerial = 0;
        gameStarted = false;
        this.snapshot = null;
        this.hostClientId = hostClientId;
        game = new GameSettings(KeyUtils.createRandomId());
        if (snapshot != null) {
            this.snapshot =  snapshot;
            game.getExpansions().addAll(snapshot.getExpansions());
            game.getCustomRules().addAll(snapshot.getCustomRules());
            loadSlotsFromSnapshot();
        } else if (settings != null) {
            game.getExpansions().addAll(settings.getExpansions());
            game.getCustomRules().addAll(settings.getCustomRules());
            loadSlotsFromGame(settings);
        } else {
            game.getExpansions().add(Expansion.BASIC);
            for (CustomRule cr : CustomRule.defaultEnabled()) {
                game.getCustomRules().add(cr);
            }
            for (int i = 0; i < slots.length; i++) {
                slots[i] = new ServerPlayerSlot(i);
            }
        }
    }

    private void loadSlotsFromGame(Game settings) {
        //Game is game from client since, so we can use isLocalHuman
        int maxSerial = 0;
        for (Player player : settings.getAllPlayers()) {
            int slotNumber = player.getSlot().getNumber();
            ServerPlayerSlot slot = new ServerPlayerSlot(slotNumber);
            slots[slotNumber] = slot;
            boolean isAi = player.getSlot().isAi();
            if (player.isLocalHuman() || isAi) {
                slot.setNickname(player.getNick());
                slot.setOwner(HOST_SESSION_PLACEHOLDER);
                if (isAi) {
                    slot.setAiClassName(player.getSlot().getAiClassName());
                }
                maxSerial = Math.max(maxSerial, player.getSlot().getSerial());
                slot.setSerial(player.getSlot().getSerial());
            }
        }
        for (int i = 0; i < slots.length; i++) {
            if (slots[i] == null) {
                slots[i] = new ServerPlayerSlot(i);
            }
        }
        slotSerial = maxSerial + 1;
    }

    private void loadSlotsFromSnapshot() {
        List<Player> players = snapshot.getPlayers();
        for (Player player : players) {
            int slotNumber = player.getSlot().getNumber();
            ServerPlayerSlot slot = new ServerPlayerSlot(slotNumber);
            slot.setNickname(player.getNick());
            slot.setAiClassName(player.getSlot().getAiClassName());
            if (player.getSlot().getState() == SlotState.OWN || slot.getAiClassName() != null) {
                slot.setOwner(HOST_SESSION_PLACEHOLDER);
            }
            slots[slotNumber] = slot;
        }
    }


    @Override
    public void onClose(WebSocket ws, int code, String reason, boolean remote) {
        if (!remote) return;
        RemoteClient conn = connections.remove(ws);
        if (conn == null) return;
        if (!gameStarted) {
            for (ServerPlayerSlot slot : slots) {
                if (slot != null && conn.getSessionId().equals(slot.getOwner())) {
                    leaveSlot(slot);
                }
            }
        }
        broadcast(new ClientUpdateMessage(game.getGameId(), conn.getSessionId(), null, ClientState.OFFLINE));
    }

    @Override
    public void onError(WebSocket ws, final Exception ex) {
        errHandler.onError(ws, ex);
    }

    @Override
    public void onMessage(WebSocket ws, String payload) {
        //logger.info(payload);
        WsMessage msg = parser.fromJson(payload);
        dispatcher.dispatch(msg, ws, this);
    }

    @Override
    public void onOpen(WebSocket ws, ClientHandshake hs) {
    }

    private String getSessionId(WebSocket ws) {
        return connections.get(ws).getSessionId();
    }

    private SlotMessage newSlotMessage(ServerPlayerSlot slot) {
        SlotMessage msg = new SlotMessage(game.getGameId(), slot.getNumber(), slot.getSerial(), slot.getOwner(), slot.getNickname());
        msg.setAiClassName(slot.getAiClassName());
        msg.setSupportedExpansions(slot.getSupportedExpansions());
        return msg;
    }

    private GameMessage newGameMessage() {
        GameSetupMessage gsm = new GameSetupMessage(game.getGameId(), game.getCustomRules(), game.getExpansions(), game.getCapabilityClasses());
        GameMessage gm = new GameMessage(game.getGameId(), "", gameStarted ? GameState.RUNNING : GameState.OPEN, gsm);
        List<SlotMessage> slotMsgs = new ArrayList<>();
        for (ServerPlayerSlot slot : slots) {
            if (slot != null) {
                SlotMessage sm = newSlotMessage(slot);
                slotMsgs.add(sm);
            }
        }
        gm.setSlots(slotMsgs.toArray(new SlotMessage[slotMsgs.size()]));
        if (snapshot != null) {
            try {
                gm.setSnapshot(snapshot.saveToString());
            } catch (TransformerException | IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
        return gm;
    }

    private String getWebsocketHost(WebSocket ws) {
        if (ws.getRemoteSocketAddress().getAddress().isLoopbackAddress()) return "localhost";
        return ws.getRemoteSocketAddress().getHostName();
    }

    @WsSubscribe
    public void handlePing(WebSocket ws, PingMessage msg) {
        send(ws, new PongMessage());
    }

    @WsSubscribe
    public void handleHello(WebSocket ws, HelloMessage msg) {
        if (new VersionComparator().compare(Application.PROTCOL_VERSION, msg.getProtocolVersion()) != 0) {
            send(ws, new ErrorMessage(ErrorMessage.BAD_VERSION, "Protocol version " + Application.PROTCOL_VERSION + " required."));
            ws.close();
            return;
        }
        if (gameStarted) throw new IllegalArgumentException("Game is already started.");
        String nickname = msg.getNickname() + '@' + getWebsocketHost(ws);
        String sessionId = KeyUtils.createRandomId();
        RemoteClient client = new RemoteClient(sessionId, nickname, ClientState.ACTIVE);
        connections.put(ws, client);
        if (msg.getClientId().equals(hostClientId)) {
            for (int i = 0; i < slots.length; i++) {
                if (slots[i] != null && HOST_SESSION_PLACEHOLDER.equals(slots[i].getOwner())) {
                    slots[i].setOwner(sessionId);
                }
            }
        }

        send(ws, new WelcomeMessage(sessionId, nickname, 120, System.currentTimeMillis()));
        send(ws, newGameMessage());
        for (RemoteClient rc : connections.values()) {
            if (!rc.getSessionId().equals(sessionId)) {
                send(ws, new ClientUpdateMessage(game.getGameId(), rc.getSessionId(), rc.getName(), ClientState.ACTIVE));
            }
        }
        broadcast(new ClientUpdateMessage(game.getGameId(), sessionId, nickname, ClientState.ACTIVE));
    }


    @WsSubscribe
    public void handleGameSetupMessage(WebSocket ws, GameSetupMessage msg) {
        if (!msg.getGameId().equals(game.getGameId())) throw new IllegalArgumentException("Invalid game id.");
        if (gameStarted) throw new IllegalArgumentException("Game is already started.");
        game.getExpansions().clear();
        game.getExpansions().addAll(msg.getExpansions());
        game.getCustomRules().clear();
        game.getCustomRules().addAll(msg.getRules());
        broadcast(msg);
    }


    @WsSubscribe
    public void handleTakeSlot(WebSocket ws, TakeSlotMessage msg) {
        if (!msg.getGameId().equals(game.getGameId())) throw new IllegalArgumentException("Invalid game id.");
        if (gameStarted) throw new IllegalArgumentException("Game is already started.");
        String sessionId = getSessionId(ws);
        int number = msg.getNumber();
        if (number < 0 || number >= slots.length || slots[number] == null) {
            send(ws, new ErrorMessage("TAKE_SLOT", "Invalid slot number"));
            return;
        }
        ServerPlayerSlot slot = slots[number];
        if (!slot.isOccupied()) {
            slot.setSerial(++slotSerial);
        }
        slot.setNickname(msg.getNickname());
        slot.setAiClassName(msg.getAiClassName());
        slot.setOwner(sessionId);
        slot.setSupportedExpansions(msg.getSupportedExpansions());
        broadcast(newSlotMessage(slot));
    }

    private void leaveSlot(ServerPlayerSlot slot) {
        if (snapshot == null) {
            slot.setNickname(null);
            slot.setAiClassName(null);
            slot.setSupportedExpansions(null);
        }
        slot.setSerial(null);
        slot.setOwner(null);
        broadcast(newSlotMessage(slot));
    }

    @WsSubscribe
    public void handleLeaveSlot(WebSocket ws, LeaveSlotMessage msg) {
        if (!msg.getGameId().equals(game.getGameId())) throw new IllegalArgumentException("Invalid game id.");
        if (gameStarted) throw new IllegalArgumentException("Game is already started.");
        int number = msg.getNumber();
        if (number < 0 || number >= slots.length || slots[number] == null) {
            send(ws, new ErrorMessage("LEAVE_SLOT", "Invalid slot number"));
            return;

        }
        ServerPlayerSlot slot = slots[number];
        leaveSlot(slot);
    }

    @WsSubscribe
    public void handleSetExpansion(WebSocket ws, SetExpansionMessage msg) {
        if (!msg.getGameId().equals(game.getGameId())) throw new IllegalArgumentException("Invalid game id.");
        if (gameStarted) throw new IllegalArgumentException("Game is already started.");
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
    public void handleSetRule(WebSocket ws, SetRuleMessage msg) {
        if (!msg.getGameId().equals(game.getGameId())) throw new IllegalArgumentException("Invalid game id.");
        if (gameStarted) throw new IllegalArgumentException("Game is already started.");
        CustomRule rule = msg.getRule();
        if (msg.isEnabled()) {
            game.getCustomRules().add(rule);
        } else {
            game.getCustomRules().remove(rule);
        }
        broadcast(msg);
    }

    @WsSubscribe
    public void handleStartGame(WebSocket ws, StartGameMessage msg) {
        if (!msg.getGameId().equals(game.getGameId())) throw new IllegalArgumentException("Invalid game id.");
        if (gameStarted) throw new IllegalArgumentException("Game is already started.");
        if (snapshot == null) {
            int playerCount = 0;
            for (ServerPlayerSlot slot : slots) {
                if (!slot.isOccupied()) continue;
                playerCount++;
                if (slot.getSupportedExpansions() != null) {
                    game.getExpansions().retainAll(Arrays.asList(slot.getSupportedExpansions()));
                }
                if (game.hasRule(CustomRule.RANDOM_SEATING_ORDER)) {
                    slot.setSerial(random.nextInt());
                }
            }
            clocks = new long[playerCount];
            runningClock = -1;
        }
        gameStarted = true;
        broadcast(newGameMessage());
    }

    @WsSubscribe
    public void handleToggleClock(WebSocket ws, ToggleClockMessage msg) {
        if (!msg.getGameId().equals(game.getGameId())) throw new IllegalArgumentException("Invalid game id.");
        if (!gameStarted) throw new IllegalArgumentException("Game is not started.");
        long ts = System.currentTimeMillis();
        if (runningClock != -1) {
           clocks[runningClock] += ts-runningSince;
        }
        runningSince = ts;
        runningClock = msg.getRun() == null ? -1 : msg.getRun();
        long[] clocksCopy = Arrays.copyOf(clocks, clocks.length);
        ClockMessage clockMsg = new ClockMessage(msg.getGameId(), msg.getRun(), clocksCopy, ts);
        broadcast(clockMsg);
    }

    @WsSubscribe
    @Deprecated
    public void handleRollFlierDice(WebSocket ws, RollFlierDiceMessage msg) {
        if (!msg.getGameId().equals(game.getGameId())) throw new IllegalArgumentException("Invalid game id.");
        if (!gameStarted) throw new IllegalArgumentException("Game is not started.");
        broadcast(new FlierDiceMessage(msg.getGameId(),msg.getMeepleType(), 1+random.nextInt(3)));
    }

    @WsSubscribe
    public void handleEndTurn(WebSocket ws, EndTurnMessage msg) {
        if (!msg.getGameId().equals(game.getGameId())) throw new IllegalArgumentException("Invalid game id.");
        if (!gameStarted) throw new IllegalArgumentException("Game is not started.");
        msg.setCurrentTime(System.currentTimeMillis());
        broadcast(msg);
    }

    @WsSubscribe
    public void handleRmi(WebSocket ws, RmiMessage msg) {
        if (!msg.getGameId().equals(game.getGameId())) throw new IllegalArgumentException("Invalid game id.");
        if (!gameStarted) throw new IllegalArgumentException("Game is not started.");
        broadcast(msg);
    }

    @WsSubscribe
    public void handleUndo(WebSocket ws, UndoMessage msg) {
        if (!msg.getGameId().equals(game.getGameId())) throw new IllegalArgumentException("Invalid game id.");
        if (!gameStarted) throw new IllegalArgumentException("Game is not started.");
        broadcast(msg);
    }

    @WsSubscribe
    public void handlePostChat(WebSocket ws, PostChatMessage msg) {
        if (!msg.getGameId().equals(game.getGameId())) throw new IllegalArgumentException("Invalid game id.");
        String sessionId = getSessionId(ws);
        ChatMessage reMsg = new ChatMessage(sessionId, msg.getText());
        reMsg.setGameId(msg.getGameId());
        broadcast(reMsg);
    }

    public void send(WebSocket ws, WsMessage message) {
        ws.send(parser.toJson(message));
    }

    public void broadcast(WsMessage data) {
        String payload = parser.toJson(data);
        for (WebSocket ws : connections.keySet()) {
            ws.send(payload);
        }
    }

    public static class StandaloneSimpleServer extends SimpleServer {

        public StandaloneSimpleServer(InetSocketAddress address, SimpleServerErrorHandler errHandler) {
            super(address, errHandler);
        }

        @WsSubscribe
        public void handleStandaloneGameOver(WebSocket ws, GameOverMessage msg) {
            for (WebSocket conn : connections.keySet()) {
                conn.close();
            }
            connections.clear();
            createGame(null, null, null);
            logger.info("Game finished. Starting a new one.");
        }
    }

    public static void main(String[] args) {
        final Logger logger = LoggerFactory.getLogger(SimpleServer.class);
        int port = ConfigLoader.DEFAULT_PORT;
        String portStr = System.getProperty("port");
        if (portStr != null && portStr.length() > 0) {
            port = Integer.parseInt(portStr);
        }
        StandaloneSimpleServer server = new StandaloneSimpleServer(new InetSocketAddress(port), new SimpleServerErrorHandler() {
            @Override
            public void onError(WebSocket ws, Exception ex) {
                logger.error(ex.getMessage(), ex);
            }
        });
        server.createGame(null, null, null);
        server.start();
        logger.info("Simple server started on port {}", port);
    }
}
