package com.jcloisterzone.ui.view;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.KeyEvent;
import java.util.Timer;
import java.util.TimerTask;

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

	private Timer timer;
	boolean repeatLeft, repeatRight, repeatUp, repeatDown;
    boolean repeatZoomIn, repeatZoomOut;


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

		timer = new Timer(true);
		timer.scheduleAtFixedRate(new KeyRepeater(), 0, 40);
	}

	@Override
	public void hide() {
		timer.cancel();
		gc.unregister(chatPanel);
		gc.unregister(this);
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
            switch (e.getKeyCode()) {
            case KeyEvent.VK_SPACE:
            case KeyEvent.VK_ENTER:
                mainPanel.getControlPanel().pass();
                return true;
            case KeyEvent.VK_TAB:
                if (e.getModifiers() == 0) {
                    mainPanel.getGridPanel().forward();
                } else if (e.getModifiers() == KeyEvent.SHIFT_MASK) {
                	mainPanel.getGridPanel().backward();
                }
                break;
            default:
                return dispatchReptable(e, true);
            }
		} else if (e.getID() == KeyEvent.KEY_RELEASED) {
            boolean result = dispatchReptable(e, false);
            if (result) e.consume();
            return result;
        } else if (e.getID() == KeyEvent.KEY_TYPED) {
            return dispatchKeyTyped(e);
        }
		return false;
	}

	private boolean dispatchReptable(KeyEvent e, boolean pressed) {
        if (e.getModifiers() != 0) return false;
        switch (e.getKeyCode()) {
        case KeyEvent.VK_LEFT:
        case KeyEvent.VK_A:
            repeatLeft = pressed;
            return true;
        case KeyEvent.VK_RIGHT:
        case KeyEvent.VK_D:
            repeatRight = pressed;
            return true;
        case KeyEvent.VK_DOWN:
        case KeyEvent.VK_S:
            repeatDown = pressed;
            return true;
        case KeyEvent.VK_UP:
        case KeyEvent.VK_W:
            repeatUp = pressed;
            return true;
        }
        if (e.getKeyChar() == '+') {
            repeatZoomIn = pressed;
            return true;
        }
        if (e.getKeyChar() == '-') {
            repeatZoomOut = pressed;
            return true;
        }
        return false;
    }

	private boolean dispatchKeyTyped(KeyEvent e) {
        if (e.getModifiers() != 0) return false;
        if (e.getKeyChar() == '+' || e.getKeyChar() == '-') {
            e.consume();
            return true;
        }
        return false;
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

	class KeyRepeater extends TimerTask {

        @Override
        public void run() {
            GridPanel gridPanel = mainPanel.getGridPanel();
            if (gridPanel == null) return;
            if (repeatLeft) {
                gridPanel.moveCenter(-1, 0);
            }
            if (repeatRight) {
                gridPanel.moveCenter(1, 0);
            }
            if (repeatUp) {
                gridPanel.moveCenter(0, -1);
            }
            if (repeatDown) {
                gridPanel.moveCenter(0, 1);
            }
            if (repeatZoomIn) {
                gridPanel.zoom(0.8);
            }
            if (repeatZoomOut) {
                gridPanel.zoom(-0.8);
            }
        }
    }
}
