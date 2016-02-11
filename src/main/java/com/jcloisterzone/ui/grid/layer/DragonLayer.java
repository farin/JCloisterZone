package com.jcloisterzone.ui.grid.layer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.util.Set;

import com.google.common.eventbus.Subscribe;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.event.NeutralFigureMoveEvent;
import com.jcloisterzone.event.SelectDragonMoveEvent;
import com.jcloisterzone.figure.neutral.Dragon;
import com.jcloisterzone.figure.neutral.NeutralFigure;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.ui.ImmutablePoint;
import com.jcloisterzone.ui.grid.GridMouseListener;
import com.jcloisterzone.ui.grid.GridPanel;


public class DragonLayer extends AbstractGridLayer implements GridMouseListener {

    private static final ImmutablePoint POINT = new ImmutablePoint(45,50);

    private Position dragonPosition;
    private Set<Position> availablePositions;
    private int movesLeft = 0;

    public DragonLayer(GridPanel gridPanel, GameController gc) {
        super(gridPanel, gc);
        gc.register(this);
        toggleVisibility();
    }

    @Override
    public void paint(Graphics2D g2) {

        if (dragonPosition == null || movesLeft == 0) return;

        // paint available moves
        if (availablePositions != null) {
        	Image availableDragonMove = rm.getImage("decorations/dragon");
            for (Position pos : availablePositions) {
                g2.drawImage(availableDragonMove, getOffsetX(pos), getOffsetY(pos), getSquareSize(), getSquareSize(), null);
            }
        }

        // paint remaining moves
        // It probably will not be completely in the middle
        drawAntialiasedTextCentered(g2, String.valueOf(movesLeft), 22, dragonPosition, POINT.rotate100(gridPanel.getBoardRotation().inverse()), Color.WHITE, null);
    }

    @Subscribe
    public void onSelectDragonMoveEvent(SelectDragonMoveEvent ev) {
	if (ev.getTargetPlayer().isLocalHuman()) {
	    availablePositions = ev.getPositions();
	} else {
	    availablePositions = null;
	}

        movesLeft = ev.getMovesLeft();

        // set visibility
        toggleVisibility();
    }

    @Subscribe
    public void onNeutralFigureMoveEvent(NeutralFigureMoveEvent ev) {
        NeutralFigure<?> fig = ev.getFigure();
        if (fig instanceof Dragon) {
            dragonPosition = ev.getTo().getPosition();

            // reset positions & movesLeft after Dragon movement
            availablePositions = null;
            movesLeft = 0;

            // set visibility
            toggleVisibility();
        }
    }

    private void toggleVisibility() {
	if (dragonPosition == null || movesLeft == 0) {
	    onHide();
	} else {
	    onShow();
	}
    }

    @Override
    public void squareEntered(MouseEvent e, Position p) {
	// useless
    }

    @Override
    public void squareExited(MouseEvent e, Position p) {
	// useless
    }

    @Override
    public void mouseClicked(MouseEvent e, Position p) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            if (availablePositions != null && availablePositions.contains(p)) {
                e.consume();
                getRmiProxy().moveNeutralFigure(p.asFeaturePointer(), Dragon.class);
            }
        }
    }


}