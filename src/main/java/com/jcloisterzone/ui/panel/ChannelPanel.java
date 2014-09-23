package com.jcloisterzone.ui.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;

import net.miginfocom.swing.MigLayout;

import com.jcloisterzone.ui.Client;
import com.jcloisterzone.ui.controls.ChatPanel;

import static com.jcloisterzone.ui.I18nUtils._;

public class ChannelPanel extends JPanel {

    private ChatPanel chatPanel;
    private JTextPane connectedClients;


    public ChannelPanel(Client client) {
        setLayout(new MigLayout("", "[]", "[grow]"));

        add(createConnectedClientsPanel(), "cell 0 0,");

//        JPanel chatColumn = new JPanel();
//        chatColumn.setOpaque(false);
//        chatColumn.setLayout(new MigLayout("ins 0, gap 0 10", "[grow]", "[60px][grow]"));
//        chatColumn.setPreferredSize(new Dimension(ChatPanel.CHAT_WIDTH, getHeight()));
//        add(chatColumn, BorderLayout.WEST);
//
//        JPanel chatBox = new JPanel();
//        chatBox.setBackground(Color.WHITE);
//        MigLayout chatBoxLayout = new MigLayout("", "[grow]", "[grow][]");
//        chatBox.setLayout(chatBoxLayout);
//        chatColumn.add(chatBox, "cell 0 1, grow");
//
//        chatPanel = new ChatPanel(client, game);
//        chatPanel.registerSwingComponents(chatBox);
//        chatBoxLayout.setComponentConstraints(chatPanel.getMessagesPane(), "cell 0 0, align 0% 100%");
//        chatBoxLayout.setComponentConstraints(chatPanel.getInput(), "cell 0 1, growx");
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

}
