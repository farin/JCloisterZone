package com.jcloisterzone.ui.grid.layer;

import java.awt.Graphics2D;
import java.awt.Image;

import com.jcloisterzone.board.Position;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.ui.grid.GridPanel;


public class FairyLayer extends AbstractTileLayer {

    protected static final String FAIRY_IMAGE_NAME = "fairy";

    private Image fairyImage;

    public FairyLayer(GridPanel gridPanel, GameController gc) {
        super(gridPanel, gc);
        fairyImage = getClient().getFigureTheme().getNeutralImage(FAIRY_IMAGE_NAME);
    }

    public void paint(Graphics2D g2) {
        Position pos = getPosition();
        if (pos != null) {
            drawImageIgnoringRotation(g2, fairyImage, pos, 0, 0, getSquareSize(), getSquareSize());
        }
    }
}
