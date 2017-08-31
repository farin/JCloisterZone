package com.jcloisterzone.ui.controls;

import static com.jcloisterzone.ui.controls.ControlPanel.CORNER_DIAMETER;

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

import com.jcloisterzone.Player;
import com.jcloisterzone.PlayerClock;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.SmallFollower;
import com.jcloisterzone.figure.Special;
import com.jcloisterzone.game.CustomRule;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.Token;
import com.jcloisterzone.game.capability.KingAndRobberBaronCapability;
import com.jcloisterzone.game.capability.TowerCapability;
import com.jcloisterzone.game.state.Flag;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.PlayersState;
import com.jcloisterzone.ui.Client;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.ui.UiUtils;
import com.jcloisterzone.ui.view.GameView;
import com.jcloisterzone.wsio.message.PayRansomMessage;

public class PlayerPanel extends MouseTrackingComponent implements RegionMouseListener {

    private static final Color KING_ROBBER_OVERLAY = new Color(0f,0f,0f,0.4f);
    private static final Color POTENTIAL_POINTS_COLOR = new Color(160, 160, 160);

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

    private Integer timeLimit;

    public PlayerPanel(Client client, GameView gameView, Player player, PlayerPanelImageCache cache) {
        this.client = client;
        this.player = player;
        this.gameView = gameView;
        this.gc = gameView.getGameController();
        this.cache = cache;
        this.fontColor = player.getColors().getFontColor();

        Game game = gc.getGame();

        timeLimit = (Integer) game.getState().getRules().get(CustomRule.CLOCK_PLAYER_TIME).getOrNull();
    }

    private void drawDelimiter(int y) {
        g2.setColor(client.getTheme().getDelimiterTopColor());
        g2.drawLine(PADDING_L, y, PANEL_WIDTH /*-PADDING_R*/, y);
        g2.setColor(client.getTheme().getDelimiterBottomColor());
        g2.drawLine(PADDING_L, y+1, PANEL_WIDTH /*-PADDING_R*/, y+1);
    }

    private void drawTextShadow(String text, int x, int y) {
        drawTextShadow(text, x, y, fontColor);
    }

    private void drawTextShadow(String text, int x, int y, Color color) {
        //TODO shadow color based on color ??
        Color shadowColor = client.getTheme().getFontShadowColor();
        if (shadowColor != null) {
            g2.setColor(shadowColor);
            g2.drawString(text, x+1, y+1);
        }
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

        //HACK - needed final bg for proper antialiasing - but can't overlap rounded corners
        g2.setColor(client.getTheme().getPlayerBoxBg());
        g2.fillRect(8, 8, PANEL_WIDTH, 36);

//		gp.profile(" > create buffer");

        getMouseRegions().clear();

        drawDelimiter(DELIMITER_Y);

        g2.setFont(FONT_POINTS);
        drawTextShadow(""+player.getPoints(game.getState()), PADDING_L, 27);


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

        GameState state = game.getState();
        PlayersState ps = state.getPlayers();
        int index = player.getIndex();

        if (timeLimit != null) {
            PlayerClock clock = game.getClocks().get(player.getIndex());
            long remainingMs = timeLimit*1000 - clock.getTime();
            if (remainingMs <= 0) {
                drawTimeTextBox("00.00", Color.RED);
            } else {
                long remaining = remainingMs / 1000;
                drawTimeTextBox(String.format("%02d.%02d", remaining / 60, remaining % 60), Color.DARK_GRAY);
            }
        }

        int small = 0;
        String smallImgKey = SmallFollower.class.getSimpleName();

        for (Follower f : player.getFollowers(state).filter(f -> f.isInSupply(state))) {
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

        for (Special meeple : player.getSpecialMeeples(state).filter(f -> f.isInSupply(state))) {
            drawMeepleBox(player, meeple.getClass().getSimpleName(), 1, false);
        }

//		gp.profile(" > special");

//   TODO IMMUTABLE

        drawMeepleBox(null, "abbey", ps.getPlayerTokenCount(index, Token.ABBEY_TILE), false);
        drawMeepleBox(null, "towerpiece", ps.getPlayerTokenCount(index, Token.TOWER_PIECE), true);
        drawMeepleBox(null, "bridge", ps.getPlayerTokenCount(index, Token.BRIDGE), true);
        drawMeepleBox(null, "castle", ps.getPlayerTokenCount(index, Token.CASTLE), true);

        drawMeepleBox(player, "tunnel.A", ps.getPlayerTokenCount(index, Token.TUNNEL_A), true);
        drawMeepleBox(player, "tunnel.B", ps.getPlayerTokenCount(index, Token.TUNNEL_B), true);
        drawMeepleBox(player, "tunnel.C", ps.getPlayerTokenCount(index, Token.TUNNEL_C), true);

        drawMeepleBox(null, "lb-shed", ps.getPlayerTokenCount(index, Token.LB_SHED), true);
        drawMeepleBox(null, "lb-house", ps.getPlayerTokenCount(index, Token.LB_HOUSE), true);
        drawMeepleBox(null, "lb-tower", ps.getPlayerTokenCount(index, Token.LB_TOWER), true);

        if (ps.getPlayerTokenCount(index, Token.KING) > 0) {
            KingAndRobberBaronCapability cap = state.getCapabilities().get(KingAndRobberBaronCapability.class);
            Rectangle r = drawMeepleBox(null, "king", 1, false, "king");
            if ("king".equals(mouseOverKey)) {
                g2.setFont(FONT_KING_ROBBER_OVERLAY);
                g2.setColor(KING_ROBBER_OVERLAY);
                g2.fillRect(r.x, r.y, r.width, r.height);
                g2.setColor(Color.WHITE);
                int size = cap.getBiggestCitySize(state);
                g2.drawString((size < 10 ? " " : "") + size, r.x+2, r.y+20);
                g2.setFont(FONT_MEEPLE);
            }
        }

        if (ps.getPlayerTokenCount(index, Token.ROBBER) > 0) {
            KingAndRobberBaronCapability cap = state.getCapabilities().get(KingAndRobberBaronCapability.class);
            Rectangle r = drawMeepleBox(null, "robber", 1, false, "robber");
            if ("robber".equals(mouseOverKey)) {
                g2.setFont(FONT_KING_ROBBER_OVERLAY);
                g2.setColor(KING_ROBBER_OVERLAY);
                g2.fillRect(r.x, r.y, r.width, r.height);
                g2.setColor(Color.WHITE);
                int size = cap.getLongestRoadSize(state);
                g2.drawString((size < 10 ? " " : "") + size, r.x+2, r.y+20);
                g2.setFont(FONT_MEEPLE);
            }
        }


        drawMeepleBox(null, "cloth", ps.getPlayerTokenCount(index, Token.CLOTH), true);
        drawMeepleBox(null, "grain", ps.getPlayerTokenCount(index, Token.GRAIN), true);
        drawMeepleBox(null, "wine", ps.getPlayerTokenCount(index, Token.WINE), true);

//        if (gldCap != null) {
//            drawMeepleBox(null, "gold", gldCap.getPlayerGoldPieces(player), true);
//        }

        io.vavr.collection.Array<io.vavr.collection.List<Follower>> towerModel = state.getCapabilityModel(TowerCapability.class);
        if (towerModel != null) {
            towerModel.get(player.getIndex()).groupBy(m -> m.getPlayer()).forEach((opponent, prisoners) -> {
                boolean isOpponentActive = opponent.equals(state.getActivePlayer()) && opponent.isLocalHuman();
                boolean clickable = isOpponentActive && !state.hasFlag(Flag.RANSOM_PAID);

                prisoners.groupBy(f -> f.getClass()).forEach((cls, items) -> {
                    drawMeepleBox(opponent, cls.getSimpleName(), items.length(), false,
                            clickable ? items.get().getId() : null, clickable
                    );
                });
            });
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
        parentGraphics.setColor(client.getTheme().getPlayerBoxBg());
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


    @Override
    public void mouseClicked(MouseEvent e, MouseListeningRegion origin) {
        String meepleId = (String) origin.getData();
        gc.getConnection().send(new PayRansomMessage(meepleId));
    }

    @Override
    public void mouseEntered(MouseEvent e, MouseListeningRegion origin) {
        if (origin.getData() instanceof String) {
            mouseOverKey = (String) origin.getData();
            gameView.getGridPanel().repaint();
        } else {
            gameView.getGridPanel().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
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
