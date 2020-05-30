package com.jcloisterzone.ui;

// TODO underscore is deprecated as an identifier, from Java 9 it will be a reserved word
import static com.jcloisterzone.ui.I18nUtils._tr;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.jcloisterzone.wsio.message.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcloisterzone.Expansion;
import com.jcloisterzone.ai.AiPlayer;
import com.jcloisterzone.board.TilePack;
import com.jcloisterzone.config.Config.AutostartConfig;
import com.jcloisterzone.config.Config.DebugConfig;
import com.jcloisterzone.config.Config.PresetConfig;
import com.jcloisterzone.debug.ForcedDrawTilePack;
import com.jcloisterzone.event.ChatEvent;
import com.jcloisterzone.event.ClientListChangedEvent;
import com.jcloisterzone.event.GameListChangedEvent;
import com.jcloisterzone.event.setup.CapabilityChangeEvent;
import com.jcloisterzone.event.setup.ExpansionChangedEvent;
import com.jcloisterzone.event.setup.PlayerSlotChangeEvent;
import com.jcloisterzone.event.setup.RuleChangeEvent;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.GameSetup;
import com.jcloisterzone.game.PlayerSlot;
import com.jcloisterzone.game.PlayerSlot.SlotState;
import com.jcloisterzone.game.Rule;
import com.jcloisterzone.online.Channel;
import com.jcloisterzone.ui.view.ChannelView;
import com.jcloisterzone.ui.view.GameSetupView;
import com.jcloisterzone.wsio.Connection;
import com.jcloisterzone.wsio.MessageDispatcher;
import com.jcloisterzone.wsio.MessageListener;
import com.jcloisterzone.wsio.WebSocketConnection;
import com.jcloisterzone.wsio.WsSubscribe;
import com.jcloisterzone.wsio.message.ChannelMessage.ChannelMessageGame;
import com.jcloisterzone.wsio.message.ClientUpdateMessage.ClientState;
import com.jcloisterzone.wsio.message.GameMessage.GameStatus;
import com.jcloisterzone.wsio.server.RemoteClient;


public class ClientMessageListener implements MessageListener {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private WebSocketConnection conn;
    private MessageDispatcher dispatcher = new MessageDispatcher();

    private Map<String, GameController> gameControllers = new HashMap<>();
    private Map<String, ChannelController> channelControllers = new HashMap<>();

    private final boolean playOnline;
    private final Client client;
    private boolean autostartPerfomed;

    public ClientMessageListener(Client client, boolean playOnline) {
        this.client = client;
        this.playOnline = playOnline;
    }

    public WebSocketConnection connect(String username, URI uri) {
        conn = new WebSocketConnection(username, client.getConfig(), uri, this);
        return conn;
    }

    public boolean isPlayOnline() {
        return playOnline;
    }

    public Connection getConnection() {
        return conn;
    }

    public Map<String, ChannelController> getChannelControllers() {
        return channelControllers;
    }

    public List<GameController> getGameControllers(String channel) {
        List<GameController> gcs = new ArrayList<GameController>();
        for (GameController gc : gameControllers.values()) {
            if (gc.getChannel().equals(channel)) {
                gcs.add(gc);
            }
        }
        return gcs;
    }

    @Override
    public void onWebsocketError(Exception ex) {
        client.onWebsocketError(ex);
    }

    @Override
    public void onWebsocketClose(int code, String reason, boolean remote) {
        // do not clear controllers, may reconnect and use them
        client.onWebsocketClose(code, reason, remote);
    }

    @Override
    public void onWebsocketMessage(WsMessage msg) {
        //TODO pass game as context to dispatch
        EventProxyUiController<?> controller = getController(msg);
        if (controller instanceof GameController) {
            GameController gc = (GameController) controller;
            if (msg instanceof WsChainedMessage) {
                String parentId = ((WsChainedMessage) msg).getParentId();
                String chainMessageId = gc.getChainMessageIdRef().get();
                if (chainMessageId != null && !parentId.equals(chainMessageId)) {
                    logger.info("Unexpected game id. Expected {}, received {}", chainMessageId, parentId);
                    conn.send(new SyncGameMessage(gc.getGame().getGameId()));
                    return;
                }
                gc.getChainMessageIdRef().set(msg.getMessageId());
            }
            Game game = gc.getGame();
            dispatcher.dispatch(msg, conn, this, game);
        } else {
            dispatcher.dispatch(msg, conn, this);
        }
    }

    public RemoteClient getClientBySessionId(EventProxyUiController<?> controller, String sessionId) {
        if (sessionId == null) return null;
        for (RemoteClient remote: controller.getRemoteClients()) {
            if (remote.getSessionId().equals(sessionId)) {
                return remote;
            }
        }
        throw new NoSuchElementException();
    }

    private EventProxyUiController<?> getController(WsMessage msg) {
        if (msg instanceof WsInGameMessage) {
            GameController gc = gameControllers.get(((WsInGameMessage) msg).getGameId());
            if (gc != null) return gc;
        }
        if (msg instanceof WsInChannelMessage) {
            ChannelController cc = channelControllers.get(((WsInChannelMessage) msg).getChannel());
            if (cc != null) return cc;
        }
        return null;
    }

    private Game getGame(WsInGameMessage msg) {
        GameController gc = (GameController) getController(msg);
        return gc == null ? null : gc.getGame();
    }

    private void updateSlot(PlayerSlot[] slots, SlotMessage slotMsg) {
        PlayerSlot slot = slots[slotMsg.getNumber()];
        slot.setNickname(slotMsg.getNickname());
        slot.setSessionId(slotMsg.getSessionId());
        slot.setClientId(slotMsg.getClientId());
        if (slotMsg.getClientId() == null) {
            slot.setState(SlotState.OPEN);
        } else {
            slot.setState(conn.getSessionId().equals(slotMsg.getSessionId()) ? SlotState.OWN : SlotState.REMOTE);
        }
        slot.setSerial(slotMsg.getSerial());
        slot.setAiClassName(slotMsg.getAiClassName());
    }

    private void createGameSlots(GameController gc, GameMessage msg) {
        PlayerSlot[] slots = new PlayerSlot[PlayerSlot.COUNT];
        for (SlotMessage slotMsg : msg.getSlots()) {
            int number = slotMsg.getNumber();
            PlayerSlot slot = new PlayerSlot(number);
            slot.setColors(client.getConfig().getPlayerColor(slot));
            slots[number] = slot;
            updateSlot(slots, slotMsg);
        }
        gc.getGame().setSlots(slots);
    }

    private GameController createGameController(GameMessage msg) {
        Game game;
        GameController gc;
        game = new Game(msg.getGameId(), msg.getInitialSeed());
        game.setName(msg.getName());
        gc = new GameController(client, game);
        // don't set conn instance on game directly!
        // instead use proxy created by GameController -> gc.getConnection()
        game.setConnection(gc.getConnection());
        gc.setReportingTool(conn.getReportingTool());
        gc.setChannel(msg.getChannel());
        gc.setPasswordProtected(msg.isPasswordProtected());
        if (msg instanceof ChannelMessageGame) {
            for (RemoteClient client : ((ChannelMessageGame)msg).getClients()) {
                if (client.getState() == null) client.setState(ClientState.ACTIVE);
                gc.getRemoteClients().add(client);
            }
        }
        if (msg.getSlots() != null) {
            createGameSlots(gc, msg);
        }
        return gc;
    }

    private void handleGameStarted(final GameController gc, long clockStart, io.vavr.collection.List<WsReplayableMessage> replay) throws InvocationTargetException, InterruptedException {
        conn.getReportingTool().setGame(gc.getGame());
        HashMap<String, Object> annotations = gc.getClient().getSavedGameAnnotations();
        gc.getGame().setClockStart(clockStart);
        gc.getGame().start(gc, replay, annotations);

        if (!"false".equals(System.getProperty("showGameDebugModeInfo"))) {
            SwingUtilities.invokeLater(() -> {
                TilePack tilePack = gc.getGame().getState().getTilePack();
                if (!tilePack.getClass().equals(TilePack.class)) {
                    String msg = String.format("Game is running in debug mode and using %s tile pack.", tilePack.getClass().getSimpleName());
                    if (tilePack instanceof ForcedDrawTilePack) {
                        msg = String.format("%s Tiles: %s ...", msg, String.join(", ", ((ForcedDrawTilePack)tilePack).getDrawQueue().slice(0, 5)));
                    }
                    gc.getGameView().getGridPanel().showInfoMessage(msg, "GAME-IN-DEBUG-MODE");
                }
            });
        }
    }

    private void openGameSetup(final GameController gc, final GameMessage msg) throws InvocationTargetException, InterruptedException {
        SwingUtilities.invokeAndWait(() -> {
            client.mountView(new GameSetupView(client, gc, msg.getStatus() == GameStatus.OPEN));
            performAutostart(gc.getGame()); //must wait for panel is created
        });
    }

    @WsSubscribe
    public void handleGame(final GameMessage msg) throws InvocationTargetException, InterruptedException {
        handleGame(msg, false);
    }

    @SuppressWarnings("incomplete-switch")
    public GameController handleGame(final GameMessage msg, boolean channelList) throws InvocationTargetException, InterruptedException {
        msg.getGameSetup().setGameId(msg.getGameId());  //fill omitted id
        if (msg.getSlots() != null) {
            for (SlotMessage slotMsg : msg.getSlots()) {
                slotMsg.setGameId(msg.getGameId()); //fill omitted id
            }
        }

        GameController gc = (GameController) getController(msg);
        if (gc == null /*|| !msg.getReplay().isEmpty()*/) {
            /* if replay, reinit controller - for some (unknown reason this was in old code
             * but now when replaye is always non null it breaks eventBus listeners
             * need to revalidate it with play server and resume, then hopefully remove it
             */
            //copy clients to new controller
            List<RemoteClient> remoteClients = new ArrayList<>();
//            if (gc != null) {
//                remoteClients.addAll(gc.getRemoteClients());
//            }
            gc = createGameController(msg);
            gc.getRemoteClients().addAll(remoteClients);
            //TODO remove games on game over
            gameControllers.put(msg.getGameId(), gc);
        }

        handleGameSetup(msg.getGameSetup());
        if (msg.getSlots() != null) {
            createGameSlots(gc, msg);
            for (SlotMessage slotMsg : msg.getSlots()) {
                handleSlot(slotMsg);
                gc.getGame().handleSlotMessage(slotMsg);
            }
        }

        gc.setGameStatus(msg.getStatus());
        gc.getChainMessageIdRef().set(null);
        if (!channelList) {
            switch (msg.getStatus()) {
            case OPEN:
            case PAUSED:
                openGameSetup(gc, msg);
                break;
            case RUNNING:
                io.vavr.collection.List<WsReplayableMessage> replay;
                if (msg.getReplay() == null) {
                    replay = io.vavr.collection.List.empty();
                } else {
                    replay = io.vavr.collection.List.ofAll(msg.getReplay());
                }
                handleGameStarted(gc, msg.getClockStart(), replay);
                break;
            }
        }
        return gc;
    }

    @WsSubscribe
    public void handleChannel(final ChannelMessage msg) throws InvocationTargetException, InterruptedException {
        Channel channel = new Channel(msg.getName());
        final ChannelController channelController = new ChannelController(client, channel);
        channelController.getRemoteClients().addAll(Arrays.asList(msg.getClients()));
        channelControllers.clear();
        channelControllers.put(channel.getName(), channelController);
        SwingUtilities.invokeAndWait(() -> client.mountView(new ChannelView(client, channelController)));
        GameController[] gameControllers = new GameController[msg.getGames().length];
        int i = 0;
        for (ChannelMessageGame game : msg.getGames()) {
            game.setChannel(msg.getName()); //fill omitted channel
            gameControllers[i++] = handleGame(game, true);
        }
        channelController.getEventProxy().post(new GameListChangedEvent(gameControllers));
    }

    @WsSubscribe
    public void handleGameUpdate(GameUpdateMessage msg) throws InvocationTargetException, InterruptedException {
        msg.getGame().setChannel(msg.getChannel()); //fill omitted channel
        GameStatus status = msg.getGame().getStatus();
        if (GameStatus.OPEN.equals(status) || GameStatus.PAUSED.equals(status)) {
            GameController gc = (GameController) getController(msg.getGame());
            if (gc != null) {
                gc.setGameStatus(status);
                return;
            }
            handleGame(msg.getGame(), true);
        } else {
            if (GameStatus.RUNNING.equals(status) &&
                    client.getView() instanceof GameSetupView) {
                GameController gc = ((GameSetupView) client.getView()).getGameController();
                if (gc.getGame().getGameId().equals(msg.getGame().getGameId())) {
                    gc.setGameStatus(status);
                    return;
                }
            }
            gameControllers.remove(msg.getGame().getGameId());
        }
        List<GameController> gcs = getGameControllers(msg.getChannel());
        ChannelController channelController = (ChannelController) getController(msg);
        channelController.getEventProxy().post(
            new GameListChangedEvent(gcs.toArray(new GameController[gcs.size()]))
        );
    }


    @WsSubscribe
    public void handleClientUpdate(ClientUpdateMessage msg) {
        EventProxyUiController<?> controller = getController(msg);
        if (controller != null) {
            RemoteClient rc = new RemoteClient(msg.getSessionId(), msg.getName(), msg.getState());
            //TODO what about use Set?
            List<RemoteClient> clients = controller.getRemoteClients();
            if (ClientState.OFFLINE.equals(msg.getState())) {
                clients.remove(rc);
            } else {
                int idx = clients.indexOf(rc);
                if (idx == -1) {
                    clients.add(rc);
                } else {
                    clients.set(idx, rc);
                }
            }
            ArrayList<RemoteClient> frozenList = new ArrayList<>(clients);
            controller.getEventProxy().post(new ClientListChangedEvent(frozenList));
        } else {
            logger.warn("No controller for message {}", msg);
        }
    }

    @WsSubscribe
    public void handleChat(ChatMessage msg) {
        EventProxyUiController<?> controller = getController(msg);
        if (controller != null) {
            ChatEvent ev = new ChatEvent(getClientBySessionId(controller, msg.getSessionId()), msg.getText());
            controller.getEventProxy().post(ev);
        } else {
            logger.warn("No controller for message {}", msg);
        }
    }

    @WsSubscribe
    public void handleSlot(SlotMessage msg) {
        Game game = getGame(msg);
        if (game != null) {
            //slot's can be updated also for running game
            PlayerSlot[] slots = game.getPlayerSlots();
            updateSlot(slots, msg);
            game.post(new PlayerSlotChangeEvent(slots[msg.getNumber()]));
        }
    }

    @WsSubscribe
    public void handleGameSetup(GameSetupMessage msg) {
        Game game = getGame(msg);
        if (game != null) {
            game.setSetup(
                    new GameSetup(
                            io.vavr.collection.HashMap.ofAll(msg.getExpansions()),
                            io.vavr.collection.HashSet.ofAll(msg.getCapabilities()),
                            io.vavr.collection.HashMap.ofAll(msg.getRules())
                    )
            );

            for (Expansion exp : Expansion.values()) {
                game.post(new ExpansionChangedEvent(exp, game.getSetup().getExpansions().get(exp).getOrElse(0)));
            }
            for (Rule rule : Rule.values()) {
                Object value = game.getSetup().getRules().get(rule).getOrNull();
                game.post(new RuleChangeEvent(rule, value));
            }
        }
    }


    @WsSubscribe
    public void handleSetExpansion(SetExpansionMessage msg) {
        Game game = getGame(msg);
        if (game != null) {
            Expansion expansion = msg.getExpansion();
            int count = msg.getCount();
            game.mapSetup(setup -> setup.mapExpansions(expansions ->
                    count > 0 ? expansions.put(expansion, count) : expansions.remove(expansion)
            ));
            game.post(new ExpansionChangedEvent(expansion, count));
        }
    }

    @WsSubscribe
    public void handleSetRule(SetRuleMessage msg) {
        Game game = getGame(msg);
        if (game != null) {
            Rule rule = msg.getRule();
            Object value = msg.getValue();
            game.mapSetup(setup -> setup.mapRules(rules ->
                    msg.getValue() == null ? rules.remove(rule) : rules.put(rule, value)
            ));
            game.post(new RuleChangeEvent(rule, msg.getValue()));
        }
    }

    @WsSubscribe
    public void handleSetCapability(SetCapabilityMessage msg) {
        Game game = getGame(msg);
        if (game != null) {
            Class<? extends Capability<?>> cap = msg.getCapability();
            boolean enabled = msg.isEnabled();
            game.mapSetup(setup -> setup.mapCapabilities(caps ->
                    enabled ? caps.add(cap) : caps.remove(cap)
            ));
            game.post(new CapabilityChangeEvent(cap, enabled));
        }
    }

    @WsSubscribe
    public void handleUndo(UndoMessage msg) {
        Game game = getGame(msg);
        if (game != null) {
            game.undo();
        }
    }

    @WsSubscribe
    public void handleError(Connection conn, ErrorMessage err) {
        switch (err.getCode()) {
        case ErrorMessage.BAD_VERSION:
            logger.warn(err.getMessage());
            String msg;
            if (playOnline) {
                msg = _tr("Online play server is not compatible with your application. Please upgrade JCloisterZone to the latest version.");
            } else {
                msg = _tr("Remote JCloisterZone is not compatible with local application. Please upgrade both applications to same version.");
            }
            JOptionPane.showMessageDialog(client, msg, _tr("Incompatible versions"), JOptionPane.ERROR_MESSAGE);
            break;
        case ErrorMessage.INVALID_PASSWORD:
            JOptionPane.showMessageDialog(client, _tr("Invalid password"), _tr("Invalid password"), JOptionPane.WARNING_MESSAGE);
        default:
            JOptionPane.showMessageDialog(client, err.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public String getSessionId() {
        return conn.getSessionId();
    }


    protected void performAutostart(Game game) {
        DebugConfig debugConfig = client.getConfig().getDebug();
        if (!autostartPerfomed && debugConfig != null && debugConfig.isAutostartEnabled()) {
            autostartPerfomed = true; // apply autostart only once
            AutostartConfig autostartConfig = debugConfig.getAutostart();
            final PresetConfig presetCfg = client.getConfig().getPresets().get(autostartConfig.getPreset());
            if (presetCfg == null) {
                logger.warn("Autostart profile {} not found.", autostartConfig.getPreset());
                return;
            }

            final List<String> players = autostartConfig.getPlayers() == null ? new ArrayList<String>()
                    : autostartConfig.getPlayers();
            if (players.isEmpty()) {
                players.add("Player");
            }

            int i = 0;
            for (String name : players) {
                Class<?> clazz = null;
                ;
                PlayerSlot slot = game.getPlayerSlots()[i];
                try {
                    clazz = Class.forName(name);
                    slot.setAiClassName(name);
                    name = "AI-" + i + "-" + clazz.getSimpleName().replace("AiPlayer", "");
                } catch (ClassNotFoundException e) {
                    // empty
                }
                TakeSlotMessage msg = new TakeSlotMessage(i, name);
                msg.setGameId(game.getGameId());
                if (slot.getAiClassName() != null) {
                    msg.setAiClassName(slot.getAiClassName());
                    AiPlayer aiPlayer;
                    try {
                        aiPlayer = (AiPlayer) Class.forName(slot.getAiClassName()).newInstance();
                        msg.setSupportedSetup(aiPlayer.supportedSetup());
                    } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                        logger.error(e.getMessage(), e);
                    }
                }
                conn.send(msg);
                i++;
            }

            presetCfg.updateGameSetup(conn, game.getGameId());

            StartGameMessage msg = new StartGameMessage();
            msg.setGameId(game.getGameId());
            conn.send(msg);
        }
    }

}
