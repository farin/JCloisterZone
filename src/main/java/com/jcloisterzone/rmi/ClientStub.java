package com.jcloisterzone.rmi;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcloisterzone.Application;
import com.jcloisterzone.Expansion;
import com.jcloisterzone.event.setup.ExpansionChangedEvent;
import com.jcloisterzone.event.setup.PlayerSlotChangeEvent;
import com.jcloisterzone.event.setup.RuleChangeEvent;
import com.jcloisterzone.game.CustomRule;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.PlayerSlot;
import com.jcloisterzone.game.phase.CreateGamePhase;
import com.jcloisterzone.game.phase.Phase;
import com.jcloisterzone.wsio.CmdHandler;
import com.jcloisterzone.wsio.Connection;
import com.jcloisterzone.wsio.message.FlierDiceMessage;
import com.jcloisterzone.wsio.message.GameMessage;
import com.jcloisterzone.wsio.message.GameMessage.GameState;
import com.jcloisterzone.wsio.message.GameSetupMessage;
import com.jcloisterzone.wsio.message.JoinGameMessage;
import com.jcloisterzone.wsio.message.RandSampleMessage;
import com.jcloisterzone.wsio.message.RmiMessage;
import com.jcloisterzone.wsio.message.SetExpansionMessage;
import com.jcloisterzone.wsio.message.SetRuleMessage;
import com.jcloisterzone.wsio.message.SlotMessage;
import com.jcloisterzone.wsio.message.WelcomeMessage;
import com.jcloisterzone.wsio.server.SimpleServer;


public abstract class ClientStub  implements InvocationHandler {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private Connection conn;
    private Client2ClientIF serverProxy;

    protected Game game;


    public Connection  connect(InetAddress ia, int port) throws URISyntaxException {
        return connect(null);
    }

    private Connection connect(SocketAddress endpoint) throws URISyntaxException {
        ////localhost:8000/ws")) {
        conn = new Connection(new URI("ws://localhost:37447/"), this);
        return conn;
    }


    public Client2ClientIF getServerProxy() {
        return serverProxy;
    }

    public void setServerProxy(Client2ClientIF serverProxy) {
        this.serverProxy = serverProxy;
    }

    public Game getGame() {
        return game;
    }

    //TODO revise; close from client side ???
    public void stop() {
        conn.close();
        conn = null;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (conn == null) {
            logger.info("Not connected. Message ignored");
        } else {
            RmiMessage rmi = new RmiMessage(SimpleServer.GAME_ID);
            rmi.encode(new CallMessage(method, args));
            conn.send(rmi);
        }
        return null;
    }



    protected void versionMismatch(int version) {
        logger.error("Version mismatch. Server version: " + version +". Client version " + Application.PROTCOL_VERSION);
    }

    protected Game createGame(GameMessage message) {
        if (message.getSnapshot() == null) {
            game = new Game();
        } else {
            throw new UnsupportedOperationException("not implemented");
            //game = msg.getSnapshot().asGame();
        }
        game = new Game();
        return game;
    }

    @CmdHandler
    public void handleWelcome(Connection conn, WelcomeMessage msg) {
        //conn.sendMessage("CREATE_GAME", new CreateGameMessage());
        conn.send(new JoinGameMessage(SimpleServer.GAME_ID));
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

    @CmdHandler
    public void handleGame(Connection conn, GameMessage msg) {
        if (msg.getState() == GameState.RUNNING) {
            CreateGamePhase phase = (CreateGamePhase)game.getPhase();
            phase.startGame();
            phaseLoop();
            return;
        }
        game = createGame(msg);
        CreateGamePhase phase;
        if (msg.getSnapshot() == null) {
            phase = new CreateGamePhase(game, getServerProxy(), conn);
        } else {
            throw new UnsupportedOperationException("not implemented");
            //phase = new LoadGamePhase(game, msg.getSnapshot(), getServerProxy());
        }
        // TODO - lagacy bridge
        PlayerSlot[] slots = new PlayerSlot[PlayerSlot.COUNT];
        for (int i = 0; i < slots.length; i++) {
            PlayerSlot slot = new PlayerSlot(i);
            slot.setColors(game.getConfig().getPlayerColor(slot));
            slots[i] = slot;
        }
        for (SlotMessage slotMsg : msg.getSlots()) {
            updateSlot(slots, slotMsg);
        }
        phase.setSlots(slots);
        game.getPhases().put(phase.getClass(), phase);
        game.setPhase(phase);
        //HACK - this should be here but is is inside GuiClientStub. we must wait for panle is created
        //handleGameSetup(Conn, msg.getGameSetup());

    }

    @CmdHandler
    public void handleSlot(Connection conn, SlotMessage msg) {
        final PlayerSlot[] slots = ((CreateGamePhase) game.getPhase()).getPlayerSlots();
        updateSlot(slots, msg);
        game.post(new PlayerSlotChangeEvent(slots[msg.getNumber()]));
        game.getPhase().handleSlotMessage(msg);
    }

    @CmdHandler
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


    @CmdHandler
    public void handleSetExpansion(Connection conn, SetExpansionMessage msg) {
        Expansion expansion = msg.getExpansion();
        if (msg.isEnabled()) {
            game.getExpansions().add(expansion);
        } else {
            game.getExpansions().remove(expansion);
        }
        game.post(new ExpansionChangedEvent(expansion, msg.isEnabled()));
    }

    @CmdHandler
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
    @CmdHandler
    public void handleRandSample(Connection conn, RandSampleMessage msg) {
        game.getPhase().handleRandSample(msg);
        phaseLoop();
    }

    @CmdHandler
    public void handleFlierDice(Connection conn, FlierDiceMessage msg) {
        game.getPhase().handleFlierDice(msg);
        phaseLoop();
    }

    @CmdHandler
    public void handleRmi(Connection conn, RmiMessage msg) {
        callMessageReceived(msg.decode());
        phaseLoop();
    }


    protected void callMessageReceived(CallMessage msg) {
        try {
            Phase phase = game.getPhase();
            logger.debug("Delegating {} on phase {}", msg.getMethod(), phase.getClass().getSimpleName());
            msg.call(phase, Client2ClientIF.class);
        } catch (InvocationTargetException ie) {
            logger.error(ie.getMessage(), ie.getCause());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
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

//    @Override
//    public void exceptionCaught(IoSession brokenSession, Throwable cause) {
//        //temporary disabled, not worked as intended
////        SocketAddress endpoint = brokenSession.getServiceAddress();
////        session = null;
////        int delay = 500;
////        logger.warn("Connection lost. Reconnecting to " + endpoint + " ...");
////        onDisconnect();
////        while (session == null) {
////
////            try {
////                Thread.sleep(delay);
////            } catch (InterruptedException e) {
////            }
////            connect(endpoint);
////            if (delay < 4000) delay *= 2;
////        }
////        onReconnect();
////        session.write(new ClientControllMessage(clientId));
//    }
//
//    protected void onDisconnect() {
//    }
//
//    protected void onReconnect() {
//    }

}
