package com.jcloisterzone.ui.grid.layer;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.util.Set;

import com.jcloisterzone.board.Position;
import com.jcloisterzone.figure.neutral.Dragon;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.ui.grid.GridMouseListener;
import com.jcloisterzone.ui.grid.GridPanel;


public class DragonAvailableMove extends AbstractGridLayer implements GridMouseListener {

    private Set<Position> positions;
    private Position selected;

    public DragonAvailableMove(GridPanel gridPanel, GameController gc) {
        super(gridPanel, gc);
    }

    public void setPositions(Set<Position> positions) {
        this.positions = positions;
    }

    public void paint(Graphics2D g2) {
        if (positions == null) return;
        Image dragon = getClient().getControlsTheme().getActionDecoration("dragon");
        for (Position pos : positions) {
            g2.drawImage(dragon, getOffsetX(pos), getOffsetY(pos), getSquareSize(), getSquareSize(), null);
        }
    }

    @Override
    public void squareEntered(MouseEvent e, Position p) {
        selected = p;
    }

    @Override
    public void squareExited(MouseEvent e, Position p) {
        selected = null;

    }

    @Override
    public void mouseClicked(MouseEvent e, Position p) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            if (positions.contains(selected)) {
                e.consume();
                getRmiProxy().moveNeutralFigure(selected.asFeaturePointer(), Dragon.class);
            }
        }
    }


}