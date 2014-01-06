package com.jcloisterzone.ui.grid.layer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.ImageIcon;

import com.jcloisterzone.board.Location;
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
    private LinkedList<MeeplePositionedImage> images = new LinkedList<>();
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
        ImmutablePoint scaledOffset = mi.getScaledOffset(boxSize);
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
        for (PositionedImage mi : images) {
            paintPositionedImage(g, mi, boxSize );
        }
        for (PositionedImage mi : permanentImages) {
            paintPositionedImage(g, mi, boxSize );
        }

    }

    @Override
    public void zoomChanged(int squareSize) {
        for (MeeplePositionedImage mi : images) {
            mi.scaledImage = null;
        }
        for (PositionedImage mi : permanentImages) {
            mi.scaledImage = null;
        }
        super.zoomChanged(squareSize);
    }

    private MeeplePositionedImage createMeepleImage(Meeple m) {
        Feature feature = m.getFeature();
        ImmutablePoint offset = getClient().getResourceManager().getMeeplePlacement(feature.getTile(), m.getClass(), m.getLocation());
        Color c = m.getPlayer().getColors().getMeepleColor();
        Image image = getClient().getFigureTheme().getFigureImage(m.getClass(), c,  getExtraDecoration(m));
        return new MeeplePositionedImage(m, offset, image);
    }

    private void rearrangeMeeples(Position p, Location loc) {
        int order = 0;
        //small followers first
        for (MeeplePositionedImage mi : images) {
            if (mi.location == loc && mi.position.equals(p)) {
                if (mi.meepleType.equals(SmallFollower.class)) {
                    mi.order = order++;
                }
            }
        }
        //others on top
        for (MeeplePositionedImage mi : images) {
            if (mi.location == loc && mi.position.equals(p)) {
                if (!mi.meepleType.equals(SmallFollower.class)) {
                    mi.order = order++;
                }
            }
        }
    }

    public void meepleDeployed(Meeple m) {
        images.add(createMeepleImage(m));
        rearrangeMeeples(m.getPosition(), m.getLocation());
    }

    public void meepleUndeployed(Meeple m) {
        if (m.getFeature() != null) {
            Iterator<MeeplePositionedImage> iter = images.iterator();
            while (iter.hasNext()) {
                MeeplePositionedImage mi = iter.next();
                if (mi.match(m)) {
                    iter.remove();
                    break;
                }
            }
            rearrangeMeeples(m.getPosition(), m.getLocation());
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

    private class PositionedImage {
        public final Position position;
        public final ImmutablePoint offset;
        public final Image sourceImage;
        public Image scaledImage;

        public PositionedImage(Position position, ImmutablePoint offset, Image sourceImage) {
            this.position = position;
            this.offset = offset;
            this.sourceImage = sourceImage;
        }

        public ImmutablePoint getScaledOffset(int boxSize) {
            return offset.scale(getSquareSize(), boxSize);
        }
    }

    private class MeeplePositionedImage extends PositionedImage{
         public final Class<? extends Meeple> meepleType;
         public final Location location;
         public int order;

         public MeeplePositionedImage(Meeple m, ImmutablePoint offset, Image sourceImage) {
             super(m.getPosition(), offset, sourceImage);
             meepleType = m.getClass();
             location = m.getLocation();
         }

         public ImmutablePoint getScaledOffset(int boxSize) {
             ImmutablePoint point = offset;
             if (order > 0) {
                 point = point.translate(10*order, 0);
             }
             return point.scale(getSquareSize(), boxSize);
         }

         public boolean match(Meeple m) {
             if (!m.getClass().equals(meepleType)) return false;
             if (location != m.getLocation()) return false;
             if (!position.equals(m.getPosition())) return false;
             return true;
         }
    }

}