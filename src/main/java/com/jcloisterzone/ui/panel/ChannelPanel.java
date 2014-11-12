package com.jcloisterzone.ui.panel;

import static com.jcloisterzone.ui.I18nUtils._;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;

import net.miginfocom.swing.MigLayout;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;
import com.jcloisterzone.Expansion;
import com.jcloisterzone.event.ClientListChangedEvent;
import com.jcloisterzone.event.GameListChangedEvent;
import com.jcloisterzone.event.setup.ExpansionChangedEvent;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.ui.ChannelController;
import com.jcloisterzone.ui.Client;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.ui.controls.chat.ChannelChatPanel;
import com.jcloisterzone.ui.controls.chat.ChatPanel;
import com.jcloisterzone.wsio.message.CreateGameMessage;
import com.jcloisterzone.wsio.message.JoinGameMessage;
import com.jcloisterzone.wsio.server.RemoteClient;

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

		private Joiner joiner = Joiner.on(", ").skipNulls();
		private Set<Expansion> expansions;

		public GameItemPanel(final GameController gc) {
			final Game game = gc.getGame();
			setLayout(new MigLayout());

			expansions = new HashSet<Expansion>(game.getExpansions());
			expansions.remove(Expansion.BASIC);

			name = new JLabel("Game");

			expansionNames = new JLabel();
			updateExpansionsLabel();
			connectedClients = new JLabel();
			updateClientsLabel(gc.getRemoteClients());

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

			//TODO but what about unregister
			gc.register(this);
		}

		private void updateClientsLabel(RemoteClient[] clients) {
			if (clients == null) return;

			List<RemoteClient> list = Arrays.asList(clients);
			connectedClients.setText(joiner.join(Lists.transform(list, new Function<RemoteClient, String>() {
				@Override
				public String apply(RemoteClient rc) {
					return rc.getName();
				}
			})));
		}

		private void updateExpansionsLabel() {
			String label = joiner.join(expansions);
			if (label.length() == 0) label = Expansion.BASIC.toString();
			expansionNames.setText(label);
		}

		@Subscribe
		public void clientListChanged(ClientListChangedEvent ev) {
	    	updateClientsLabel(ev.getClients());
	    }

		@Subscribe
		public void expansionsChanged(ExpansionChangedEvent ev) {
			if (ev.getExpansion() == Expansion.BASIC) return;
			if (ev.isEnabled()) {
				expansions.add(ev.getExpansion());
			} else {
				expansions.remove(ev.getExpansion());
			}
			updateExpansionsLabel();
		}
	}
}
