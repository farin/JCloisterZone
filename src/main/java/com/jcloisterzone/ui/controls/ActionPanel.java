package com.jcloisterzone.ui.controls;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.List;

import javax.swing.JPanel;

import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.action.TunnelAction;
import com.jcloisterzone.ui.Client;


public class ActionPanel extends JPanel {

	public static final int ICON_SIZE = 30;
	public static final int ICON_MARGIN = 2;

	private final Client client;
	private PlayerAction[] actions;
	private int selectedActionIndex;

	public ActionPanel(Client client) {
		this.client = client;

		Dimension d = new Dimension(ControlPanel.PANEL_WIDTH, ICON_SIZE);
		setMinimumSize(d);
		setMaximumSize(d);
		setPreferredSize(d);

		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() != MouseEvent.BUTTON1) return;
				if (e.getX() < 2) return;
				int index = (e.getX() - 2) / ICON_SIZE;
				if (index < actions.length && index >= 0) {
					setSelectedActionIndex(index);
				}
			}
		});
	}

	public PlayerAction[] getActions() {
		return actions;
	}

	public void setActions(PlayerAction[] actions) {
		if (client.isClientActive()) {
			this.actions = actions;
			if (actions.length > 0) {
				setSelectedActionIndex(0);
			}
			repaint();
		}
	}

	public void clearActions() {
		this.actions = null;
		repaint();
	}

	public void nextAction() {
		if (client.isClientActive()) {
			if (actions.length == 0) return;
			setSelectedActionIndex(selectedActionIndex == actions.length - 1 ? 0 : selectedActionIndex + 1);
			repaint();
			client.getGridPanel().repaint();
		}
	}

	private void setSelectedActionIndex(int selectedActionIndex) {
		this.selectedActionIndex = selectedActionIndex;
		PlayerAction action = actions[selectedActionIndex];
		client.getControlPanel().getNextTileLabel().setSelectedFigure( getActionImage(action, true));
		client.getMainPanel().prepareAction(action);
	}

	public PlayerAction getSelectedAction() {
		return actions[selectedActionIndex];
	}

	private Image getActionImage(PlayerAction action, boolean active) {
		Color color = Color.GRAY;
		if (active) {
			if (action instanceof TunnelAction && ((TunnelAction) action).isSecondTunnelPiece()) {
				color = client.getPlayerSecondTunelColor(client.getGame().getActivePlayer());
			} else {
				color = client.getPlayerColor();
			}
		}
		return client.getFigureTheme().getActionImage(action, color);
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		Graphics2D g2 = (Graphics2D) g;

		g2.setColor(Color.BLACK);
		g2.drawLine(0, 0, getWidth()-1, 0);
		g2.drawLine(0, getHeight()-1, getWidth()-1, getHeight()-1);

		if (actions == null || actions.length == 0) return;

		int offset = 0;
		for(PlayerAction action : actions) {
			boolean active = false;
			if (action == actions[selectedActionIndex]) {
				active = true;
				g2.setColor(Color.WHITE);
				g2.fillRect(offset, 1, ICON_SIZE, ICON_SIZE-2);
			}
			Image img = getActionImage(action, active);
			g2.drawImage(img, offset+ICON_MARGIN, ICON_MARGIN, ICON_SIZE-ICON_MARGIN, ICON_SIZE-ICON_MARGIN, null);
			offset += ICON_SIZE;
		}

	}

}

