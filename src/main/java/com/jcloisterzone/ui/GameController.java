package com.jcloisterzone.ui;

import static com.jcloisterzone.ui.I18nUtils._tr;

import java.awt.Color;
import java.awt.Image;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.jcloisterzone.wsio.message.*;
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
import com.jcloisterzone.game.PlayerSlot;
import com.jcloisterzone.game.capability.TunnelCapability.Tunnel;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.ui.MenuBar.MenuItem;
import com.jcloisterzone.ui.controls.chat.GameChatPanel;
import com.jcloisterzone.ui.dialog.DiscardedTilesDialog;
import com.jcloisterzone.ui.panel.GameOverPanel;
import com.jcloisterzone.ui.resources.LayeredImageDescriptor;
import com.jcloisterzone.ui.view.ChannelView;
import com.jcloisterzone.ui.view.GameChatView;
import com.jcloisterzone.ui.view.GameView;
import com.jcloisterzone.ui.view.StartView;
import com.jcloisterzone.wsio.Connection;
import com.jcloisterzone.wsio.message.GameMessage.GameStatus;

import io.vavr.collection.Array;
import io.vavr.collection.Stream;

public class GameController extends EventProxyUiController<Game> {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private final Game game;
    private GameStatus gameStatus;
    private String channel;
    private boolean passwordProtected;

    private ReportingTool reportingTool;

    private GameView gameView;
    private Connection connProxy;
    private AtomicReference<String> chainMessageId = new AtomicReference<>();
    private AtomicBoolean actionLock = new AtomicBoolean();

    public GameController(Client client, Game game) {
        super(client, game);
        this.game = game;
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

    public AtomicReference<String> getChainMessageIdRef() {
        return chainMessageId;
     }

    public AtomicBoolean getActionLock() {
        return actionLock;
    }

    public void setGameStatus(GameStatus gameStatus) {
        this.gameStatus = gameStatus;
    }

    public void onGameStarted(Game game) {
        Stream<PlayerSlot> slots = Stream.ofAll(Arrays.asList(game.getPlayerSlots()));
        Array<PlayerSlot> occupiedSlots = slots.filter(slot -> slot != null && slot.isOccupied()).toArray();
        // for free color we can't search slot - because for loaded game, slots are already filtered
        // to existing ones
        Array<PlayerColors> freeColors = Stream.range(0, PlayerSlot.COUNT)
            .filter(i -> occupiedSlots.find(s -> s.getNumber() == i).isEmpty())
            .map(i -> getConfig().getPlayerColor(i))
            .toArray();

        int occupiedSize = occupiedSlots.size();
        int freeSize = freeColors.size();
        int i = 0;
        for (PlayerSlot slot : occupiedSlots) {
            Map<Tunnel, Color> tunnelColors = new HashMap<>();
            tunnelColors.put(Tunnel.TUNNEL_A, slot.getColors().getMeepleColor());
            if (freeSize >= occupiedSize) {
                tunnelColors.put(Tunnel.TUNNEL_B, freeColors.get(i).getMeepleColor());
                i++;
            }
            if (freeSize >= 2 * occupiedSize) {
                tunnelColors.put(Tunnel.TUNNEL_C, freeColors.get(i).getMeepleColor());
                i++;
            }
            slot.getColors().setTunnelColors(tunnelColors);
        }

        if (gameView == null) {
            gameView = new GameView(client, this);
            if (client.getView() instanceof GameChatView) {
                GameChatView prevView = (GameChatView) client.getView();
                gameView.setChatPanel(prevView.getChatPanel());
            } else {
                gameView.setChatPanel(new GameChatPanel(client, game));
            }
        } else {
            gameView.getGridPanel().hideErrorMessage(GameView.RECONNECTING_ERR_MSG);
        }
        if (client.getView() != gameView) {
            SwingUtilities.invokeLater(() -> {
                client.mountView(gameView);
            });
        }
    }


    @Subscribe
    public void handleGameChanged(GameChangedEvent ev) {
        //TODO probably can be removed
        if (gameView == null) {
            logger.warn("gameView is null");
            return;
        }
        // reset chaing, eg after resync
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
        gameView.getGridPanel().add(panel, "pos 0 35");
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
        title.append(" ⋅ ").append(String.format(_tr("%d tiles left"), packSize));

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
            if (msg.getMessageId() == null) {
                msg.setMessageId(UUID.randomUUID().toString());
            }
            if (msg instanceof WsInGameMessage) {
                ((WsInGameMessage) msg).setGameId(game.getGameId());
            }
            if (msg instanceof WsChainedMessage) {
                ((WsChainedMessage) msg).setParentId(chainMessageId.get());
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
        public void reconnect(String gameId, long initialDelay) {
            if (getConnection() != null) {
                getConnection().reconnect(gameId, initialDelay);
            }
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