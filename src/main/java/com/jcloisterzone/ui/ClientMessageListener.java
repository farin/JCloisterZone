package com.jcloisterzone.ui;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
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

    private GameController gc;
    private Game game;
    private Channel channel;

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
        GamePanel gp = gc == null ? null : gc.getGamePanel();
        if (gp != null) {
            gp.onWebsocketError(ex);
            return;
        }
        logger.error(ex.getMessage(), ex);
    }

    @Override
    public void onWebsocketClose(int code, String reason, boolean remote) {
        //empty for now
    }

    @Override
    public void onWebsocketMessage(WsMessage msg) {
        //TODO fix this and use
//        if (msg instanceof WsInGameMessage && game != null && game.getGameId().equals(((WsInGameMessage) msg).getGameId())) {
//            dispatcher.dispatch(msg, game, this, game.getPhase());
//            gc.phaseLoop();
//        } else {
//            dispatcher.dispatch(msg, conn, this);
//        }

        if (game == null) {
            dispatcher.dispatch(msg, conn, this);
        } else {
            dispatcher.dispatch(msg, conn, this, game.getPhase());
            gc.phaseLoop();
        }
    }

    public RemoteClient getClientById(Game game, String clientId) {;
        for (RemoteClient remote: game.getRemoteClients()) {
            if (remote.getClientId().equals(clientId)) {
                return remote;
            }
        }
        throw new NoSuchElementException();
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

        if (snapshot == null) {
            game = new Game(msg.getGameId());
            gc = new GameController(client, game);
            phase = new CreateGamePhase(game, gc);
        } else {
            game = snapshot.asGame(msg.getGameId());
            gc = new GameController(client, game);
            phase = new LoadGamePhase(game, snapshot, gc);
        }
        game.setConfig(client.getConfig());
        game.setReportingTool(conn.getReportingTool());
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
                public void run() {
                    GamePanel panel = client.newGamePanel(gc, msg.getSnapshot() == null, slots);
                    gc.setGamePanel(panel);

                    client.setActivity(gc);
                    client.setGame(game);

                    //HACK - we must wait for panel is created
                    handleGameSetup(msg.getGameSetup());
                    performAutostart();
                }
            });
        }
    }

    @WsSubscribe
    public void handleChannel(final ChannelMessage msg) throws InvocationTargetException, InterruptedException {
        channel = new Channel(msg.getName());
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                client.newChannelPanel(channel, msg.getName());
            }
        });
    }

    @WsSubscribe
    public void handleClientList(ClientListMessage msg) {
        if (msg.getChannel() != null) {
            channel.setRemoteClients(msg.getClients());
        } else {
            game.setRemoteClients(msg.getClients());
            game.post(new ClientListChangedEvent(msg.getClients()));
        }
    }

    @WsSubscribe
    public void handleChat(ChatMessage msg) {
        game.post(new ChatEvent(getClientById(game, msg.getClientId()), msg.getText()));
    }

    @WsSubscribe
    public void handleSlot(SlotMessage msg) {
        PlayerSlot[] slots = ((CreateGamePhase) game.getPhase()).getPlayerSlots();
        updateSlot(slots, msg);
        game.post(new PlayerSlotChangeEvent(slots[msg.getNumber()]));
    }

    @WsSubscribe
    public void handleGameSetup(GameSetupMessage msg) {
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


  protected void performAutostart() {
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
