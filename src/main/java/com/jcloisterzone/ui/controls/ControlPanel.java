package com.jcloisterzone.ui.controls;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import net.miginfocom.swing.MigLayout;

import com.google.common.eventbus.Subscribe;
import com.jcloisterzone.Player;
import com.jcloisterzone.PlayerClock;
import com.jcloisterzone.PointCategory;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.TilePack;
import com.jcloisterzone.event.BazaarSelectBuyOrSellEvent;
import com.jcloisterzone.event.ClockUpdateEvent;
import com.jcloisterzone.event.FeatureCompletedEvent;
import com.jcloisterzone.event.FeatureEvent;
import com.jcloisterzone.event.MeepleEvent;
import com.jcloisterzone.event.RequestConfirmEvent;
import com.jcloisterzone.event.ScoreEvent;
import com.jcloisterzone.event.TileEvent;
import com.jcloisterzone.feature.Castle;
import com.jcloisterzone.feature.Completable;
import com.jcloisterzone.feature.Farm;
import com.jcloisterzone.feature.score.ScoreAllCallback;
import com.jcloisterzone.feature.score.ScoreAllFeatureFinder;
import com.jcloisterzone.feature.score.ScoringStrategy;
import com.jcloisterzone.feature.visitor.score.CompletableScoreContext;
import com.jcloisterzone.feature.visitor.score.FarmScoreContext;
import com.jcloisterzone.figure.Barn;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.CustomRule;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.capability.BazaarCapability;
import com.jcloisterzone.ui.Client;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.ui.view.GameView;
import com.jcloisterzone.wsio.message.CommitMessage;

import static com.jcloisterzone.ui.I18nUtils._;

public class ControlPanel extends JPanel {

    private static Font FONT_PACK_SIZE = new Font(null, Font.PLAIN, 20);

    public static final Color HEADER_FONT_COLOR = new Color(170, 170, 170, 200);
    public static final Color PLAYER_BG_COLOR = new Color(210, 210, 210, 200);
    public static final Color PANEL_BG_COLOR = new Color(255, 255, 255, 225);
    public static final Color PANEL_DARK_BG_COLOR = new Color(255, 255, 255, 245);
    public static final Color PANEL_BG_COLOR_SHADOW = new Color(255, 255, 255, 158);

    @Deprecated
    public static final Color FONT_SHADOW_COLOR = new Color(0, 0, 0, 60);
    public static final int CORNER_DIAMETER = 16;
    public static final int PANEL_WIDTH = 220; //TOOD remove
    public static final int PANEL_SHADOW_WIDTH = 3;
    public static final int LEFT_PADDING = 20;
    public static final int LEFT_MARGIN = 15;
    public static final int ACTIVE_MARKER_SIZE = 25;
    public static final int ACTIVE_MARKER_PADDING = 6;

    private static final String PASS_LABEL = _("Skip");
    private static final String CONFIRMATION_LABEL = _("Continue");

    private final Client client;
    private final GameView gameView;
    private final GameController gc;
    private final Game game;

    private JButton passButton;
    private boolean showConfirmRequest;
    private boolean canPass;
    private boolean showProjectedPoints, projectedPointsValid = true;

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
        gc.register(this);

        bcb = game.getCapability(BazaarCapability.class);

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

        if (bcb != null) {
            bazaarSupplyPanel = new BazaarSupplyPanel();
            bazaarSupplyPanel.setVisible(false);
            add(bazaarSupplyPanel, "wrap, growx, gapbottom 12, h 40, hidemode 3");
        }

        Player[] players = game.getAllPlayers();
        PlayerPanelImageCache cache = new PlayerPanelImageCache(client, game);
        playerPanels = new PlayerPanel[players.length];

        for (int i = 0; i < players.length; i++) {
            playerPanels[i] = new PlayerPanel(client, gameView, players[i], cache);
            add(playerPanels[i], "wrap, growx, gapleft 35, gapbottom 12, h pref");
        }

        neutralPanel = new NeutralFigurePanel(game, cache);
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


    private void setCanPass(boolean canPass) {
        this.canPass = canPass;
        passButton.setVisible(canPass);
    }


    private void paintBackgroundBody(Graphics2D g2) {
        g2.setColor(PANEL_BG_COLOR);
        g2.fillRect(LEFT_MARGIN+LEFT_PADDING , 0, getWidth()-LEFT_MARGIN-LEFT_PADDING, getHeight());
    }

    private void paintBackgroundShadow(Graphics2D g2) {
        int h = getHeight();
        g2.translate(LEFT_MARGIN+LEFT_PADDING, 0); //adpat to old legacy code

        Player player = game.getTurnPlayer();
        if (player == null) {
            g2.setColor(PANEL_BG_COLOR);
            g2.fillRect(-LEFT_PADDING , 0, LEFT_PADDING, h);
            g2.setColor(PANEL_BG_COLOR_SHADOW);
            g2.fillRect(-LEFT_PADDING-3, 0, 3, h);
        } else {
            PlayerPanel pp = playerPanels[player.getIndex()];
            int y = pp.getY() + pp.getRealHeight() / 2;

            g2.setColor(PANEL_BG_COLOR);
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
            g2.setColor(PANEL_BG_COLOR_SHADOW);
            //g2.setColor(Color.RED);
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

        player = game.getActivePlayer();
        if (player != null) {
            PlayerPanel pp = playerPanels[player.getIndex()];
            int y = pp.getY() + pp.getRealHeight() / 2;

            g2.setColor(Color.BLACK);
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

        TilePack tilePack = game.getTilePack();
        if (tilePack != null) { //null is possible for just loaded game
            g2.setFont(FONT_PACK_SIZE);
            g2.setColor(HEADER_FONT_COLOR);
            int packSize = tilePack.totalSize();
            g2.drawString("" + packSize, w - 42, 24);
        }

        boolean doRevalidate = false;

        if (bazaarSupplyPanel != null) {
            boolean showSupply = gameView.getGridPanel().getBazaarPanel() == null && bcb.getBazaarSupply() != null;
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

    public void pass() {
        if (showConfirmRequest) {
            setShowConfirmRequest(false);
            gc.getConnection().send(new CommitMessage(game.getGameId()));
            repaint();
        } else {
            gc.getRmiProxy().pass();
        }
    }

    public ActionPanel getActionPanel() {
        return actionPanel;
    }


    public void selectAction(Player targetPlayer, List<? extends PlayerAction<?>> actions, boolean canPass) {
        // direct collection sort can be unsupported - so copy to array first!
        int i = 0;
        PlayerAction<?>[] arr = new PlayerAction[actions.size()];
        for (PlayerAction<?> pa : actions) {
            pa.setClient(client);
            arr[i++] = pa;
        }
        Arrays.sort(arr);
        actionPanel.setActions(targetPlayer.isLocalHuman(), arr);
        setCanPass(targetPlayer.isLocalHuman() ? canPass : false);
    }

    public void clearActions() {
        actionPanel.clearActions();
        actionPanel.setFakeAction(null);
        setCanPass(false);
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
        projectedPointsValid = false;
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                //run only once in one time - refreshPotentialPoints can be triggered by more events
                if (!projectedPointsValid) {
                    projectedPointsValid = true;

                    for (PlayerPanel playerPanel : playerPanels) {
                        playerPanel.setPotentialPoints(playerPanel.getPlayer().getPoints());
                    }

                    PotentialPointScoringStrategy strategy = new PotentialPointScoringStrategy();
                    ScoreAllFeatureFinder scoreAll = new ScoreAllFeatureFinder();
                    scoreAll.scoreAll(game, strategy);
                    game.finalScoring(strategy);

                    repaint();
                }
            }
        });
    }

    public void setShowConfirmRequest(boolean showConfirmRequest) {
        passButton.setText(showConfirmRequest ? CONFIRMATION_LABEL : PASS_LABEL);
        passButton.setVisible(showConfirmRequest);
        this.showConfirmRequest = showConfirmRequest;
        actionPanel.setShowConfirmRequest(showConfirmRequest, false);
        repaint();
    }

    @Subscribe
    public void handleRequestConfirm(RequestConfirmEvent ev) {
        clearActions();
        if (ev.getTargetPlayer().isLocalHuman()) {
            setShowConfirmRequest(true);
        } else {
            actionPanel.setShowConfirmRequest(true, true);
            repaint();
        }
    }

    @Subscribe
    public void handleClockUpdateEvent(ClockUpdateEvent ev) {
        timer.stop();
        if (ev.isClockRunning() && game.getCustomRules().get(CustomRule.CLOCK_PLAYER_TIME) != null) {
            PlayerClock runningClock = ev.getRunningClockPlayer().getClock();
            //this solution is not much accurate - TODO fix
            //+clean time from roundtrip!!!
            timer.setInitialDelay((int) runningClock.getTime() % 1000);
            timer.start();
        }
    }

    @Subscribe
    public void handleScoreEvent(ScoreEvent ev) {
        refreshPotentialPoints();
    }

    @Subscribe
    public void handleTileEvent(TileEvent ev) {
        if (ev.getType() == TileEvent.PLACEMENT || ev.getType() == TileEvent.REMOVE) {
            refreshPotentialPoints();
        }
    }

    @Subscribe
    public void handleMeepleEvent(MeepleEvent ev) {
        refreshPotentialPoints();
    }

    @Subscribe
    public void handleBazaarSelectBuyOrSellEvent(BazaarSelectBuyOrSellEvent ev) {
        refreshPotentialPoints();
    }

    @Subscribe
    public void handleFeatureCompletedEvent(FeatureCompletedEvent ev) { //needs eg for King score
        refreshPotentialPoints();
    }

    @Subscribe
    public void handleFeatureEvent(FeatureEvent ev) {
        refreshPotentialPoints();
    }

    @Subscribe
    public void handleMeeplePrisonEvent(FeatureEvent ev) {
        refreshPotentialPoints();
    }

    class PotentialPointScoringStrategy implements ScoringStrategy, ScoreAllCallback {

        @Override
        public void addPoints(Player player, int points, PointCategory category) {
            playerPanels[player.getIndex()].addPotentialPoints(points);
        }

        @Override
        public void scoreCompletableFeature(CompletableScoreContext ctx) {
            int points = ctx.getPoints();
            for (Player p : ctx.getMajorOwners()) {
                addPoints(p, points, null);
            }
        }

        @Override
        public void scoreFarm(FarmScoreContext ctx, Player player) {
            addPoints(player, ctx.getPoints(player), null);

        }

        @Override
        public void scoreBarn(FarmScoreContext ctx, Barn meeple) {
            addPoints(meeple.getPlayer(), ctx.getBarnPoints(), null);
        }

        @Override
        public void scoreCastle(Meeple meeple, Castle castle) {
            //empty
        }

        @Override
        public CompletableScoreContext getCompletableScoreContext(Completable completable) {
            return completable.getScoreContext();
        }

        @Override
        public FarmScoreContext getFarmScoreContext(Farm farm) {
            return farm.getScoreContext();
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
            List<Tile> queue = bcb.getDrawQueue();
            if (!queue.isEmpty()) {
                int x = LEFT_MARGIN+LEFT_PADDING;
                for (Tile tile : queue) {
                    Image img = client.getResourceManager().getTileImage(tile);
                    g2.drawImage(img, x, 0, 40, 40, null);
                    x += 45;
                }
            }
        }
    }
}
