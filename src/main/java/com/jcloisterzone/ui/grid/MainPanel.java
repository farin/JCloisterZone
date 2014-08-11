package com.jcloisterzone.ui.grid;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Image;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.event.MeepleEvent;
import com.jcloisterzone.event.TileEvent;
import com.jcloisterzone.feature.Castle;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.figure.SmallFollower;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.Snapshot;
import com.jcloisterzone.game.capability.BridgeCapability;
import com.jcloisterzone.game.capability.CastleCapability;
import com.jcloisterzone.game.capability.DragonCapability;
import com.jcloisterzone.game.capability.FairyCapability;
import com.jcloisterzone.game.capability.PlagueCapability;
import com.jcloisterzone.game.capability.TowerCapability;
import com.jcloisterzone.ui.Client;
import com.jcloisterzone.ui.ImmutablePoint;
import com.jcloisterzone.ui.animation.AnimationService;
import com.jcloisterzone.ui.animation.FlierDiceRollAnimation;
import com.jcloisterzone.ui.animation.ScoreAnimation;
import com.jcloisterzone.ui.controls.ControlPanel;
import com.jcloisterzone.ui.grid.layer.AbstractTilePlacementLayer;
import com.jcloisterzone.ui.grid.layer.AnimationLayer;
import com.jcloisterzone.ui.grid.layer.BridgeLayer;
import com.jcloisterzone.ui.grid.layer.CastleLayer;
import com.jcloisterzone.ui.grid.layer.DragonAvailableMove;
import com.jcloisterzone.ui.grid.layer.DragonLayer;
import com.jcloisterzone.ui.grid.layer.FairyLayer;
import com.jcloisterzone.ui.grid.layer.FarmHintsLayer;
import com.jcloisterzone.ui.grid.layer.MeepleLayer;
import com.jcloisterzone.ui.grid.layer.PlagueLayer;
import com.jcloisterzone.ui.grid.layer.TileLayer;
import com.jcloisterzone.ui.grid.layer.TowerLayer;
import com.jcloisterzone.ui.panel.BackgroundPanel;


@SuppressWarnings("serial")
public class MainPanel extends BackgroundPanel {

    private final Client client;
    private final Game game;
    private AnimationService animationService;

    private GridPanel gridPanel;
    private ControlPanel controlPanel;

    private TileLayer tileLayer;
    private MeepleLayer meepleLayer;
    private TowerLayer towerLayer;
    private DragonLayer dragonLayer;
    private FairyLayer fairyLayer;
    private BridgeLayer bridgeLayer;
    private CastleLayer castleLayer;
    private PlagueLayer plagueLayer;
    private FarmHintsLayer farmHintLayer;

    public MainPanel(Client client, Game game) {
        this.client = client;
        this.game = game;
        animationService = new AnimationService();
        animationService.start();

        setLayout(new BorderLayout());
    }

    public GridPanel getGridPanel() {
        return gridPanel;
    }

    public ControlPanel getControlPanel() {
        return controlPanel;
    }

    public AnimationService getAnimationService() {
        return animationService;
    }

    public void setShowFarmHints(boolean showFarmHints) {
         farmHintLayer.setVisible(showFarmHints);
    }

    public void started(Snapshot snapshot) {
        animationService.clearAll();
        animationService.setGridPanel(null);
        removeAll();
        setVisible(false);

        controlPanel = new ControlPanel(client, game);
        gridPanel = new GridPanel(client, controlPanel, snapshot);
        meepleLayer = new MeepleLayer(gridPanel);
        tileLayer = new TileLayer(gridPanel);
        farmHintLayer = new FarmHintsLayer(gridPanel);
        gridPanel.addLayer(tileLayer);
        gridPanel.addLayer(meepleLayer);
        gridPanel.addLayer(farmHintLayer);

        gridPanel.addLayer(new AnimationLayer(gridPanel, animationService));

        animationService.setGridPanel(gridPanel);

        for (Class<? extends Capability> capClass : game.getCapabilityClasses()) {
            if (capClass.equals(TowerCapability.class)) {
                towerLayer = new TowerLayer(gridPanel);
                gridPanel.addLayer(towerLayer);
            }
            if (capClass.equals(DragonCapability.class)) {
                dragonLayer = new DragonLayer(gridPanel, null);
                gridPanel.addLayer(dragonLayer);
            }
            if (capClass.equals(FairyCapability.class)) {
                fairyLayer = new FairyLayer(gridPanel, null);
                gridPanel.addLayer(fairyLayer);
            }
            if (capClass.equals(BridgeCapability.class)) {
                bridgeLayer = new BridgeLayer(gridPanel);
                gridPanel.addLayer(bridgeLayer);
            }
            if (capClass.equals(CastleCapability.class)) {
                castleLayer = new CastleLayer(gridPanel);
                gridPanel.addLayer(castleLayer);
            }
            if (capClass.equals(PlagueCapability.class)) {
                plagueLayer = new PlagueLayer(gridPanel);
                gridPanel.addLayer(plagueLayer);
            }
        }
        add(gridPanel);
        setVisible(true);
    }

    public void closeGame() {
        gridPanel.clearActionDecorations();
        gridPanel.removeLayer(AbstractTilePlacementLayer.class);
        gridPanel.setSecondPanel(null);
    }

    public void tileEvent(TileEvent ev) {
        gridPanel.tileEvent(ev, tileLayer);
        if (farmHintLayer != null) {
            farmHintLayer.tileEvent(ev);
        }
    }


    public void meepleEvent(MeepleEvent ev) {
        gridPanel.clearActionDecorations();
        if (ev.getFrom() != null) {
            meepleLayer.meepleUndeployed(ev);
        }
        if (ev.getTo() != null) {
            meepleLayer.meepleDeployed(ev);
        }
        farmHintLayer.meepleEvent(ev);

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
        Integer duration = client.getConfig().getScore_display_duration();
        return duration == null ? 10 : Math.max(duration, 1);
    }

    public void scored(Feature scoreable, String points, Meeple m, boolean finalScoring) {
        Tile tile = scoreable.getTile();
        Position pos = tile.getPosition();
        ImmutablePoint offset = client.getResourceManager().getMeeplePlacement(tile, m.getClass(), scoreable.getLocation());
        animationService.registerAnimation(new ScoreAnimation(
            pos,
            points,
            offset,
            m.getPlayer().getColors().getMeepleColor(),
            finalScoring ? null : getScoreAnimationDuration()
        ));
    }

    public void scored(Position pos, Player player, String points, boolean finalScoring) {
        animationService.registerAnimation(new ScoreAnimation(
            pos,
            points,
            new ImmutablePoint(50, 50),
            player.getColors().getMeepleColor(),
            finalScoring ? null : getScoreAnimationDuration()
        ));

    }

    public void flierRoll(Position pos, int distance) {
        animationService.registerAnimation(new FlierDiceRollAnimation(pos, distance));
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
            c = player.getColors().getMeepleColor();
        }
        Image tunnelPiece = client.getFigureTheme().getTunnelImage(c);
        Tile tile = gridPanel.getTile(p);
        ImmutablePoint offset = client.getResourceManager().getMeeplePlacement(tile, SmallFollower.class, loc);
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
