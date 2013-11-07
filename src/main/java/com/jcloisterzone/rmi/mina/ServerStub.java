package com.jcloisterzone.rmi.mina;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.serialization.ObjectSerializationCodecFactory;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcloisterzone.Application;
import com.jcloisterzone.Expansion;
import com.jcloisterzone.game.CustomRule;
import com.jcloisterzone.rmi.CallMessage;
import com.jcloisterzone.rmi.ClientControllMessage;
import com.jcloisterzone.rmi.ControllMessage;
import com.jcloisterzone.rmi.ServerIF;
import com.jcloisterzone.server.Server;


public class ServerStub extends IoHandlerAdapter implements InvocationHandler {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private Server server;
    private NioSocketAcceptor acceptor;
    private boolean engageSlots = true;
    private boolean acceptingNew = true;

    private Map<Long, List<CallMessage>> undelivered = new HashMap<>();

    public ServerStub(Server server, int port) throws IOException {
        this.server = server;
        bind(new InetSocketAddress(port));
    }

    private void bind(SocketAddress address) throws IOException {
        acceptor = new NioSocketAcceptor();
        acceptor.setReuseAddress(true);
        //acceptor.getFilterChain().addLast("logger", new LoggingFilter() );
        acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(new ObjectSerializationCodecFactory()));
        //acceptor.getSessionConfig().setReadBufferSize( 2048 );
        //acceptor.getSessionConfig().setIdleTime( IdleStatus.BOTH_IDLE, 10 );
        acceptor.setHandler(this);
        acceptor.setCloseOnDeactivation(false);
        acceptor.bind(address);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        CallMessage msg = new CallMessage(method, args);
        acceptor.broadcast(msg);
        for (List<CallMessage> queue : undelivered.values()) {
            queue.add(msg);
        }
        return null;
    }

    public void stop() {
        acceptor.setCloseOnDeactivation(true);
        acceptor.unbind();
        acceptor.dispose();
    }

    public void closeAccepting() {
        acceptingNew = false;
    }

    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {
        if (message instanceof ClientControllMessage) {
            ClientControllMessage msg = (ClientControllMessage) message;
            if (msg.getClientId() == null) {
                session.setAttribute("clientId", session.getId());
                sessionReceivedControllMessage(session);
            } else {
                session.setAttribute("clientId", msg.getClientId());
                for (CallMessage callMsg : undelivered.remove(msg.getClientId())) {
                    session.write(callMsg);
                }
            }
        } else {
            //TODO check rights (has token)
            ((CallMessage) message).call(server, ServerIF.class);
        }
    }

    private void sessionReceivedControllMessage(IoSession session) {
        if (!acceptingNew) {
            session.close(true);
            return;
        }

        session.write(new ControllMessage(session.getId(), Application.PROTCOL_VERSION, server.getSnapshot(), server.getSlots()));

        for (Expansion exp: server.getExpansions()) {
            session.write(new CallMessage("updateExpansion", new Object[] { exp, true }));
        }
        for (CustomRule rule: server.getCustomRules()) {
            session.write(new CallMessage("updateCustomRule", new Object[] { rule, true }));
        }
    }

    @Override
    public void sessionOpened(IoSession session) throws Exception {
        if (engageSlots) {
            //first connected client is game owner - engage local slots
            engageSlots = false;
            server.engageSlots(session.getId());
        }

    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) {
        logger.info("Session error " + session.getRemoteAddress());
        undelivered.put((Long)session.getAttribute("clientId"), new ArrayList<CallMessage>());
    }
}
