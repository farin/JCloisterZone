package com.jcloisterzone.ui.controls;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JOptionPane;

import com.google.common.collect.Iterables;
import com.jcloisterzone.LittleBuilding;
import com.jcloisterzone.Player;
import com.jcloisterzone.TradeResource;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.SmallFollower;
import com.jcloisterzone.figure.Special;
import com.jcloisterzone.figure.predicate.MeeplePredicates;
import com.jcloisterzone.game.CustomRule;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.capability.AbbeyCapability;
import com.jcloisterzone.game.capability.BridgeCapability;
import com.jcloisterzone.game.capability.CastleCapability;
import com.jcloisterzone.game.capability.ClothWineGrainCapability;
import com.jcloisterzone.game.capability.KingAndRobberBaronCapability;
import com.jcloisterzone.game.capability.LittleBuildingsCapability;
import com.jcloisterzone.game.capability.TowerCapability;
import com.jcloisterzone.game.capability.TunnelCapability;
import com.jcloisterzone.ui.Client;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.ui.UiUtils;
import com.jcloisterzone.ui.view.GameView;

import static com.jcloisterzone.ui.I18nUtils._;
import static com.jcloisterzone.ui.controls.ControlPanel.CORNER_DIAMETER;
import static com.jcloisterzone.ui.controls.ControlPanel.PLAYER_BG_COLOR;

public class PlayerPanel extends MouseTrackingComponent implements RegionMouseListener {

    private static final Color DELIM_TOP_COLOR = new Color(250,250,250);
    private static final Color DELIM_BOTTOM_COLOR = new Color(220,220,220);
    private static final Color KING_ROBBER_OVERLAY = new Color(0f,0f,0f,0.4f);
    private static final Color POTENTIAL_POINTS_COLOR = new Color(160, 160, 160);
    //private static final Color ACTIVE_TOWER_BG = new Color(255, 255, 70);

    private static Font FONT_POINTS = new Font("Georgia", Font.BOLD, 30);
    private static Font FONT_MEEPLE = new Font("Georgia", Font.BOLD, 18);
    private static Font FONT_KING_ROBBER_OVERLAY = new Font("Georgia", Font.BOLD, 22);
    private static Font FONT_NICKNAME = new Font(null, Font.BOLD, 18);
    private static Font FONT_OFFLINE = new Font(null, Font.BOLD, 20);

    private static final int PADDING_L = 9;
    private static final int PADDING_R = 11;
    private static final int LINE_HEIGHT = 32;
    private static final int DELIMITER_Y = 34;

    private final Client client;
    private final GameView gameView;
    private final GameController gc;
    private final Player player;
    private Color fontColor;

    private int potentialPoints = 0;

    private final PlayerPanelImageCache cache;

    //paint context variables
    private int PANEL_WIDTH = 1; //TODO clean, it's not constant now
    private BufferedImage bimg;
    private Graphics2D g2;
    private int realHeight = 1;
    private int bx, by;

    private String mouseOverKey = null;

    private final AbbeyCapability abbeyCap;
    private final TowerCapability towerCap;
    private final BridgeCapability bridgeCap;
    private final CastleCapability castleCap;
    private final KingAndRobberBaronCapability kingRobberCap;
    private final ClothWineGrainCapability cwgCap;
    private final LittleBuildingsCapability lbCap;
    private final TunnelCapability tunnelCap;

    private Integer timeLimit;

    public PlayerPanel(Client client, GameView gameView, Player player, PlayerPanelImageCache cache) {
        this.client = client;
        this.player = player;
        this.gameView = gameView;
        this.gc = gameView.getGameController();
        this.cache = cache;
        this.fontColor = player.getColors().getFontColor();

        Game game = gc.getGame();
        abbeyCap = game.getCapability(AbbeyCapability.class);
        towerCap = game.getCapability(TowerCapability.class);
        bridgeCap = game.getCapability(BridgeCapability.class);
        castleCap = game.getCapability(CastleCapability.class);
        kingRobberCap = game.getCapability(KingAndRobberBaronCapability.class);
        cwgCap = game.getCapability(ClothWineGrainCapability.class);
        lbCap = game.getCapability(LittleBuildingsCapability.class);
        tunnelCap = game.getCapability(TunnelCapability.class);

        timeLimit = (Integer) game.getCustomRules().get(CustomRule.CLOCK_PLAYER_TIME);
    }

    private void drawDelimiter(int y) {
        g2.setColor(DELIM_TOP_COLOR);
        g2.drawLine(PADDING_L, y, PANEL_WIDTH /*-PADDING_R*/, y);
        g2.setColor(DELIM_BOTTOM_COLOR);
        g2.drawLine(PADDING_L, y+1, PANEL_WIDTH /*-PADDING_R*/, y+1);
    }

    private void drawTextShadow(String text, int x, int y) {
        drawTextShadow(text, x, y, fontColor);
    }

    private void drawTextShadow(String text, int x, int y, Color color) {
        //TODO shadow color based on color ??
        /*g2.setColor(Color.DARK_GRAY);
        g2.drawString(text, x+0.8f, y+0.7f);*/
        g2.setColor(ControlPanel.FONT_SHADOW_COLOR);
        g2.drawString(text, x+0.6f, y+0.5f);
        g2.setColor(color);
        g2.drawString(text, x, y);
    }

    public Player getPlayer() {
        return player;
    }

    private Rectangle drawMeepleBox(Player playerKey, String imgKey, int count, boolean showOne) {
        return drawMeepleBox(playerKey, imgKey, count, showOne, null, false);
    }

    private Rectangle drawMeepleBox(Player playerKey, String imgKey, int count, boolean showOne, Object regionData) {
        return drawMeepleBox(playerKey, imgKey, count, showOne, regionData, false);
    }

    private Rectangle drawMeepleBox(Player playerKey, String imgKey, int count, boolean showOne, Object regionData, boolean active) {
        if (count == 0) return null;

        int w = 30;
        if (count > 1 || (count == 1 && showOne)) {
            w = count < 10 ? 47 : 60;
        }
        int h = 22;
        if (bx+w > PANEL_WIDTH-PADDING_R-PADDING_L) {
            bx = PADDING_L;
            by += LINE_HEIGHT;
        }
        g2.setColor(active ? Color.BLACK : Color.WHITE);
        g2.fillRoundRect(bx, by, w, h, 8, 8);
        g2.drawImage(cache.get(playerKey, imgKey), bx, by-4, null);

        Rectangle rect = null;
        if (regionData != null) {
            rect = new Rectangle(bx, by-4, w, h+8);
            getMouseRegions().add(new MouseListeningRegion(rect, this, regionData));
        }

        if (count > 1 || (count == 1 && showOne)) {
            g2.setColor(active ? Color.WHITE : Color.BLACK);
            g2.drawString(""+count, bx+LINE_HEIGHT, by+17);
        }
        bx += w + 8;
        return rect;
    }

    private void drawTimeTextBox(String text, Color textColor) {
        int w = 64;
        int h = 22;
        if (bx+w > PANEL_WIDTH-PADDING_R-PADDING_L) {
            bx = PADDING_L;
            by += LINE_HEIGHT;
        }
        g2.setColor(Color.WHITE);
        g2.fillRoundRect(bx, by, w, h, 8, 8);
        g2.setColor(textColor);
        g2.drawString(text, bx+4, by+17);
        bx += w + 8;
    }


    public boolean repaintContent(int width) {
        Game game = gc.getGame();
        PANEL_WIDTH = width;

        bimg = UiUtils.newTransparentImage(PANEL_WIDTH, 200);
        g2 = bimg.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

//		gp.profile(" > create buffer");

        drawDelimiter(DELIMITER_Y);

        g2.setFont(FONT_POINTS);
        drawTextShadow(""+player.getPoints(), PADDING_L, 27);


        //TODO cache ref (also would be fine to cache capabilities above)
        if (!game.isOver() && gc.getGameView().getControlPanel().isShowPotentialPoints()) {
            drawTextShadow("/ "+potentialPoints, 78, 27, POTENTIAL_POINTS_COLOR);
        } else {
            if (player.getSlot().isDisconnected()) {
                g2.setFont(FONT_OFFLINE);
                g2.setColor(POTENTIAL_POINTS_COLOR);
                g2.drawString("OFFLINE " + (player.getNick()), 65, 27);
            } else {
                g2.setFont(FONT_NICKNAME);
                drawTextShadow(player.getNick(), 78, 27);
            }
        }


//		gp.profile(" > nick & score");

        g2.setFont(FONT_MEEPLE);
        bx = PADDING_L;
        by = 43;

        if (timeLimit != null) {
            long remainingMs = timeLimit*1000 - player.getClock().getTime();
            if (remainingMs <= 0) {
                drawTimeTextBox("00.00", Color.RED);
            } else {
                long remaining = remainingMs / 1000;
                drawTimeTextBox(String.format("%02d.%02d", remaining / 60, remaining % 60), Color.DARK_GRAY);
            }
        }

        int small = 0;
        String smallImgKey = SmallFollower.class.getSimpleName();
        for (Follower f : Iterables.filter(player.getFollowers(), MeeplePredicates.inSupply())) {
            //instanceof cannot be used because of Phantom
            if (f.getClass().equals(SmallFollower.class)) {
                small++;
            } else { //all small followers are at beginning of collection
                drawMeepleBox(player, smallImgKey, small, true);
                small = 0;
                drawMeepleBox(player, f.getClass().getSimpleName(), 1, false);
            }
        }
        drawMeepleBox(player, smallImgKey, small, true); //case when only small followers are in collection (not drawn yet)

//		gp.profile(" > followers");

        for (Special meeple : Iterables.filter(player.getSpecialMeeples(), MeeplePredicates.inSupply())) {
            drawMeepleBox(player, meeple.getClass().getSimpleName(), 1, false);
        }

//		gp.profile(" > special");



        if (abbeyCap != null) {
            drawMeepleBox(null, "abbey", abbeyCap.hasUnusedAbbey(player) ? 1 : 0, false);
        }

        if (towerCap != null) {
            drawMeepleBox(null, "towerpiece", towerCap.getTowerPieces(player), true);
            getMouseRegions().clear();
        }

        if (bridgeCap != null) {
            drawMeepleBox(null, "bridge", bridgeCap.getPlayerBridges(player), true);
        }
        if (castleCap != null) {
            drawMeepleBox(null, "castle", castleCap.getPlayerCastles(player), true);
        }
        if (tunnelCap != null) {
            drawMeepleBox(player, "tunnelA", tunnelCap.getTunnelTokens(player, false), true);
            drawMeepleBox(player, "tunnelB", tunnelCap.getTunnelTokens(player, true), true);
        }

        if (lbCap != null) {
        	drawMeepleBox(null, "lb-shed", lbCap.getBuildingsCount(player, LittleBuilding.SHED), true);
        	drawMeepleBox(null, "lb-house", lbCap.getBuildingsCount(player, LittleBuilding.HOUSE), true);
            drawMeepleBox(null, "lb-tower", lbCap.getBuildingsCount(player, LittleBuilding.TOWER), true);
        }

        if (kingRobberCap != null) {
            if (kingRobberCap.getKing() == player) {
                Rectangle r = drawMeepleBox(null, "king", 1, false, "king");
                if ("king".equals(mouseOverKey)) {
                    g2.setFont(FONT_KING_ROBBER_OVERLAY);
                    g2.setColor(KING_ROBBER_OVERLAY);
                    g2.fillRect(r.x, r.y, r.width, r.height);
                    g2.setColor(Color.WHITE);
                    int size = kingRobberCap.getBiggestCitySize();
                    g2.drawString((size < 10 ? " " : "") + size, r.x+2, r.y+20);
                    g2.setFont(FONT_MEEPLE);
                }
            }
            if (kingRobberCap.getRobberBaron() == player) {
                Rectangle r = drawMeepleBox(null, "robber", 1, false, "robber");
                if ("robber".equals(mouseOverKey)) {
                    g2.setFont(FONT_KING_ROBBER_OVERLAY);
                    g2.setColor(KING_ROBBER_OVERLAY);
                    g2.fillRect(r.x, r.y, r.width, r.height);
                    g2.setColor(Color.WHITE);
                    int size = kingRobberCap.getLongestRoadLength();
                    g2.drawString((size < 10 ? " " : "") + size, r.x+2, r.y+20);
                    g2.setFont(FONT_MEEPLE);
                }
            }
        }
        if (cwgCap != null) {
            drawMeepleBox(null, "cloth", cwgCap.getTradeResources(player, TradeResource.CLOTH), true);
            drawMeepleBox(null, "grain", cwgCap.getTradeResources(player, TradeResource.GRAIN), true);
            drawMeepleBox(null, "wine", cwgCap.getTradeResources(player, TradeResource.WINE), true);
        }
        if (towerCap != null) {
            List<Follower> capturedFigures = towerCap.getPrisoners().get(player);
            Map<Class<? extends Follower>, Integer> groupedByType;
            if (!capturedFigures.isEmpty()) {
                groupedByType = new HashMap<>();
                for (Player opponent : game.getAllPlayers()) {
                    if (opponent == player) continue;
                    boolean isOpponentActive = opponent.equals(game.getActivePlayer()) && opponent.isLocalHuman();
                    boolean clickable = isOpponentActive && !towerCap.isRansomPaidThisTurn();
                    for (Follower f : capturedFigures) {
                        if (f.getPlayer() == opponent) {
                            Integer prevVal = groupedByType.get(f.getClass());
                            groupedByType.put(f.getClass(), prevVal == null ? 1 : prevVal+1);
                        }
                    }
                    for (Entry<Class<? extends Follower>, Integer> entry : groupedByType.entrySet()) {
                        drawMeepleBox(opponent, entry.getKey().getSimpleName(), entry.getValue(), false,
                                clickable ? entry.getKey() : null, clickable
                        );
                    }
                    groupedByType.clear();
                }
            }
        }

//		gp.profile(" > expansions");
        int oldValue = realHeight;

        realHeight = by + (bx > PADDING_L ? LINE_HEIGHT : 0);

        g2.dispose();
        g2 = null;

        return realHeight != oldValue;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(PANEL_WIDTH, realHeight);
    }

    /*
     * translates parentGraphics, which is not much clean!
     */
    @Override
    public void paint(Graphics g) {
        Graphics2D parentGraphics = (Graphics2D) g;
        parentGraphics.setColor(PLAYER_BG_COLOR);
        parentGraphics.fillRoundRect(0, 0, PANEL_WIDTH+CORNER_DIAMETER, realHeight, CORNER_DIAMETER, CORNER_DIAMETER);
        parentGraphics.drawImage(bimg, 0, 0, PANEL_WIDTH, realHeight, 0, 0, PANEL_WIDTH, realHeight, null);
        super.paintComponent(g);
    }

    public int getRealHeight() {
        return realHeight;
    }

    public int getPotentialPoints() {
        return potentialPoints;
    }

    public void setPotentialPoints(int potentialPoints) {
        this.potentialPoints = potentialPoints;
    }

    public void addPotentialPoints(int potentialPoints) {
        this.potentialPoints += potentialPoints;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void mouseClicked(MouseEvent e, MouseListeningRegion origin) {
        if (!(origin.getData() instanceof Class)) return;
        Class<? extends Follower> followerClass = (Class<? extends Follower>) origin.getData();
        TowerCapability tg = gameView.getGame().getCapability(TowerCapability.class);
        if (!tg.isRansomPaidThisTurn()) {
            if (client.getConfig().getConfirm().getRansom_payment()) {
                String options[] = {_("Pay ransom"), _("Cancel") };
                int result = JOptionPane.showOptionDialog(client,
                        _("Do you really want to pay 3 points to release prisoner?"),
                        _("Confirm ransom payment"),
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
                if (JOptionPane.YES_OPTION != result) return;
            }
            gc.getRmiProxy().payRansom(player.getIndex(), followerClass);
        }
    }

    @Override
    public void mouseEntered(MouseEvent e, MouseListeningRegion origin) {
        if (origin.getData() instanceof String) {
            mouseOverKey = (String) origin.getData();
            gameView.getGridPanel().repaint();
        } else {
            TowerCapability tg = gameView.getGame().getCapability(TowerCapability.class);
            if (!tg.isRansomPaidThisTurn()) {
                gameView.getGridPanel().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }
        }
    }

    @Override
    public void mouseExited(MouseEvent e, MouseListeningRegion origin) {
        if (mouseOverKey != null) {
            mouseOverKey = null;
            gameView.getGridPanel().repaint();
        } else {
            gameView.getGridPanel().setCursor(Cursor.getDefaultCursor());
        }
    }
}
