package com.jcloisterzone.ui;

import java.awt.Color;
import java.awt.Image;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Set;

import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;
import com.jcloisterzone.Player;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.bugreport.ReportingTool;
import com.jcloisterzone.event.BazaarAuctionEndEvent;
import com.jcloisterzone.event.BazaarMakeBidEvent;
import com.jcloisterzone.event.BazaarSelectBuyOrSellEvent;
import com.jcloisterzone.event.BazaarSelectTileEvent;
import com.jcloisterzone.event.BazaarTileSelectedEvent;
import com.jcloisterzone.event.CornCircleSelectOptionEvent;
import com.jcloisterzone.event.GameStateChangeEvent;
import com.jcloisterzone.event.MageWitchSelectRemoval;
import com.jcloisterzone.event.MeeplePrisonEvent;
import com.jcloisterzone.event.PlayerTurnEvent;
import com.jcloisterzone.event.SelectActionEvent;
import com.jcloisterzone.event.SelectDragonMoveEvent;
import com.jcloisterzone.event.TileEvent;
import com.jcloisterzone.event.TowerIncreasedEvent;
import com.jcloisterzone.figure.SmallFollower;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.capability.BazaarItem;
import com.jcloisterzone.game.phase.Phase;
import com.jcloisterzone.ui.MenuBar.MenuItem;
import com.jcloisterzone.ui.controls.ActionPanel;
import com.jcloisterzone.ui.controls.ControlPanel;
import com.jcloisterzone.ui.dialog.DiscardedTilesDialog;
import com.jcloisterzone.ui.grid.BazaarPanel;
import com.jcloisterzone.ui.grid.BazaarPanel.BazaarPanelState;
import com.jcloisterzone.ui.grid.CornCirclesPanel;
import com.jcloisterzone.ui.grid.GridPanel;
import com.jcloisterzone.ui.grid.SelectMageWitchRemovalPanel;
import com.jcloisterzone.ui.grid.layer.DragonAvailableMove;
import com.jcloisterzone.ui.grid.layer.DragonLayer;
import com.jcloisterzone.ui.panel.GameOverPanel;
import com.jcloisterzone.ui.view.ChannelView;
import com.jcloisterzone.ui.view.GameView;
import com.jcloisterzone.ui.view.StartView;
import com.jcloisterzone.wsio.RmiProxy;
import com.jcloisterzone.wsio.message.LeaveGameMessage;
import com.jcloisterzone.wsio.message.RmiMessage;

import static com.jcloisterzone.ui.I18nUtils._;

public class GameController extends EventProxyUiController<Game> implements InvocationHandler {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private final Game game;
    private String channel;

    private final RmiProxy rmiProxy;
    private ReportingTool reportingTool;

    private GameView gameView;

    public GameController(Client client, Game game) {
        super(client, game);
        this.game = game;
        rmiProxy = (RmiProxy) Proxy.newProxyInstance(RmiProxy.class.getClassLoader(), new Class[] { RmiProxy.class }, this);
        getInvokeInSwingUiAdapter().setReportingTool(reportingTool);
    }

    public Game getGame() {
        return game;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (getConnection() == null) {
            logger.info("Not connected. Message ignored");
        } else {
            RmiMessage rmi = new RmiMessage(game.getGameId(), method.getName(), args);
            getConnection().send(rmi);
        }
        return null;
    }

    void phaseLoop() {
        Phase phase = game.getPhase();
        while (phase != null && !phase.isEntered()) {
            logger.debug("Entering phase {}",  phase.getClass().getSimpleName());
            phase.setEntered(true);
            phase.enter();
            phase = game.getPhase();
            game.flushEventQueue();
        }
        game.flushEventQueue();
    }

    void clearActions() {
        ControlPanel controlPanel = gameView.getControlPanel();
        ActionPanel ap = controlPanel.getActionPanel();
        if (ap.getActions() != null) {
            controlPanel.clearActions();
        }
        ap.setFakeAction(null);
        client.getJMenuBar().setItemEnabled(MenuItem.UNDO, false);
    }


    @Subscribe
    public void gameStateChange(GameStateChangeEvent ev) {
        if (ev.getType() == GameStateChangeEvent.GAME_OVER) {
            boolean showPlayAgain = client.getLocalServer() != null;
            gameView.setGameRunning(false);
            if (gameView.getChatPanel() != null) { //TODO allow chat after game is over
                gameView.getGridPanel().remove(gameView.getChatPanel());
            }
            client.closeGame(true);
            GameOverPanel panel = new GameOverPanel(client, this, showPlayAgain);
            gameView.getGridPanel().add(panel, "pos 0 0");
            gameView.getGridPanel().revalidate();

        }
    }


    @Subscribe
    public void turnChanged(PlayerTurnEvent ev) {
        gameView.getGridPanel().repaint();

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
            clearActions();
            refreshWindowTitle();
            break;
        case TileEvent.DISCARD:
            DiscardedTilesDialog discardedTilesDialog = client.getDiscardedTilesDialog();
            if (discardedTilesDialog == null) {
                discardedTilesDialog = new DiscardedTilesDialog(client);
                client.setDiscardedTilesDialog(discardedTilesDialog);
                client.getJMenuBar().setItemEnabled(MenuItem.DISCARDED_TILES, true);
            }
            discardedTilesDialog.addTile(ev.getTile());
            discardedTilesDialog.setVisible(true);
            break;
        case TileEvent.PLACEMENT:
        case TileEvent.REMOVE:
            gameView.getMainPanel().tileEvent(ev);
            break;
        }
    }

    @Subscribe
    public void towerIncreased(TowerIncreasedEvent ev) {
        clearActions();
    }

    @Subscribe
    public void meeplePrisonEvent(MeeplePrisonEvent ev) {
        gameView.getGridPanel().repaint();
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
        clearActions();
        gameView.getControlPanel().getActionPanel().setFakeAction("dragonmove");
        DragonLayer dragonDecoration = gameView.getGridPanel().findLayer(DragonLayer.class);
        dragonDecoration.setMoves(movesLeft);
        gameView.getGridPanel().repaint();
        logger.debug("UI selectdragon move, left {}, {}", movesLeft, positions);
        if (ev.getTargetPlayer().isLocalHuman()) {
            DragonAvailableMove availMoves = gameView.getGridPanel().findLayer(DragonAvailableMove.class);
            availMoves.setPositions(positions);
            gameView.getGridPanel().showLayer(availMoves);
            client.beep();
        }
    }

    @Subscribe
    public void selectAction(SelectActionEvent ev) {
        clearActions();
        gameView.getControlPanel().selectAction(ev.getTargetPlayer(), ev.getActions(), ev.isPassAllowed());
        gameView.getGridPanel().repaint();
        //TODO generic solution
        if (game.isUndoAllowed() && ev.getTargetPlayer().isLocalHuman()) {
            client.getJMenuBar().setItemEnabled(MenuItem.UNDO, true);
        }
    }

    @Subscribe
    public void selectCornCircleOption(CornCircleSelectOptionEvent ev) {
        clearActions();
        CornCirclesPanel panel = new CornCirclesPanel(this);
        GridPanel gridPanel = gameView.getGridPanel();
        gridPanel.add(panel, "pos (100%-525) 0 (100%-275) 100%"); //TODO more robust layouting
        gridPanel.revalidate();
    }

    @Subscribe
    public void selectMageAndWitchRemoval(MageWitchSelectRemoval ev) {
        clearActions();
        SelectMageWitchRemovalPanel panel = new SelectMageWitchRemovalPanel(this);
        GridPanel gridPanel = gameView.getGridPanel();
        gridPanel.setMageWitchPanel(panel);
        gridPanel.add(panel, "pos (100%-525) 0 (100%-275) 100%"); //TODO more robust layouting
        gridPanel.revalidate();

    }

    public BazaarPanel showBazaarPanel() {
        BazaarPanel panel = gameView.getGridPanel().getBazaarPanel();
        if (panel == null) {
            panel = new BazaarPanel(client, gameView.getGameController());
            gameView.getGridPanel().add(panel, "pos (100%-525) 0 (100%-275) 100%"); //TODO more robust layouting
            gameView.getGridPanel().setBazaarPanel(panel);

        }
        return panel;
    }

    @Subscribe
    public void selectBazaarTile(BazaarSelectTileEvent ev) {
        clearActions();
        BazaarPanel bazaarPanel = showBazaarPanel();
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
        gameView.getGridPanel().repaint();
    }

    @Subscribe
    public void bazaarTileSelected(BazaarTileSelectedEvent ev) {
        BazaarPanel bazaarPanel = showBazaarPanel();
        bazaarPanel.setState(BazaarPanelState.INACTIVE);
        gameView.getGridPanel().repaint();
    }

    @Subscribe
    public void makeBazaarBid(BazaarMakeBidEvent ev) {
        BazaarPanel bazaarPanel = showBazaarPanel();
        bazaarPanel.setSelectedItem(ev.getSupplyIndex());
        if (ev.getTargetPlayer().isLocalHuman()) {
            bazaarPanel.setState(BazaarPanelState.MAKE_BID);
        } else {
            bazaarPanel.setState(BazaarPanelState.INACTIVE);
        }
        clearActions();
        gameView.getGridPanel().repaint();
    }

    @Subscribe
    public void selectBuyOrSellBazaarOffer(BazaarSelectBuyOrSellEvent ev) {
        BazaarPanel bazaarPanel = showBazaarPanel();
        bazaarPanel.setSelectedItem(ev.getSupplyIndex());
        if (ev.getTargetPlayer().isLocalHuman()) {
            bazaarPanel.setState(BazaarPanelState.BUY_OR_SELL);
        } else {
            bazaarPanel.setState(BazaarPanelState.INACTIVE);
        }
    }

    @Subscribe
    public void bazaarAuctionsEnded(BazaarAuctionEndEvent ev) {
        BazaarPanel panel = gameView.getGridPanel().getBazaarPanel();
        if (panel != null) {
            gameView.getGridPanel().remove(panel);
            gameView.getGridPanel().setBazaarPanel(null);
        }
    }

    public void leaveGame() {
        if (getChannel() == null) {
            client.closeGame();
            client.mountView(new StartView(client));
        } else {
            getConnection().send(new LeaveGameMessage(game.getGameId()));
            ChannelController ctrl = client.getClientMessageListener().getChannelControllers().get(channel);
            client.mountView(new ChannelView(client, ctrl));
        }
    }

    public RmiProxy getRmiProxy() {
        return rmiProxy;
    }

    public GameView getGameView() {
        return gameView;
    }

    public void setGameView(GameView gameView) {
        this.gameView = gameView;
    }

    public ReportingTool getReportingTool() {
        return reportingTool;
    }

    public void setReportingTool(ReportingTool reportingTool) {
        this.reportingTool = reportingTool;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }
}