package com.jcloisterzone.ui.view;

import java.awt.Container;
import java.awt.event.KeyEvent;

import com.jcloisterzone.ui.ChannelController;
import com.jcloisterzone.ui.Client;
import com.jcloisterzone.ui.controls.chat.ChatPanel;
import com.jcloisterzone.ui.panel.ChannelPanel;

public class ChannelView extends AbstractUiView {

	private final ChannelController cc;

	private ChannelPanel channelPanel;
	private ChatPanel chatPanel;

	public ChannelView(Client client, ChannelController cc) {
		super(client);
		this.cc = cc;
	}

	@Override
	public void show(Container pane, Object ctx) {
		channelPanel = new ChannelPanel(client, cc);
        pane.add(channelPanel);

        chatPanel = channelPanel.getChatPanel();
        cc.setChannelPanel(channelPanel);
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent e) {
		if (chatPanel.getInput().hasFocus()) return false;
		if (e.getID() == KeyEvent.KEY_PRESSED) {
            if (e.getKeyChar() == '`' || e.getKeyChar() == ';') {
                e.consume();
                chatPanel.activateChat();
                return true;
            }
		}
		return false;
	}

}
