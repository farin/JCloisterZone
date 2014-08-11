package com.jcloisterzone.ui.panel;

import java.awt.BorderLayout;
import java.awt.GridBagLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.jcloisterzone.event.GameStateChangeEvent;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.PlayerSlot;
import com.jcloisterzone.ui.Client;
import com.jcloisterzone.ui.controls.ControlPanel;
import com.jcloisterzone.ui.grid.GridPanel;
import com.jcloisterzone.ui.grid.MainPanel;

public class GamePanel extends JPanel {

    private final Client client;
    private final Game game;

    private CreateGamePanel createGamePanel;

    private MainPanel mainPanel;


    public GamePanel(Client client, Game game) {
        this.client = client;
        this.game = game;
        setLayout(new BorderLayout());
    }

    public void showCreateGamePanel(boolean mutableSlots, PlayerSlot[] slots) {
        createGamePanel = new CreateGamePanel(client, game, mutableSlots, slots);
        JPanel envelope = new BackgroundPanel();
        envelope.setLayout(new GridBagLayout()); //to have centered inner panel
        envelope.add(createGamePanel);

        JScrollPane scroll = new JScrollPane(envelope);
        add(scroll, BorderLayout.CENTER);
    }

    public void started(GameStateChangeEvent ev) {
        disposePanel(); //free createGamePanel
        removeAll();

        mainPanel = new MainPanel(client, game);
        add(mainPanel, BorderLayout.CENTER);
        mainPanel.started(ev.getSnapshot());
    }

    public void disposePanel() {
        if (createGamePanel != null) createGamePanel.disposePanel();
    }

    public CreateGamePanel getCreateGamePanel() {
        return createGamePanel;
    }

    public Game getGame() {
        return game;
    }

    public GridPanel getGridPanel() {
        if (mainPanel == null) return null;
        return mainPanel.getGridPanel();
    }

    public MainPanel getMainPanel() {
        return mainPanel;
    }

    public void setMainPanel(MainPanel mainPanel) {
        this.mainPanel = mainPanel;
    }

    public ControlPanel getControlPanel() {
        return mainPanel == null ? null : mainPanel.getControlPanel();
    }

}
