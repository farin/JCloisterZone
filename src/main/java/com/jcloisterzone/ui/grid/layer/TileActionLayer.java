package com.jcloisterzone.ui.grid.layer;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.MouseEvent;

import com.jcloisterzone.action.SelectTileAction;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.ui.grid.GridMouseListener;
import com.jcloisterzone.ui.grid.GridPanel;


public class TileActionLayer extends AbstractGridLayer implements GridMouseListener {

    private final SelectTileAction action;
    private final Image gridDecoration;

    public TileActionLayer(GridPanel gridPanel, SelectTileAction action, Image gridDecoration) {
        super(gridPanel);
        this.action = action;
        this.gridDecoration = gridDecoration;
    }

    @Override
    public int getZIndex() {
        return 70;
    }

    public void paint(Graphics2D g2) {
        int imgSize = gridDecoration.getWidth(null);
        for (Position pos : action.getSites()) {
            g2.drawImage(gridDecoration, getAffineTransform(imgSize, pos), null);
        }
    }

    @Override
    public void mouseClicked(MouseEvent e, Position p) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            if (action.getSites().contains(p)) {
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
