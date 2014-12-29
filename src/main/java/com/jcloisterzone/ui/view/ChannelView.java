package com.jcloisterzone.ui.view;

import java.awt.Container;
import java.awt.event.KeyEvent;

import com.jcloisterzone.ui.ChannelController;
import com.jcloisterzone.ui.Client;
import com.jcloisterzone.ui.MenuBar;
import com.jcloisterzone.ui.MenuBar.MenuItem;
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

        MenuBar menu = client.getJMenuBar();
        menu.setItemEnabled(MenuItem.LOAD, false);
		menu.setItemEnabled(MenuItem.NEW_GAME, false);
		menu.setItemEnabled(MenuItem.DIRECT_CONNECT, false);
		menu.setItemEnabled(MenuItem.PLAY_ONLINE, false);
	}

	@Override
	public void hide() {
		MenuBar menu = client.getJMenuBar();
		//TODO this doesnt't work - need to disable all time it is connected to channel
		menu.setItemEnabled(MenuItem.LOAD, true);
		menu.setItemEnabled(MenuItem.NEW_GAME, true);
		menu.setItemEnabled(MenuItem.DIRECT_CONNECT, true);
		menu.setItemEnabled(MenuItem.PLAY_ONLINE, true);
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
