package com.jcloisterzone.wsio.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
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
import com.jcloisterzone.PlayerClock;
import com.jcloisterzone.VersionComparator;
import com.jcloisterzone.config.ConfigLoader;
import com.jcloisterzone.game.CustomRule;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.GameSettings;
import com.jcloisterzone.game.PlayerSlot;
import com.jcloisterzone.game.Snapshot;
import com.jcloisterzone.wsio.MessageDispatcher;
import com.jcloisterzone.wsio.MessageParser;
import com.jcloisterzone.wsio.WsSubscribe;
import com.jcloisterzone.wsio.message.ChatMessage;
import com.jcloisterzone.wsio.message.ClientUpdateMessage;
import com.jcloisterzone.wsio.message.ClientUpdateMessage.ClientState;
import com.jcloisterzone.wsio.message.ClockMessage;
import com.jcloisterzone.wsio.message.CommitMessage;
import com.jcloisterzone.wsio.message.DeployFlierMessage;
import com.jcloisterzone.wsio.message.ErrorMessage;
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

    private final SimpleServerErrorHandler errHandler;

    private MessageParser parser = new MessageParser();
    private MessageDispatcher dispatcher = new MessageDispatcher();

    private GameSettings game;
    protected final ServerPlayerSlot[] slots;
    protected int slotSerial;
    private final List<String> replay = new ArrayList<String>();

    private Snapshot snapshot;
    private boolean gameStarted;

    private long[] clocks;
    private int runningClock;
    private long runningSince;

    protected final Map<WebSocket, ServerRemoteClient> connections = new HashMap<>();
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
        runningClock = -1;
        gameStarted = false;
        replay.clear();
        this.snapshot = null;
        this.hostClientId = hostClientId;
        game = new GameSettings(KeyUtils.createRandomId());
        if (snapshot != null) {
            this.snapshot =  snapshot;
            game.getExpansions().addAll(snapshot.getExpansions());
            game.getCustomRules().putAll(snapshot.getCustomRules());
            loadSlotsFromSnapshot();
        } else if (settings != null) {
            game.getExpansions().addAll(settings.getExpansions());
            game.getCustomRules().putAll(settings.getCustomRules());
            loadSlotsFromGame(settings);
        } else {
            game.getExpansions().add(Expansion.BASIC);
            game.getCustomRules().putAll(CustomRule.getDefaultRules());
            for (int i = 0; i < slots.length; i++) {
                slots[i] = new ServerPlayerSlot(i);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void loadSlotsFromGame(Game settings) {
        //Game is game from client since, so we can use isLocalHuman
        int maxSerial = 0;
        for (Player player : settings.getAllPlayers()) {
            int slotNumber = player.getSlot().getNumber();
            ServerPlayerSlot slot = new ServerPlayerSlot(slotNumber);
            slots[slotNumber] = slot;
            boolean isAi = player.getSlot().isAi();
            if (player.isLocalHuman() || isAi) {
                if (isAi) {
                    String className = player.getSlot().getAiClassName();
                    try {
                        EnumSet<Expansion> supported = (EnumSet<Expansion>) Class.forName(className).getMethod("supportedExpansions").invoke(null);
                        slot.setSupportedExpansions(supported.toArray(new Expansion[supported.size()]));
                        slot.setAiClassName(className);
                    } catch (Exception e) {
                        logger.warn("AI class is not present " + className);
                        continue;
                    }
                }
                slot.setNickname(player.getNick());
                slot.setAutoAssignClientId(player.getSlot().getClientId());
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
            slot.setAutoAssignClientId(player.getSlot().getClientId());
            slots[slotNumber] = slot;
        }
    }


    @Override
    public void onClose(WebSocket ws, int code, String reason, boolean remote) {
        if (!remote) return;
        RemoteClient conn = connections.remove(ws);
        if (conn == null) return;

        for (ServerPlayerSlot slot : slots) {
            if (slot != null && conn.getSessionId().equals(slot.getSessionId())) {
                if (!gameStarted) {
                    leaveSlot(slot);
                } else {
                    slot.setSessionId(null);
                    broadcast(newSlotMessage(slot), false);
                }
            }
        }
        broadcast(new ClientUpdateMessage(game.getGameId(), conn.getSessionId(), null, ClientState.OFFLINE), false);
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
        SlotMessage msg = new SlotMessage(game.getGameId(), slot.getNumber(), slot.getSerial(), slot.getSessionId(), slot.getClientId(), slot.getNickname());
        msg.setAiClassName(slot.getAiClassName());
        msg.setSupportedExpansions(slot.getSupportedExpansions());
        return msg;
    }

    private GameMessage newGameMessage(boolean includeReplay) {
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
        if (includeReplay) {
            gm.setReplay(replay.toArray(new String[replay.size()]));
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

    private boolean isParticipant(String clientId, String secret) {
        for (int i = 0; i < slots.length; i++) {
            if (clientId.equals(slots[i].getClientId()) && secret.equals(slots[i].getSecret())) {
                return true;
            }
        }
        return false;
    }

    private boolean shouldAutoAssign(HelloMessage msg, String sessionId, ServerPlayerSlot slot) {
        if (slot == null || slot.getSessionId() != null) return false;
        if (gameStarted) {
            return msg.getClientId().equals(slot.getClientId()) && msg.getSecret().equals(slot.getSecret());
        } else {
            boolean isHostClient = msg.getClientId().equals(hostClientId);
            return msg.getClientId().equals(slot.getAutoAssignClientId()) || (isHostClient && slot.getAiClassName() != null);
        }
    }

    @WsSubscribe
    public void handleHello(WebSocket ws, HelloMessage msg) {
        if (new VersionComparator().compare(Application.PROTCOL_VERSION, msg.getProtocolVersion()) != 0) {
            send(ws, new ErrorMessage(ErrorMessage.BAD_VERSION, "Protocol version " + Application.PROTCOL_VERSION + " required."));
            ws.close();
            return;
        }
        if (gameStarted) {
            if (!isParticipant(msg.getClientId(), msg.getSecret())) {
                send(ws, new ErrorMessage(ErrorMessage.NOT_ALLOWED, "Join not allowed."));
                ws.close();
                return;
            }
        }
        String nickname = msg.getNickname() + '@' + getWebsocketHost(ws);
        String sessionId = KeyUtils.createRandomId();
        ServerRemoteClient client = new ServerRemoteClient(sessionId, nickname, ClientState.ACTIVE);
        client.setClientId(msg.getClientId());
        client.setSecret(msg.getSecret());

        for (ServerPlayerSlot slot : slots) {
            if (shouldAutoAssign(msg, sessionId, slot)) {
                slot.setClientId(msg.getClientId());
                slot.setSessionId(sessionId);
                slot.setSecret(msg.getSecret());
                broadcast(newSlotMessage(slot), false);
            }
        }

        //add after broadcasting slot update
        connections.put(ws, client);

        send(ws, new WelcomeMessage(sessionId, nickname, 120, System.currentTimeMillis()));
        send(ws, newGameMessage(gameStarted));
        for (ServerRemoteClient rc : connections.values()) {
            if (!rc.getSessionId().equals(sessionId)) {
                send(ws, new ClientUpdateMessage(game.getGameId(), rc.getSessionId(), rc.getName(), ClientState.ACTIVE));
            }
        }
        broadcast(new ClientUpdateMessage(game.getGameId(), sessionId, nickname, ClientState.ACTIVE), false);
        if (gameStarted) {
            long ts = System.currentTimeMillis();
            long[] clocksCopy = Arrays.copyOf(clocks, clocks.length);
            if (runningClock != -1) {
                clocksCopy[runningClock] += ts-runningSince;
             }
            ClockMessage clockMsg = new ClockMessage(game.getGameId(), runningClock == -1 ? null : runningClock, clocksCopy, ts);
            send(ws, clockMsg);
        }
    }


    @WsSubscribe
    public void handleGameSetupMessage(WebSocket ws, GameSetupMessage msg) {
        if (!msg.getGameId().equals(game.getGameId())) throw new IllegalArgumentException("Invalid game id.");
        if (gameStarted) throw new IllegalArgumentException("Game is already started.");
        game.getExpansions().clear();
        game.getExpansions().addAll(msg.getExpansions());
        game.getCustomRules().clear();
        game.getCustomRules().putAll(msg.getRules());
        broadcast(msg, false);
    }


    @WsSubscribe
    public void handleTakeSlot(WebSocket ws, TakeSlotMessage msg) {
        if (!msg.getGameId().equals(game.getGameId())) throw new IllegalArgumentException("Invalid game id.");
        if (gameStarted) throw new IllegalArgumentException("Game is already started.");
        ServerRemoteClient client = connections.get(ws);
        String sessionId = client.getSessionId();
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
        slot.setSessionId(sessionId);
        slot.setSupportedExpansions(msg.getSupportedExpansions());
        slot.setClientId(client.getClientId());
        slot.setSecret(client.getSecret());
        broadcast(newSlotMessage(slot), false);
    }

    private void leaveSlot(ServerPlayerSlot slot) {
        if (snapshot == null) {
            slot.setNickname(null);
            slot.setAiClassName(null);
            slot.setSupportedExpansions(null);
        }
        slot.setSerial(null);
        slot.setSessionId(null);
        slot.setClientId(null);
        slot.setSecret(null);
        broadcast(newSlotMessage(slot), false);
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
        broadcast(msg, false);
    }

    @WsSubscribe
    public void handleSetRule(WebSocket ws, SetRuleMessage msg) {
        if (!msg.getGameId().equals(game.getGameId())) throw new IllegalArgumentException("Invalid game id.");
        if (gameStarted) throw new IllegalArgumentException("Game is already started.");
        CustomRule rule = msg.getRule();
        game.getCustomRules().put(rule, msg.getValue());
        broadcast(msg, false);
    }

    @WsSubscribe
    public void handleStartGame(WebSocket ws, StartGameMessage msg) {
        if (!msg.getGameId().equals(game.getGameId())) throw new IllegalArgumentException("Invalid game id.");
        if (gameStarted) throw new IllegalArgumentException("Game is already started.");
        runningClock = -1;
        if (snapshot == null) {
            int playerCount = 0;
            for (ServerPlayerSlot slot : slots) {
                if (!slot.isOccupied()) continue;
                playerCount++;
                if (slot.getSupportedExpansions() != null) {
                    game.getExpansions().retainAll(Arrays.asList(slot.getSupportedExpansions()));
                }
                if (game.getBooleanValue(CustomRule.RANDOM_SEATING_ORDER)) {
                    slot.setSerial(random.nextInt());
                }
            }
            clocks = new long[playerCount];
        } else {
            List<Player> players = snapshot.getPlayers();
            clocks = new long[players.size()];
            for (int i = 0; i < clocks.length; i++) {
                PlayerClock clock = players.get(i).getClock();
                clocks[i] = clock.resetRunning();
                if (clock.isRunning()) {
                    runningClock = i;
                }
            }
            runningSince = System.currentTimeMillis();
        }
        gameStarted = true;
        broadcast(newGameMessage(false), false);
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
        broadcast(clockMsg, false);
    }

    @WsSubscribe
    public void handleDeployFlier(WebSocket ws, DeployFlierMessage msg) {
        if (!msg.getGameId().equals(game.getGameId())) throw new IllegalArgumentException("Invalid game id.");
        if (!gameStarted) throw new IllegalArgumentException("Game is not started.");
        msg.setCurrentTime(System.currentTimeMillis());
        broadcast(msg, true);
    }

    @WsSubscribe
    public void handleCommit(WebSocket ws, CommitMessage msg) {
        if (!msg.getGameId().equals(game.getGameId())) throw new IllegalArgumentException("Invalid game id.");
        if (!gameStarted) throw new IllegalArgumentException("Game is not started.");
        msg.setCurrentTime(System.currentTimeMillis());
        broadcast(msg, true);
    }

    @WsSubscribe
    public void handleRmi(WebSocket ws, RmiMessage msg) {
        if (!msg.getGameId().equals(game.getGameId())) throw new IllegalArgumentException("Invalid game id.");
        if (!gameStarted) throw new IllegalArgumentException("Game is not started.");
        broadcast(msg, true);
    }

    @WsSubscribe
    public void handleUndo(WebSocket ws, UndoMessage msg) {
        if (!msg.getGameId().equals(game.getGameId())) throw new IllegalArgumentException("Invalid game id.");
        if (!gameStarted) throw new IllegalArgumentException("Game is not started.");
        broadcast(msg, true);
    }

    @WsSubscribe
    public void handlePostChat(WebSocket ws, PostChatMessage msg) {
        if (!msg.getGameId().equals(game.getGameId())) throw new IllegalArgumentException("Invalid game id.");
        String sessionId = getSessionId(ws);
        ChatMessage reMsg = new ChatMessage(sessionId, msg.getText());
        reMsg.setGameId(msg.getGameId());
        broadcast(reMsg, false);
    }

    public void send(WebSocket ws, WsMessage message) {
        ws.send(parser.toJson(message));
    }

    public void broadcast(WsMessage data, boolean recordReplay) {
        String payload = parser.toJson(data);
        if (recordReplay) {
            replay.add(payload);
        }
        for (WebSocket ws : connections.keySet()) {
        	if (ws.isOpen()) { //prevent exception when server is closing
        		ws.send(payload);
        	}
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
