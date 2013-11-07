package com.jcloisterzone.ui.controls;

import static com.jcloisterzone.ui.I18nUtils._;
import static com.jcloisterzone.ui.controls.ControlPanel.CORNER_DIAMETER;
import static com.jcloisterzone.ui.controls.ControlPanel.PANEL_WIDTH;
import static com.jcloisterzone.ui.controls.ControlPanel.PLAYER_BG_COLOR;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
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
import com.jcloisterzone.Player;
import com.jcloisterzone.TradeResource;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.SmallFollower;
import com.jcloisterzone.figure.Special;
import com.jcloisterzone.figure.predicate.MeeplePredicates;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.capability.AbbeyCapability;
import com.jcloisterzone.game.capability.BridgeCapability;
import com.jcloisterzone.game.capability.CastleCapability;
import com.jcloisterzone.game.capability.ClothWineGrainCapability;
import com.jcloisterzone.game.capability.KingScoutCapability;
import com.jcloisterzone.game.capability.TowerCapability;
import com.jcloisterzone.ui.Client;
import com.jcloisterzone.ui.UiUtils;

public class PlayerPanel extends FakeComponent implements RegionMouseListener {

    private static final Color DELIM_TOP_COLOR = new Color(250,250,250);
    private static final Color DELIM_BOTTOM_COLOR = new Color(220,220,220);
    private static final Color KING_SCOUT_OVERLAY = new Color(0f,0f,0f,0.4f);
    //private static final Color ACTIVE_TOWER_BG = new Color(255, 255, 70);

    private static Font FONT_POINTS = new Font("Georgia", Font.BOLD, 30);
    private static Font FONT_MEEPLE = new Font("Georgia", Font.BOLD, 18);
    private static Font FONT_KING_SCOUT_OVERLAY = new Font("Georgia", Font.BOLD, 22);
    private static Font FONT_NICKNAME = new Font(null, Font.BOLD, 18);

    private static final int PADDING_L = 9;
    private static final int PADDING_R = 11;
    private static final int LINE_HEIGHT = 32;
    private static final int DELIMITER_Y = 34;

    private final Player player;
    private final Color color;

    private final PlayerPanelImageCache cache;

    private int centerY;

    //paint context variables
    private Graphics2D g2;
    private int bx, by;

    private String mouseOverKey = null;

    public PlayerPanel(Client client, Player player, PlayerPanelImageCache cache) {
        super(client);
        this.player = player;
        this.cache = cache;
        this.color = client.getPlayerColor(player);
    }

    private void drawDelimiter(int y) {
        g2.setColor(DELIM_TOP_COLOR);
        g2.drawLine(PADDING_L, y, PANEL_WIDTH /*-PADDING_R*/, y);
        g2.setColor(DELIM_BOTTOM_COLOR);
        g2.drawLine(PADDING_L, y+1, PANEL_WIDTH /*-PADDING_R*/, y+1);
    }

    private void drawTextShadow(String text, int x, int y) {
        //TODO shadow color based on color ??
        /*g2.setColor(Color.DARK_GRAY);
        g2.drawString(text, x+0.8f, y+0.7f);*/
        g2.setColor(ControlPanel.FONT_SHADOW_COLOR);
        g2.drawString(text, x+0.6f, y+0.5f);
        g2.setColor(color);
        g2.drawString(text, x, y);
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
        if (bx+w > PANEL_WIDTH-PADDING_R) {
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

    /*
     * translates parentGraphics, which is not much clean!
     */
    @Override
    public void paintComponent(Graphics2D parentGraphics) {
        super.paintComponent(parentGraphics);

        Game game = client.getGame();

//		GridPanel gp = client.getGridPanel();

//        boolean isActive = game.getActivePlayer() == player;
//        boolean playerTurn = game.getTurnPlayer() == player;

//		gp.profile(" > get flags");

        BufferedImage bimg = UiUtils.newTransparentImage(PANEL_WIDTH, 200);
        g2 = bimg.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

//		gp.profile(" > create buffer");

        drawDelimiter(DELIMITER_Y);

        g2.setFont(FONT_POINTS);
        drawTextShadow(""+player.getPoints(), PADDING_L, 27);

        g2.setFont(FONT_NICKNAME);
        drawTextShadow(player.getNick(), 78, 27);

//		gp.profile(" > nick & score");

        g2.setFont(FONT_MEEPLE);
        bx = PADDING_L;
        by = 43;

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

        AbbeyCapability abbeyCap = game.getCapability(AbbeyCapability.class);
        TowerCapability towerCap = game.getCapability(TowerCapability.class);
        BridgeCapability bridgeCap = game.getCapability(BridgeCapability.class);
        CastleCapability castleCap = game.getCapability(CastleCapability.class);
        KingScoutCapability kingScoutCap = game.getCapability(KingScoutCapability.class);
        ClothWineGrainCapability cwgCap = game.getCapability(ClothWineGrainCapability.class);

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

        if (kingScoutCap != null) {
            if (kingScoutCap.getKing() == player) {
                Rectangle r = drawMeepleBox(null, "king", 1, false, "king");
                if ("king".equals(mouseOverKey)) {
                    g2.setFont(FONT_KING_SCOUT_OVERLAY);
                    g2.setColor(KING_SCOUT_OVERLAY);
                    g2.fillRect(r.x, r.y, r.width, r.height);
                    g2.setColor(Color.WHITE);
                    int size = kingScoutCap.getBiggestCitySize();
                    g2.drawString((size < 10 ? " " : "") + size, r.x+2, r.y+20);
                    g2.setFont(FONT_MEEPLE);
                }
            }
            if (kingScoutCap.getRobberBaron() == player) {
                Rectangle r = drawMeepleBox(null, "robber", 1, false, "robber");
                if ("robber".equals(mouseOverKey)) {
                    g2.setFont(FONT_KING_SCOUT_OVERLAY);
                    g2.setColor(KING_SCOUT_OVERLAY);
                    g2.fillRect(r.x, r.y, r.width, r.height);
                    g2.setColor(Color.WHITE);
                    int size = kingScoutCap.getLongestRoadLength();
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
                    boolean isOpponentActive = game.getActivePlayer() == opponent;
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


        int realHeight = by + (bx > PADDING_L ? LINE_HEIGHT : 0);

//        if (isActive) {
//            //TODO
//            //parentGraphics.setColor(Color.BLACK);
//            //parentGraphics.fillRoundRect(0, -5, PANEL_WIDTH+CORNER_DIAMETER, realHeight+10, CORNER_DIAMETER, CORNER_DIAMETER);
//        }

        parentGraphics.setColor(PLAYER_BG_COLOR);
        parentGraphics.fillRoundRect(0, 0, PANEL_WIDTH+CORNER_DIAMETER, realHeight, CORNER_DIAMETER, CORNER_DIAMETER);

        centerY = (int) parentGraphics.getTransform().getTranslateY() + realHeight/2;

        parentGraphics.drawImage(bimg, 0, 0, PANEL_WIDTH, realHeight, 0, 0, PANEL_WIDTH, realHeight, null);
        parentGraphics.translate(0, realHeight); //add also padding

        g2 = null;

//		gp.profile(" > complete");
    }

    public int getCenterY() {
        return centerY;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void mouseClicked(MouseEvent e, MouseListeningRegion origin) {
        if (!(origin.getData() instanceof Class)) return;
        Class<? extends Follower> followerClass = (Class<? extends Follower>) origin.getData();
        TowerCapability tg = client.getGame().getCapability(TowerCapability.class);
        if (!tg.isRansomPaidThisTurn()) {
            if (client.getSettings().isConfirmRansomPayment()) {
                String options[] = {_("Pay ransom"), _("Cancel") };
                int result = JOptionPane.showOptionDialog(client,
                        _("Do you really want to pay 3 points to release prisoner?"),
                        _("Confirm ransom payment"),
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
                if (JOptionPane.YES_OPTION != result) return;
            }
            client.getServer().payRansom(player.getIndex(), followerClass);
        }
    }

    @Override
    public void mouseEntered(MouseEvent e, MouseListeningRegion origin) {
        if (origin.getData() instanceof String) {
            mouseOverKey = (String) origin.getData();
            client.getGridPanel().repaint();
        } else {
            TowerCapability tg = client.getGame().getCapability(TowerCapability.class);
            if (!tg.isRansomPaidThisTurn()) {
                client.getGridPanel().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }
        }
    }

    @Override
    public void mouseExited(MouseEvent e, MouseListeningRegion origin) {
        if (mouseOverKey != null) {
            mouseOverKey = null;
            client.getGridPanel().repaint();
        } else {
            client.getGridPanel().setCursor(Cursor.getDefaultCursor());
        }
    }

}

