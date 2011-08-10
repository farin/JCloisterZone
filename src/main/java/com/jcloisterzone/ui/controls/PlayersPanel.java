package com.jcloisterzone.ui.controls;

import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import com.jcloisterzone.Player;
import com.jcloisterzone.ui.Client;
import com.jcloisterzone.ui.legacy.PlayerPanel;


public class PlayersPanel extends JPanel {

	private static final long serialVersionUID = -4517651017858336804L;
	
	private final Client client;

	public PlayersPanel(Client client) {
		this.client = client;
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
	}

	public void started() {
		removeAll();

		Dimension d = new Dimension(ControlPanel.PANEL_WIDTH, PlayerPanel.getHeight(client.getGame()));

		for(Player player : client.getGame().getAllPlayers()) {
			PlayerPanel panel = new PlayerPanel(client, player);
			panel.setPreferredSize(d);
			panel.setMinimumSize(d);
			panel.setMaximumSize(d);
			add(panel);
		}

		repaint(); //clear if prev game has more players than next
	}


}
