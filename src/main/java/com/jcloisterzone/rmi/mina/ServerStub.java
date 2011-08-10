package com.jcloisterzone.rmi.mina;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.serialization.ObjectSerializationCodecFactory;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import com.jcloisterzone.Application;
import com.jcloisterzone.Expansion;
import com.jcloisterzone.game.CustomRule;
import com.jcloisterzone.game.PlayerSlot;
import com.jcloisterzone.rmi.CallMessage;
import com.jcloisterzone.rmi.ControllMessage;
import com.jcloisterzone.rmi.ServerIF;
import com.jcloisterzone.server.Server;


public class ServerStub extends IoHandlerAdapter implements InvocationHandler {

	private Server server;
	private NioSocketAcceptor acceptor;
	private boolean engageSlots = true;

	public ServerStub(Server server, int port) throws IOException {
		this.server = server;

		acceptor = new NioSocketAcceptor();
		acceptor.setReuseAddress(true);
		//acceptor.getFilterChain().addLast("logger", new LoggingFilter() );
		acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(new ObjectSerializationCodecFactory()));
		//acceptor.getSessionConfig().setReadBufferSize( 2048 );
        //acceptor.getSessionConfig().setIdleTime( IdleStatus.BOTH_IDLE, 10 );
		acceptor.setHandler(this);
		acceptor.setCloseOnDeactivation(false);
		acceptor.bind(new InetSocketAddress(port));
		
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		CallMessage msg = new CallMessage(method, args);
		acceptor.broadcast(msg);
		return null;
	}

	public void stop() {
		acceptor.setCloseOnDeactivation(true);
		acceptor.unbind();
		acceptor.dispose();
	}

	public void closeAccepting() {
		acceptor.unbind();
	}

	@Override
	public void messageReceived(IoSession session, Object message) throws Exception {
		//TODO check rights (has token)
		((CallMessage) message).call(server, ServerIF.class);
	}

	@Override
	public void sessionOpened(IoSession session) throws Exception {
		if (engageSlots) {
			//first connected client is game owner - engage local slots
			engageSlots = false;
			server.engageSlots(session.getId());
		}
		session.write(new ControllMessage(session.getId(), Application.PROTCOL_VERSION, server.getSnapshot()));
		int slotId = 0;
		for(PlayerSlot slot : server.getSlots()) {
			session.write(new CallMessage("updateSlot", new Object[] { slot }));
			slotId++;
		}
		for(Expansion exp: server.getExpansions()) {
			session.write(new CallMessage("updateExpansion", new Object[] { exp, true }));
		}
		for(CustomRule rule: server.getCustomRules()) {
			session.write(new CallMessage("updateCustomRule", new Object[] { rule, true }));
		}
	}
}
