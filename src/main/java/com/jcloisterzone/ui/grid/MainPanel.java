package com.jcloisterzone.ui.grid;

import java.awt.Color;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.Set;

import com.jcloisterzone.Expansion;
import com.jcloisterzone.Player;
import com.jcloisterzone.action.BarnAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.action.SelectFeatureAction;
import com.jcloisterzone.action.SelectTileAction;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.feature.Castle;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.Road;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.Snapshot;
import com.jcloisterzone.game.phase.TilePhase;
import com.jcloisterzone.ui.Client;
import com.jcloisterzone.ui.ImmutablePoint;
import com.jcloisterzone.ui.animation.AnimationService;
import com.jcloisterzone.ui.animation.ScoreAnimation;
import com.jcloisterzone.ui.grid.layer.AnimationLayer;
import com.jcloisterzone.ui.grid.layer.AbstractTilePlacementLayer;
import com.jcloisterzone.ui.grid.layer.BarnAreaLayer;
import com.jcloisterzone.ui.grid.layer.BridgeLayer;
import com.jcloisterzone.ui.grid.layer.CastleLayer;
import com.jcloisterzone.ui.grid.layer.DragonAvailableMove;
import com.jcloisterzone.ui.grid.layer.DragonLayer;
import com.jcloisterzone.ui.grid.layer.FairyLayer;
import com.jcloisterzone.ui.grid.layer.FeatureAreaLayer;
import com.jcloisterzone.ui.grid.layer.MeepleLayer;
import com.jcloisterzone.ui.grid.layer.TileActionLayer;
import com.jcloisterzone.ui.grid.layer.TileLayer;
import com.jcloisterzone.ui.grid.layer.TowerLayer;
import com.jcloisterzone.ui.panel.BackgroundPanel;


@SuppressWarnings("serial")
public class MainPanel extends BackgroundPanel {


	private final Client client;
	private AnimationService animationService;

	private GridPanel gridPanel;
	private TileLayer tileLayer;
	private MeepleLayer meepleLayer;
	private TowerLayer towerLayer;
	private DragonLayer dragonLayer;
	private FairyLayer fairyLayer;
	private BridgeLayer bridgeLayer;
	private CastleLayer castleLayer;

	public MainPanel(Client client) {
		this.client = client;
		animationService = new AnimationService();
		animationService.start();

		//setLayout(new GridBagLayout());
		//setLayout(new BorderLayout());
		//setLayout(new MigLayout());
		setLayout(new MainPanelLayout());
	}
	
//	@Override
//	public boolean isOptimizedDrawingEnabled() {
//		return false;
//	}

	public GridPanel getGridPanel() {
		return gridPanel;
	}

	public AnimationService getAnimationService() {
		return animationService;
	}


	public void started(Snapshot snapshot) {
		animationService.clearAll();
		animationService.setGridPanel(null);
		removeAll();
		setVisible(false);

		//squareSizeUpdate = 0;

		gridPanel = new GridPanel(client, snapshot);
		meepleLayer = new MeepleLayer(gridPanel);
		tileLayer = new TileLayer(gridPanel);
		gridPanel.addLayer(tileLayer);
		gridPanel.addLayer(meepleLayer);
		gridPanel.addLayer(new AnimationLayer(gridPanel, animationService));

		animationService.setGridPanel(gridPanel);

		if (client.getGame().hasExpansion(Expansion.TOWER)) {
			towerLayer = new TowerLayer(gridPanel);
			gridPanel.addLayer(towerLayer);
		}
		if (client.getGame().hasExpansion(Expansion.PRINCESS_AND_DRAGON)) {
			dragonLayer = new DragonLayer(gridPanel, null);
			fairyLayer = new FairyLayer(gridPanel, null);
			gridPanel.addLayer(dragonLayer);
			gridPanel.addLayer(fairyLayer);
		}
		if (client.getGame().hasExpansion(Expansion.BRIDGES_CASTLES_AND_BAZAARS)) {
			bridgeLayer = new BridgeLayer(gridPanel);
			gridPanel.addLayer(bridgeLayer);
			castleLayer = new CastleLayer(gridPanel);
			gridPanel.addLayer(castleLayer);
		}

		add(gridPanel);

		addMouseListener(
			new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					if (!client.isClientActive()) return;
					if (e.getButton() == MouseEvent.BUTTON3) {
						client.getControlPanel().getActionPanel().switchAction();						
					}
				}
			}
		);
		addMouseWheelListener(new MouseWheelListener() {
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				gridPanel.zoom(-e.getWheelRotation());
			}
		});
		setVisible(true);
	}

	public void closeGame() {
		gridPanel.clearActionDecorations();
		gridPanel.removeLayer(AbstractTilePlacementLayer.class);
	}

	public void tilePlaced(Tile tile) {
		gridPanel.tilePlaced(tile, tileLayer);
	}

	public void deployed(Meeple m) {
		gridPanel.clearActionDecorations();
		meepleLayer.meepleDeployed(m);
	}

	public void undeployed(Meeple m) {
		gridPanel.clearActionDecorations();
		meepleLayer.meepleUndeployed(m);
	}
	
	public void bridgeDeployed(Position pos, Location loc) {
		gridPanel.clearActionDecorations();
		bridgeLayer.bridgeDeployed(pos, loc);	
	}
	
	public void castleDeployed(Castle castle1, Castle castle2) {
		gridPanel.clearActionDecorations();
		castleLayer.castleDeployed(castle1, castle2);
	}

	private Integer getScoreAnimationDuration() {
		return client.getConfig().get("ui", "score_display_duration", Integer.class);
	}

	public void scored(Feature scoreable, String points, Meeple m, boolean finalScoring) {
		Position pos = m.getPosition();
		animationService.registerAnimation(pos,
			new ScoreAnimation(
					pos,
					points,
					client.getTileTheme().getFigurePlacement(client.getGame().getBoard().get(pos), m),
					client.getPlayerColor(m.getPlayer()),
					finalScoring ? null : getScoreAnimationDuration()
			)
		);
	}

	public void scored(Position pos, Player player, String points, boolean finalScoring) {
		animationService.registerAnimation(pos,
			new ScoreAnimation(
					pos,
					points,
					new ImmutablePoint(50, 50),
					client.getPlayerColor(player),
					finalScoring ? null : getScoreAnimationDuration()
			)
		);

	}

	public void towerIncreased(Position p, Integer height) {
		towerLayer.setTowerHeight(p, height);
		gridPanel.repaint();
	}

	public void tunnelPiecePlaced(Player player, Position p, Location loc, boolean isSecondPiece) {
		Color c;
		if (isSecondPiece) {
			c = client.getPlayerSecondTunelColor(player);
		} else {
			c = client.getPlayerColor(player);
		}
		Image tunnelPiece = client.getFigureTheme().getTunnelImage(c);
		ImmutablePoint offset = client.getTileTheme().getFigurePlacement(gridPanel.getTile(p), Road.class, loc);
		meepleLayer.addPermanentImage(p, offset, tunnelPiece);
	}

	public void dragonMoved(Position p) {
		dragonLayer.setPosition(p);
		dragonLayer.setMoves(0);
		gridPanel.removeLayer(DragonAvailableMove.class);
		gridPanel.repaint();
	}

	public void fairyMoved(Position p) {
		fairyLayer.setPosition(p);
	}

}
