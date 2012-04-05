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

    public static final Color HEADER_COLOR = new Color(170, 170, 170, 200);
    public static final Color BG_COLOR = new Color(210, 210, 210, 200);
    // public static final Color ACTIVE_BG_COLOR = new Color(206, 206, 206,
    // 200);
    @Deprecated
    public static final Color ACTIVE_BG_COLOR = BG_COLOR;
    public static final Color SHADOW_COLOR = new Color(0, 0, 0, 60);

    public static final int CORNER_DIAMETER = 16;

    private final Client client;

    private boolean canPass;

    private ActionPanel actionPanel;
    private PlayerPanel[] playerPanels;

    public static final int PANEL_WIDTH = 220;
    public static final String STR_CLICK_TO = "click to ";
    public static final String STR_PASS = "pass";
    private int clickToWidth, passWidth;

    public ControlPanel(final Client client) {
        this.client = client;

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

    // //TODO clean coupling and component initialization (GridPanel, MainPanel
    // & ControlPanel
    // public void registerMouseListener() {
    // client.getGridPanel().addMouseListener(new MouseAdapter() {
    // @Override
    // public void mouseClicked(MouseEvent e) {
    // int w = client.getGridPanel().getWidth();
    // if (e.getX() > w-PANEL_WIDTH) {
    // //click on panel
    //
    // }
    // }
    // });
    // }

    @Override
    public void paintComponent(Graphics2D g2) {
        super.paintComponent(g2);
        AffineTransform origTransform = g2.getTransform();

//		GridPanel gp = client.getGridPanel();

        g2.setFont(FONT_PACK_SIZE);
        g2.setColor(HEADER_COLOR);
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
        client.getGridPanel().repaint(); // players only
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
