package com.jcloisterzone.rmi.mina;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.serialization.ObjectSerializationCodecFactory;
import org.apache.mina.filter.logging.LogLevel;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;
import com.jcloisterzone.Application;
import com.jcloisterzone.Player;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.PlayerSlot;
import com.jcloisterzone.game.phase.CreateGamePhase;
import com.jcloisterzone.game.phase.LoadGamePhase;
import com.jcloisterzone.game.phase.Phase;
import com.jcloisterzone.rmi.CallMessage;
import com.jcloisterzone.rmi.ClientControllMessage;
import com.jcloisterzone.rmi.ClientIF;
import com.jcloisterzone.rmi.ControllMessage;
import com.jcloisterzone.rmi.ServerIF;


public abstract class ClientStub extends IoHandlerAdapter implements InvocationHandler {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private IoSession session;
    private long clientId = -1;  //remote session id

    private ServerIF serverProxy;

    protected Game game;


    public void connect(InetAddress ia, int port) {
        InetSocketAddress endpoint = new InetSocketAddress(ia, port);
        connect(endpoint);
        session.write(new ClientControllMessage(null));
    }

    private void connect(SocketAddress endpoint) {
        NioSocketConnector connector = new NioSocketConnector();
        connector.getFilterChain().addLast("codec", new ProtocolCodecFilter(new ObjectSerializationCodecFactory()));
        if (logger.isDebugEnabled()) {
            LoggingFilter logFilter = new LoggingFilter();
            logFilter.setMessageSentLogLevel(LogLevel.DEBUG);
            logFilter.setMessageReceivedLogLevel(LogLevel.DEBUG);
            connector.getFilterChain().addLast("logger", logFilter);
        }
        connector.setHandler(this);

        ConnectFuture future = connector.connect(endpoint);
        future.awaitUninterruptibly();
        if (future.isConnected()) {
            session = future.getSession();
        }
    }


    public ServerIF getServerProxy() {
        return serverProxy;
    }

    public void setServerProxy(ServerIF serverProxy) {
        this.serverProxy = serverProxy;
    }

    public Game getGame() {
        return game;
    }

    //TODO revise; close from client side ???
    public void stop() {
        session.close(false);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (session == null) {
            logger.info("Not connected. Message ignored");
        } else {
            session.write(new CallMessage(method, args));
        }
        return null;
    }

    @Override
    public final void messageReceived(IoSession session, Object message) {
        if (message instanceof ControllMessage) {
            ControllMessage cm = (ControllMessage) message;
            clientId = cm.getClientId();
            if (cm.getProtocolVersion() != Application.PROTCOL_VERSION) {
                versionMismatch(cm.getProtocolVersion());
                session.close(true);
                return;
            }
            controllMessageReceived(cm);
        } else {
            callMessageReceived((CallMessage) message);
        }
    }

    protected void versionMismatch(int version) {
        logger.error("Version mismatch. Server version: " + version +". Client version " + Application.PROTCOL_VERSION);
    }

    protected Game createGame(ControllMessage msg) {
        if (msg.getSnapshot() == null) {
            game = new Game();
        } else {
            game = msg.getSnapshot().asGame();
        }
        return game;
    }

    protected void controllMessageReceived(ControllMessage msg) {
        game = createGame(msg);
        CreateGamePhase phase;
        if (msg.getSnapshot() == null) {
            phase = new CreateGamePhase(game, getServerProxy());
        } else {
            phase = new LoadGamePhase(game, msg.getSnapshot(), getServerProxy());
        }
        phase.setSlots(msg.getSlots());
        game.getPhases().put(phase.getClass(), phase);
        game.setPhase(phase);
    }

    protected void callMessageReceived(CallMessage msg) {
        try {
            Phase phase = game.getPhase();
            logger.debug("Delegating {} on phase {}", msg.getMethod(), phase.getClass().getSimpleName());
            msg.call(phase, ClientIF.class);
            phase = game.getPhase(); //new phase can differ from the phase in prev msg.call !!!
            while (phase != null && !phase.isEntered()) {
                logger.debug("Entering phase {}",  phase.getClass().getSimpleName());
                phase.setEntered(true);
                phase.enter();
                phase = game.getPhase();
                game.fireGameEvent().phaseEntered(phase);
            }
        } catch (InvocationTargetException ie) {
            logger.error(ie.getMessage(), ie.getCause());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }


    public long getClientId() {
        if (clientId == -1) throw new IllegalStateException("Client id hasn't been assigned yet");
        return clientId;
    }

    public boolean isLocalPlayer(Player player) {
        if (player == null) return false;
        return Objects.equal(clientId, player.getOwnerId());
    }

    public boolean isLocalSlot(PlayerSlot slot) {
        if (slot == null) return false;
        return Objects.equal(clientId, slot.getOwner());
    }

    @Override
    public void exceptionCaught(IoSession brokenSession, Throwable cause) {
        SocketAddress endpoint = brokenSession.getServiceAddress();
        session = null;
        int delay = 500;
        logger.warn("Connection lost. Reconnecting to " + endpoint + " ...");
        onDisconnect();
        while (session == null) {

            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
            }
            connect(endpoint);
            if (delay < 4000) delay *= 2;
        }
        onReconnect();
        session.write(new ClientControllMessage(clientId));
    }

    protected void onDisconnect() {
    }

    protected void onReconnect() {
    }

}
