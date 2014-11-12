package com.jcloisterzone.ui.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

import net.miginfocom.swing.MigLayout;

import org.java_websocket.exceptions.WebsocketNotConnectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Collections2;
import com.google.common.eventbus.Subscribe;
import com.jcloisterzone.Player;
import com.jcloisterzone.event.ClientListChangedEvent;
import com.jcloisterzone.event.GameStateChangeEvent;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.PlayerSlot;
import com.jcloisterzone.ui.Client;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.ui.controls.ControlPanel;
import com.jcloisterzone.ui.controls.chat.ChatPanel;
import com.jcloisterzone.ui.controls.chat.GameChatPanel;
import com.jcloisterzone.ui.grid.GridPanel;
import com.jcloisterzone.ui.grid.MainPanel;
import com.jcloisterzone.wsio.server.RemoteClient;

import static com.jcloisterzone.ui.I18nUtils._;

@SuppressWarnings("serial")
public class GamePanel extends BackgroundPanel {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private final Client client;
    private final Game game;
    private final GameController gc;

    private ChatPanel chatPanel;
    private CreateGamePanel createGamePanel;
    private JTextPane connectedClients;

    private MainPanel mainPanel;


    public GamePanel(Client client, GameController gc) {
        this.client = client;
        this.gc = gc;
        this.game = gc.getGame();
        setLayout(new BorderLayout());
        gc.register(this);
    }

    private JPanel createConnectedClientsPanel() {
         JPanel panel = new JPanel();
         panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
         panel.setBackground(Color.WHITE);
         panel.setLayout(new BorderLayout());
         panel.add(new JLabel(_("Connected clients:")), BorderLayout.NORTH);
         connectedClients = new JTextPane();
         connectedClients.setEditable(false);
         panel.add(connectedClients, BorderLayout.CENTER);
         return panel;
    }

    public void showCreateGamePanel(boolean mutableSlots, PlayerSlot[] slots) {
        createGamePanel = new CreateGamePanel(client, game, mutableSlots, slots);
        JPanel envelope = new BackgroundPanel();
        envelope.setLayout(new GridBagLayout()); //to have centered inner panel
        envelope.add(createGamePanel);

        JScrollPane scroll = new JScrollPane(envelope);
        scroll.setViewportBorder(null);  //ubuntu jdk
        scroll.setBorder(BorderFactory.createEmptyBorder()); //win jdk
        add(scroll, BorderLayout.CENTER);

        JPanel chatColumn = new JPanel();
        chatColumn.setOpaque(false);
        chatColumn.setLayout(new MigLayout("ins 0, gap 0 10", "[grow]", "[60px][grow]"));
        chatColumn.setPreferredSize(new Dimension(ChatPanel.CHAT_WIDTH, getHeight()));
        add(chatColumn, BorderLayout.WEST);

        chatColumn.add(createConnectedClientsPanel(), "cell 0 0, grow");

        JPanel chatBox = new JPanel();
        chatBox.setBackground(Color.WHITE);
        MigLayout chatBoxLayout = new MigLayout("", "[grow]", "[grow][]");
        chatBox.setLayout(chatBoxLayout);
        chatColumn.add(chatBox, "cell 0 1, grow");

        chatPanel = new GameChatPanel(client, game);
        chatPanel.registerSwingComponents(chatBox);
        chatBoxLayout.setComponentConstraints(chatPanel.getMessagesPane(), "cell 0 0, align 0% 100%");
        chatBoxLayout.setComponentConstraints(chatPanel.getInput(), "cell 0 1, growx");

        gc.register(createGamePanel);
        gc.register(chatPanel);
    }

    @Subscribe
    public void clientListChanged(ClientListChangedEvent ev) {
    	RemoteClient[] clients = ev.getClients();
        if (game.isStarted()) {
            if (!game.isOver()) {
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
        } else {
            connectedClients.setText(Joiner.on("\n").join(
                Collections2.transform(Arrays.asList(clients), new Function<RemoteClient, String>() {
                    @Override
                    public String apply(RemoteClient input) {
                        return input.getName();
                    }
            })));
        }
    }

    public void onWebsocketError(Exception ex) {
        GridPanel gp = getGridPanel();
        String msg = ex.getMessage();
        if (ex instanceof WebsocketNotConnectedException) {
            if (game.isStarted()) {
                msg = _("Connection lost") + " - save game and load on server side and then connect with client as workaround" ;
            } else {
                msg = _("Connection lost");
            }
        } else {
            logger.error(ex.getMessage(), ex);
        }
        if (msg == null || msg.length() == 0) {
            msg = ex.getClass().getSimpleName();
        }
        if (gp != null) {
            gp.setErrorMessage(msg);
        } else {
            JOptionPane.showMessageDialog(client, msg, _("Error"), JOptionPane.ERROR_MESSAGE);
        }
    }

    public void disposePanel() {
        if (createGamePanel != null) {
        	gc.unregister(createGamePanel);
        	createGamePanel.disposePanel();
        }
        if (chatPanel != null) {
        	gc.unregister(chatPanel);
        }
        gc.unregister(this);
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


    public void toggleRecentHistory(boolean show) {
        if (mainPanel != null) mainPanel.toggleRecentHistory(show);
    }


    public void setShowFarmHints(boolean showFarmHints) {
        if (mainPanel != null) mainPanel.setShowFarmHints(showFarmHints);
    }


    public void zoom(double steps) {
        GridPanel gp = getGridPanel();
        if (gp != null) gp.zoom(steps);
    }

    @Subscribe
    public void started(GameStateChangeEvent ev) {
        disposePanel(); //free createGamePanel
        removeAll();
        setBackgroundImage(null);

        createGamePanel = null;
        connectedClients = null;
        mainPanel = new MainPanel(client, gc, chatPanel);
        add(mainPanel, BorderLayout.CENTER);
        gc.getReportingTool().setContainer(getMainPanel());
        mainPanel.started(ev.getSnapshot());
    }
}
