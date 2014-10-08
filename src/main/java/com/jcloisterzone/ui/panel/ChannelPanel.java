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

import net.miginfocom.swing.MigLayout;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Collections2;
import com.google.common.eventbus.Subscribe;
import com.jcloisterzone.event.ClientListChangedEvent;
import com.jcloisterzone.ui.ChannelController;
import com.jcloisterzone.ui.Client;
import com.jcloisterzone.ui.controls.chat.ChannelChatPanel;
import com.jcloisterzone.ui.controls.chat.ChatPanel;
import com.jcloisterzone.wsio.message.CreateGameMessage;
import com.jcloisterzone.wsio.server.RemoteClient;

import static com.jcloisterzone.ui.I18nUtils._;

@SuppressWarnings("serial")
public class ChannelPanel extends JPanel {

    private final ChannelController cc;
    private ChatPanel chatPanel;
    private JTextPane connectedClients;
    private JPanel gameListPanel;

    private JButton createGameButton;


    public ChannelPanel(Client client, ChannelController cc) {
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
        gameListPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        gameListPanel.setBackground(Color.WHITE);
        add(gameListPanel, "cell 2 0, grow");

        createGameButton = new JButton(_("Create game"));
        createGameButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				createGame();
			}
		});
        gameListPanel.add(createGameButton);


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

}
