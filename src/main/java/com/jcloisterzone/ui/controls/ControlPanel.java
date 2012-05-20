package com.jcloisterzone.ui.controls;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.font.TextAttribute;
import java.awt.geom.AffineTransform;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jcloisterzone.Player;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.ui.Client;
import com.jcloisterzone.ui.grid.GridPanel;

public class ControlPanel extends FakeComponent {

    // public static final Color BG_COLOR = new Color(0, 0, 0, 30);
    // public static final Color ACTIVE_BG_COLOR = new Color(0, 0, 0, 45);
    // public static final Color BG_COLOR = new Color(223, 223, 223, 200);
    // public static final Color ACTIVE_BG_COLOR = new Color(208, 208, 208,
    // 200);
    // public static final Color SHADOW_COLOR = new Color(0, 0, 0, 60);
    // public static final int CORNER_DIAMETER = 20;

    private static Font FONT_PACK_SIZE = new Font(null, Font.PLAIN, 20);
    private static Font FONT_PASS_PLAIN = new Font(null, Font.PLAIN, 14);
    private static Font FONT_PASS_UNDERLINE;

    static {
        Map<TextAttribute, Integer> fontAttributes = new HashMap<TextAttribute, Integer>();
        fontAttributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
        FONT_PASS_UNDERLINE = FONT_PASS_PLAIN.deriveFont(fontAttributes);
    }

    public static final Color HEADER_FONT_COLOR = new Color(170, 170, 170, 200);
    public static final Color PLAYER_BG_COLOR = new Color(210, 210, 210, 200);
    public static final Color PANEL_BG_COLOR = new Color(255, 255, 255, 225);
    public static final Color PANEL_BG_COLOR_SHADOW = new Color(255, 255, 255, 158);


    @Deprecated
    public static final Color FONT_SHADOW_COLOR = new Color(0, 0, 0, 60);

    public static final int CORNER_DIAMETER = 16;
    public static final int PANEL_WIDTH = 220;
    public static final int PANEL_SHADOW_WIDTH = 3;
    public static final int LEFT_PADDING = 20;
    public static final int ACTIVE_MARKER_SIZE = 25;
    public static final int ACTIVE_MARKER_PADDING = 6;


    private boolean canPass;

    private ActionPanel actionPanel;
    private PlayerPanel[] playerPanels;

    public static final String STR_CLICK_TO = "click to ";
    public static final String STR_PASS = "pass";
    private int clickToWidth, passWidth;

    public ControlPanel(final Client client) {
        super(client);

        FontMetrics fm = client.getFontMetrics(FONT_PASS_PLAIN);
        clickToWidth = fm.stringWidth(STR_CLICK_TO);
        passWidth = fm.stringWidth(STR_PASS);

        getMouseRegions().add(new MouseListeningRegion(
            new Rectangle(2, 6, clickToWidth+passWidth+6, 19), new RegionMouseListener() {
                @Override
                public void mouseClicked(MouseEvent e, MouseListeningRegion origin) {
                    pass();
                }
            }, null
        ));

        actionPanel = new ActionPanel(client);

        Player[] players = client.getGame().getAllPlayers();
        PlayerPanelImageCache cache = new PlayerPanelImageCache(client);
        playerPanels = new PlayerPanel[players.length];

        for (int i = 0; i < players.length; i++) {
            playerPanels[i] = new PlayerPanel(client, players[i], cache);
        }
    }

    @Override
    public void dispatchMouseEvent(MouseEvent e) {
        super.dispatchMouseEvent(e);
        if (e.isConsumed()) return;
        actionPanel.dispatchMouseEvent(e);
        for(PlayerPanel pp : playerPanels) {
            if (e.isConsumed()) return;
            pp.dispatchMouseEvent(e);
        }
    }

    private void paintBackgroundBody(Graphics2D g2) {
        GridPanel gp = client.getGridPanel();
        int h = gp.getHeight();

        g2.setColor(PANEL_BG_COLOR);
        g2.fillRect(0 , 0, PANEL_WIDTH, h);
    }

    private void paintBackgroundShadow(Graphics2D g2) {
        GridPanel gp = client.getGridPanel();
        int h = gp.getHeight();

        Player active = client.getGame().getActivePlayer();
        if (active == null) {
            g2.setColor(PANEL_BG_COLOR);
            g2.fillRect(-LEFT_PADDING , 0, LEFT_PADDING, h);
            g2.setColor(PANEL_BG_COLOR_SHADOW);
            g2.fillRect(-LEFT_PADDING-3, 0, 3, h);
        } else {
            int y = playerPanels[active.getIndex()].getCenterY();

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
            g2.fillRect(-LEFT_PADDING-PANEL_SHADOW_WIDTH, 0, PANEL_SHADOW_WIDTH, y-ACTIVE_MARKER_SIZE);
            g2.fillRect(-LEFT_PADDING-PANEL_SHADOW_WIDTH, y+ACTIVE_MARKER_SIZE, PANEL_SHADOW_WIDTH, h-y+ACTIVE_MARKER_SIZE);
            g2.fillPolygon(
                new int[] { -LEFT_PADDING-PANEL_SHADOW_WIDTH, -LEFT_PADDING, 0, -PANEL_SHADOW_WIDTH },
                new int[] { y-ACTIVE_MARKER_SIZE, y-ACTIVE_MARKER_SIZE, y, y}, 4
            );
            g2.fillPolygon(
                new int[] { -LEFT_PADDING-PANEL_SHADOW_WIDTH, -LEFT_PADDING, 0, -PANEL_SHADOW_WIDTH },
                new int[] { y+ACTIVE_MARKER_SIZE, y+ACTIVE_MARKER_SIZE, y, y}, 4
            );
        }
    }

    @Override
    public void paintComponent(Graphics2D g2) {
        super.paintComponent(g2);
        AffineTransform origTransform = g2.getTransform();

//		GridPanel gp = client.getGridPanel();

        paintBackgroundBody(g2);

        g2.setFont(FONT_PACK_SIZE);
        g2.setColor(HEADER_FONT_COLOR);
        int packSize = client.getGame().getTilePack().totalSize();
        g2.drawString("" + packSize, PANEL_WIDTH - 42, 24);

        if (canPass) {
            g2.setFont(FONT_PASS_PLAIN);
            g2.drawString(STR_CLICK_TO, 4, 21);
            g2.setFont(FONT_PASS_UNDERLINE);
            g2.drawString(STR_PASS, 4 + clickToWidth, 21);
        }

        g2.translate(0, 44);
        actionPanel.paintComponent(g2);

//		gp.profile("action panel");

        g2.translate(0, 60);
        for (PlayerPanel pp : playerPanels) {
            pp.paintComponent(g2);
        }

//		gp.profile("players");

        g2.setTransform(origTransform);

        paintBackgroundShadow(g2);

    }

    public void pass() {
        if (canPass) {
            client.getServer().pass();
        }
    }

    public ActionPanel getActionPanel() {
        return actionPanel;
    }

    public void selectAction(List<PlayerAction> actions, boolean canPass) {
        // direct collection sort can be unsupported - so copy to array first!
        int i = 0;
        PlayerAction[] arr = new PlayerAction[actions.size()];
        for (PlayerAction pa : actions) {
            pa.setClient(client);
            arr[i++] = pa;
        }
        Arrays.sort(arr);
        actionPanel.setActions(arr);
        this.canPass = canPass;
    }

    public void clearActions() {
        actionPanel.clearActions();
        canPass = false;
    }

    public void playerActivated(Player turn, Player active) {
        client.getGridPanel().repaint(); // players only
    }

    public void closeGame() {
        clearActions();
        canPass = false;
    }

}
