package com.jcloisterzone.ui.grid.layer;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.MouseEvent;

import com.jcloisterzone.action.FairyAction;
import com.jcloisterzone.action.SelectTileAction;
import com.jcloisterzone.action.TowerPieceAction;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.ui.grid.ActionLayer;
import com.jcloisterzone.ui.grid.GridMouseListener;
import com.jcloisterzone.ui.grid.GridPanel;


public class TileActionLayer extends AbstractGridLayer implements GridMouseListener, ActionLayer<SelectTileAction> {

    private SelectTileAction action;
    private boolean active;
    private Image gridDecoration;

    public TileActionLayer(GridPanel gridPanel) {
        super(gridPanel);
    }

    @Override
    public void setAction(boolean active, SelectTileAction action) {
        this.action = action;
        this.active = active;
        if (action == null) {
            gridDecoration = null;
        } else if (action instanceof FairyAction) {
            gridDecoration = getClient().getControlsTheme().getActionDecoration("fairy");
        } else if (action instanceof TowerPieceAction) {
            gridDecoration = getClient().getControlsTheme().getActionDecoration("tower");
        }
    }

    @Override
    public SelectTileAction getAction() {
        return action;
    }

    public void paint(Graphics2D g2) {
        int imgSize = gridDecoration.getWidth(null);
        for (Position pos : action.getOptions()) {
            g2.drawImage(gridDecoration, getAffineTransform(imgSize, pos), null);
        }
    }

    @Override
    public void mouseClicked(MouseEvent e, Position p) {
        if (!active) return;
        if (e.getButton() == MouseEvent.BUTTON1) {
            if (action.getOptions().contains(p)) {
                e.consume();
                action.perform(getClient().getServer(), p);
            }
        }
    }


    @Override
    public void squareEntered(MouseEvent e, Position p) { }
    @Override
    public void squareExited(MouseEvent e, Position p) {  }


}
