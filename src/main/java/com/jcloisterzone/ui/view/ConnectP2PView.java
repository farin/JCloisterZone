package com.jcloisterzone.ui.view;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridBagLayout;

import javax.swing.JPanel;

import com.jcloisterzone.ui.Client;
import com.jcloisterzone.ui.panel.BackgroundPanel;
import com.jcloisterzone.ui.panel.ConnectGamePanel;

public class ConnectP2PView extends AbstractUiView {

    private ConnectGamePanel panel;

    public ConnectP2PView(Client client) {
        super(client);
    }

    @Override
    public void show(Container pane, Object ctx) {
        JPanel envelope = new BackgroundPanel();
        envelope.setLayout(new GridBagLayout()); //to have centered inner panel

        panel = new ConnectGamePanel(client);
        envelope.add(panel);

        pane.add(envelope, BorderLayout.CENTER);
    }

    @Override
    public void onWebsocketError(Exception ex) {
        panel.onWebsocketError(ex);
    }
}
