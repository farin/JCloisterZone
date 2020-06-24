package com.jcloisterzone.ui.grid.layer;

import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;

import com.jcloisterzone.action.SelectFollowerAction;
import com.jcloisterzone.board.pointer.BoardPointer;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.board.pointer.MeeplePointer;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.ui.ImmutablePoint;
import com.jcloisterzone.ui.grid.GridPanel;
import com.jcloisterzone.ui.resources.FeatureArea;
import com.jcloisterzone.ui.resources.ResourceManager;

import io.vavr.Tuple2;
import io.vavr.collection.HashMap;
import io.vavr.collection.Map;
import io.vavr.collection.Set;
import io.vavr.collection.Stream;


public class FollowerAreaLayer extends AbstractAreaLayer {

    private static int REF_SIZE = 100;

    private final MeepleLayer meepleLayer;

    public FollowerAreaLayer(GridPanel gridPanel, GameController gc, MeepleLayer meepleLayer) {
        super(gridPanel, gc);
        this.meepleLayer = meepleLayer;
    }

    @Override
    public SelectFollowerAction getAction() {
        return (SelectFollowerAction) super.getAction();
    }

    @Override
    protected Map<BoardPointer, FeatureArea> prepareAreas() {
        SelectFollowerAction action = getAction();
        int r = (int) (REF_SIZE / 3.0);
        int innerR = (int) (REF_SIZE / 4.2);

        Map<BoardPointer, FeatureArea> result = HashMap.empty();
        Map<String, Stream<FigureImage>> images = meepleLayer.getAllFigureImages()
            .filter(fi -> fi.getFigure() instanceof Meeple)
            .groupBy(fi -> fi.getFigure().getId());

        for (Tuple2<FeaturePointer, Set<MeeplePointer>> t : action.groupByFeaturePointer()) {
            int order = 0;
            for (MeeplePointer pointer : t._2) {
                FigureImage pfi = images.get(pointer.getMeepleId()).getOrElse(Stream.empty()).getOrNull();

                if (pfi != null) {
                    ImmutablePoint offset = pfi.getOffset();
                    int cx = offset.getX();
                    int cy = offset.getY();

                    Area trackingArea = new Area(new Ellipse2D.Double(cx-r, cy-r, 2*r, 2*r));
                    Area displayArea = new Area(trackingArea);
                    displayArea.subtract(new Area(new Ellipse2D.Double(cx-innerR, cy-innerR, 2*innerR, 2*innerR)));

                    //TODO IMMUTABLE
                    if (order > 0) {
                        //more then one meeple on feature, remove part of are over prev meeple
                        int subWidth = r*4/5;
                        trackingArea.subtract(new Area(new Rectangle(cx-r, cy-r, subWidth, 2*r)));
                    }

                    FeatureArea fa = new FeatureArea(trackingArea, displayArea, order);
                    fa = fa.setForceAreaColor(((Meeple) pfi.getFigure()).getPlayer().getColors().getMeepleColor());

                    int scale = ResourceManager.NORMALIZED_SIZE / REF_SIZE;
                    AffineTransform translation = AffineTransform.getScaleInstance(scale, scale);
                    FeatureArea translated = fa.transform(translation);
                    result = result.put(pointer, translated);
                    order++;
                }
            }
        }
        return result;
    }

    @Override
    protected void performAction(BoardPointer ptr) {
        if (!gc.getActionLock().get()) {
            gc.getConnection().send(getAction().select((MeeplePointer) ptr));
        }
    }
}
