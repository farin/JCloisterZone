package com.jcloisterzone.ui.panel;

import static com.jcloisterzone.ui.I18nUtils._;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
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
import javax.swing.JTextField;
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
import com.jcloisterzone.event.setup.RuleChangeEvent;
import com.jcloisterzone.game.CustomRule;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.ui.ChannelController;
import com.jcloisterzone.ui.Client;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.ui.LengthRestrictedDocument;
import com.jcloisterzone.ui.controls.chat.ChannelChatPanel;
import com.jcloisterzone.ui.controls.chat.ChatPanel;
import com.jcloisterzone.wsio.message.CreateGameMessage;
import com.jcloisterzone.wsio.message.JoinGameMessage;
import com.jcloisterzone.wsio.server.RemoteClient;

@SuppressWarnings("serial")
public class ChannelPanel extends JPanel {

	private static final int MAX_GAME_TITLE_LENGTH = 60;

	private final Client client;
    private final ChannelController cc;
    private ChatPanel chatPanel;
    private JTextPane connectedClients;
    private JPanel gameListPanel;

    public ChannelPanel(Client client, ChannelController cc) {
    	this.client = client;
        this.cc = cc;
        setLayout(new MigLayout("ins 0", "[][][grow]", "[grow]"));

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

        gameListPanel.add(createCreateGamePanel(), "wrap, growx");

        cc.register(this);
        cc.register(chatPanel);
    }

    private JPanel createCreateGamePanel() {
    	JPanel createGamePanel = new JPanel(new MigLayout());

    	createGamePanel.add(new JLabel(_("Game title")+":"));

    	String defaultTitle = cc.getConnection().getNickname() + "'s game";
    	final JTextField gameTitle = new JTextField();
    	gameTitle.setDocument(new LengthRestrictedDocument(MAX_GAME_TITLE_LENGTH));
    	gameTitle.setText(defaultTitle); //set after document
    	createGamePanel.add(gameTitle, "wrap, width 250::");

        JButton createGameButton = new JButton(_("Create game"));
        createGameButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String title = gameTitle.getText().trim();
				cc.getConnection().send(new CreateGameMessage(title, cc.getChannel().getName()));
			}
		});
        createGamePanel.add(createGameButton, "wrap, span 2");
        return createGamePanel;
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

	private static Font FONT_GAME_TITLE = new Font(null, Font.BOLD, 20);

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

			name = new JLabel(game.getName());
			name.setFont(FONT_GAME_TITLE);

			expansionNames = new JLabel();
			updateExpansionsLabel();
			connectedClients = new JLabel();
			updateClientsLabel(gc.getRemoteClients());

			joinButton = new JButton(_("Join game"));
			joinButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					cc.getConnection().send(new JoinGameMessage(game.getGameId()));
					//client.openGameSetup(gc, true);
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
			String label = joiner.join(Lists.transform(list, new Function<RemoteClient, String>() {
				@Override
				public String apply(RemoteClient rc) {
					return rc.getName();
				}
			}));
			connectedClients.setText(_("Players") + ": " + label);
		}

		private void updateExpansionsLabel() {
			String label = joiner.join(expansions);
			if (label.length() == 0) label = Expansion.BASIC.toString();
			expansionNames.setText(_("Expansions") + ": " + label);
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
