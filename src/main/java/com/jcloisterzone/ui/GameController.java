package com.jcloisterzone.ui;

import static com.jcloisterzone.ui.I18nUtils._;

import java.awt.Color;
import java.awt.Image;
import java.util.List;

import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;
import com.jcloisterzone.Player;
import com.jcloisterzone.bugreport.ReportingTool;
import com.jcloisterzone.event.GameChangedEvent;
import com.jcloisterzone.event.GameListChangedEvent;
import com.jcloisterzone.event.GameOverEvent;
import com.jcloisterzone.figure.SmallFollower;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.ui.MenuBar.MenuItem;
import com.jcloisterzone.ui.dialog.DiscardedTilesDialog;
import com.jcloisterzone.ui.panel.GameOverPanel;
import com.jcloisterzone.ui.resources.LayeredImageDescriptor;
import com.jcloisterzone.ui.view.ChannelView;
import com.jcloisterzone.ui.view.GameView;
import com.jcloisterzone.ui.view.StartView;
import com.jcloisterzone.wsio.Connection;
import com.jcloisterzone.wsio.message.GameMessage.GameStatus;
import com.jcloisterzone.wsio.message.LeaveGameMessage;
import com.jcloisterzone.wsio.message.WsInGameMessage;
import com.jcloisterzone.wsio.message.WsMessage;

public class GameController extends EventProxyUiController<Game> {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private final Game game;
    private GameStatus gameStatus;
    private String channel;
    private boolean passwordProtected;

    private ReportingTool reportingTool;

    private GameView gameView;
    private Connection connProxy;

    public GameController(Client client, Game game) {
        super(client, game);
        this.game = game;
        getInvokeInSwingUiAdapter().setReportingTool(reportingTool);
        connProxy = new ConnectionProxy();
    }

    public Game getGame() {
        return game;
    }

    public String getGameId() {
        return game.getGameId();
    }

    public GameStatus getGameStatus() {
        return gameStatus;
    }

    public void setGameStatus(GameStatus gameStatus) {
        this.gameStatus = gameStatus;
    }

    @Subscribe
    public void handleGameChanged(GameChangedEvent ev) {
        //TODO probabaly can be removed
        if (gameView == null) {
            logger.warn("gameView is null");
            return;
        }
        GameState state = ev.getCurrentState();

        if (ev.hasDiscardedTilesChanged()) {
            DiscardedTilesDialog discardedTilesDialog = client.getDiscardedTilesDialog();
            if (discardedTilesDialog == null) {
                discardedTilesDialog = new DiscardedTilesDialog(client);
                client.setDiscardedTilesDialog(discardedTilesDialog);
                client.getJMenuBar().setItemEnabled(MenuItem.DISCARDED_TILES, true);
            }
            discardedTilesDialog.setDiscardedTiles(state.getDiscardedTiles());
            discardedTilesDialog.setVisible(true);
        }

        if (ev.hasPlayerActionsChanged()) {
            Player pl = state.getActivePlayer();
            boolean canUndo = pl != null && pl.isLocalHuman() && game.isUndoAllowed();
            client.getJMenuBar().setItemEnabled(MenuItem.UNDO, canUndo);
        }

        if (ev.hasTurnPlayerChanged()) {
            Player pl = state.getTurnPlayer();

            if (pl.isLocalHuman()) {
                client.beep();
            }

            // TODO better image quality ?
            Color c = pl.getColors().getMeepleColor();
            Image image = client.getResourceManager().getLayeredImage(new LayeredImageDescriptor(SmallFollower.class, c));
            client.setIconImage(image);
        }
    }

    @Subscribe
    public void handleGameStateChange(GameOverEvent ev) {
        boolean showPlayAgain = client.getLocalServer() != null;
        gameView.setGameRunning(false);
        //TODO allow chat after game also for standalone server
        if (getChannel() == null && gameView.getChatPanel() != null) {
            gameView.getGridPanel().remove(gameView.getChatPanel());
        }
        client.closeGame(true);
        GameOverPanel panel = new GameOverPanel(client, this, showPlayAgain);
        gameView.getGridPanel().add(panel, "pos 0 0");
        gameView.getGridPanel().revalidate();
    }

    public void refreshWindowTitle() {
        StringBuilder title = new StringBuilder(Client.BASE_TITLE);
        GameState state = game.getState();

        Player activePlayer = state.getActivePlayer();
        if (activePlayer != null) {
            title.append(" ⋅ ").append(activePlayer.getNick());
        }
        int packSize = state.getTilePack().totalSize();
        title.append(" ⋅ ").append(String.format(_("%d tiles left"), packSize));

        client.setTitle(title.toString());
    }

    // User interface

    //@Override
    public void showWarning(String title, String message) {
        JOptionPane.showMessageDialog(client, message, title, JOptionPane.WARNING_MESSAGE);
    }

//    @Subscribe
//    public void handleSelectMageAndWitchRemoval(MageWitchSelectRemoval ev) {
//        SelectMageWitchRemovalPanel panel = new SelectMageWitchRemovalPanel(this);
//        GridPanel gridPanel = gameView.getGridPanel();
//        //gridPanel.setMageWitchPanel(panel);
//        gridPanel.add(panel, "pos (100%-525) 0 (100%-275) 100%"); //TODO more robust layouting
//        gridPanel.revalidate();
//
//    }

    public void leaveGame() {
        if (getChannel() == null) {
            client.mountView(new StartView(client));
        } else {
            if (getConnection().isClosed()) {
                //TODO stop reconnecting
                client.mountView(new StartView(client));
            } else {
                ClientMessageListener cml = client.getClientMessageListener();
                getConnection().send(new LeaveGameMessage());
                ChannelController ctrl = cml.getChannelControllers().get(channel);
                client.mountView(new ChannelView(client, ctrl));

                List<GameController> gcs = cml.getGameControllers(channel);
                ctrl.getEventProxy().post(
                    new GameListChangedEvent(gcs.toArray(new GameController[gcs.size()]))
                );
            }
        }
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

    public boolean isPasswordProtected() {
        return passwordProtected;
    }

    public void setPasswordProtected(boolean passwordProtected) {
        this.passwordProtected = passwordProtected;
    }

    @Override
    public Connection getConnection() {
        return connProxy;
    }

    /**
     * Connection proxy sets gameId field for every sent WsInGameMessage
     */
    class ConnectionProxy implements Connection {

        private Connection getConnection() {
            return client.getConnection();
        }

        @Override
        public void send(WsMessage msg) {
            if (msg instanceof WsInGameMessage) {
                ((WsInGameMessage) msg).setGameId(game.getGameId());
            }
            getConnection().send(msg);
        }

        @Override
        public boolean isClosed() {
            if (getConnection() != null) {
                return getConnection().isClosed();
            } else {
                return false;
            }
        }

        @Override
        public void close() {
            if (getConnection() != null) {
                getConnection().close();
            }
        }

        @Override
        public void reconnect(String gameId) {
            getConnection().reconnect(gameId);
        }

        @Override
        public void stopReconnecting() {
            if (getConnection() != null) {
                getConnection().stopReconnecting();
            }
        }

        @Override
        public String getSessionId() {
            return getConnection().getSessionId();
        }

        @Override
        public String getNickname() {
            return client.getConnection().getNickname();
        }
    }
}