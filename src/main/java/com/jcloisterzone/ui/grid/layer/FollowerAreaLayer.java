package com.jcloisterzone.ui.grid.layer;

import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.util.HashMap;
import java.util.Map;

import com.jcloisterzone.action.SelectFollowerAction;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.pointer.BoardPointer;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.board.pointer.MeeplePointer;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.ui.ImmutablePoint;
import com.jcloisterzone.ui.grid.ActionLayer;
import com.jcloisterzone.ui.grid.GridPanel;
import com.jcloisterzone.ui.grid.layer.MeepleLayer.PositionedFigureImage;
import com.jcloisterzone.ui.resources.FeatureArea;


public class FollowerAreaLayer extends AbstractAreaLayer implements ActionLayer<SelectFollowerAction> {

    private SelectFollowerAction action;
    private final MeepleLayer meepleLayer;

    public FollowerAreaLayer(GridPanel gridPanel, GameController gc, MeepleLayer meepleLayer) {
        super(gridPanel, gc);
        this.meepleLayer = meepleLayer;
    }

    @Override
    public void setAction(boolean active, SelectFollowerAction action) {
        this.action = action;
        setActive(active);
    }

    @Override
    public SelectFollowerAction getAction() {
        return action;
    }


    @Override
    protected Map<BoardPointer, FeatureArea> prepareAreas(Tile tile, Position p) {
        int r = (int) (getTileWidth() / 3.0);
        int innerR = (int) (getTileWidth() / 4.2);
        int boxSize = (int) (getTileWidth() * MeepleLayer.FIGURE_SIZE_RATIO);

        Map<BoardPointer, FeatureArea> areas = new HashMap<>();
        for (MeeplePointer pointer : action.getMeeplePointers(p)) {
            PositionedFigureImage pfi = null;
            for (PositionedFigureImage item : meepleLayer.getPositionedFigures()) {
                if (item.getFigure() instanceof Meeple) {
                    Meeple meeple = (Meeple) item.getFigure();
                    if (pointer.match(meeple)) {
                        pfi = item;
                        break;
                    }
                }
            }
            if (pfi != null) {
                ImmutablePoint offset = pfi.getScaledOffset(boxSize);
                int x = offset.getX();
                int y = offset.getY();
                int width = (int) (getTileWidth() * MeepleLayer.FIGURE_SIZE_RATIO * pfi.xScaleFactor);
                int height = (int) (pfi.heightWidthRatio * width * pfi.yScaleFactor);
                int cx = x+(width/2);
                int cy = y+(height/2);

                Area trackingArea = new Area(new Ellipse2D.Double(cx-r, cy-r, 2*r, 2*r));
                Area displyArea = new Area(trackingArea);
                displyArea.subtract(new Area(new Ellipse2D.Double(cx-innerR, cy-innerR, 2*innerR, 2*innerR)));
                if (pfi.order > 0) {
                    //more then one meeple on feature, remove part of are over prev meeple
                    int subWidth = r*4/5;
                    trackingArea.subtract(new Area(new Rectangle(cx-r, cy-r, subWidth, 2*r)));
                }

                FeatureArea fa = new FeatureArea(trackingArea, displyArea, pfi.order);
                fa.setForceAreaColor(((Meeple) pfi.getFigure()).getPlayer().getColors().getMeepleColor());
                areas.put(pointer, fa);
            }
        }
        return areas;
    }


    @Override
    protected void performAction(BoardPointer ptr) {
        action.perform(getRmiProxy(), (MeeplePointer) ptr);
    }


}
