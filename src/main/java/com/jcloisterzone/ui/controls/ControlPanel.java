package com.jcloisterzone.ui.controls;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.util.Arrays;
import java.util.List;

import javax.swing.JPanel;

import com.jcloisterzone.Player;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.ui.Client;

public class ControlPanel extends JPanel {
	
	public static final Color BG_COLOR = new Color(0, 0, 0, 30);
	public static final Color ACTIVE_BG_COLOR = new Color(0, 0, 0, 45);
	public static final Color SHADOW_COLOR = new Color(0, 0, 0, 60);
	public static final int CORNER_DIAMETER = 20;

	private final Client client;
	
	private boolean canPass;	
	
	/*private NextSquare nextTileLabel;
	private JLabelWithAntialiasing packSize;
	private JButton buttonNextTurn;*/
	
	private ActionPanel actionPanel;	
	private PlayerPanel[] playerPanels;

	public static final int PANEL_WIDTH = 220;
//	private static Font cardFont = new Font("Helvecia", Font.BOLD, 22);
//	private static final String IMG_END_TURN = "sysimages/endTurn.png";

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
					}
				}
				return false;
			}
		});		
		
		setOpaque(false);
//		setLayout(new MigLayout("", "[]", "[][][]"));
//
//			JPanel top = new JPanel(new MigLayout());
//			nextTileLabel = new NextSquare(client);
//			top.add(nextTileLabel, "");
//
//			packSize = new JLabelWithAntialiasing("");
//			packSize.setFont(cardFont);
//			top.add(packSize, "wrap, gapleft 10");
//
//			buttonNextTurn = new JButton(new ImageIcon(ControlPanel.class.getClassLoader().getResource(IMG_END_TURN)));
//			buttonNextTurn.setToolTipText(_("Next"));
//			buttonNextTurn.addActionListener(new ActionListener() {
//				public void actionPerformed(ActionEvent e) {
//					selectNoAction();
//				}
//			});
//			buttonNextTurn.setEnabled(false);
//			top.add(buttonNextTurn, "wrap");
//
//		add(top, "wrap");

		actionPanel = new ActionPanel(client);		
		
		Player[] players = client.getGame().getAllPlayers();
		playerPanels = new PlayerPanel[players.length];
		for (int i = 0; i < players.length; i++) {
			playerPanels[i] = new PlayerPanel(client, players[i]);
		}
		
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		Graphics2D g2 = (Graphics2D) g;
		AffineTransform origTransform = g2.getTransform();
		
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		g2.translate(0, 70);		
		actionPanel.paintComponent(g2);
		g2.translate(0, 60);
		
		for (PlayerPanel pp : playerPanels) {			
			pp.paintComponent(g2);
		}
		
		g2.setTransform(origTransform);
	}

	public void pass() {
		client.getServer().pass();		
	}

	public ActionPanel getActionPanel() {
		return actionPanel;
	}

//	public Rotation getRotation() {
//		return tileRotation;
//	}

//	public void rotateTile() {
//		if (tileRotation != null) {
//			tileRotation = tileRotation.next();
//			client.getGridPanel().repaint();		
//			repaint();
//		}
//	}

	public void started() {
		//playersPanel.started();
	}

//	public void tileDrawn(Tile tile) {
//		tileRotation = Rotation.R0;
//		//nextTileLabel.setTile(tile);
//		//packSize.setText(client.getGame().getTilePack().tolalSize() + "");
//	}

//	public void selectTilePlacement(Set<Position> positions) {
//		tileRotation = Rotation.R0;
//		//nextTileLabel.setEnabled(true);
//		//nextTileLabel.requestFocus();
//		TilePlacementAction action = new TilePlacementAction(client.getGame().getTile(), positions);
//		action.setClient(client);
//		actionPanel.setActions(new PlayerAction[] { action } );
//	}

//	public void selectAbbeyPlacement(Set<Position> positions) {
//		//tileRotation = Rotation.R0;
//		//nextTileLabel.setEnabled(true);
//		//nextTileLabel.requestFocus();
//		//canPass = true;
//	}

//	public void tilePlaced(Tile tile) {
//		//nextTileLabel.setEmptyIcon();
//		tileRotation = null;
//	}

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
//		buttonNextTurn.setEnabled(canPass);
//		buttonNextTurn.requestFocus();
		repaint(); //players only
	}

	public void clearActions() {
		actionPanel.clearActions();
		canPass = false;
	}

	public void playerActivated(Player turn, Player active) {
		repaint(); //players only
	}

	public void closeGame() {
		clearActions();
		//nextTileLabel.setEmptyIcon();
		//buttonNextTurn.setEnabled(false);
		canPass = false;
		//packSize.setText("");
	}

}

