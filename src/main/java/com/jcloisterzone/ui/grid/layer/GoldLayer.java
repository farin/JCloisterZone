package com.jcloisterzone.ui.grid.layer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.jcloisterzone.board.Position;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.ui.ImmutablePoint;
import com.jcloisterzone.ui.grid.GridPanel;

public class GoldLayer extends AbstractGridLayer {

    private final Image goldImage;
    private final double widthHeightRatio;
    private final Map<Position, Integer> placedGold = new HashMap<>();

    private final static Color FILL_COLOR = new Color(40,40,40,150);

    public GoldLayer(GridPanel gridPanel, GameController gc) {
        super(gridPanel, gc);
        goldImage = getClient().getFigureTheme().getNeutralImage("gold");
        widthHeightRatio = goldImage.getWidth(null) / (double) goldImage.getHeight(null);
    }

    public void setGoldCount(Position pos, int count) {
        if (count == 0) {
            placedGold.remove(pos);
        } else {
            placedGold.put(pos, count);
        }
    }

    @Override
    public void paint(Graphics2D g2) {
        int size = getSquareSize();
        int w = (int)(size*0.4);
        int h = (int)(w / widthHeightRatio);
        g2.setColor(FILL_COLOR);
        for (Entry<Position, Integer> entry : placedGold.entrySet()) {
            Position pos = entry.getKey();
            int x = getOffsetX(pos) + (int)(size*0.45);
            int y = getOffsetY(pos);
            g2.fillRect(x, y, w + (int)(size*0.15), h);
            g2.drawImage(goldImage, x, y, w, h, null);
            drawAntialiasedTextCentered(g2, ""+entry.getValue(), 20, entry.getKey(), new ImmutablePoint(90,10), Color.WHITE, null);
        }

    }

}
