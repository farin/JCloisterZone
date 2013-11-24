package com.jcloisterzone.ui.controls;

import static com.jcloisterzone.ui.I18nUtils._;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.util.Arrays;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;

import com.jcloisterzone.Player;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.TilePack;
import com.jcloisterzone.game.capability.BazaarCapability;
import com.jcloisterzone.ui.Client;
import com.jcloisterzone.ui.grid.BazaarPanel;
import com.jcloisterzone.ui.grid.GridPanel;

public class ControlPanel extends FakeComponent {

    private static Font FONT_PACK_SIZE = new Font(null, Font.PLAIN, 20);

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

    private JButton passButton;
    private boolean canPass;

    private ActionPanel actionPanel;
    private PlayerPanel[] playerPanels;

    public ControlPanel(final Client client) {
        super(client);

        actionPanel = new ActionPanel(client);

        Player[] players = client.getGame().getAllPlayers();
        PlayerPanelImageCache cache = new PlayerPanelImageCache(client);
        playerPanels = new PlayerPanel[players.length];

        for (int i = 0; i < players.length; i++) {
            playerPanels[i] = new PlayerPanel(client, players[i], cache);
        }
    }

    @Override
    public void componentResized(ComponentEvent e) {
        refreshComponents();
        client.getGridPanel().repaint();
    }

    @Override
    public void registerSwingComponents(JComponent parent) {
        passButton = new JButton(_("Skip"));
        passButton.setMargin(new Insets(1,1,1,1));
        passButton.setVisible(false);
        passButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pass();
            }
        });
        parent.add(passButton);
    }

    @Override
    public void destroySwingComponents(JComponent parent) {
        parent.remove(passButton);
    }

    private void refreshComponents() {
        passButton.setVisible(canPass);
        if (canPass) {
            //TODO hardcoded offset - but no better solution for now
            int x = client.getGridPanel().getWidth()-PANEL_WIDTH;
            passButton.setBounds(x, 4, 90, 19);
        }
    }

    @Override
    public void dispatchMouseEvent(MouseEvent e) {
        super.dispatchMouseEvent(e);
        if (e.isConsumed()) return;
        actionPanel.dispatchMouseEvent(e);
        for (PlayerPanel pp : playerPanels) {
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

        Player player = client.getGame().getTurnPlayer();
        if (player == null) {
            g2.setColor(PANEL_BG_COLOR);
            g2.fillRect(-LEFT_PADDING , 0, LEFT_PADDING, h);
            g2.setColor(PANEL_BG_COLOR_SHADOW);
            g2.fillRect(-LEFT_PADDING-3, 0, 3, h);
        } else {
            int y = playerPanels[player.getIndex()].getCenterY();

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

        player = client.getGame().getActivePlayer();
        if (player != null) {
            g2.setColor(Color.BLACK);
            int y = playerPanels[player.getIndex()].getCenterY();
//            g2.fillPolygon(
//                new int[] { -LEFT_PADDING-PANEL_SHADOW_WIDTH, -PANEL_SHADOW_WIDTH-ACTIVE_MARKER_PADDING, -LEFT_PADDING-PANEL_SHADOW_WIDTH},
//                new int[] { y-ACTIVE_MARKER_SIZE, y, y+ACTIVE_MARKER_SIZE,}, 3
//            );
            g2.fillPolygon(
                new int[] { -LEFT_PADDING-PANEL_SHADOW_WIDTH-3, -PANEL_SHADOW_WIDTH-ACTIVE_MARKER_PADDING, -LEFT_PADDING-PANEL_SHADOW_WIDTH-3},
                new int[] { y-ACTIVE_MARKER_SIZE-4, y, y+ACTIVE_MARKER_SIZE+4,}, 3
            );
        }
    }

    @Override
    public void paintComponent(Graphics2D g2) {
        super.paintComponent(g2);
        AffineTransform origTransform = g2.getTransform();

//		GridPanel gp = client.getGridPanel();

        paintBackgroundBody(g2);

        TilePack tilePack = client.getGame().getTilePack();
        if (tilePack != null) { //null is possible for just loaded game
            g2.setFont(FONT_PACK_SIZE);
            g2.setColor(HEADER_FONT_COLOR);
            int packSize = tilePack.totalSize();
            g2.drawString("" + packSize, PANEL_WIDTH - 42, 24);
        }

        g2.translate(0, 46);
        actionPanel.paintComponent(g2);
//		gp.profile("action panel");

        g2.translate(0, 60);

        BazaarCapability bcb = client.getGame().getCapability(BazaarCapability.class);
        if (bcb != null && !(client.getGridPanel().getSecondPanel() instanceof BazaarPanel)) { //show bazaar supply only if panel is hidden
            List<Tile> queue = bcb.getDrawQueue();
            if (!queue.isEmpty()) {
                int x = 0;
                for (Tile tile : queue) {
                    Image img = client.getResourceManager().getTileImage(tile);
                    g2.drawImage(img, x, 0, 40, 40, null);
                    x += 45;
                }

                g2.translate(0, 50);
            }
        }


        for (PlayerPanel pp : playerPanels) {
            pp.paintComponent(g2);
            g2.translate(0, 12);
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
        this.canPass = client.isClientActive() ? canPass : false;
        refreshComponents();
    }

    public void clearActions() {
        actionPanel.clearActions();
        canPass = false;
        refreshComponents();
    }

    public void playerActivated(Player turn, Player active) {
        client.getGridPanel().repaint(); // players only
    }

    public void closeGame() {
        clearActions();
        canPass = false;
        refreshComponents();
    }

}
