package com.jcloisterzone.ui.grid;

import java.awt.Color;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.Set;

import com.jcloisterzone.Expansion;
import com.jcloisterzone.Player;
import com.jcloisterzone.action.BarnAction;
import com.jcloisterzone.action.FeatureAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.action.TileAction;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.Road;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.Snapshot;
import com.jcloisterzone.ui.Client;
import com.jcloisterzone.ui.ImmutablePoint;
import com.jcloisterzone.ui.animation.AnimationService;
import com.jcloisterzone.ui.animation.ScoreAnimation;
import com.jcloisterzone.ui.grid.layer.AnimationLayer;
import com.jcloisterzone.ui.grid.layer.AvailableMovesLayer;
import com.jcloisterzone.ui.grid.layer.BarnAreaLayer;
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

	public MainPanel(Client client) {
		this.client = client;
		animationService = new AnimationService();
		animationService.start();

		setLayout(new GridBagLayout());
	}

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

		add(gridPanel);

		addMouseListener(
			new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					if (e.getButton() == MouseEvent.BUTTON3) {
						Tile nextTile = client.getControlPanel().getNextTileLabel().getTile();
						if (nextTile != null) {
							client.getControlPanel().rotateTile();
							return;
						}
						if (client.getControlPanel().getActionPanel().getActions() != null) {
							client.getControlPanel().getActionPanel().nextAction();
							return;
						}
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


	public void selectTilePlacement(Set<Position> positions) {
		gridPanel.clearActionDecorations();
		gridPanel.removeLayer(AvailableMovesLayer.class); //has effect after selectAbbeyPlacement withou abbey place!
		gridPanel.addLayer(new AvailableMovesLayer(gridPanel, positions));
	}


	public void closeGame() {
		gridPanel.clearActionDecorations();
		gridPanel.removeLayer(AvailableMovesLayer.class);
	}

	public void prepareAction(PlayerAction action) {
		gridPanel.clearActionDecorations();
		GridLayer layer = null;
		if (action instanceof FeatureAction) {
			layer = new FeatureAreaLayer(gridPanel, (FeatureAction) action);
		} else if (action instanceof TileAction) {
			layer = new TileActionLayer(gridPanel, (TileAction) action);
		} else if (action instanceof BarnAction) {
			layer = new BarnAreaLayer(gridPanel, (BarnAction) action);
		}
		gridPanel.addLayer(layer);
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
