package com.jcloisterzone.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Image;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JLayer;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

import com.jcloisterzone.Expansion;
import com.jcloisterzone.Player;
import com.jcloisterzone.UserInterface;
import com.jcloisterzone.action.CaptureAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.event.GameEventListener;
import com.jcloisterzone.feature.Castle;
import com.jcloisterzone.feature.Completable;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.visitor.score.CompletableScoreContext;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.figure.SmallFollower;
import com.jcloisterzone.game.CustomRule;
import com.jcloisterzone.game.PlayerSlot;
import com.jcloisterzone.game.Snapshot;
import com.jcloisterzone.ui.controls.ControlPanel;
import com.jcloisterzone.ui.dialog.DiscardedTilesDialog;
import com.jcloisterzone.ui.dialog.GameOverDialog;
import com.jcloisterzone.ui.grid.MainPanel;
import com.jcloisterzone.ui.grid.OverlayScrollPaneLayout;
import com.jcloisterzone.ui.grid.layer.DragonAvailableMove;
import com.jcloisterzone.ui.grid.layer.DragonLayer;

public class ClientController implements GameEventListener, UserInterface {
	
	private final Client client;
	
	public ClientController(Client client) {
		this.client = client;
	}
	
	@Override
	public void updateCustomRule(CustomRule rule, Boolean enabled) {
		client.getCreateGamePanel().updateCustomRule(rule, enabled);
	}

	@Override
	public void updateExpansion(Expansion expansion, Boolean enabled) {
		client.getCreateGamePanel().updateExpansion(expansion, enabled);
	}

	@Override
	public void updateSlot(PlayerSlot slot) {
		client.getCreateGamePanel().updateSlot(slot);
	}

	@Override
	public void updateSupportedExpansions(EnumSet<Expansion> expansions) {
		client.getCreateGamePanel().updateSupportedExpansions(expansions);
	}

	@Override
	public void started(Snapshot snapshot) {
		client.cleanContentPane();

		Container pane = client.getContentPane();		
		pane.setLayout(new BorderLayout());		
		
		JScrollPane scroll = new JScrollPane();
		scroll.setLayout(new OverlayScrollPaneLayout());
		scroll.setWheelScrollingEnabled(false);
				
		//JLayer needed to correct rendering of overlay components
		JLayer<JComponent> layer = new JLayer<JComponent>(scroll);
		pane.add(layer, BorderLayout.CENTER);				
				
		MainPanel mainPanel = new MainPanel(client);
		client.setMainPanel(mainPanel);
		scroll.setViewportView(mainPanel);
		
		ControlPanel controlPanel = new ControlPanel(client);
		client.setControlPanel(controlPanel);
		scroll.add(controlPanel, OverlayScrollPaneLayout.OVERLAY, 0);		
		
		
		pane.setVisible(true);

		mainPanel.started(snapshot);
		controlPanel.started();
		MenuBar menu = client.getJMenuBar();
		menu.setZoomInEnabled(true);
		menu.setZoomOutEnabled(true);
		menu.setIsGameRunning(true);
	}

	@Override
	public void playerActivated(Player turnPlayer, Player activePlayer) {
		client.setActivePlayer(activePlayer);
		client.getControlPanel().playerActivated(turnPlayer, activePlayer);

		//TODO better image quality ?
		Color c = client.getPlayerColor(activePlayer);
		Image image = client.getFigureTheme().getFigureImage(SmallFollower.class, c, null);
		client.setIconImage(image);
	}

	@Override
	public void tileDrawn(Tile tile) {
		client.clearActions();
		client.getControlPanel().tileDrawn(tile);
	}

	@Override
	public void tileDiscarded(String tileId) {
		DiscardedTilesDialog discardedTilesDialog = client.getDiscardedTilesDialog();
		if (discardedTilesDialog == null) {
			discardedTilesDialog = new DiscardedTilesDialog(client);
			client.setDiscardedTilesDialog(discardedTilesDialog);
			client.getJMenuBar().setShowDiscardedEnabled(true);
		}
		discardedTilesDialog.addTile(tileId);
		discardedTilesDialog.setVisible(true);
	}

	@Override
	public void tilePlaced(Tile tile) {
		client.getMainPanel().tilePlaced(tile);
		client.getControlPanel().tilePlaced(tile);
	}

	@Override
	public void dragonMoved(Position p) {
		client.getMainPanel().dragonMoved(p);
	}
	
	@Override
	public void tunnelPiecePlaced(Player player, Position p, Location d, boolean isSecondPiece) {
		client.getMainPanel().tunnelPiecePlaced(player, p, d, isSecondPiece);
	}


	@Override
	public void gameOver() {
		client.resetWindowIcon();
		client.closeGame(true);
		new GameOverDialog(client);
	}
	
	@Override
	public void fairyMoved(Position p) {
		client.getMainPanel().fairyMoved(p);
	}

	@Override
	public void towerIncreased(Position p, Integer height) {
		client.clearActions();
		client.getMainPanel().towerIncreased(p, height);
	}

	@Override
	public void ransomPaid(Player from, Player to, Follower f) {
		client.getControlPanel().repaint();
	}
	
	//------------------ Meeple events -----------


	@Override
	public void deployed(Meeple m) {
		client.getMainPanel().deployed(m);
	}

	@Override
	public void undeployed(Meeple m) {
		client.getMainPanel().undeployed(m);
	}
	
	@Override
	public void bridgeDeployed(Position pos, Location loc) {
		client.getMainPanel().bridgeDeployed(pos, loc);	
	}
	
	@Override
	public void castleDeployed(Castle castle1, Castle castle2) {
		client.getMainPanel().castleDeployed(castle1, castle2);
	}
	
	// ------------------ Feature evnts ----------

	@Override
	public void completed(Completable feature, CompletableScoreContext ctx) { }

	@Override
	public void scored(Feature feature, int points, String label, Meeple meeple, boolean finalScoring) {
		client.getMainPanel().scored(feature, label, meeple, finalScoring);
		client.getControlPanel().repaint(); //players only
	}

	@Override
	public void scored(Position position, Player player, int points, String label, boolean finalScoring) {
		client.getMainPanel().scored(position, player, label, finalScoring);
		client.getControlPanel().repaint(); //players only
	}
	
	// User interface
	
	@Override
	public void showWarning(String title, String message) {
		JOptionPane.showMessageDialog(client, message, title, JOptionPane.WARNING_MESSAGE);		
	}

	

	@Override
	public void selectDragonMove(Set<Position> positions, int movesLeft) {
		client.clearActions();
		DragonLayer dragonDecoration = client.getGridPanel().findDecoration(DragonLayer.class);
		dragonDecoration.setMoves(movesLeft);
		client.getGridPanel().repaint();
		if (client.isClientActive()) {
			client.getGridPanel().addLayer(new DragonAvailableMove(client.getGridPanel(), positions));
			client.beep();
		}
	}	

	@Override
	public void selectAbbeyPlacement(Set<Position> positions) {
		client.clearActions();
		client.getControlPanel().tileDrawn(client.getGame().getTilePack().getAbbeyTile());
		if (client.isClientActive()) {
			client.beep();
			client.getControlPanel().selectAbbeyPlacement(positions);
		}
		client.getMainPanel().selectTilePlacement(positions);
	}

	@Override
	public void selectTilePlacement(Map<Position, Set<Rotation>> placements) {
		client.clearActions();
		Set<Position> positions = placements.keySet();
		if (client.isClientActive()) {
			client.beep();
			client.getControlPanel().selectTilePlacement(positions);
		}
		client.getMainPanel().selectTilePlacement(positions);
	}

	@Override
	public void selectAction(List<PlayerAction> actions) {
		if (client.isClientActive()) {
			client.getControlPanel().selectAction(actions, true);
		}
	}

	@Override
	public void selectTowerCapture(CaptureAction action) {
		client.getControlPanel().selectAction(Collections.<PlayerAction>singletonList(action), false);
	}

}
