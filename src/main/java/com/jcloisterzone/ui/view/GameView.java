package com.jcloisterzone.ui.view;

import static com.jcloisterzone.ui.I18nUtils._tr;

import java.awt.Container;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.java_websocket.framing.CloseFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;
import com.jcloisterzone.Player;
import com.jcloisterzone.bugreport.BugReportDialog;
import com.jcloisterzone.event.ClientListChangedEvent;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.save.SavedGame;
import com.jcloisterzone.game.save.SavedGameParser;
import com.jcloisterzone.ui.Client;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.ui.MenuBar;
import com.jcloisterzone.ui.MenuBar.MenuItem;
import com.jcloisterzone.ui.SavegameFileFilter;
import com.jcloisterzone.ui.controls.ControlPanel;
import com.jcloisterzone.ui.controls.chat.ChatPanel;
import com.jcloisterzone.ui.dialog.GameSetupDialog;
import com.jcloisterzone.ui.grid.GridPanel;
import com.jcloisterzone.ui.grid.MainPanel;
import com.jcloisterzone.wsio.Connection;
import com.jcloisterzone.wsio.message.UndoMessage;
import com.jcloisterzone.wsio.message.WsReplayableMessage;

import io.vavr.collection.List;

public class GameView extends AbstractUiView implements WindowStateListener, GameChatView {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    public static final String RECONNECTING_ERR_MSG = "RECONNECTING";

    private final GameController gc;
    private final Game game;
    private boolean gameRunning = true; //is it needed, what about use game state (but force close don't change it)

    private ChatPanel chatPanel;

    private MainPanel mainPanel;

    private Timer timer;
    boolean repeatLeft, repeatRight, repeatUp, repeatDown;
    boolean repeatZoomIn, repeatZoomOut;


    public GameView(Client client, GameController gc) {
        super(client);
        this.gc = gc;
        this.game = gc.getGame();
        gc.setGameView(this);
    }

    public boolean isGameRunning() {
        return gameRunning;
    }

    public void setGameRunning(boolean gameRunning) {
        this.gameRunning = gameRunning;
    }

    public GameController getGameController() {
        return gc;
    }

    public Game getGame() {
        return game;
    }

    @Override
    public void show(Container pane) {
        mainPanel = new MainPanel(client, this, chatPanel);
        mainPanel.setBackground(client.getTheme().getMainBg());

        pane.add(mainPanel);

        gc.getReportingTool().setContainer(mainPanel);

        registerChildComponents(mainPanel, gc);

        timer = new Timer(true);
        timer.scheduleAtFixedRate(new KeyRepeater(), 0, 40);

        MenuBar menu = client.getJMenuBar();
        menu.setItemActionListener(MenuItem.SAVE, e -> handleSave());
        menu.setItemActionListener(MenuItem.UNDO, e -> {
            menu.setItemEnabled(MenuItem.UNDO, false);
            List<WsReplayableMessage> replay = game.getUndoHistory().head().getReplay();
            String lastMessageId = replay.isEmpty() ? "" : replay.last().getMessageId();
            gc.getConnection().send(new UndoMessage(lastMessageId));
        });
        menu.setItemActionListener(MenuItem.ZOOM_IN, e -> zoom(2.0));
        menu.setItemActionListener(MenuItem.ZOOM_OUT, e -> zoom(-2.0));
        menu.setItemActionListener(MenuItem.ROTATE_BOARD, e -> rotateBoard());
        menu.setItemActionListener(MenuItem.GAME_EVENTS, e -> {
            JCheckBoxMenuItem ch = (JCheckBoxMenuItem) e.getSource();
            mainPanel.getGridPanel().toggleGameEvents(ch.isSelected());
        });
        if (menu.isSelected(MenuItem.LAST_PLACEMENTS)) {
            mainPanel.getGridPanel().toggleGameEvents(true);
        }
        menu.setItemActionListener(MenuItem.LAST_PLACEMENTS, e -> {
            JCheckBoxMenuItem ch = (JCheckBoxMenuItem) e.getSource();
            mainPanel.toggleRecentHistory(ch.isSelected());
        });
        if (menu.isSelected(MenuItem.LAST_PLACEMENTS)) {
            mainPanel.toggleRecentHistory(true);
        }
        menu.setItemActionListener(MenuItem.FARM_HINTS, e -> {
            JCheckBoxMenuItem ch = (JCheckBoxMenuItem) e.getSource();
            mainPanel.setShowFarmHints(ch.isSelected());
        });
        if (menu.isSelected(MenuItem.FARM_HINTS)) {
            mainPanel.setShowFarmHints(true);
        }
        menu.setItemActionListener(MenuItem.PROJECTED_POINTS, e -> {
            JCheckBoxMenuItem ch = (JCheckBoxMenuItem) e.getSource();
            getControlPanel().setShowProjectedPoints(ch.isSelected());
        });
        if (menu.isSelected(MenuItem.PROJECTED_POINTS)) {
            getControlPanel().setShowProjectedPoints(true);
        }
        menu.setItemActionListener(MenuItem.DISCARDED_TILES, e -> client.getDiscardedTilesDialog().setVisible(true));
        menu.setItemActionListener(MenuItem.GAME_SETUP, e -> showGameSetupDialog());
        menu.setItemActionListener(MenuItem.TAKE_SCREENSHOT, e -> takeScreenshot());
        menu.setItemActionListener(MenuItem.REPORT_BUG, e -> new BugReportDialog(gc.getReportingTool()));
        menu.setItemActionListener(MenuItem.LEAVE_GAME, e -> gc.leaveGame());

        menu.setItemEnabled(MenuItem.FARM_HINTS, true);
        menu.setItemEnabled(MenuItem.GAME_EVENTS, true);
        menu.setItemEnabled(MenuItem.LAST_PLACEMENTS, true);
        menu.setItemEnabled(MenuItem.PROJECTED_POINTS, true);

        menu.setItemEnabled(MenuItem.REPORT_BUG, true);
        menu.setItemEnabled(MenuItem.GAME_SETUP, true);
        menu.setItemEnabled(MenuItem.TAKE_SCREENSHOT, true);
        menu.setItemEnabled(MenuItem.LEAVE_GAME, true);
        menu.setItemEnabled(MenuItem.ZOOM_IN, true);
        menu.setItemEnabled(MenuItem.ZOOM_OUT, true);
        menu.setItemEnabled(MenuItem.ROTATE_BOARD, true);
        menu.setItemEnabled(MenuItem.SAVE, true);
        menu.setItemEnabled(MenuItem.LOAD, false);
        menu.setItemEnabled(MenuItem.NEW_GAME, false);
        menu.setItemEnabled(MenuItem.CONNECT_P2P, false);
        menu.setItemEnabled(MenuItem.PLAY_ONLINE, false);

        client.addWindowStateListener(this);
    }

    @Override
    public boolean requestHide(UiView nextView) {
        if (gameRunning && gc.getChannel() == null) return client.closeGame();
        return true;
    }

    @Override
    public void hide(UiView nextView) {
        timer.cancel();

        unregisterChildComponents(mainPanel, gc);

        Connection conn = gc.getConnection();
        if (conn != null) conn.stopReconnecting();

        MenuBar menu = client.getJMenuBar();
        menu.setItemEnabled(MenuItem.FARM_HINTS, false);
        menu.setItemEnabled(MenuItem.GAME_EVENTS, false);
        menu.setItemEnabled(MenuItem.LAST_PLACEMENTS, false);
        menu.setItemEnabled(MenuItem.PROJECTED_POINTS, false);
        menu.setItemEnabled(MenuItem.ZOOM_IN, false);
        menu.setItemEnabled(MenuItem.ZOOM_OUT, false);
        menu.setItemEnabled(MenuItem.ROTATE_BOARD, false);
        menu.setItemEnabled(MenuItem.LEAVE_GAME, false);
        menu.setItemEnabled(MenuItem.TAKE_SCREENSHOT, false);
        menu.setItemEnabled(MenuItem.DISCARDED_TILES, false);

        client.removeWindowStateListener(this);
    }

    @Override
    public void windowStateChanged(WindowEvent e) {
        ChatPanel chatPanel = getGridPanel().getChatPanel();
        if (chatPanel != null) {
            chatPanel.windowStateChanged(e);
        }
    }

    public void closeGame() {
        gameRunning = false;
        getMainPanel().closeGame();
        getControlPanel().clearActions();

        MenuBar menu = client.getJMenuBar();
        menu.setItemEnabled(MenuItem.DISCARDED_TILES, false);
        menu.setItemEnabled(MenuItem.UNDO, false);
        menu.setItemEnabled(MenuItem.REPORT_BUG, false);

        if (gc.getChannel() == null) {
            menu.setItemEnabled(MenuItem.NEW_GAME, true);
            menu.setItemEnabled(MenuItem.CONNECT_P2P, true);
            menu.setItemEnabled(MenuItem.PLAY_ONLINE, true);
            menu.setItemEnabled(MenuItem.LOAD, true);
        }
    }

    @Override
    public void onWebsocketClose(int code, String reason, boolean remote) {
        String message = _tr("Connection lost") + ". " + _tr("Reconnecting...");
        if (code == CloseFrame.ABNORMAL_CLOSE || remote) {
            if (gc.getChannel() == null) {
                if (!game.isOver()) {
                    //simple server sends game message automatically, send game id for online server only
                    gc.getConnection().reconnect(null);
                    getGridPanel().showErrorMessage(message, RECONNECTING_ERR_MSG);
                }
            } else {
                gc.getConnection().reconnect(game.isOver() ? null : game.getGameId());
                getGridPanel().showErrorMessage(message, RECONNECTING_ERR_MSG);
            }
        }
    }

    @Override
    public void onWebsocketError(Exception ex) {
        String message = ex.getMessage();
        if (message == null || message.length() == 0) {
            message = ex.getClass().getSimpleName();
        }
        logger.error(message, ex);
        getGridPanel().showErrorMessage(message, null);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {
        if (chatPanel != null && chatPanel.getInput().hasFocus()) return false;
        if (e.getID() == KeyEvent.KEY_PRESSED) {
            if (e.getKeyChar() == '`' || e.getKeyChar() == ';') {
                e.consume();
                chatPanel.activateChat();
                return true;
            }
            switch (e.getKeyCode()) {
            case KeyEvent.VK_SPACE:
            case KeyEvent.VK_ENTER:
                mainPanel.getControlPanel().pass();
                return true;
            case KeyEvent.VK_TAB:
                if (e.getModifiers() == 0) {
                    mainPanel.getGridPanel().forward();
                } else if (e.getModifiers() == KeyEvent.SHIFT_MASK) {
                    mainPanel.getGridPanel().backward();
                }
                break;
            default:
                return dispatchReptable(e, true);
            }
        } else if (e.getID() == KeyEvent.KEY_RELEASED) {
            boolean result = dispatchReptable(e, false);
            if (result) e.consume();
            return result;
        } else if (e.getID() == KeyEvent.KEY_TYPED) {
            e.setKeyChar(Character.toLowerCase(e.getKeyChar()));
            return dispatchKeyTyped(e);
        }
        return false;
    }

    private boolean dispatchReptable(KeyEvent e, boolean pressed) {
        if (e.getModifiers() != 0) return false;
        switch (e.getKeyCode()) {
        case KeyEvent.VK_LEFT:
        case KeyEvent.VK_A:
            repeatLeft = pressed;
            return true;
        case KeyEvent.VK_RIGHT:
        case KeyEvent.VK_D:
            repeatRight = pressed;
            return true;
        case KeyEvent.VK_DOWN:
        case KeyEvent.VK_S:
            repeatDown = pressed;
            return true;
        case KeyEvent.VK_UP:
        case KeyEvent.VK_W:
            repeatUp = pressed;
            return true;
        }
        if (e.getKeyChar() == '+') {
            repeatZoomIn = pressed;
            return true;
        }
        if (e.getKeyChar() == '-') {
            repeatZoomOut = pressed;
            return true;
        }
        return false;
    }

    private boolean dispatchKeyTyped(KeyEvent e) {
        if (e.getModifiers() != 0) return false;
        if (e.getKeyChar() == '+' || e.getKeyChar() == '-') {
            e.consume();
            return true;
        }
        return false;
    }

    public MainPanel getMainPanel() {
        return mainPanel;
    }

    @Override
    public ChatPanel getChatPanel() {
        return chatPanel;
    }

    public void setChatPanel(ChatPanel chatPanel) {
        this.chatPanel = chatPanel;
    }

    //helpers

    public GridPanel getGridPanel() {
        return mainPanel.getGridPanel();
    }

    public ControlPanel getControlPanel() {
        return mainPanel.getControlPanel();
    }

    public void zoom(double steps) {
        GridPanel gp = getGridPanel();
        if (gp != null) gp.zoom(steps);
    }

    public void rotateBoard() {
        GridPanel gp = getGridPanel();
        if (gp != null) gp.rotateBoard();
    }

    @Subscribe
    public void clientListChanged(ClientListChangedEvent ev) {
        if (!game.isOver()) {
            getMainPanel().repaint();
        }
    }

    public void handleSave() {
        JFileChooser fc = new JFileChooser(client.getSavesDirectory());
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fc.setDialogTitle(_tr("Save game"));
        fc.setDialogType(JFileChooser.SAVE_DIALOG);
        fc.setFileFilter(new SavegameFileFilter());
        fc.setLocale(client.getLocale());
        int returnVal = fc.showSaveDialog(client);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            if (file != null) {
                if (!file.getName().endsWith(".jcz")) {
                    file = new File(file.getAbsolutePath() + ".jcz");
                }
                try (Writer writer = new FileWriter(file)) {
                    SavedGame save = new SavedGame(game);
                    SavedGameParser parser = new SavedGameParser("pretty".equals(getClient().getConfig().getSaved_games().getFormat()));
                    save.setAnnotations(game.getGameAnnotations());
                    parser.toJson(save, writer);
                } catch (IOException ex) {
                    logger.error(ex.getMessage(), ex);
                    JOptionPane.showMessageDialog(client, ex.getLocalizedMessage(), _tr("Error"), JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void showGameSetupDialog() {
        (new GameSetupDialog(client, gc.getGame())).setVisible(true);
    }

    public void takeScreenshot() {
        GridPanel container = getGridPanel();
        File screenshotFolder = client.getScreenshotDirectory();

         //player names:
         StringBuilder players = new StringBuilder();
         boolean hasAi = false;
         for (Player p : game.getState().getPlayers().getPlayers()) {
             if (p.getSlot().isAi()) {
                 hasAi = true;
             } else {
                 players.append(p.getNick());
                 players.append("_");
             }
         }
         if (hasAi) players.append("AI_");
         SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss");
         File filename = new File(screenshotFolder, players.toString() + sdf.format(new Date()) + ".png");
        //
        try (FileOutputStream fos = new FileOutputStream(filename)) {
            BufferedImage im = container.takeScreenshot();
            ImageIO.write(im, "PNG", fos);
            client.playSound("audio/shutter.wav");
        } catch (IOException ex) {
            logger.error(ex.getMessage(), ex);
            JOptionPane.showMessageDialog(client, ex.getLocalizedMessage(), _tr("Error"), JOptionPane.ERROR_MESSAGE);
        }
    }

    class KeyRepeater extends TimerTask {

        @Override
        public void run() {
            GridPanel gridPanel = mainPanel.getGridPanel();
            if (gridPanel == null) return;
            if (repeatLeft) {
                gridPanel.moveCenter(-1, 0);
            }
            if (repeatRight) {
                gridPanel.moveCenter(1, 0);
            }
            if (repeatUp) {
                gridPanel.moveCenter(0, -1);
            }
            if (repeatDown) {
                gridPanel.moveCenter(0, 1);
            }
            if (repeatZoomIn) {
                gridPanel.zoom(0.8);
            }
            if (repeatZoomOut) {
                gridPanel.zoom(-0.8);
            }
        }
    }
}
