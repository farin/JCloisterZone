package com.jcloisterzone.ui.grid.layer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;

import com.jcloisterzone.board.Position;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.ui.ImmutablePoint;
import com.jcloisterzone.ui.grid.GridPanel;


public class DragonLayer extends AbstractTileLayer {

    protected static final String DRAGON_IMAGE_NAME = "dragon";
    private static final ImmutablePoint POINT = new ImmutablePoint(45,50);

    private int moves;
    private Image dragonImage;

    public DragonLayer(GridPanel gridPanel, GameController gc) {
        super(gridPanel, gc);
        dragonImage = getClient().getFigureTheme().getNeutralImage(DRAGON_IMAGE_NAME);
    }


    public void paint(Graphics2D g2) {
        Position pos = getPosition();
        if (pos != null) {
            drawImageIgnoringRotation(g2, dragonImage, pos, 0, 0, getSquareSize(), getSquareSize());
            if (moves > 0) {
                //tohle asi nebude uplne uprostred
                drawAntialiasedTextCentered(g2, moves + "", 22, POINT.rotate(gridPanel.getBoardRotation().inverse()), Color.WHITE, null);
            }

        }
    }

    @Override
    public void zoomChanged(int squareSize) {
        super.zoomChanged(squareSize);
    }


    public void setMoves(int moves) {
        this.moves = moves;

    }
}
