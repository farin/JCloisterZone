package com.jcloisterzone.rmi;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcloisterzone.Expansion;
import com.jcloisterzone.event.setup.ExpansionChangedEvent;
import com.jcloisterzone.event.setup.PlayerSlotChangeEvent;
import com.jcloisterzone.event.setup.RuleChangeEvent;
import com.jcloisterzone.game.CustomRule;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.PlayerSlot;
import com.jcloisterzone.game.Snapshot;
import com.jcloisterzone.game.phase.CreateGamePhase;
import com.jcloisterzone.game.phase.LoadGamePhase;
import com.jcloisterzone.game.phase.Phase;
import com.jcloisterzone.ui.Client;
import com.jcloisterzone.ui.panel.ConnectGamePanel;
import com.jcloisterzone.wsio.Connection;
import com.jcloisterzone.wsio.WsReceiver;
import com.jcloisterzone.wsio.WsSubscribe;
import com.jcloisterzone.wsio.message.ErrorMessage;
import com.jcloisterzone.wsio.message.FlierDiceMessage;
import com.jcloisterzone.wsio.message.GameMessage;
import com.jcloisterzone.wsio.message.GameMessage.GameState;
import com.jcloisterzone.wsio.message.GameSetupMessage;
import com.jcloisterzone.wsio.message.RandSampleMessage;
import com.jcloisterzone.wsio.message.RmiMessage;
import com.jcloisterzone.wsio.message.SetExpansionMessage;
import com.jcloisterzone.wsio.message.SetRuleMessage;
import com.jcloisterzone.wsio.message.SlotMessage;
import com.jcloisterzone.wsio.message.WsMessage;

import static com.jcloisterzone.ui.I18nUtils._;


public class ClientStub  implements InvocationHandler, WsReceiver {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private Connection conn;
    private RmiProxy serverProxy;

    protected Game game;

    private final Client client;
    private boolean autostartPerfomed;

    public ClientStub(Client client) {
        this.client = client;
    }


    public Connection connect(String hostname, int port) throws URISyntaxException {
        URI uri = new URI("ws", null, hostname, port, "/", null, null);
        //URI uri = new URI("ws://localhost:37447/");
        ////localhost:8000/ws")) {
        conn = new Connection(uri, this);
        return conn;
    }

    public Game getGame() {
        return game;
    }


    @Override
    public void onWebsocketError(Exception ex) {
        ConnectGamePanel cgp = client.getConnectGamePanel();
        if (cgp == null) {
            logger.error(ex.getMessage(), ex);
        } else {
            cgp.onWebsocketError(ex);
        }
    }

    @Override
    public void onWebsocketClose(int code, String reason, boolean remote) {
        //empty for now
    }

    @Override
    public void onWebsocketMessage(WsMessage msg) {
        phaseLoop();
    }


    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (conn == null) {
            logger.info("Not connected. Message ignored");
        } else {
            RmiMessage rmi = new RmiMessage(game.getGameId(), method.getName(), args);
            conn.send(rmi);
        }
        return null;
    }



    private void updateSlot(PlayerSlot[] slots, SlotMessage slotMsg) {
        PlayerSlot slot = slots[slotMsg.getNumber()];
        slot.setNickname(slotMsg.getNickname());
        slot.setState(slotMsg.getState());
        slot.setSerial(slotMsg.getSerial());
        if (!slot.isOwn() || !slotMsg.isAi()) {
            slot.setAiClassName(null);
        }
    }

    //TODO inline
    protected void initGame(Game game) {
        game.setConfig(client.getConfig());
        client.setGame(game);
    }


    @WsSubscribe
    public void handleGame(final Connection conn, final GameMessage msg) {
        if (msg.getState() == GameState.RUNNING) {
            CreateGamePhase phase = (CreateGamePhase)game.getPhase();
            phase.startGame();
            phaseLoop();
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
            phase = new CreateGamePhase(game, conn);
        } else {
            game = snapshot.asGame(msg.getGameId());
            phase = new LoadGamePhase(game, snapshot, conn);
        }
        initGame(game);

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
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    client.showCreateGamePanel(msg.getSnapshot() == null, slots);
                    //HACK - we must wait for panel is created
                    handleGameSetup(conn, msg.getGameSetup());
                }
            });
        }

    }

    @WsSubscribe
    public void handleSlot(Connection conn, SlotMessage msg) {
        final PlayerSlot[] slots = ((CreateGamePhase) game.getPhase()).getPlayerSlots();
        updateSlot(slots, msg);
        game.post(new PlayerSlotChangeEvent(slots[msg.getNumber()]));
        game.getPhase().handleSlotMessage(msg);
    }

    @WsSubscribe
    public void handleGameSetup(Connection conn, GameSetupMessage msg) {
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
    public void handleSetExpansion(Connection conn, SetExpansionMessage msg) {
        Expansion expansion = msg.getExpansion();
        if (msg.isEnabled()) {
            game.getExpansions().add(expansion);
        } else {
            game.getExpansions().remove(expansion);
        }
        game.post(new ExpansionChangedEvent(expansion, msg.isEnabled()));
    }

    @WsSubscribe
    public void handleSetRule(Connection conn, SetRuleMessage msg) {
        CustomRule rule = msg.getRule();
        if (msg.isEnabled()) {
            game.getCustomRules().add(rule);
        } else {
            game.getCustomRules().remove(rule);
        }
        game.post(new RuleChangeEvent(rule, msg.isEnabled()));
    }

    //TODO add CmdHandler to phase and pass to phase automatically
    @WsSubscribe
    public void handleRandSample(Connection conn, RandSampleMessage msg) {
        game.getPhase().handleRandSample(msg);
    }

    @WsSubscribe
    public void handleFlierDice(Connection conn, FlierDiceMessage msg) {
        game.getPhase().handleFlierDice(msg);
    }

    @WsSubscribe
    public void handleRmi(Connection conn, RmiMessage msg) {
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

    protected void phaseLoop() {
        Phase phase = game.getPhase(); //new phase can differ from the phase in prev msg.call !!!
        while (phase != null && !phase.isEntered()) {
            logger.debug("Entering phase {}",  phase.getClass().getSimpleName());
            phase.setEntered(true);
            phase.enter();
            phase = game.getPhase();
            //game.post(new PhaseEnterEvent(phase));
        }
    }


    public String getClientId() {
        return conn.getClientId();
    }

 // AUTOSTART not convered yet

//  @Override
//  protected void controllMessageReceived(final ControllMessage msg) {
//      super.controllMessageReceived(msg);
//
//      DebugConfig debugConfig = client.getConfig().getDebug();
//      if (!autostartPerfomed && debugConfig.isAutostartEnabled()) {
//          autostartPerfomed = true; //apply autostart only once
//          AutostartConfig autostartConfig = debugConfig.getAutostart();
//          final PresetConfig presetCfg = client.getConfig().getPresets().get(autostartConfig.getPreset());
//          if (presetCfg == null) {
//              logger.warn("Autostart profile {} not found.", autostartConfig.getPreset());
//              return;
//          }
//
//          final List<String> players = autostartConfig.getPlayers() == null ? new ArrayList<String>() : autostartConfig.getPlayers();
//          if (players.isEmpty()) {
//              players.add("Player");
//          }
//          SwingUtilities.invokeLater(new Runnable() {
//              public void run() {
//                  int i = 0;
//                  for (String name: players) {
//                      PlayerSlot slot;
//                      try {
//                          Class<?> clazz = Class.forName(name);
//                          slot = new PlayerSlot(i, SlotType.AI, "AI-"+i+"-"+clazz.getSimpleName(), getClientId());
//                          slot.setAiClassName(clazz.getName());
//                      } catch (ClassNotFoundException e) {
//                          slot = new PlayerSlot(i, SlotType.PLAYER, name, getClientId());
//                      }
//                      client.getServer().updateSlot(slot, null);
//                      i++;
//                  }
//
//                  presetCfg.updateGameSetup(client.getServer());
//                  client.getServer().startGame();
//              }
//          });
//      }
//  }

}
