package com.jcloisterzone.wsio.server;

import com.jcloisterzone.Expansion;
import com.jcloisterzone.KeyUtils;
import com.jcloisterzone.VersionComparator;
import com.jcloisterzone.ai.AiPlayer;
import com.jcloisterzone.config.ConfigLoader;
import com.jcloisterzone.game.*;
import com.jcloisterzone.game.capability.StandardGameCapability;
import com.jcloisterzone.game.save.SavedGame;
import com.jcloisterzone.game.save.SavedGame.SavedGamePlayerSlot;
import com.jcloisterzone.ui.JCloisterZone;
import com.jcloisterzone.wsio.MessageDispatcher;
import com.jcloisterzone.wsio.MessageParser;
import com.jcloisterzone.wsio.WsSubscribe;
import com.jcloisterzone.wsio.message.*;
import com.jcloisterzone.wsio.message.ClientUpdateMessage.ClientState;
import com.jcloisterzone.wsio.message.GameMessage.GameStatus;
import io.vavr.collection.HashMap;
import io.vavr.collection.Map;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class SimpleServer extends WebSocketServer  {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private final SimpleServerErrorHandler errHandler;

    private MessageParser parser = new MessageParser();
    private MessageDispatcher dispatcher = new MessageDispatcher();

    private GameSetup gameSetup;
    private String gameId;
    private long initialSeed;
    protected final ServerPlayerSlot[] slots;
    protected int slotSerial;
    private List<WsReplayableMessage> replay;

    private SavedGame savedGame;
    private boolean gameStarted;
    private Long clockStart;
    private String chainMessageId;

    protected Map<WebSocket, ServerRemoteClient> connections = HashMap.empty();
    protected java.util.Set<String> alreadyReceived = new java.util.HashSet();
    private String hostClientId;

    private Random random = new Random();

    public interface SimpleServerErrorHandler {
        void onError(WebSocket ws, final Exception ex);
    }

    public SimpleServer(InetSocketAddress address, SimpleServerErrorHandler errHandler) {
        super(address);
//        setReuseAddr(true);
//        if (System.getProperty("hearthbeat") != null) {
//            setConnectionLostTimeout(Integer.parseInt(System.getProperty("hearthbeat")));
//        } else {
//            setConnectionLostTimeout(Connection.DEFAULT_HEARTHBEAT_INTERVAL);
//        }

        this.errHandler = errHandler;
        slots = new ServerPlayerSlot[PlayerSlot.COUNT];
    }

    public void createGame(SavedGame savedGame, Game game, String hostClientId) {
        slotSerial = 0;
        gameStarted = false;
        this.savedGame = savedGame;
        this.hostClientId = hostClientId;

        if (savedGame != null) {
            gameId = savedGame.getGameId();
            initialSeed = savedGame.getInitialSeed();
            gameSetup = savedGame.getSetup().asGameSetup();
            replay = new ArrayList<>(savedGame.getReplay());
            loadSlotsFromSavedGame(savedGame);
        } else {
            gameId = KeyUtils.createRandomId();
            initialSeed = random.nextLong();
            replay = new ArrayList<>();
            for (int i = 0; i < slots.length; i++) {
                slots[i] = new ServerPlayerSlot(i);
            }
            if (game == null) {
                gameSetup = new GameSetup(
                    io.vavr.collection.HashMap.of(Expansion.BASIC, 1),
                    io.vavr.collection.HashSet.of(StandardGameCapability.class),
                    Rule.getDefaultRules()
                );
            } else {
                gameSetup = game.getSetup();
                int maxSerial = 0;
                for (PlayerSlot slot : game.getPlayerSlots()) {
                    boolean ownedByCreator = hostClientId != null && hostClientId.equals(slot.getClientId());
                    if (ownedByCreator || slot.getAiClassName() != null) {
                        int idx = slot.getNumber();
                        slots[idx].setAutoAssignClientId(hostClientId);
                        slots[idx].setNickname(slot.getNickname());
                        slots[idx].setSerial(slot.getSerial());
                        slots[idx].setAiClassName(slot.getAiClassName());
                        if (slot.getAiClassName() != null) {
                            try {
                                AiPlayer aiPlayer = (AiPlayer) Class.forName(slot.getAiClassName()).newInstance();
                                slots[idx].setSupportedSetup(aiPlayer.supportedSetup());
                            } catch (Exception e) {
                                logger.error(e.getMessage(), e);
                            }
                        }
                        maxSerial = Math.max(maxSerial, slot.getSerial());
                    }
                }
                slotSerial = maxSerial + 1;
            }

        }
    }

    private void loadSlotsFromSavedGame(SavedGame savedGame) {
        int maxSerial = 0;
        for (SavedGamePlayerSlot sgSlot : savedGame.getSlots()) {
            int idx = sgSlot.getNumber();
            slots[idx] = new ServerPlayerSlot(idx);
            slots[idx].setAutoAssignClientId(sgSlot.getClientId());
            slots[idx].setNickname(sgSlot.getNickname());
            slots[idx].setSerial(sgSlot.getSerial());
            slots[idx].setAiClassName(sgSlot.getAiClassName());
            maxSerial = Math.max(maxSerial, sgSlot.getSerial());
        }
        slotSerial = maxSerial + 1;
    }

    @Override
    public void onClose(WebSocket ws, int code, String reason, boolean remote) {
        if (!remote) return;
        RemoteClient conn = connections.get(ws).getOrNull();
        if (conn == null) {
            return;
        }

        connections = connections.remove(ws);

        for (ServerPlayerSlot slot : slots) {
            if (slot != null && conn.getSessionId().equals(slot.getSessionId())) {
                if (!gameStarted) {
                    leaveSlot(slot);
                } else {
                    slot.setSessionId(null);
                    broadcast(newSlotMessage(slot));
                }
            }
        }
        ClientUpdateMessage msg = new ClientUpdateMessage(conn.getSessionId(), null, ClientState.OFFLINE);
        msg.setGameId(gameId);
        broadcast(msg);
    }

    @Override
    public void onError(WebSocket ws, final Exception ex) {
        errHandler.onError(ws, ex);
    }

//    @Override
//    public void onStart() {
//
//    }

    @Override
    synchronized public void onMessage(WebSocket ws, String payload) {
        logger.info(payload);
        WsMessage msg = parser.fromJson(payload);
        if (alreadyReceived.contains(msg.getMessageId())) {
            logger.info("Dropping already received message. " + payload);
        } else {
            if (msg instanceof WsChainedMessage) {
                String parentId = ((WsChainedMessage) msg).getParentId();
                if (parentId != null && !parentId.equals(chainMessageId)) {
                    logger.info("Invalid parent id");
                    return;
                }
                chainMessageId = msg.getMessageId();
            }
            alreadyReceived.add(msg.getMessageId());
            dispatcher.dispatch(msg, ws, this);
        }

    }

    @Override
    public void onOpen(WebSocket ws, ClientHandshake hs) {
    }

    private String getSessionId(WebSocket ws) {
        return connections.get(ws).get().getSessionId();
    }

    private SlotMessage newSlotMessage(ServerPlayerSlot slot) {
        SlotMessage msg = new SlotMessage(slot.getNumber(), slot.getSerial(), slot.getSessionId(), slot.getClientId(), slot.getNickname());
        msg.setGameId(gameId);
        msg.setAiClassName(slot.getAiClassName());
        msg.setSupportedSetup(slot.getSupportedSetup());
        return msg;
    }

    @Override
    public void start() {
        super.start();
    }

    private GameMessage newGameMessage(boolean includeReplay) {
        GameSetupMessage setupMessage = new GameSetupMessage(
            gameSetup.getRules().toJavaMap(),
            gameSetup.getCapabilities().toJavaSet(),
            gameSetup.getExpansions().toJavaMap()
        );
        setupMessage.setGameId(gameId);

        GameStatus status;
        if (gameStarted) {
            status = GameStatus.RUNNING;
        } else if (savedGame == null) {
            status = GameStatus.OPEN;
        } else {
            status = GameStatus.PAUSED;
        }
        GameMessage gm = new GameMessage(gameId, "", status, setupMessage);
        gm.setInitialSeed(initialSeed);
        gm.setClockStart(clockStart);
        List<SlotMessage> slotMsgs = new ArrayList<>();
        for (ServerPlayerSlot slot : slots) {
            if (slot != null) {
                SlotMessage sm = newSlotMessage(slot);
                slotMsgs.add(sm);
            }
        }
        gm.setSlots(slotMsgs.toArray(new SlotMessage[slotMsgs.size()]));
        if (includeReplay) {
            gm.setReplay(replay);
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
            if (slots[i] != null && clientId.equals(slots[i].getClientId()) && secret.equals(slots[i].getSecret())) {
                return true;
            }
        }
        return false;
    }

    private boolean shouldAutoAssign(HelloMessage msg, String sessionId, ServerPlayerSlot slot) {
        if (gameStarted) {
            return msg.getClientId().equals(slot.getClientId()) && msg.getSecret().equals(slot.getSecret());
        } else {
            if (slot.getSessionId() == null) {
                boolean isHostClient = msg.getClientId().equals(hostClientId);
                return msg.getClientId().equals(slot.getAutoAssignClientId()) || (isHostClient && slot.getAiClassName() != null);
            } else {
                return false;
            }
        }
    }

    private long createSalt() {
        return System.currentTimeMillis();
    }

    private void closeStaleConnections(String sessionId) {
        connections
            .filter((ws, client) -> client.getSessionId() == sessionId)
            .forEach((ws, client) -> { ws.close(); });
    }

    @WsSubscribe
    public void handleHello(WebSocket ws, HelloMessage msg) {
        if (new VersionComparator().compare(JCloisterZone.PROTCOL_VERSION, msg.getProtocolVersion()) != 0) {
            send(ws, new ErrorMessage(ErrorMessage.BAD_VERSION, "Protocol version " + JCloisterZone.PROTCOL_VERSION + " required."));
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
            if (slot != null && shouldAutoAssign(msg, sessionId, slot)) {
                if (gameStarted && slot.getSessionId() != null) {
                    // when already paired with session, close old session and pair with new one
                    closeStaleConnections(slot.getSessionId());
                }

                slot.setClientId(msg.getClientId());
                slot.setSessionId(sessionId);
                slot.setSecret(msg.getSecret());
                broadcast(newSlotMessage(slot));
            }
        }

        //add after broadcasting slot update
        connections = connections.put(ws, client);

        send(ws, new WelcomeMessage(sessionId, nickname, 120, System.currentTimeMillis()));
        send(ws, newGameMessage(gameStarted));
        for (ServerRemoteClient rc : connections.values()) {
            if (!rc.getSessionId().equals(sessionId)) {
                ClientUpdateMessage updateMsg = new ClientUpdateMessage(rc.getSessionId(), rc.getName(), ClientState.ACTIVE);
                updateMsg.setGameId(gameId);
                send(ws, updateMsg);
            }
        }
        ClientUpdateMessage updateMsg = new ClientUpdateMessage(sessionId, nickname, ClientState.ACTIVE);
        updateMsg.setGameId(gameId);
        broadcast(updateMsg);
    }


    @WsSubscribe
    public void handleGameSetupMessage(WebSocket ws, GameSetupMessage msg) {
        if (!msg.getGameId().equals(gameId)) throw new IllegalArgumentException("Invalid game id.");
        if (gameStarted) throw new IllegalArgumentException("Game is already started.");
        gameSetup = new GameSetup(
            io.vavr.collection.HashMap.ofAll(msg.getExpansions()),
            io.vavr.collection.HashSet.ofAll(msg.getCapabilities()),
            io.vavr.collection.HashMap.ofAll(msg.getRules())
        );
        broadcast(msg);
    }


    @WsSubscribe
    public void handleTakeSlot(WebSocket ws, TakeSlotMessage msg) {
        if (!msg.getGameId().equals(gameId)) throw new IllegalArgumentException("Invalid game id.");
        if (gameStarted) throw new IllegalArgumentException("Game is already started.");
        ServerRemoteClient client = connections.get(ws).get();
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
        slot.setSupportedSetup(msg.getSupportedSetup());
        slot.setClientId(client.getClientId());
        slot.setSecret(client.getSecret());
        broadcast(newSlotMessage(slot));
    }

    private void leaveSlot(ServerPlayerSlot slot) {
        if (savedGame == null) {
            slot.setNickname(null);
            slot.setAiClassName(null);
            slot.setSupportedSetup(null);
        }
        slot.setSerial(null);
        slot.setSessionId(null);
        slot.setClientId(null);
        slot.setSecret(null);
        broadcast(newSlotMessage(slot));
    }

    @WsSubscribe
    public void handleLeaveSlot(WebSocket ws, LeaveSlotMessage msg) {
        if (!msg.getGameId().equals(gameId)) throw new IllegalArgumentException("Invalid game id.");
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
        if (!msg.getGameId().equals(gameId)) throw new IllegalArgumentException("Invalid game id.");
        if (gameStarted) throw new IllegalArgumentException("Game is already started.");
        Expansion expansion = msg.getExpansion();
        gameSetup = gameSetup.mapExpansions(expansions ->
            msg.getCount() > 0 ? expansions.put(expansion, msg.getCount()) : expansions.remove(expansion)
        );
        broadcast(msg);
    }

    @WsSubscribe
    public void handleSetRule(WebSocket ws, SetRuleMessage msg) {
        if (!msg.getGameId().equals(gameId)) throw new IllegalArgumentException("Invalid game id.");
        if (gameStarted) throw new IllegalArgumentException("Game is already started.");
        Rule rule = msg.getRule();
        gameSetup = gameSetup.mapRules(rules ->
            msg.getValue() == null ? rules.remove(rule) : rules.put(rule, msg.getValue())
        );
        broadcast(msg);
    }

    @WsSubscribe
    public void handleSetCapability(WebSocket ws, SetCapabilityMessage msg) {
        if (!msg.getGameId().equals(gameId)) throw new IllegalArgumentException("Invalid game id.");
        if (gameStarted) throw new IllegalArgumentException("Game is already started.");
        Class<? extends Capability<?>> cap = msg.getCapability();
        gameSetup = gameSetup.mapCapabilities(cps ->
            msg.isEnabled() ? cps.add(cap) : cps.remove(cap)
        );
        broadcast(msg);
    }

    @WsSubscribe
    public void handleStartGame(WebSocket ws, StartGameMessage msg) {
        if (!msg.getGameId().equals(gameId)) throw new IllegalArgumentException("Invalid game id.");
        if (gameStarted) throw new IllegalArgumentException("Game is already started.");
        if (savedGame == null) {
            int playerCount = 0;
            for (ServerPlayerSlot slot : slots) {
                if (!slot.isOccupied()) continue;
                playerCount++;
//                if (slot.getSupportedExpansions() != null) {
//                    gameSetup.getExpansions().retainAll(Arrays.asList(slot.getSupportedExpansions()));
//                }
                if (gameSetup.getBooleanValue(Rule.RANDOM_SEATING_ORDER)) {
                    slot.setSerial(random.nextInt());
                }
            }
            clockStart = System.currentTimeMillis();
        } else {
            // for saved games, CLOCK message is emitted by active client after load
            clockStart = System.currentTimeMillis() - savedGame.getClock();
        }
        gameStarted = true;
        broadcast(newGameMessage(true));
    }


    private void handleInGameMessage(WsInGameMessage msg) {
        if (!msg.getGameId().equals(gameId)) throw new IllegalArgumentException("Invalid game id.");
        if (!gameStarted) throw new IllegalArgumentException("Game is not started.");
        if (msg instanceof WsSaltMessage) {
            ((WsSaltMessage) msg).setSalt(createSalt());
        }
        broadcast(msg);
    }

    @WsSubscribe
    public void handleSyncGame(WebSocket ws, SyncGameMessage msg) {
        send(ws, newGameMessage(gameStarted));
    }

    @WsSubscribe
    public void handleDeployFlier(WebSocket ws, DeployFlierMessage msg) {
        handleInGameMessage(msg);
    }

    @WsSubscribe
    public void handleCommit(WebSocket ws, CommitMessage msg) {
        handleInGameMessage(msg);
    }

    @WsSubscribe
    public void handlePass(WebSocket ws, PassMessage msg) {
        handleInGameMessage(msg);
    }

    @WsSubscribe
    public void handlePlaceTile(WebSocket ws, PlaceTileMessage msg) {
        handleInGameMessage(msg);
    }

    @WsSubscribe
    public void handleDeployMeeple(WebSocket ws, DeployMeepleMessage msg) {
        handleInGameMessage(msg);
    }

    @WsSubscribe
    public void handleReturnMeeple(WebSocket ws, ReturnMeepleMessage msg) {
        handleInGameMessage(msg);
    }

    @WsSubscribe
    public void handleMoveNeutralFigureMessage(WebSocket ws, MoveNeutralFigureMessage msg) {
        handleInGameMessage(msg);
    }

    @WsSubscribe
    public void handlePlaceTokenMessage(WebSocket ws, PlaceTokenMessage msg) {
        handleInGameMessage(msg);
    }

    @WsSubscribe
    public void handleCaptureFollowerMessage(WebSocket ws, CaptureFollowerMessage msg) {
        handleInGameMessage(msg);
    }

    @WsSubscribe
    public void handlePayRansomMessage(WebSocket ws, PayRansomMessage msg) {
        handleInGameMessage(msg);
    }

    @WsSubscribe
    public void handleExchangeFollowerChoiceMessage(WebSocket ws, ExchangeFollowerChoiceMessage msg) {
        handleInGameMessage(msg);
    }

    @WsSubscribe
    public void handleBazaarBidMessage(WebSocket ws, BazaarBidMessage msg) {
        handleInGameMessage(msg);
    }

    @WsSubscribe
    public void handleBazaarBuyOrSellMessage(WebSocket ws, BazaarBuyOrSellMessage msg) {
        handleInGameMessage(msg);
    }

    @WsSubscribe
    public void handleCornCircleRemoveOrDeployMessage(WebSocket ws, CornCircleRemoveOrDeployMessage msg) {
        handleInGameMessage(msg);
    }

    @WsSubscribe
    public void hadleFlockMessage(WebSocket ws, FlockMessage msg) {
    	handleInGameMessage(msg);
    }

    @WsSubscribe
    public void handleGameOverMessage(WebSocket ws, GameOverMessage msg) {
        handleInGameMessage(msg);
    }

    @WsSubscribe
    public void handleUndo(WebSocket ws, UndoMessage msg) {
        if (!msg.getGameId().equals(gameId)) throw new IllegalArgumentException("Invalid game id.");
        if (!gameStarted) throw new IllegalArgumentException("Game is not started.");
        replay.remove(replay.size() - 1);
        broadcast(msg);
    }

    @WsSubscribe
    public void handlePostChat(WebSocket ws, PostChatMessage msg) {
        if (!msg.getGameId().equals(gameId)) throw new IllegalArgumentException("Invalid game id.");
        String sessionId = getSessionId(ws);
        ChatMessage reMsg = new ChatMessage(sessionId, msg.getText());
        reMsg.setGameId(msg.getGameId());
        broadcast(reMsg);
    }

    private void beforeSend(WsMessage msg) {
        if (msg.getMessageId() == null) {
            msg.setMessageId(UUID.randomUUID().toString());
        }
        if (gameStarted && msg instanceof WsReplayableMessage) {
            replay.add((WsReplayableMessage) msg);
            ((WsReplayableMessage) msg).setClock(System.currentTimeMillis() - clockStart);
        }
    }

    public void send(WebSocket ws, WsMessage msg) {
        beforeSend(msg);
        ws.send(parser.toJson(msg));
    }

    public void broadcast(WsMessage msg) {
        for (WebSocket ws : connections.keySet()) {
            if (ws.isOpen()) { //prevent exception when server is closing
                send(ws, msg);
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
            connections = HashMap.empty();
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
