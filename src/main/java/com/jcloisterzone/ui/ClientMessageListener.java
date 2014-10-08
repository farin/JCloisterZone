package com.jcloisterzone.ui;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcloisterzone.Expansion;
import com.jcloisterzone.config.Config.AutostartConfig;
import com.jcloisterzone.config.Config.DebugConfig;
import com.jcloisterzone.config.Config.PresetConfig;
import com.jcloisterzone.event.ChatEvent;
import com.jcloisterzone.event.ClientListChangedEvent;
import com.jcloisterzone.event.setup.ExpansionChangedEvent;
import com.jcloisterzone.event.setup.PlayerSlotChangeEvent;
import com.jcloisterzone.event.setup.RuleChangeEvent;
import com.jcloisterzone.game.CustomRule;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.PlayerSlot;
import com.jcloisterzone.game.PlayerSlot.SlotState;
import com.jcloisterzone.game.Snapshot;
import com.jcloisterzone.game.phase.CreateGamePhase;
import com.jcloisterzone.game.phase.LoadGamePhase;
import com.jcloisterzone.game.phase.Phase;
import com.jcloisterzone.online.Channel;
import com.jcloisterzone.ui.panel.ChannelPanel;
import com.jcloisterzone.ui.panel.ConnectPanel;
import com.jcloisterzone.ui.panel.GamePanel;
import com.jcloisterzone.wsio.Connection;
import com.jcloisterzone.wsio.MessageDispatcher;
import com.jcloisterzone.wsio.MessageListener;
import com.jcloisterzone.wsio.RmiProxy;
import com.jcloisterzone.wsio.WsSubscribe;
import com.jcloisterzone.wsio.message.ChannelMessage;
import com.jcloisterzone.wsio.message.ChatMessage;
import com.jcloisterzone.wsio.message.ClientListMessage;
import com.jcloisterzone.wsio.message.ErrorMessage;
import com.jcloisterzone.wsio.message.GameMessage;
import com.jcloisterzone.wsio.message.GameMessage.GameState;
import com.jcloisterzone.wsio.message.GameSetupMessage;
import com.jcloisterzone.wsio.message.RmiMessage;
import com.jcloisterzone.wsio.message.SetExpansionMessage;
import com.jcloisterzone.wsio.message.SetRuleMessage;
import com.jcloisterzone.wsio.message.SlotMessage;
import com.jcloisterzone.wsio.message.StartGameMessage;
import com.jcloisterzone.wsio.message.TakeSlotMessage;
import com.jcloisterzone.wsio.message.UndoMessage;
import com.jcloisterzone.wsio.message.WsInGameMessage;
import com.jcloisterzone.wsio.message.WsMessage;
import com.jcloisterzone.wsio.server.RemoteClient;

import static com.jcloisterzone.ui.I18nUtils._;


public class ClientMessageListener implements MessageListener {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private Connection conn;
    private MessageDispatcher dispatcher = new MessageDispatcher();

    private Map<String, GameController> gameControllers = new HashMap<>();
    private ChannelController channelController;

    private final Client client;
    private boolean autostartPerfomed;

    public ClientMessageListener(Client client) {
        this.client = client;
    }


    public Connection connect(String username, URI uri) {
        conn = new Connection(username, uri, this);
        return conn;
    }


    @Override
    public void onWebsocketError(Exception ex) {
        ConnectPanel cgp = client.getConnectGamePanel();
        if (cgp != null) {
            cgp.onWebsocketError(ex);
            return;
        }
        //TODO temporary code, connection error should be handled at client level
        if (!gameControllers.isEmpty()) {
        	GameController gc = gameControllers.values().iterator().next();
        	GamePanel gp = gc == null ? null : gc.getGamePanel();
	        if (gp != null) {
	            gp.onWebsocketError(ex);
	            return;
	        }
        }
        logger.error(ex.getMessage(), ex);
    }

    @Override
    public void onWebsocketClose(int code, String reason, boolean remote) {
        //empty for now
    }

    @Override
    public void onWebsocketMessage(WsMessage msg) {
        //TODO pass game as context to dispatch
    	GameController gc = msg instanceof WsInGameMessage ? getGameController((WsInGameMessage) msg) : null;
        if (gc == null) {
            dispatcher.dispatch(msg, conn, this);
        } else {
            dispatcher.dispatch(msg, conn, this, gc.getGame().getPhase());
            gc.phaseLoop();
        }
    }

    public RemoteClient getClientById(AbstractController controller, String clientId) {;
        for (RemoteClient remote: controller.getRemoteClients()) {
            if (remote.getClientId().equals(clientId)) {
                return remote;
            }
        }
        throw new NoSuchElementException();
    }

    private GameController getGameController(WsInGameMessage msg) {
    	return gameControllers.get(msg.getGameId());
    }

    private Game getGame(WsInGameMessage msg) {
    	GameController gc = getGameController(msg);
    	return gc == null ? null : gc.getGame();
    }

    private void updateSlot(PlayerSlot[] slots, SlotMessage slotMsg) {
        PlayerSlot slot = slots[slotMsg.getNumber()];
        slot.setNickname(slotMsg.getNickname());
        slot.setClientId(slotMsg.getOwner());
        if (slotMsg.getOwner() == null) {
            slot.setState(SlotState.OPEN);
        } else {
            slot.setState(slotMsg.getOwner().equals(conn.getClientId()) ? SlotState.OWN : SlotState.REMOTE);
        }
        slot.setSerial(slotMsg.getSerial());
        slot.setAiClassName(slotMsg.getAiClassName());
    }

    @WsSubscribe
    public void handleGame(final GameMessage msg) throws InvocationTargetException, InterruptedException {
        if (msg.getState() == GameState.RUNNING) {
        	Game game = getGame(msg);
            handleGameSetup(msg.getGameSetup());
            for (SlotMessage slotMsg : msg.getSlots()) {
                handleSlot(slotMsg);
            }
            CreateGamePhase phase = (CreateGamePhase)game.getPhase();
            phase.startGame();
            return;
        }
        CreateGamePhase phase;
        Snapshot snapshot = null;
        if (msg.getSnapshot() != null) {
            try {
                snapshot = new Snapshot(msg.getSnapshot());
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
                return;
            }
        }

        final Game game;
        final GameController gc;
        if (snapshot == null) {
            game = new Game(msg.getGameId());
            gc = new GameController(client, game, conn.getReportingTool());
            phase = new CreateGamePhase(game, gc);
        } else {
            game = snapshot.asGame(msg.getGameId());
            gc = new GameController(client, game, conn.getReportingTool());
            phase = new LoadGamePhase(game, snapshot, gc);
        }
        gameControllers.clear(); //TODO remove games on game over - now only single game window is allowed
        gameControllers.put(game.getGameId(), gc);
        game.setConfig(client.getConfig());
        conn.getReportingTool().setGame(game);

        final PlayerSlot[] slots = new PlayerSlot[PlayerSlot.COUNT];

        for (SlotMessage slotMsg : msg.getSlots()) {
            int number = slotMsg.getNumber();
            PlayerSlot slot = new PlayerSlot(number);
            slot.setColors(game.getConfig().getPlayerColor(slot));
            slots[number] = slot;
            updateSlot(slots, slotMsg);
        }
        phase.setSlots(slots);
        game.getPhases().put(phase.getClass(), phase);
        game.setPhase(phase);

        if (msg.getState() == GameState.OPEN) {
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
				public void run() {
                    GamePanel panel = client.newGamePanel(gc, msg.getSnapshot() == null, slots);
                    gc.setGamePanel(panel);

                    client.setActivity(gc);
                    client.setGame(game);

                    //we must wait for panel is created
                    handleGameSetup(msg.getGameSetup());
                    performAutostart(game);
                }
            });
        }
    }

    @WsSubscribe
    public void handleChannel(final ChannelMessage msg) throws InvocationTargetException, InterruptedException {
        channelController = new ChannelController(client, new Channel(msg.getName()));
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
			public void run() {
            	ChannelPanel panel = client.newChannelPanel(channelController, msg.getName());
            	channelController.setChannelPanel(panel);
            }
        });
    }

    @WsSubscribe
    public void handleClientList(ClientListMessage msg) {
    	GameController gc = getGameController(msg);
    	if (gc != null) {
        	gc.setRemoteClients(msg.getClients());
        	gc.getGame().post(new ClientListChangedEvent(msg.getClients()));
        } else if (channelController != null) {
        	channelController.setRemoteClients(msg.getClients());
        	channelController.getChannel().post(new ClientListChangedEvent(msg.getClients()));
        } else {
        	logger.warn("No target for message");
        }
    }

    @WsSubscribe
    public void handleChat(ChatMessage msg) {
    	GameController gc = getGameController(msg);
    	if (gc != null) {
    		ChatEvent ev = new ChatEvent(getClientById(gc, msg.getClientId()), msg.getText());
    		gc.getGame().post(ev);
    	} else if (channelController != null) {
    		ChatEvent ev = new ChatEvent(getClientById(channelController, msg.getClientId()), msg.getText());
    		channelController.getChannel().post(ev);
    	} else {
    		logger.warn("No target for message");
    	}
    }

    @WsSubscribe
    public void handleSlot(SlotMessage msg) {
    	Game game = getGame(msg);
        PlayerSlot[] slots = ((CreateGamePhase) game.getPhase()).getPlayerSlots();
        updateSlot(slots, msg);
        game.post(new PlayerSlotChangeEvent(slots[msg.getNumber()]));
    }

    @WsSubscribe
    public void handleGameSetup(GameSetupMessage msg) {
    	Game game = getGame(msg);
        game.getExpansions().clear();
        game.getExpansions().addAll(msg.getExpansions());
        game.getCustomRules().clear();
        game.getCustomRules().addAll(msg.getCustomRules());

        for (Expansion exp : Expansion.values()) {
            if (!exp.isImplemented()) continue;
            game.post(new ExpansionChangedEvent(exp, game.getExpansions().contains(exp)));
        }
        for (CustomRule rule : CustomRule.values()) {
            game.post(new RuleChangeEvent(rule, game.getCustomRules().contains(rule)));
        }
    }


    @WsSubscribe
    public void handleSetExpansion(SetExpansionMessage msg) {
    	Game game = getGame(msg);
        Expansion expansion = msg.getExpansion();
        if (msg.isEnabled()) {
            game.getExpansions().add(expansion);
        } else {
            game.getExpansions().remove(expansion);
        }
        game.post(new ExpansionChangedEvent(expansion, msg.isEnabled()));
    }

    @WsSubscribe
    public void handleSetRule(SetRuleMessage msg) {
    	Game game = getGame(msg);
        CustomRule rule = msg.getRule();
        if (msg.isEnabled()) {
            game.getCustomRules().add(rule);
        } else {
            game.getCustomRules().remove(rule);
        }
        game.post(new RuleChangeEvent(rule, msg.isEnabled()));
    }

    @WsSubscribe
    public void handleRmi(RmiMessage msg) {
    	Game game = getGame(msg);
        try {
            Phase phase = game.getPhase();
            Method[] methods = RmiProxy.class.getMethods();
            for (int i = 0; i < methods.length; i++) {
                if (methods[i].getName().equals(msg.getMethod())) {
                    methods[i].invoke(phase, (Object[]) msg.decode(msg.getArgs()));
                    return;
                }
            }
        } catch (InvocationTargetException ie) {
            logger.error(ie.getMessage(), ie.getCause());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @WsSubscribe
    public void handleUndo(UndoMessage msg) {
    	Game game = getGame(msg);
        game.undo();
    }

    @WsSubscribe
    public void handleError(Connection conn, ErrorMessage err) {
        switch (err.getCode()) {
        case ErrorMessage.BAD_VERSION:
            logger.warn(err.getMessage());
            JOptionPane.showMessageDialog(client,
                    _("Remote JCloisterZone is not compatible with local application. Please upgrade both applications to same version."),
                    _("Incompatible versions"),
                    JOptionPane.ERROR_MESSAGE
            );
            break;
        default:
            logger.error(err.getMessage());
        }
    }

    public String getClientId() {
        return conn.getClientId();
    }


  protected void performAutostart(Game game) {
      DebugConfig debugConfig = client.getConfig().getDebug();
      if (!autostartPerfomed && debugConfig != null && debugConfig.isAutostartEnabled()) {
          autostartPerfomed = true; //apply autostart only once
          AutostartConfig autostartConfig = debugConfig.getAutostart();
          final PresetConfig presetCfg = client.getConfig().getPresets().get(autostartConfig.getPreset());
          if (presetCfg == null) {
              logger.warn("Autostart profile {} not found.", autostartConfig.getPreset());
              return;
          }

          final List<String> players = autostartConfig.getPlayers() == null ? new ArrayList<String>() : autostartConfig.getPlayers();
          if (players.isEmpty()) {
              players.add("Player");
          }

          int i = 0;
          for (String name: players) {
              Class<?> clazz = null;;
              PlayerSlot slot =  ((CreateGamePhase) game.getPhase()).getPlayerSlots()[i];
              try {
                  clazz = Class.forName(name);
                  slot.setAiClassName(name);
                  name = "AI-" + i + "-" + clazz.getSimpleName().replace("AiPlayer", "");
              } catch (ClassNotFoundException e) {
                  //empty
              }
              TakeSlotMessage msg = new TakeSlotMessage(game.getGameId(), i, name);
              if (slot.getAiClassName() != null) {
                  msg.setAiClassName(slot.getAiClassName());
              }
              conn.send(msg);
              i++;
          }

          presetCfg.updateGameSetup(conn, game.getGameId());
          conn.send(new StartGameMessage(game.getGameId()));
      }
  }

}
