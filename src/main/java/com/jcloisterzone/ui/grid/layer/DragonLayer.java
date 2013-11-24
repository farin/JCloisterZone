package com.jcloisterzone.ui.grid.layer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;

import com.jcloisterzone.board.Position;
import com.jcloisterzone.ui.ImmutablePoint;
import com.jcloisterzone.ui.grid.GridPanel;


public class DragonLayer extends AbstractTileLayer {

    protected static final String DRAGON_IMAGE_NAME = "dragon";
    private static final ImmutablePoint POINT = new ImmutablePoint(45,50);

    private int moves;
    private Image dragonImage;



    public DragonLayer(GridPanel gridPanel, Position position) {
        super(gridPanel, position);
        dragonImage = getClient().getFigureTheme().getNeutralImage(DRAGON_IMAGE_NAME);
    }

    public void paint(Graphics2D g2) {
        if (getPosition() != null) {
            g2.drawImage(dragonImage, getOffsetX(), getOffsetY(), getSquareSize(), getSquareSize(), null);
            if (moves > 0) {
                //tohle asi nebude uplne uprostred
                drawAntialiasedTextCentered(g2, moves + "", 22, POINT, Color.WHITE, null);
            }
        }
    }

    @Override
    public void zoomChanged(int squareSize) {
        super.zoomChanged(squareSize);
    }


    @Override
    public int getZIndex() {
        return 90;
    }

    public void setMoves(int moves) {
        this.moves = moves;

    }


}
