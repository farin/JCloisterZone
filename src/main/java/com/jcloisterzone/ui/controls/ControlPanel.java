package com.jcloisterzone.ui.controls;

import static com.jcloisterzone.ui.I18nUtils._tr;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;

import com.google.common.eventbus.Subscribe;
import com.jcloisterzone.Player;
import com.jcloisterzone.PlayerClock;
import com.jcloisterzone.action.ConfirmAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.action.TilePlacementAction;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.TilePack;
import com.jcloisterzone.config.Config.ConfirmConfig;
import com.jcloisterzone.event.ClockUpdateEvent;
import com.jcloisterzone.event.GameChangedEvent;
import com.jcloisterzone.event.play.MeepleDeployed;
import com.jcloisterzone.event.play.PlayEvent;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.Rule;
import com.jcloisterzone.game.capability.AbbeyCapability;
import com.jcloisterzone.game.capability.BazaarCapability;
import com.jcloisterzone.game.capability.BazaarCapabilityModel;
import com.jcloisterzone.game.capability.BazaarItem;
import com.jcloisterzone.game.phase.BazaarPhase;
import com.jcloisterzone.game.state.ActionsState;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.reducers.FinalScoring;
import com.jcloisterzone.ui.Client;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.ui.UIEventListener;
import com.jcloisterzone.ui.annotations.LinkedPanel;
import com.jcloisterzone.ui.view.GameView;
import com.jcloisterzone.wsio.message.CommitMessage;
import com.jcloisterzone.wsio.message.PassMessage;

import io.vavr.collection.Array;
import io.vavr.collection.Queue;
import io.vavr.collection.Vector;
import net.miginfocom.swing.MigLayout;

public class ControlPanel extends JPanel implements UIEventListener {

    private static Font FONT_PACK_SIZE = new Font(null, Font.PLAIN, 20);


    @Deprecated
    public static final Color FONT_SHADOW_COLOR = new Color(0, 0, 0, 60);
    public static final int CORNER_DIAMETER = 16;
    public static final int PANEL_WIDTH = 220; //TOOD remove
    public static final int PANEL_SHADOW_WIDTH = 3;
    public static final int LEFT_PADDING = 20;
    public static final int LEFT_MARGIN = 15;
    public static final int ACTIVE_MARKER_SIZE = 25;
    public static final int ACTIVE_MARKER_PADDING = 6;

    private static final String PASS_LABEL = _tr("Skip");
    private static final String CONFIRMATION_LABEL = _tr("Continue");

    private final Client client;
    private final GameView gameView;
    private final GameController gc;
    private final Game game;

    private JButton passButton;
    private boolean showConfirmRequest;
    private boolean showProjectedPoints;
    private GameState projectedPointsSource = null;

    private ActionPanel actionPanel;
    private PlayerPanel[] playerPanels;
    private NeutralFigurePanel neutralPanel;

    private BazaarCapability bcb;
    private BazaarSupplyPanel bazaarSupplyPanel;

    private final Timer timer;

    public ControlPanel(GameView gameView) {
        this.client = gameView.getClient();
        this.gameView = gameView;
        this.game = gameView.getGame();
        this.gc = gameView.getGameController();

        setOpaque(false);
        setLayout(new MigLayout("ins 0, gap 0", "[grow]", ""));

        passButton = new JButton(PASS_LABEL);
        passButton.setBorder(BorderFactory.createEmptyBorder(1, 30, 1, 30));
        passButton.setVisible(false);
        passButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pass();
            }
        });
        add(passButton, "pos 35 4");

        actionPanel = new ActionPanel(gameView);
        add(actionPanel, "wrap, growx, gapleft 35, h 106");

        if (game.getState().getCapabilities().contains(BazaarCapability.class)) {
            bazaarSupplyPanel = new BazaarSupplyPanel();
            bazaarSupplyPanel.setVisible(false);
            add(bazaarSupplyPanel, "wrap, growx, gapbottom 12, h 40, hidemode 3");
        }

        Array<Player> players = game.getState().getPlayers().getPlayers();
        PlayerPanelImageCache cache = new PlayerPanelImageCache(client, game);
        playerPanels = new PlayerPanel[players.length()];

        for (int i = 0; i < players.length(); i++) {
            playerPanels[i] = new PlayerPanel(client, gameView, players.get(i), cache);
            add(playerPanels[i], "wrap, growx, gapleft 35, gapbottom 12, h pref");
        }

        neutralPanel = new NeutralFigurePanel(client, game, cache);
        add(neutralPanel, "wrap, growx, gapleft 35, gapbottom 12, h pref");

        //better be accurate and repaint just every second - TODO
        timer = new Timer(500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (PlayerPanel panel : playerPanels) {
                    panel.repaint();
                }
            }
        });
    }

    private void paintBackgroundBody(Graphics2D g2) {
        g2.setColor(client.getTheme().getTransparentPanelBg());
        g2.fillRect(LEFT_MARGIN+LEFT_PADDING , 0, getWidth()-LEFT_MARGIN-LEFT_PADDING, getHeight());
    }

    private void paintBackgroundShadow(Graphics2D g2) {
        int h = getHeight();
        g2.translate(LEFT_MARGIN+LEFT_PADDING, 0); //adpat to old legacy code

        GameState state = game.getState();

        Player player = state.getTurnPlayer();
        if (player == null) {
            g2.setColor(client.getTheme().getTransparentPanelBg());
            g2.fillRect(-LEFT_PADDING , 0, LEFT_PADDING, h);
            g2.setColor(client.getTheme().getPanelShadow());
            g2.fillRect(-LEFT_PADDING-3, 0, 3, h);
        } else {
            PlayerPanel pp = playerPanels[player.getIndex()];
            int y = pp.getY() + pp.getRealHeight() / 2;

            g2.setColor(client.getTheme().getTransparentPanelBg());
            g2.fillRect(-LEFT_PADDING , 0, LEFT_PADDING, y-ACTIVE_MARKER_SIZE);
            g2.fillRect(-LEFT_PADDING , y+ACTIVE_MARKER_SIZE, LEFT_PADDING, h-y-ACTIVE_MARKER_SIZE);
            g2.fillPolygon(
                new int[] { -LEFT_PADDING, 0, 0, -ACTIVE_MARKER_PADDING },
                new int[] { y-ACTIVE_MARKER_SIZE, y-ACTIVE_MARKER_SIZE, y, y}, 4
            );
            g2.fillPolygon(
                new int[] { -LEFT_PADDING, 0, 0, -ACTIVE_MARKER_PADDING },
                new int[] { y+ACTIVE_MARKER_SIZE, y+ACTIVE_MARKER_SIZE, y, y}, 4
            );
            g2.setColor(client.getTheme().getPanelShadow());
            //g2.setColors(Color.RED);
            g2.fillRect(-LEFT_PADDING-PANEL_SHADOW_WIDTH, 0, PANEL_SHADOW_WIDTH, y-ACTIVE_MARKER_SIZE);
            g2.fillRect(-LEFT_PADDING-PANEL_SHADOW_WIDTH, y+ACTIVE_MARKER_SIZE, PANEL_SHADOW_WIDTH, h-y+ACTIVE_MARKER_SIZE);
            g2.fillPolygon(
                new int[] { -LEFT_PADDING-PANEL_SHADOW_WIDTH, -LEFT_PADDING, -ACTIVE_MARKER_PADDING, -PANEL_SHADOW_WIDTH-ACTIVE_MARKER_PADDING},
                new int[] { y-ACTIVE_MARKER_SIZE, y-ACTIVE_MARKER_SIZE, y, y}, 4
            );
            g2.fillPolygon(
                new int[] { -LEFT_PADDING-PANEL_SHADOW_WIDTH, -LEFT_PADDING, -ACTIVE_MARKER_PADDING, -PANEL_SHADOW_WIDTH-ACTIVE_MARKER_PADDING },
                new int[] { y+ACTIVE_MARKER_SIZE, y+ACTIVE_MARKER_SIZE, y, y}, 4
            );
        }

        player = state.getActivePlayer();
        if (player != null) {
            PlayerPanel pp = playerPanels[player.getIndex()];
            int y = pp.getY() + pp.getRealHeight() / 2;

            g2.setColor(client.getTheme().getMarkerColor());
//            g2.fillPolygon(
//                new int[] { -LEFT_PADDING-PANEL_SHADOW_WIDTH, -PANEL_SHADOW_WIDTH-ACTIVE_MARKER_PADDING, -LEFT_PADDING-PANEL_SHADOW_WIDTH},
//                new int[] { y-ACTIVE_MARKER_SIZE, y, y+ACTIVE_MARKER_SIZE,}, 3
//            );
            g2.fillPolygon(
                new int[] { -LEFT_PADDING-PANEL_SHADOW_WIDTH-3, -PANEL_SHADOW_WIDTH-ACTIVE_MARKER_PADDING, -LEFT_PADDING-PANEL_SHADOW_WIDTH-3},
                new int[] { y-ACTIVE_MARKER_SIZE-4, y, y+ACTIVE_MARKER_SIZE+4,}, 3
            );
        }

        g2.translate(-LEFT_MARGIN-LEFT_PADDING, 0);
    }


    @Override
    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        AffineTransform origTransform = g2.getTransform();
        int w = getWidth();

        paintBackgroundBody(g2);

        TilePack tilePack = game.getState().getTilePack();
        g2.setFont(FONT_PACK_SIZE);
        g2.setColor(client.getTheme().getHeaderFontColor());
        int packSize = tilePack.totalSize();
        g2.drawString("" + packSize, w - 42, 24);


        boolean doRevalidate = false;

        if (bazaarSupplyPanel != null) {
            //TODO Immutable - change bazaarSupplyPanel state base on gameChangeEvent !!!
            BazaarCapabilityModel model = gc.getGame().getState().getCapabilityModel(BazaarCapability.class);
            boolean showSupply = model.getSupply() != null;
            if (showSupply ^ bazaarSupplyPanel.isVisible()) {
                doRevalidate = true;
                bazaarSupplyPanel.setVisible(showSupply);
            }
        }

        for (PlayerPanel pp : playerPanels) {
            doRevalidate = doRevalidate || pp.repaintContent(w);
        }
        doRevalidate = doRevalidate || neutralPanel.repaintContent(w);

        if (doRevalidate) {
            revalidate();
        }

        g2.setTransform(origTransform);
        super.paint(g2);
    }

    @Override
    protected void paintChildren(Graphics g) {
        super.paintChildren(g);
        paintBackgroundShadow((Graphics2D) g);
    }

    private boolean isLastAbbeyPlacement(GameState state) {
        ActionsState as = state.getPlayerActions();
        if (as == null || as.getActions().isEmpty()) return false;
        PlayerAction<?> action = as.getActions().get();
        if (!(action instanceof TilePlacementAction)) return false;
        TilePlacementAction tpa = (TilePlacementAction) action;
        if (!AbbeyCapability.isAbbey(tpa.getTile())) return false;
        return state.getTilePack().size() == 0;
    }

    public void pass() {
        GameState state = game.getState();
        Player player = state.getActivePlayer();
        if (player == null || !player.isLocalHuman()) {
            return;
        }

        if (showConfirmRequest) {
            setShowConfirmRequest(false);
            gc.getConnection().send(new CommitMessage());
            repaint();
        } else {
            ActionsState actions = state.getPlayerActions();
            if (!actions.isPassAllowed()) {
                return;
            }

            if (isLastAbbeyPlacement(state)) {
                String[] options = new String[] {_tr("Skip Abbey"), _tr("Cancel and place Abbey") };
                int result = JOptionPane.showOptionDialog(client,
                    _tr("This is your last turn. If you skip it your Abbey remain unplaced."),
                    _tr("Last chance to place the Abbey"),
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
                if (result == -1 || result == 1) { //closed dialog
                    return;
                }
            }
            gc.getConnection().send(new PassMessage());
        }
    }

    public ActionPanel getActionPanel() {
        return actionPanel;
    }

    public void clearActions() {
        actionPanel.clearActions();
        passButton.setVisible(false);
        setShowConfirmRequest(false);
    }

    public boolean isShowPotentialPoints() {
        return showProjectedPoints;
    }

    public void setShowProjectedPoints(boolean showProjectedPoints) {
        this.showProjectedPoints = showProjectedPoints;
        if (showProjectedPoints) {
            refreshPotentialPoints();
        } else {
            gameView.getGridPanel().repaint(); //repaint immediately
        }
    }

    private void refreshPotentialPoints() {
        if (!showProjectedPoints) return;

        GameState state = game.getState();
        if (projectedPointsSource != state) {
            projectedPointsSource = state;
            GameState scored = (new FinalScoring()).apply(state);

            for (PlayerPanel playerPanel : playerPanels) {
                playerPanel.setPotentialPoints(playerPanel.getPlayer().getPoints(scored));
            }
        }

        repaint();
    }

    public void setShowConfirmRequest(boolean showConfirmRequest) {
        passButton.setText(showConfirmRequest ? CONFIRMATION_LABEL : PASS_LABEL);
        passButton.setVisible(showConfirmRequest);
        this.showConfirmRequest = showConfirmRequest;
        actionPanel.setShowConfirmRequest(showConfirmRequest, false);
        repaint();
    }

    @Subscribe
    public void handleGameChanged(GameChangedEvent ev) {
        if (ev.hasPlayerActionsChanged()) {
            GameState state = ev.getCurrentState();
            ActionsState actionsState = state.getPlayerActions();
            clearActions();
            if (actionsState != null) {
                boolean isLocal = actionsState.getPlayer().isLocalHuman();
                Vector<PlayerAction<?>> actions = actionsState.getActions();
                PlayerAction<?> first = actions.getOrNull();

                if (first instanceof ConfirmAction) {
                    handleConfirmAction(state, isLocal);
                } else if (
                    first != null && first.getClass().isAnnotationPresent(LinkedPanel.class)
                ) {
                    //do nothing - ignore actions managed by panels
                    //TODO show image anyway on control/action panel
                } else {
                    actionPanel.onPlayerActionsChanged(actionsState);
                    passButton.setVisible(isLocal && actionsState.isPassAllowed());
                }
            }
        }
        refreshPotentialPoints();
    }

    public void handleConfirmAction(GameState state, boolean isLocal) {
        if (isLocal) {
            boolean needsConfirm = false;
            PlayEvent last = state.getEvents().last();
            if (last instanceof MeepleDeployed) {
                ConfirmConfig cfg =  gc.getConfig().getConfirm();
                MeepleDeployed ev = (MeepleDeployed) last;
                if (cfg.getAny_deployment()) {
                    needsConfirm = true;
                } else if (cfg.getFarm_deployment() && ev.getLocation().isFarmLocation()) {
                    needsConfirm = true;
                } else if (cfg.getOn_tower_deployment() && ev.getLocation() == Location.TOWER) {
                    needsConfirm = true;
                }
            }
            if (needsConfirm) {
                setShowConfirmRequest(true);
            } else {
                gc.getConnection().send(new CommitMessage());
            }
        } else {
            actionPanel.setShowConfirmRequest(true, true);
            repaint();
        }
    }

    @Subscribe
    public void handleClockUpdateEvent(ClockUpdateEvent ev) {
        timer.stop();
        if (ev.isClockRunning() && game.getSetup().getRules().get(Rule.CLOCK_PLAYER_TIME).isDefined()) {
            PlayerClock runningClock = ev.getClocks().get(ev.getRunning());
            //this solution is not much accurate - TODO fix
            //+clean time from round trip!!!
            timer.setInitialDelay((int) runningClock.getTime() % 1000);
            timer.start();
        }
    }

    class BazaarSupplyPanel extends JPanel {

        public BazaarSupplyPanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D)g;
            super.paintComponent(g);
            GameState state = game.getState();
            BazaarCapabilityModel model = state.getCapabilityModel(BazaarCapability.class);
            Queue<BazaarItem> supply = model.getSupply();
            if (supply != null && !state.getPhase().equals(BazaarPhase.class)) {
                int x = LEFT_MARGIN+LEFT_PADDING;
                for (BazaarItem bi : supply) {
                    Tile tile = bi.getTile();
                    Image img = client.getResourceManager().getTileImage(tile.getId(), Rotation.R0).getImage();
                    g2.drawImage(img, x, 0, 40, 40, null);
                    x += 45;
                }
            }
        }
    }
}
