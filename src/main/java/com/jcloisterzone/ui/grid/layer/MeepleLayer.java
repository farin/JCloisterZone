package com.jcloisterzone.ui.grid.layer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.ImageIcon;

import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.MeepleEvent;
import com.jcloisterzone.feature.Bridge;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.figure.BigFollower;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.figure.SmallFollower;
import com.jcloisterzone.ui.GameController;
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

    public MeepleLayer(GridPanel gridPanel, GameController gc) {
        super(gridPanel, gc);
    }

    private void paintPositionedImage(Graphics2D g, PositionedImage mi, int boxSize) {
        ImmutablePoint scaledOffset = mi.getScaledOffset(boxSize);
        //TODO optimize also for scrolling
        if (mi.scaledImage == null) {
            int width = (int) (getSquareSize() * FIGURE_SIZE_RATIO * mi.xScaleFactor);
            int height = (int) (mi.heightWidthRatio * width * mi.yScaleFactor);
            Image img = mi.sourceImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            mi.scaledImage = new ImageIcon(img).getImage();
        }
        int x = getOffsetX(mi.position) + scaledOffset.getX();
        int y = getOffsetY(mi.position) + scaledOffset.getY();
        g.rotate(-gridPanel.getBoardRotation().getTheta(), x+boxSize/2, y+boxSize/2);
        g.drawImage(mi.scaledImage, x, y, gridPanel);
        g.rotate(gridPanel.getBoardRotation().getTheta(), x+boxSize/2, y+boxSize/2);
    }

    @Override
    public void paint(Graphics2D g) {
        int boxSize = (int) (getSquareSize() * FIGURE_SIZE_RATIO); //TODO no resize - direct image resize???
        for (MeeplePositionedImage mi : images) {
            if (!mi.bridgePlacement) {
                paintPositionedImage(g, mi, boxSize);
            }
        }
        for (PositionedImage mi : permanentImages) {
            paintPositionedImage(g, mi, boxSize);
        }

    }

    public void paintMeeplesOnBridges(Graphics2D g) {
        int boxSize = (int) (getSquareSize() * FIGURE_SIZE_RATIO); //TODO no resize - direct image resize???
        for (MeeplePositionedImage mi : images) {
            if (mi.bridgePlacement) {
                paintPositionedImage(g, mi, boxSize );
            }
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

    private MeeplePositionedImage createMeepleImage(Class<? extends Meeple> type, Color c, FeaturePointer fp) {
        Feature feature = getGame().getBoard().get(fp);
        ImmutablePoint offset = getClient().getResourceManager().getMeeplePlacement(feature.getTile(), type, fp.getLocation());
        Image image = getClient().getFigureTheme().getFigureImage(type, c, getExtraDecoration(type, fp));
        if (fp.getLocation() == Location.ABBOT) {
            image = rotate(image, 90);
        }
        return new MeeplePositionedImage(type, fp, offset, image, feature instanceof Bridge);
    }

    private void rearrangeMeeples(FeaturePointer fp) {
        int order = 0;
        //small followers first
        for (MeeplePositionedImage mi : images) {
            if (mi.location == fp.getLocation() && mi.position.equals(fp.getPosition())) {
                if (mi.meepleType.equals(SmallFollower.class)) {
                    mi.order = order++;
                }
            }
        }
        //others on top
        for (MeeplePositionedImage mi : images) {
            if (mi.location == fp.getLocation() && mi.position.equals(fp.getPosition())) {
                if (!mi.meepleType.equals(SmallFollower.class)) {
                    mi.order = order++;
                }
            }
        }
    }

    public void meepleDeployed(MeepleEvent ev) {
        Color c = ev.getMeeple().getPlayer().getColors().getMeepleColor();
        images.add(createMeepleImage(ev.getMeeple().getClass(), c, ev.getTo()));
        rearrangeMeeples(ev.getTo());
    }

    public void meepleUndeployed(MeepleEvent ev) {
        Iterator<MeeplePositionedImage> iter = images.iterator();
        while (iter.hasNext()) {
            MeeplePositionedImage mi = iter.next();
            if (mi.match(ev.getMeeple().getClass(), ev.getFrom())) {
                iter.remove();
                break;
            }
        }
        rearrangeMeeples(ev.getFrom());
    }

    public void addPermanentImage(Position position, ImmutablePoint offset, Image image) {
    	addPermanentImage(position, offset, image, 1.0, 1.0);
    }


    //TODO hack with xScale, yScale - clean and do better
    public void addPermanentImage(Position position, ImmutablePoint offset, Image image, double xScale, double yScale) {
    	PositionedImage pi = new PositionedImage(position, offset, image);
    	pi.heightWidthRatio = image.getHeight(null) / image.getWidth(null);
    	pi.xScaleFactor = xScale;
    	pi.yScaleFactor = yScale;
        permanentImages.add(pi);
    }

    //TODO path from Theme
    public String getExtraDecoration(Class<? extends Meeple> type, FeaturePointer fp) {
        if (Follower.class.isAssignableFrom(type) && fp.getLocation().isFarmLocation()) {
            return "farm.png";
        }
        if (fp.getLocation() == Location.TOWER) {
            if (BigFollower.class.isAssignableFrom(type)) {
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
        public double heightWidthRatio = 1.0;
        public double xScaleFactor = 1.0;
        public double yScaleFactor = 1.0;

        public PositionedImage(Position position, ImmutablePoint offset, Image sourceImage) {
            this.position = position;
            this.offset = offset;
            this.sourceImage = sourceImage;
        }

        public ImmutablePoint getScaledOffset(int boxSize) {
            return offset.scale(getSquareSize(), boxSize);
        }
    }

    private class MeeplePositionedImage extends PositionedImage {
         public final Class<? extends Meeple> meepleType;
         public final Location location;
         public final boolean bridgePlacement;
         public int order;

         public MeeplePositionedImage(Class<? extends Meeple> meepleType, FeaturePointer fp, ImmutablePoint offset, Image sourceImage, boolean bridgePlacement) {
             super(fp.getPosition(), offset, sourceImage);
             this.meepleType = meepleType;
             location = fp.getLocation();
             this.bridgePlacement = bridgePlacement;
         }

         @Override
		public ImmutablePoint getScaledOffset(int boxSize) {
             ImmutablePoint point = offset;
             if (order > 0) {
                 point = point.translate(10*order, 0);
             }
             return point.scale(getSquareSize(), boxSize);
         }

         public boolean match(Class<? extends Meeple> meepleType, FeaturePointer fp) {
             if (!meepleType.equals(this.meepleType)) return false;
             if (location != fp.getLocation()) return false;
             if (!position.equals(fp.getPosition())) return false;
             return true;
         }
    }

    //TODO better use affine transform while drawing
    @Deprecated
    public static Image rotate(Image img, double angle) {
        double sin = Math.abs(Math.sin(Math.toRadians(angle))),
               cos = Math.abs(Math.cos(Math.toRadians(angle)));

        int w = img.getWidth(null), h = img.getHeight(null);

        int neww = (int) Math.floor(w*cos + h*sin),
            newh = (int) Math.floor(h*cos + w*sin);

        BufferedImage bimg = new BufferedImage(neww, newh, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bimg.createGraphics();

        g.translate((neww-w)/2, (newh-h)/2);
        g.rotate(Math.toRadians(angle), w/2, h/2);
        g.drawRenderedImage(toBufferedImage(img), null);
        g.dispose();
        return bimg;
    }

    private static BufferedImage toBufferedImage(Image img)
    {
        if (img instanceof BufferedImage)
        {
            return (BufferedImage) img;
        }

        // Create a buffered image with transparency
        BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

        // Draw the image on to the buffered image
        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();

        // Return the buffered image
        return bimage;
    }

}