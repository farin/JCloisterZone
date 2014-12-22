package com.jcloisterzone.ui.view;

import java.awt.BorderLayout;
import java.awt.Container;

import com.google.common.eventbus.Subscribe;
import com.jcloisterzone.Player;
import com.jcloisterzone.event.ClientListChangedEvent;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.PlayerSlot;
import com.jcloisterzone.game.Snapshot;
import com.jcloisterzone.ui.Client;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.ui.controls.ControlPanel;
import com.jcloisterzone.ui.controls.chat.ChatPanel;
import com.jcloisterzone.ui.grid.GridPanel;
import com.jcloisterzone.ui.grid.MainPanel;
import com.jcloisterzone.ui.panel.BackgroundPanel;
import com.jcloisterzone.wsio.server.RemoteClient;

public class GameView implements UiView {

	private final Client client;
	private final GameController gc;
	private final Game game;

	private ChatPanel chatPanel;
	private Snapshot snapshot;

	private MainPanel mainPanel;

	public GameView(Client client, GameController gc) {
		this.client = client;
		this.gc = gc;
		this.game = gc.getGame();
		gc.setGameView(this);
	}

	@Override
	public void show(Container pane) {
		BackgroundPanel bg = new BackgroundPanel();
    	bg.setLayout(new BorderLayout());
    	pane.add(bg);

        mainPanel = new MainPanel(client, gc, chatPanel);
        bg.add(mainPanel, BorderLayout.CENTER);
        gc.getReportingTool().setContainer(mainPanel);
        mainPanel.started(snapshot);

        gc.register(chatPanel);
		gc.register(this);
	}

	@Override
	public void hide() {
		gc.unregister(chatPanel);
		gc.unregister(this);
	}

	public MainPanel getMainPanel() {
		return mainPanel;
	}

	public ChatPanel getChatPanel() {
		return chatPanel;
	}

	public void setChatPanel(ChatPanel chatPanel) {
		this.chatPanel = chatPanel;
	}

	public Snapshot getSnapshot() {
		return snapshot;
	}

	public void setSnapshot(Snapshot snapshot) {
		this.snapshot = snapshot;
	}

	//helpers

	public GridPanel getGridPanel() {
        return mainPanel.getGridPanel();
    }

	public ControlPanel getControlPanel() {
	    return mainPanel.getControlPanel();
	}

	//TODO review legacy Activity interface

	public void toggleRecentHistory(boolean show) {
        mainPanel.toggleRecentHistory(show);
    }


    public void setShowFarmHints(boolean showFarmHints) {
        mainPanel.setShowFarmHints(showFarmHints);
    }

    public void zoom(double steps) {
        GridPanel gp = getGridPanel();
        if (gp != null) gp.zoom(steps);
    }

	@Subscribe
    public void clientListChanged(ClientListChangedEvent ev) {
        if (!game.isOver()) {
        	RemoteClient[] clients = ev.getClients();
            for (Player p : game.getAllPlayers()) {
                PlayerSlot slot = p.getSlot();
                boolean match = false;
                for (RemoteClient rc: clients) {
                    if (rc.getClientId().equals(slot.getClientId())) {
                        match = true;
                        break;
                    }
                }
                slot.setDisconnected(!match);
            }
        }
    }
}
