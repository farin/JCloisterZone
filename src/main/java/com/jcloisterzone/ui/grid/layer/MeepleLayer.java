package com.jcloisterzone.ui.grid.layer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import javax.swing.ImageIcon;

import com.google.common.collect.Maps;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.feature.Farm;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.Tower;
import com.jcloisterzone.figure.BigFollower;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.figure.SmallFollower;
import com.jcloisterzone.ui.ImmutablePoint;
import com.jcloisterzone.ui.grid.GridPanel;

public class MeepleLayer extends AbstractGridLayer {

    public static final double FIGURE_SIZE_RATIO = 0.35;

    /**
     * Corn circles allows multiple meeples on single feature.
     * In such case double meeple should be displayed after common ones.
     */
    private LinkedHashMap<Meeple, PositionedImage> images = Maps.newLinkedHashMap();
    //TODO own layer ???
    private List<PositionedImage> permanentImages = new ArrayList<>();

    public MeepleLayer(GridPanel gridPanel) {
        super(gridPanel);
    }

    @Override
    public int getZIndex() {
        return 50;
    }

    private void paintPositionedImage(Graphics2D g, PositionedImage mi, int boxSize) {
        ImmutablePoint scaledOffset = mi.offset.scale(getSquareSize(), boxSize);
        //TODO optimize also for scrolling
        if (mi.scaledImage == null) {
            int size = (int) (getSquareSize() * FIGURE_SIZE_RATIO);
            Image img = mi.sourceImage.getScaledInstance(size, size, Image.SCALE_SMOOTH);
            mi.scaledImage = new ImageIcon(img).getImage();
        }
        g.drawImage(mi.scaledImage, getOffsetX(mi.position) + scaledOffset.getX(), getOffsetY(mi.position) + scaledOffset.getY(), gridPanel);
    }

    @Override
    public void paint(Graphics2D g) {
        int boxSize = (int) (getSquareSize() * FIGURE_SIZE_RATIO); //TODO no resize - direct image resize???
        for (PositionedImage mi : images.values()) {
            paintPositionedImage(g, mi, boxSize );
        }
        for (PositionedImage mi : permanentImages) {
            paintPositionedImage(g, mi, boxSize );
        }

    }

    @Override
    public void zoomChanged(int squareSize) {
        for (PositionedImage mi : images.values()) {
            mi.scaledImage = null;
        }
        for (PositionedImage mi : permanentImages) {
            mi.scaledImage = null;
        }
        super.zoomChanged(squareSize);
    }

    private PositionedImage createMeepleImage(Meeple m, int order) {
        Feature feature = m.getFeature();
        ImmutablePoint offset = getClient().getResourceManager().getMeeplePlacement(feature.getTile(), m.getClass(), m.getLocation());
        if (order > 0) {
            offset = new ImmutablePoint(offset.getX() + 10*order, offset.getY());
        }
        Color c = getClient().getPlayerColor(m.getPlayer());
        Image image = getClient().getFigureTheme().getFigureImage(m.getClass(), c,  getExtraDecoration(m));
        return new PositionedImage(m.getPosition(), offset, image);
    }

    /**
     * recompute offset, keep big follower on top
     */
    private void rearrangeMeeples(final Feature feature) {
        for (Meeple m : feature.getMeeples()) {
            images.remove(m);
        }

        int i = 0;
        //clone meeples to freeze its state
        for (Meeple m : feature.getMeeples()) {
            if (m instanceof SmallFollower) {
                Meeple c = (Meeple) m.clone();
                if (c.getPosition() == null) continue; //synchronization issue, because reading directly from Feature, not from args passed to ui layer
                images.put(c, createMeepleImage(c, i++));
            }

        }
        for (Meeple m : feature.getMeeples()) {
            if (!(m instanceof SmallFollower)) {
                Meeple c = (Meeple) m.clone();
                if (c.getPosition() == null) continue; //synchronization issue, because reading directly from Feature, not from args passed to ui layer
                images.put(c, createMeepleImage(c, i++));
            }
        }
    }

    public void meepleDeployed(Meeple m) {
        assert !images.containsKey(m);
        rearrangeMeeples(m.getFeature());
    }

    public void meepleUndeployed(Meeple m) {
        if (m.getFeature() != null) {
            images.remove(m);
            rearrangeMeeples(m.getFeature());
        }
    }

    public void addPermanentImage(Position position, ImmutablePoint offset, Image image) {
        permanentImages.add(new PositionedImage(position, offset, image));
    }


    //TODO path from Theme
    public String getExtraDecoration(Meeple m) {
        if (m instanceof Follower && m.getFeature() instanceof Farm) {
            return "farm.png";
        }
        if (m.getFeature() instanceof Tower) {
            if (m instanceof BigFollower) {
                return "big_tower.png";
            } else {
                return "tower.png";
            }
        }
        return null;
    }

    private static class PositionedImage {
        public final Position position;
        public final ImmutablePoint offset;
        public final Image sourceImage;
        public Image scaledImage;


        public PositionedImage(Position position, ImmutablePoint offset, Image sourceImage) {
            this.position = position;
            this.offset = offset;
            this.sourceImage = sourceImage;
        }
    }
}