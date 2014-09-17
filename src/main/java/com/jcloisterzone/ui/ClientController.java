package com.jcloisterzone.ui;

import java.awt.Color;
import java.awt.Image;
import java.util.List;
import java.util.Set;

import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;
import com.jcloisterzone.Player;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.event.BazaarAuctionEndEvent;
import com.jcloisterzone.event.BazaarMakeBidEvent;
import com.jcloisterzone.event.BazaarSelectBuyOrSellEvent;
import com.jcloisterzone.event.BazaarSelectTileEvent;
import com.jcloisterzone.event.BazaarTileSelectedEvent;
import com.jcloisterzone.event.BridgeDeployedEvent;
import com.jcloisterzone.event.CastleDeployedEvent;
import com.jcloisterzone.event.ChatEvent;
import com.jcloisterzone.event.ClientListChangedEvent;
import com.jcloisterzone.event.CornCircleSelectOptionEvent;
import com.jcloisterzone.event.FlierRollEvent;
import com.jcloisterzone.event.GameStateChangeEvent;
import com.jcloisterzone.event.MeepleEvent;
import com.jcloisterzone.event.MeeplePrisonEvent;
import com.jcloisterzone.event.NeutralFigureMoveEvent;
import com.jcloisterzone.event.PlayerTurnEvent;
import com.jcloisterzone.event.ScoreEvent;
import com.jcloisterzone.event.SelectActionEvent;
import com.jcloisterzone.event.SelectDragonMoveEvent;
import com.jcloisterzone.event.TileEvent;
import com.jcloisterzone.event.TowerIncreasedEvent;
import com.jcloisterzone.event.TunnelPiecePlacedEvent;
import com.jcloisterzone.event.setup.ExpansionChangedEvent;
import com.jcloisterzone.event.setup.PlayerSlotChangeEvent;
import com.jcloisterzone.event.setup.RuleChangeEvent;
import com.jcloisterzone.event.setup.SupportedExpansionsChangeEvent;
import com.jcloisterzone.figure.SmallFollower;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.PlayerSlot;
import com.jcloisterzone.game.capability.BazaarItem;
import com.jcloisterzone.ui.controls.ChatPanel;
import com.jcloisterzone.ui.controls.FakeComponent;
import com.jcloisterzone.ui.dialog.DiscardedTilesDialog;
import com.jcloisterzone.ui.dialog.GameOverDialog;
import com.jcloisterzone.ui.grid.BazaarPanel;
import com.jcloisterzone.ui.grid.BazaarPanel.BazaarPanelState;
import com.jcloisterzone.ui.grid.CornCirclesPanel;
import com.jcloisterzone.ui.grid.GridPanel;
import com.jcloisterzone.ui.grid.layer.DragonAvailableMove;
import com.jcloisterzone.ui.grid.layer.DragonLayer;
import com.jcloisterzone.ui.panel.GamePanel;

import static com.jcloisterzone.ui.I18nUtils._;

public class ClientController  {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private final Client client;
    private final Game game;
    private final GamePanel gamePanel;


    public ClientController(Client client, Game game, GamePanel gamePanel) {
        this.client = client;
        this.game = game;
        this.gamePanel = gamePanel;
    }

    @Subscribe
    public void updateCustomRule(RuleChangeEvent ev) {
        gamePanel.getCreateGamePanel().updateCustomRule(ev.getRule(), ev.isEnabled());
    }

    @Subscribe
    public void updateExpansion(ExpansionChangedEvent ev) {
        gamePanel.getCreateGamePanel().updateExpansion(ev.getExpansion(), ev.isEnabled());
    }

    @Subscribe
    public void updateSlot(PlayerSlotChangeEvent ev) {
        PlayerSlot slot = ev.getSlot();
        if (gamePanel.getCreateGamePanel() != null) {
            gamePanel.getCreateGamePanel().updateSlot(slot.getNumber());
        } else {
            throw new IllegalStateException();
        }
    }

    @Subscribe
    public void updateConnectedClients(ClientListChangedEvent ev) {
        gamePanel.clientListChanged(ev.getClients());
    }

    @Subscribe
    public void updateSupportedExpansions(SupportedExpansionsChangeEvent ev) {
        gamePanel.getCreateGamePanel().updateSupportedExpansions(ev.getExpansions());
    }

    @Subscribe
    public void gameStateChange(GameStateChangeEvent ev) {
        switch (ev.getType()) {
        case GameStateChangeEvent.GAME_START:
            started(ev);
            break;
        case GameStateChangeEvent.GAME_OVER:
            client.closeGame(true);
            new GameOverDialog(client);
            break;
        }
    }

    private void started(GameStateChangeEvent ev) {
        gamePanel.started(ev);
        MenuBar menu = client.getJMenuBar();
        menu.setZoomInEnabled(true);
        menu.setZoomOutEnabled(true);
        menu.setIsGameRunning(true);
    }

    @Subscribe
    public void turnChanged(PlayerTurnEvent ev) {
        gamePanel.getGridPanel().repaint();

        if (ev.getTargetPlayer().isLocalHuman()) {
            client.beep();
        }

        // TODO better image quality ?
        Color c = ev.getTargetPlayer().getColors().getMeepleColor();
        Image image = client.getFigureTheme().getFigureImage(SmallFollower.class, c, null);
        client.setIconImage(image);
    }

    public void refreshWindowTitle() {
        StringBuilder title = new StringBuilder(Client.BASE_TITLE);

        Player activePlayer = game.getActivePlayer();
        if (activePlayer != null) {
            title.append(" ⋅ ").append(activePlayer.getNick());
        }
        int packSize = game.getTilePack().totalSize();
        title.append(" ⋅ ").append(String.format(_("%d tiles left"), packSize));

        client.setTitle(title.toString());
    }

    @Subscribe
    public void tileEvent(TileEvent ev) {
        switch (ev.getType()) {
        case TileEvent.DRAW:
            client.clearActions();
            refreshWindowTitle();
            break;
        case TileEvent.DISCARD:
            DiscardedTilesDialog discardedTilesDialog = client.getDiscardedTilesDialog();
            if (discardedTilesDialog == null) {
                discardedTilesDialog = new DiscardedTilesDialog(client);
                client.setDiscardedTilesDialog(discardedTilesDialog);
                client.getJMenuBar().setShowDiscardedEnabled(true);
            }
            discardedTilesDialog.addTile(ev.getTile());
            discardedTilesDialog.setVisible(true);
            break;
        case TileEvent.PLACEMENT:
        case TileEvent.REMOVE:
            gamePanel.getMainPanel().tileEvent(ev);
            break;
        }
    }

    @Subscribe
    public void dragonMoved(NeutralFigureMoveEvent ev) {
        switch (ev.getType()) {
        case NeutralFigureMoveEvent.DRAGON:
            gamePanel.getMainPanel().dragonMoved(ev.getTo());
            break;
        case NeutralFigureMoveEvent.FAIRY:
            gamePanel.getMainPanel().fairyMoved(ev.getTo());
            break;
        }
    }

    @Subscribe
    public void tunnelPiecePlaced(TunnelPiecePlacedEvent ev) {
        gamePanel.getMainPanel().tunnelPiecePlaced(ev.getTriggeringPlayer(), ev.getPosition(), ev.getLocation(), ev.isSecondPiece());
    }


    @Subscribe
    public void flierRoll(FlierRollEvent ev) {
        gamePanel.getMainPanel().flierRoll(ev.getPosition(), ev.getDistance());
    }

    @Subscribe
    public void towerIncreased(TowerIncreasedEvent ev) {
        client.clearActions();
        gamePanel.getMainPanel().towerIncreased(ev.getPosition(), ev.getCaptureRange());
    }

    // ------------------ Meeple events -----------

    @Subscribe
    public void meepleEvent(MeepleEvent ev) {
        gamePanel.getMainPanel().meepleEvent(ev);
    }

    @Subscribe
    public void meeplePrisonEvent(MeeplePrisonEvent ev) {
        gamePanel.getGridPanel().repaint();
    }


    @Subscribe
    public void bridgeDeployed(BridgeDeployedEvent ev) {
        gamePanel.getMainPanel().bridgeDeployed(ev.getPosition(), ev.getLocation());
    }

    @Subscribe
    public void castleDeployed(CastleDeployedEvent ev) {
        gamePanel.getMainPanel().castleDeployed(ev.getPart1(), ev.getPart2());
    }

    // ------------------ Feature events ----------


    @Subscribe
    public void scored(ScoreEvent ev) {
        if (ev.getFeature() == null) {
            gamePanel.getMainPanel().scored(ev.getPosition(), ev.getTargetPlayer(), ev.getLabel(), ev.isFinal());
        } else {
            gamePanel.getMainPanel().scored(ev.getFeature(), ev.getTargetPlayer(), ev.getLabel(), ev.getMeepleType(), ev.isFinal());
        }
        gamePanel.getMainPanel().repaint(); // players only
    }



    // User interface

    //@Override
    public void showWarning(String title, String message) {
        JOptionPane.showMessageDialog(client, message, title, JOptionPane.WARNING_MESSAGE);
    }

    @Subscribe
    public void selectDragonMove(SelectDragonMoveEvent ev) {
        Set<Position> positions = ev.getPositions();
        int movesLeft = ev.getMovesLeft();
        client.clearActions();
        client.getControlPanel().getActionPanel().setFakeAction("dragonmove");
        DragonLayer dragonDecoration = gamePanel.getGridPanel().findLayer(DragonLayer.class);
        dragonDecoration.setMoves(movesLeft);
        gamePanel.getGridPanel().repaint();
        logger.debug("UI selectdragon move, left {}, {}", movesLeft, positions);
        if (ev.getTargetPlayer().isLocalHuman()) {
            DragonAvailableMove availMoves = gamePanel.getGridPanel().findLayer(DragonAvailableMove.class);
            availMoves.setPositions(positions);
            gamePanel.getGridPanel().showLayer(availMoves);
            client.beep();
        }
    }

    @Subscribe
    public void selectAction(SelectActionEvent ev) {
        client.clearActions();
        gamePanel.getControlPanel().selectAction(ev.getTargetPlayer(), ev.getActions(), ev.isPassAllowed());
        gamePanel.getGridPanel().repaint();
        //TODO generic solution
        if (game.isUndoAllowed() && ev.getTargetPlayer().isLocalHuman()) {
            client.getJMenuBar().getUndo().setEnabled(true);
        }
    }

    @Subscribe
    public void selectCornCircleOption(CornCircleSelectOptionEvent ev) {
        client.clearActions();
        createSecondPanel(CornCirclesPanel.class);
        gamePanel.getGridPanel().repaint();
    }

    @SuppressWarnings("unchecked")
    public <T extends FakeComponent> T createSecondPanel(Class<T> type) {
        GridPanel grid = gamePanel.getGridPanel();
        FakeComponent panel = grid.getSecondPanel();
        if (type.isInstance(panel)) {
            return (T) panel;
        }
        T newPanel;
        try {
            newPanel = type.getConstructor(Client.class).newInstance(client);
        } catch (Exception e) {
            // should never happen;
            e.printStackTrace();
            return null;
        }
        newPanel.registerSwingComponents(grid);
        newPanel.layoutSwingComponents(grid);
        grid.setSecondPanel(newPanel);
        return newPanel;
    }

    @Subscribe
    public void selectBazaarTile(BazaarSelectTileEvent ev) {
        client.clearActions();
        BazaarPanel bazaarPanel = createSecondPanel(BazaarPanel.class);
        if (ev.getTargetPlayer().isLocalHuman()) {
            List<BazaarItem> supply = ev.getBazaarSupply();
            for (int i = 0; i < supply.size(); i++) {
                // find first allowed item
                if (supply.get(i).getOwner() == null) {
                    bazaarPanel.setSelectedItem(i);
                    break;
                }
            }
            bazaarPanel.setState(BazaarPanelState.SELECT_TILE);
        } else {
            bazaarPanel.setState(BazaarPanelState.INACTIVE);
        }
        gamePanel.getGridPanel().repaint();
    }

    @Subscribe
    public void bazaarTileSelected(BazaarTileSelectedEvent ev) {
        BazaarPanel bazaarPanel = createSecondPanel(BazaarPanel.class);
        bazaarPanel.setState(BazaarPanelState.INACTIVE);
        gamePanel.getGridPanel().repaint();
    }

    @Subscribe
    public void makeBazaarBid(BazaarMakeBidEvent ev) {
        BazaarPanel bazaarPanel = createSecondPanel(BazaarPanel.class);
        bazaarPanel.setSelectedItem(ev.getSupplyIndex());
        if (ev.getTargetPlayer().isLocalHuman()) {
            bazaarPanel.setState(BazaarPanelState.MAKE_BID);
        } else {
            bazaarPanel.setState(BazaarPanelState.INACTIVE);
        }
        client.clearActions();
        gamePanel.getGridPanel().repaint();
    }

    @Subscribe
    public void selectBuyOrSellBazaarOffer(BazaarSelectBuyOrSellEvent ev) {
        BazaarPanel bazaarPanel = createSecondPanel(BazaarPanel.class);
        bazaarPanel.setSelectedItem(ev.getSupplyIndex());
        if (ev.getTargetPlayer().isLocalHuman()) {
            bazaarPanel.setState(BazaarPanelState.BUY_OR_SELL);
        } else {
            bazaarPanel.setState(BazaarPanelState.INACTIVE);
        }
    }

    @Subscribe
    public void bazaarAuctionsEnded(BazaarAuctionEndEvent ev) {
        gamePanel.getGridPanel().setSecondPanel(null);
    }

    @Subscribe
    public void chatMessageReceived(ChatEvent ev) {
        ChatPanel chatPanel = gamePanel.getChatPanel();
        if (chatPanel != null) {
            chatPanel.displayChatMessage(ev);
        }
    }
}
