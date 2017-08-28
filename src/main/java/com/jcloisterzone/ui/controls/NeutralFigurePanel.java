package com.jcloisterzone.ui.controls;

import static com.jcloisterzone.ui.controls.ControlPanel.CORNER_DIAMETER;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;

import com.jcloisterzone.figure.neutral.Dragon;
import com.jcloisterzone.figure.neutral.Fairy;
import com.jcloisterzone.figure.neutral.Mage;
import com.jcloisterzone.figure.neutral.NeutralFigure;
import com.jcloisterzone.figure.neutral.Witch;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.capability.DragonCapability;
import com.jcloisterzone.game.capability.FairyCapability;
import com.jcloisterzone.game.capability.MageAndWitchCapability;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.NeutralFiguresState;
import com.jcloisterzone.ui.Client;
import com.jcloisterzone.ui.UiUtils;

import io.vavr.collection.Seq;

public class NeutralFigurePanel extends JComponent {

    private static final int PADDING_L = 9;
    private static final int PADDING_R = 11;
    private static final int LINE_HEIGHT = 32;

    private int PANEL_WIDTH = 1; //TODO clean, it's not constant now

    private BufferedImage bimg;
    private Graphics2D g2;

    private final Client client;
    private final Game game;
    private final PlayerPanelImageCache cache;

    private int realHeight = 1;
    private int bx = 0, by = 0;

    public NeutralFigurePanel(Client client, Game game, PlayerPanelImageCache cache) {
        this.client = client;
        this.game = game;
        this.cache = cache;
    }

    private Rectangle drawMeepleBox(Graphics2D g2, String imgKey) {
        return drawMeepleBox(g2, imgKey, 0, 0);
    }


    private Rectangle drawMeepleBox(Graphics2D g2, String imgKey, int offsetx, int offsety) {
        g2.setColor(Color.WHITE);

        int w = 30;
        int h = 22;
        if (bx+w > PANEL_WIDTH-PADDING_R-PADDING_L) {
            bx = PADDING_L;
            by += LINE_HEIGHT;
        }
        g2.fillRoundRect(bx, by, w, h, 8, 8);
        g2.drawImage(cache.get(null, imgKey), bx+offsetx, by-4+offsety, null);

        Rectangle rect = null;

        bx += w + 8;
        return rect;
    }


    public boolean repaintContent(int width) {
        PANEL_WIDTH = width;

        bimg = UiUtils.newTransparentImage(PANEL_WIDTH, 200);
        g2 = bimg.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        bx = PADDING_L;
        by = PADDING_L;

        GameState state = game.getState();

        NeutralFiguresState nfState = state.getNeutralFigures();

        if (nfState.getDragon() != null && nfState.getDragonDeployment() == null) {
            drawMeepleBox(g2, "dragon", -7, -6);
        }
        if (nfState.getFairy() != null && nfState.getFairyDeployment() == null) {
            drawMeepleBox(g2, "fairy");
        }
        if (nfState.getMage() != null && nfState.getMageDeployment() == null) {
            drawMeepleBox(g2, "mage");
        }
        if (nfState.getWitch() != null && nfState.getWitchDeployment() == null) {
            drawMeepleBox(g2, "witch");
        }

        //TODO show unassigned gold mines
        //TODO show unassigned TB tokens
        //TODO show king and robber

        int oldValue = realHeight;

        realHeight = by + (bx > PADDING_L ? LINE_HEIGHT : 0);
        if (realHeight == PADDING_L) realHeight = 0;

        g2.dispose();
        g2 = null;

        return realHeight != oldValue;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(PANEL_WIDTH, realHeight);
    }

    @Override
    public void paint(Graphics g) {
        if (realHeight > 0) {
            Graphics2D parentGraphics = (Graphics2D) g;
            parentGraphics.setColor(client.getTheme().getPlayerBoxBg());
            parentGraphics.fillRoundRect(0, 0, PANEL_WIDTH+CORNER_DIAMETER, realHeight, CORNER_DIAMETER, CORNER_DIAMETER);
            parentGraphics.drawImage(bimg, 0, 0, PANEL_WIDTH, realHeight, 0, 0, PANEL_WIDTH, realHeight, null);
        }
        super.paintComponent(g);
    }
}
