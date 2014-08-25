package com.jcloisterzone.ui.grid;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Image;

import javax.swing.JPanel;

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
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.Snapshot;
import com.jcloisterzone.game.capability.AbbeyCapability;
import com.jcloisterzone.game.capability.BarnCapability;
import com.jcloisterzone.game.capability.BridgeCapability;
import com.jcloisterzone.game.capability.CastleCapability;
import com.jcloisterzone.game.capability.DragonCapability;
import com.jcloisterzone.game.capability.FairyCapability;
import com.jcloisterzone.game.capability.LittleBuildingsCapability;
import com.jcloisterzone.game.capability.PlagueCapability;
import com.jcloisterzone.game.capability.TowerCapability;
import com.jcloisterzone.ui.Client;
import com.jcloisterzone.ui.ImmutablePoint;
import com.jcloisterzone.ui.animation.AnimationService;
import com.jcloisterzone.ui.animation.FlierDiceRollAnimation;
import com.jcloisterzone.ui.animation.ScoreAnimation;
import com.jcloisterzone.ui.controls.ChatPanel;
import com.jcloisterzone.ui.controls.ControlPanel;
import com.jcloisterzone.ui.grid.layer.AbbeyPlacementLayer;
import com.jcloisterzone.ui.grid.layer.AbstractTilePlacementLayer;
import com.jcloisterzone.ui.grid.layer.AnimationLayer;
import com.jcloisterzone.ui.grid.layer.BarnAreaLayer;
import com.jcloisterzone.ui.grid.layer.BridgeLayer;
import com.jcloisterzone.ui.grid.layer.CastleLayer;
import com.jcloisterzone.ui.grid.layer.DragonAvailableMove;
import com.jcloisterzone.ui.grid.layer.DragonLayer;
import com.jcloisterzone.ui.grid.layer.FairyLayer;
import com.jcloisterzone.ui.grid.layer.FarmHintsLayer;
import com.jcloisterzone.ui.grid.layer.FeatureAreaLayer;
import com.jcloisterzone.ui.grid.layer.FollowerAreaLayer;
import com.jcloisterzone.ui.grid.layer.LittleBuildingActionLayer;
import com.jcloisterzone.ui.grid.layer.MeepleLayer;
import com.jcloisterzone.ui.grid.layer.PlacementHistory;
import com.jcloisterzone.ui.grid.layer.PlagueLayer;
import com.jcloisterzone.ui.grid.layer.TileActionLayer;
import com.jcloisterzone.ui.grid.layer.TileLayer;
import com.jcloisterzone.ui.grid.layer.TilePlacementLayer;
import com.jcloisterzone.ui.grid.layer.TowerLayer;


@SuppressWarnings("serial")
public class MainPanel extends JPanel {

    private final Client client;
    private final Game game;
    private AnimationService animationService;

    private GridPanel gridPanel;
    private ControlPanel controlPanel;
    private ChatPanel chatPanel;

    private TileLayer tileLayer;
    private MeepleLayer meepleLayer;
    private TowerLayer towerLayer;
    private DragonLayer dragonLayer;
    private FairyLayer fairyLayer;
    private BridgeLayer bridgeLayer;
    private CastleLayer castleLayer;
    private PlagueLayer plagueLayer;
    private FarmHintsLayer farmHintLayer;
    private PlacementHistory placementHistoryLayer;

    public MainPanel(Client client, Game game, ChatPanel chatPanel) {
        this.client = client;
        this.game = game;
        this.chatPanel = chatPanel;
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
        gridPanel = new GridPanel(client, controlPanel, chatPanel, snapshot);
        meepleLayer = new MeepleLayer(gridPanel);
        tileLayer = new TileLayer(gridPanel);
        farmHintLayer = new FarmHintsLayer(gridPanel);
        gridPanel.addLayer(tileLayer);  //zindex 2
        if (game.hasCapability(TowerCapability.class)) {
            towerLayer = new TowerLayer(gridPanel);
            gridPanel.addLayer(towerLayer); //5
        }

        gridPanel.addLayer(farmHintLayer); //zindex 10

        if (game.hasCapability(CastleCapability.class)) {
            castleLayer = new CastleLayer(gridPanel);
            gridPanel.addLayer(castleLayer); //45
        }
        if (game.hasCapability(PlagueCapability.class)) {
            plagueLayer = new PlagueLayer(gridPanel);
            gridPanel.addLayer(plagueLayer); //45
        }

        gridPanel.addLayer(meepleLayer); //zindex 50
        if (game.hasCapability(BridgeCapability.class)) {
            bridgeLayer = new BridgeLayer(gridPanel);
            bridgeLayer.setMeepleLayer(meepleLayer);
            gridPanel.addLayer(bridgeLayer);
        }

        gridPanel.addLayer(new FollowerAreaLayer(gridPanel), false); //70



        if (game.hasCapability(DragonCapability.class)) {
            gridPanel.addLayer(new DragonAvailableMove(gridPanel), false);
            dragonLayer = new DragonLayer(gridPanel);
            gridPanel.addLayer(dragonLayer); //90
        }
        if (game.hasCapability(FairyCapability.class)) {
            fairyLayer = new FairyLayer(gridPanel);
            gridPanel.addLayer(fairyLayer); //90
        }

        if (game.hasCapability(BarnCapability.class)) {
            gridPanel.addLayer(new BarnAreaLayer(gridPanel), false);
        }

        gridPanel.addLayer(new TilePlacementLayer(gridPanel), false);
        gridPanel.addLayer(new FeatureAreaLayer(gridPanel), false);
        if (game.hasCapability(TowerCapability.class) || game.hasCapability(FairyCapability.class)) {
            gridPanel.addLayer(new TileActionLayer(gridPanel), false);
        }
        if (game.hasCapability(AbbeyCapability.class)) {
            gridPanel.addLayer(new AbbeyPlacementLayer(gridPanel), false);
        }
        if (game.hasCapability(LittleBuildingsCapability.class)) {
            gridPanel.addLayer(new LittleBuildingActionLayer(gridPanel), false); //100
        }


        //abstractare - zindex 100
        //tile placement 3

        gridPanel.addLayer(new AnimationLayer(gridPanel, animationService)); //zindex 800
        animationService.setGridPanel(gridPanel);

        placementHistoryLayer = new PlacementHistory(gridPanel, game.getBoard().getAllTiles());
        gridPanel.addLayer(placementHistoryLayer, false);

        add(gridPanel);
        setVisible(true);
    }

    public void showRecentHistory() {
        placementHistoryLayer.update();
        gridPanel.showLayer(placementHistoryLayer);
    }

    public void hideRecentHistory() {
        gridPanel.hideLayer(placementHistoryLayer);
    }


    public void closeGame() {
        gridPanel.clearActionDecorations();
        gridPanel.hideLayer(AbstractTilePlacementLayer.class);
        gridPanel.setSecondPanel(null);
    }

    public void tileEvent(TileEvent ev) {
        placementHistoryLayer.update();
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
        gridPanel.hideLayer(DragonAvailableMove.class);
        gridPanel.repaint();
    }

    public void fairyMoved(Position p) {
        fairyLayer.setPosition(p);
    }

}
