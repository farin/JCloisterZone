package com.jcloisterzone.ui.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import net.miginfocom.swing.MigLayout;

import com.jcloisterzone.event.GameStateChangeEvent;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.PlayerSlot;
import com.jcloisterzone.ui.Client;
import com.jcloisterzone.ui.controls.ChatPanel;
import com.jcloisterzone.ui.controls.ControlPanel;
import com.jcloisterzone.ui.grid.GridPanel;
import com.jcloisterzone.ui.grid.MainPanel;

public class GamePanel extends JPanel {

    private final Client client;
    private final Game game;

    private ChatPanel chatPanel;
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
        scroll.setViewportBorder(null);
        add(scroll, BorderLayout.CENTER);

        JPanel chatColumn = new JPanel();
        MigLayout chatColumnLayout = new MigLayout("", "[grow]", "[grow][]");
        chatColumn.setLayout(chatColumnLayout);
        chatColumn.setPreferredSize(new Dimension(ChatPanel.CHAT_WIDTH, getHeight()));
        chatColumn.setBackground(Color.WHITE);
        add(chatColumn, BorderLayout.WEST);

        chatPanel = new ChatPanel(client, game);
        chatPanel.registerSwingComponents(chatColumn);
        chatColumnLayout.setComponentConstraints(chatPanel.getMessagesPane(), "cell 0 0, align 0% 100%");
        chatColumnLayout.setComponentConstraints(chatPanel.getInput(), "cell 0 1, growx");

    }

    public void started(GameStateChangeEvent ev) {
        disposePanel(); //free createGamePanel
        removeAll();

        mainPanel = new MainPanel(client, game, chatPanel);
        add(mainPanel, BorderLayout.CENTER);
        mainPanel.started(ev.getSnapshot());
    }

    public void disposePanel() {
        if (createGamePanel != null) createGamePanel.disposePanel();
    }

    public CreateGamePanel getCreateGamePanel() {
        return createGamePanel;
    }

    public ChatPanel getChatPanel() {
        return chatPanel;
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
