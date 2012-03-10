package com.jcloisterzone.ui.controls;

import static com.jcloisterzone.ui.controls.ControlPanel.CORNER_DIAMETER;
import static com.jcloisterzone.ui.controls.ControlPanel.PANEL_WIDTH;

import java.awt.Graphics2D;
import java.awt.Image;

import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.ui.Client;
import com.jcloisterzone.ui.FakeComponent;

public class ActionPanel implements FakeComponent {
	
	public static final int LINE_HEIGHT = 30;
	public static final int PADDING = 3;
	public static final int ICON_SIZE = 40;
	public static final int ACTIVE_ICON_SIZE = 50;

	private final Client client;
	private PlayerAction[] actions;
	private int selectedActionIndex = -1;

	public ActionPanel(Client client) {
		this.client = client;

		//TODO
//		addMouseListener(new MouseAdapter() {
//			@Override
//			public void mouseClicked(MouseEvent e) {
//				if (e.getButton() != MouseEvent.BUTTON1) return;
//				if (e.getX() < 2) return;
//				int index = (e.getX() - 2) / ICON_SIZE;
//				if (index < actions.length && index >= 0) {
//					setSelectedActionIndex(index);
//				}
//			}
//		});
	}
	
	private void repaint() {
		client.getControlPanel().repaint();
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
		deselectAction();
		this.actions = null;
		this.selectedActionIndex = -1;
		repaint();
	}
	
	public void switchAction() {
		getSelectedAction().switchAction();
	}

	public void nextAction() {
		if (client.isClientActive()) {
			if (actions.length == 0) return;
			setSelectedActionIndex(selectedActionIndex == actions.length - 1 ? 0 : selectedActionIndex + 1);
			repaint();
			client.getGridPanel().repaint();
		}
	}
	
	private void deselectAction() {
		if (this.selectedActionIndex != -1) {
			PlayerAction prev = actions[this.selectedActionIndex];
			prev.deselect();
		}
	}

	private void setSelectedActionIndex(int selectedActionIndex) {
		deselectAction();
		this.selectedActionIndex = selectedActionIndex;
		PlayerAction action = actions[selectedActionIndex];		
		action.select();
	}

	public PlayerAction getSelectedAction() {
		return actions[selectedActionIndex];
	}
	
	@Override
	public void paintComponent(Graphics2D g2) {
		g2.setColor(ControlPanel.BG_COLOR);
		g2.fillRoundRect(0, 0, PANEL_WIDTH+CORNER_DIAMETER, LINE_HEIGHT, CORNER_DIAMETER, CORNER_DIAMETER);
		
		if (actions == null || actions.length == 0) return;

		int x = 2*PADDING;
		
		for(PlayerAction action : actions) {
			boolean active = (action == actions[selectedActionIndex]);					

			Image img = action.getImage(client.getGame().getActivePlayer(), active);
			int size = active ? ACTIVE_ICON_SIZE : ICON_SIZE;
			int iy = (LINE_HEIGHT-size) / 2;
			
			g2.drawImage(img, x, iy, size, size, null);
			x += size + PADDING;
		}	
	}
}
