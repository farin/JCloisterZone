package com.jcloisterzone.ui.controls;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.util.Arrays;
import java.util.List;

import com.jcloisterzone.Player;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.ui.Client;
import com.jcloisterzone.ui.FakeComponent;

public class ControlPanel implements FakeComponent {
	
	public static final Color BG_COLOR = new Color(0, 0, 0, 30);
	public static final Color ACTIVE_BG_COLOR = new Color(0, 0, 0, 45);
	public static final Color SHADOW_COLOR = new Color(0, 0, 0, 60);
	public static final int CORNER_DIAMETER = 20;

	private final Client client;
	
	private boolean canPass;	
		
	private ActionPanel actionPanel;	
	private PlayerPanel[] playerPanels;

	public static final int PANEL_WIDTH = 220;

	public ControlPanel(final Client client) {
		this.client = client;
		
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
			@Override
			public boolean dispatchKeyEvent(KeyEvent e) {				
				if (e.getModifiers() == 0 && e.getID() == KeyEvent.KEY_PRESSED) {
					switch (e.getKeyCode()) {
					case KeyEvent.VK_SPACE:
					case KeyEvent.VK_ENTER:					
						if (canPass && client.isClientActive()) {
							pass();
							return true;
						}
						break;
					case KeyEvent.VK_TAB:
						if (client.isClientActive()) {
							actionPanel.switchAction();							
							return true;
						}
						break;
					case KeyEvent.VK_LEFT:
						client.getGridPanel().moveCenter(-1, 0);
						break;
					case KeyEvent.VK_RIGHT:
						client.getGridPanel().moveCenter(1, 0);
						break;
					case KeyEvent.VK_DOWN:
						client.getGridPanel().moveCenter(0, -1);
						break;
					case KeyEvent.VK_UP:
						client.getGridPanel().moveCenter(0, 1);
						break;
					}						
				}
				return false;
			}
		});		

		actionPanel = new ActionPanel(client);		
		
		Player[] players = client.getGame().getAllPlayers();
		playerPanels = new PlayerPanel[players.length];
		for (int i = 0; i < players.length; i++) {
			playerPanels[i] = new PlayerPanel(client, players[i]);
		}
		
	}
	
	@Override
	public void paintComponent(Graphics2D g2) {
		AffineTransform origTransform = g2.getTransform();
				
//		GridPanel gp = client.getGridPanel();
		
		g2.translate(0, 70);		
		actionPanel.paintComponent(g2);
		
//		gp.profile("action panel");
		
		g2.translate(0, 60);						
		for (PlayerPanel pp : playerPanels) {			
			pp.paintComponent(g2);			
		}
		
//		gp.profile("players");
		
		g2.setTransform(origTransform);
	}

	public void pass() {
		client.getServer().pass();		
	}

	public ActionPanel getActionPanel() {
		return actionPanel;
	}


	public void selectAction(List<PlayerAction> actions, boolean canPass) {
		//direct collection sort can be unsupported - so copy to array first!
		int i = 0;
		PlayerAction[] arr = new PlayerAction[actions.size()];
		for(PlayerAction pa : actions) {
			pa.setClient(client);
			arr[i++] = pa;			
		}
		Arrays.sort(arr);
		actionPanel.setActions(arr);
		this.canPass = canPass; 
		client.getGridPanel().repaint(); //players only
	}

	public void clearActions() {
		actionPanel.clearActions();
		canPass = false;
	}

	public void playerActivated(Player turn, Player active) {
		client.getGridPanel().repaint(); //players only
	}

	public void closeGame() {
		clearActions();
		canPass = false;
	}

}

