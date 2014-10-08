package com.jcloisterzone.ui.grid;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Image;

import javax.swing.JPanel;

import com.google.common.eventbus.Subscribe;
import com.jcloisterzone.Player;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.event.BridgeDeployedEvent;
import com.jcloisterzone.event.CastleDeployedEvent;
import com.jcloisterzone.event.FlierRollEvent;
import com.jcloisterzone.event.MeepleEvent;
import com.jcloisterzone.event.NeutralFigureMoveEvent;
import com.jcloisterzone.event.ScoreEvent;
import com.jcloisterzone.event.TileEvent;
import com.jcloisterzone.event.TowerIncreasedEvent;
import com.jcloisterzone.event.TunnelPiecePlacedEvent;
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
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.ui.ImmutablePoint;
import com.jcloisterzone.ui.animation.AnimationService;
import com.jcloisterzone.ui.animation.FlierDiceRollAnimation;
import com.jcloisterzone.ui.animation.ScoreAnimation;
import com.jcloisterzone.ui.controls.ControlPanel;
import com.jcloisterzone.ui.controls.chat.ChatPanel;
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
    private final GameController gc;
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

    public MainPanel(Client client, GameController gc, ChatPanel chatPanel) {
        this.client = client;
        this.gc = gc;
        this.game = gc.getGame();
        this.chatPanel = chatPanel;
        gc.register(this);

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

        controlPanel = new ControlPanel(client, gc);
        gridPanel = new GridPanel(client, gc, controlPanel, chatPanel, snapshot);
        meepleLayer = new MeepleLayer(gridPanel, gc);
        tileLayer = new TileLayer(gridPanel, gc);
        farmHintLayer = new FarmHintsLayer(gridPanel, gc);
        gridPanel.addLayer(tileLayer);  //zindex 2
        if (game.hasCapability(TowerCapability.class)) {
            towerLayer = new TowerLayer(gridPanel, gc);
            gridPanel.addLayer(towerLayer); //5
        }

        gridPanel.addLayer(farmHintLayer); //zindex 10

        if (game.hasCapability(CastleCapability.class)) {
            castleLayer = new CastleLayer(gridPanel, gc);
            gridPanel.addLayer(castleLayer); //45
        }
        if (game.hasCapability(PlagueCapability.class)) {
            plagueLayer = new PlagueLayer(gridPanel, gc);
            gridPanel.addLayer(plagueLayer); //45
        }

        gridPanel.addLayer(meepleLayer); //zindex 50
        if (game.hasCapability(BridgeCapability.class)) {
            bridgeLayer = new BridgeLayer(gridPanel, gc);
            bridgeLayer.setMeepleLayer(meepleLayer);
            gridPanel.addLayer(bridgeLayer);
        }

        gridPanel.addLayer(new FollowerAreaLayer(gridPanel, gc), false); //70

        if (game.hasCapability(DragonCapability.class)) {
            gridPanel.addLayer(new DragonAvailableMove(gridPanel, gc), false);
            dragonLayer = new DragonLayer(gridPanel, gc);
            gridPanel.addLayer(dragonLayer); //90
        }
        if (game.hasCapability(FairyCapability.class)) {
            fairyLayer = new FairyLayer(gridPanel, gc);
            gridPanel.addLayer(fairyLayer); //90
        }

        if (game.hasCapability(BarnCapability.class)) {
            gridPanel.addLayer(new BarnAreaLayer(gridPanel, gc), false);
        }

        gridPanel.addLayer(new TilePlacementLayer(gridPanel, gc), false);
        gridPanel.addLayer(new FeatureAreaLayer(gridPanel, gc), false);
        if (game.hasCapability(TowerCapability.class) || game.hasCapability(FairyCapability.class)) {
            gridPanel.addLayer(new TileActionLayer(gridPanel, gc), false);
        }
        if (game.hasCapability(AbbeyCapability.class)) {
            gridPanel.addLayer(new AbbeyPlacementLayer(gridPanel, gc), false);
        }
        if (game.hasCapability(LittleBuildingsCapability.class)) {
            gridPanel.addLayer(new LittleBuildingActionLayer(gridPanel, gc), false); //100
        }

        //abstractare - zindex 100
        //tile placement 3

        gridPanel.addLayer(new AnimationLayer(gridPanel, gc, animationService)); //zindex 800
        animationService.setGridPanel(gridPanel);

        placementHistoryLayer = new PlacementHistory(gridPanel, gc, game.getBoard().getAllTiles());
        gridPanel.addLayer(placementHistoryLayer, false);

        add(gridPanel);
        setVisible(true);
    }

    public void toggleRecentHistory(boolean show) {
        if (show) {
            placementHistoryLayer.update();
            gridPanel.showLayer(placementHistoryLayer);
        } else {
            gridPanel.hideLayer(placementHistoryLayer);
        }
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

    @Subscribe
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

    @Subscribe
    public void bridgeDeployed(BridgeDeployedEvent ev) {
        gridPanel.clearActionDecorations();
        bridgeLayer.bridgeDeployed(ev.getPosition(), ev.getLocation());
    }

    @Subscribe
    public void castleDeployed(CastleDeployedEvent ev) {
        gridPanel.clearActionDecorations();
        castleLayer.castleDeployed(ev.getPart1(), ev.getPart2());
    }


    private Integer getScoreAnimationDuration() {
        Integer duration = client.getConfig().getScore_display_duration();
        return duration == null ? 10 : Math.max(duration, 1);
    }

    public void scored(Feature scoreable, Player player, String points, Class<? extends Meeple> meepleType, boolean finalScoring) {
        Tile tile = scoreable.getTile();
        Position pos = tile.getPosition();
        ImmutablePoint offset = client.getResourceManager().getMeeplePlacement(tile, meepleType, scoreable.getLocation());
        animationService.registerAnimation(new ScoreAnimation(
            pos,
            points,
            offset,
            player.getColors().getMeepleColor(),
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

    @Subscribe
    public void scored(ScoreEvent ev) {
        if (ev.getFeature() == null) {
            scored(ev.getPosition(), ev.getTargetPlayer(), ev.getLabel(), ev.isFinal());
        } else {
            scored(ev.getFeature(), ev.getTargetPlayer(), ev.getLabel(), ev.getMeepleType(), ev.isFinal());
        }
        repaint();
    }

    @Subscribe
    public void dragonMoved(NeutralFigureMoveEvent ev) {
        switch (ev.getType()) {
        case NeutralFigureMoveEvent.DRAGON:
            dragonLayer.setPosition(ev.getTo());
            dragonLayer.setMoves(0);
            gridPanel.hideLayer(DragonAvailableMove.class);
            gridPanel.repaint();
            break;
        case NeutralFigureMoveEvent.FAIRY:
            fairyLayer.setPosition(ev.getTo());
            break;
        }
    }

    @Subscribe
    public void tunnelPiecePlaced(TunnelPiecePlacedEvent ev) {
        Player player = ev.getTriggeringPlayer();
        Color c;
        if (ev.isSecondPiece()) {
            c = client.getPlayerSecondTunelColor(player);
        } else {
            c = player.getColors().getMeepleColor();
        }
        Image tunnelPiece = client.getFigureTheme().getTunnelImage(c);
        Tile tile = gridPanel.getTile(ev.getPosition());
        ImmutablePoint offset = client.getResourceManager().getMeeplePlacement(tile, SmallFollower.class, ev.getLocation());
        meepleLayer.addPermanentImage(ev.getPosition(), offset, tunnelPiece);
    }

    @Subscribe
    public void flierRoll(FlierRollEvent ev) {
        animationService.registerAnimation(new FlierDiceRollAnimation(ev.getPosition(), ev.getDistance()));
    }

    @Subscribe
    public void towerIncreased(TowerIncreasedEvent ev) {
        towerLayer.setTowerHeight(ev.getPosition(), ev.getCaptureRange());
        gridPanel.repaint();
    }
}
