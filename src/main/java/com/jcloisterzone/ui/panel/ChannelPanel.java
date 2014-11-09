package com.jcloisterzone.ui.panel;

import static com.jcloisterzone.ui.I18nUtils._;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Collections2;
import com.google.common.eventbus.Subscribe;
import com.jcloisterzone.Expansion;
import com.jcloisterzone.event.ClientListChangedEvent;
import com.jcloisterzone.event.GameListChangedEvent;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.ui.ChannelController;
import com.jcloisterzone.ui.Client;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.ui.controls.chat.ChannelChatPanel;
import com.jcloisterzone.ui.controls.chat.ChatPanel;
import com.jcloisterzone.wsio.message.CreateGameMessage;
import com.jcloisterzone.wsio.message.JoinGameMessage;
import com.jcloisterzone.wsio.server.RemoteClient;

import static com.jcloisterzone.ui.I18nUtils._;

@SuppressWarnings("serial")
public class ChannelPanel extends JPanel {

	private final Client client;
    private final ChannelController cc;
    private ChatPanel chatPanel;
    private JTextPane connectedClients;
    private JPanel gameListPanel;

    private JButton createGameButton;


    public ChannelPanel(Client client, ChannelController cc) {
    	this.client = client;
        this.cc = cc;
        setLayout(new MigLayout("", "[][][grow]", "[grow]"));

        add(createConnectedClientsPanel(), "cell 0 0, grow");

        JPanel chatBox = new JPanel();
        chatBox.setBackground(Color.WHITE);
        MigLayout chatBoxLayout = new MigLayout("", "[grow]", "[grow][]");
        chatBox.setLayout(chatBoxLayout);
        add(chatBox, "cell 1 0, grow, w 250");

        chatPanel = new ChannelChatPanel(client, cc);
        chatPanel.registerSwingComponents(chatBox);
        chatBoxLayout.setComponentConstraints(chatPanel.getMessagesPane(), "cell 0 0, align 0% 100%");
        chatBoxLayout.setComponentConstraints(chatPanel.getInput(), "cell 0 1, growx");

        gameListPanel = new JPanel();
        gameListPanel.setLayout(new MigLayout("", "[grow]", ""));
        gameListPanel.setBackground(Color.WHITE);
        add(gameListPanel, "cell 2 0, grow");

        createGameButton = new JButton(_("Create game"));
        createGameButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				createGame();
			}
		});
        gameListPanel.add(createGameButton, "wrap");

        cc.register(this);
        cc.register(chatPanel);
    }

    //copy from GamePanel!!!
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

    private void createGame() {
    	cc.getConnection().send(new CreateGameMessage(cc.getChannel().getName()));
    }

	@Subscribe
	public void clientListChanged(ClientListChangedEvent ev) {
    	RemoteClient[] clients = ev.getClients();

        connectedClients.setText(Joiner.on("\n").join(
            Collections2.transform(Arrays.asList(clients), new Function<RemoteClient, String>() {
                @Override
                public String apply(RemoteClient input) {
                    return input.getName();
                }
        })));

    }

	@Subscribe
	public void gameListChanged(GameListChangedEvent ev) {
		//remove all expect new game component
		while (gameListPanel.getComponentCount() > 1) {
			gameListPanel.remove(gameListPanel.getComponentCount() - 1);
		}

		for (GameController gc : ev.getGameControllers()) {
			gameListPanel.add(new GameItemPanel(gc), "wrap, growx");
		}
		gameListPanel.validate();
		gameListPanel.repaint();
    }

	class GameItemPanel extends JPanel {

		private JLabel name;
		private JLabel expansionNames;
		private JLabel connectedClients;
		private JButton joinButton;

		public GameItemPanel(final GameController gc) {
			final Game game = gc.getGame();
			setLayout(new MigLayout());

			name = new JLabel("Game");
			Joiner joiner = Joiner.on(", ").skipNulls();
			expansionNames = new JLabel(joiner.join(game.getExpansions()));
			connectedClients = new JLabel();
			if (gc.getRemoteClients() != null) {
				connectedClients.setText(joiner.join(gc.getRemoteClients()));
			}

			joinButton = new JButton(_("Join game"));
			joinButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					cc.getConnection().send(new JoinGameMessage(game.getGameId()));
					client.openGameSetup(gc, true);
				}
			});

			add(name, "wrap");
			add(expansionNames, "wrap");
			add(connectedClients, "wrap");
			add(joinButton, "wrap");
		}
	}
}
