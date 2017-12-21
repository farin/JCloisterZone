package com.jcloisterzone.ui.grid.layer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import com.google.common.eventbus.Subscribe;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.pointer.BoardPointer;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.GameChangedEvent;
import com.jcloisterzone.feature.Bridge;
import com.jcloisterzone.figure.Barn;
import com.jcloisterzone.figure.BigFollower;
import com.jcloisterzone.figure.Figure;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.figure.neutral.Count;
import com.jcloisterzone.figure.neutral.Fairy;
import com.jcloisterzone.figure.neutral.Mage;
import com.jcloisterzone.figure.neutral.NeutralFigure;
import com.jcloisterzone.figure.neutral.Witch;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.PlacedTile;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.ui.ImmutablePoint;
import com.jcloisterzone.ui.grid.GridPanel;
import com.jcloisterzone.ui.resources.LayeredImageDescriptor;

import io.vavr.Tuple2;
import io.vavr.collection.Stream;

public class MeepleLayer extends AbstractGridLayer {

    public static class MeppleLayerModel {
        ArrayList<FigureImage> outsideBridge = new ArrayList<>();
        ArrayList<FigureImage> onBridge = new ArrayList<>();
    }

    public static final double FIGURE_SIZE_RATIO = 0.35f;

    /**
     * Corn circles allows multiple meeples on single feature.
     * In such case double meeple should be displayed after common ones.
     */
    private MeppleLayerModel model = new MeppleLayerModel();

    public MeepleLayer(GridPanel gridPanel, GameController gc) {
        super(gridPanel, gc);
    }

    @Subscribe
    public void handleGameChanged(GameChangedEvent ev) {
        if (ev.hasMeeplesChanged() || ev.hasNeutralFiguresChanged()) {
            model = createModel(ev.getCurrentState());
            gridPanel.repaint();
        }
    }

    private MeppleLayerModel createModel(GameState state) {
        MeppleLayerModel model = new MeppleLayerModel();

        HashMap<FeaturePointer, LinkedList<Figure<?>>> onFeature = new HashMap<>();
        LinkedList<Tuple2<Position, NeutralFigure<?>>> onTile = new LinkedList<>();

        for (Tuple2<Meeple, FeaturePointer> t : state.getDeployedMeeples()) {
            LinkedList<Figure<?>> list = onFeature.get(t._2);
            if (list == null) {
                list = new LinkedList<>();
                onFeature.put(t._2, list);
            }
            list.add(t._1);
        }

        for (Tuple2<NeutralFigure<?>, BoardPointer> t : state.getNeutralFigures().getDeployedNeutralFigures()) {
            if (t._2 instanceof Position) {
                onTile.add(new Tuple2<>((Position) t._2, t._1));
            } else {
                FeaturePointer fp = t._2.asFeaturePointer();
                LinkedList<Figure<?>> list = onFeature.get(fp);
                if (list == null) {
                    list = new LinkedList<>();
                    onFeature.put(fp, list);
                }
                list.add(t._1);
            }
        }

        onFeature.forEach((fp, list) -> {
            //TODO rearrange

            int order = 0;

            for (Figure<?> fig : list) {
                PlacedTile placedTile = state.getPlacedTile(fp.getPosition());

                FigureImage fi = new FigureImage(fig);
                fi.offset = getFigureOffset(placedTile, fig, fp);
                if (order > 0) {
                    fi.offset = fi.offset.add(10 * order, 0);
                }
                initFigureImage(fi, placedTile, fig, fp);

                if (placedTile.getInitialFeaturePartOf(fp.getLocation()) instanceof Bridge) {
                    model.onBridge.add(fi);
                } else {
                    model.outsideBridge.add(fi);
                }
                order++;
            }
        });

        onTile.forEach(t -> {
            Position pos = t._1;
            NeutralFigure<?> fig = t._2;
            PlacedTile pt = state.getPlacedTile(pos);

            FigureImage fi = new FigureImage(fig);
            fi.offset = getFigureOffset(pt, fig, pos);
            initFigureImage(fi, pt, fig, pos);

            model.outsideBridge.add(fi);
        });

        return model;
    }

    public Stream<FigureImage> getAllFigureImages() {
        return Stream.concat(model.onBridge, model.outsideBridge);
    }

    private ImmutablePoint getFigureOffset(PlacedTile placedTile, Figure<?> fig, BoardPointer ptr) {
        ImmutablePoint point = getFigureTileOffset(placedTile, fig, ptr);
        Position pos = ptr.getPosition();
        return point.add(100 * pos.x, 100 * pos.y);
    }

    private ImmutablePoint getFigureTileOffset(PlacedTile tile, Figure<?> fig, BoardPointer ptr) {
        if (ptr instanceof Position) {
            if (fig instanceof Fairy) {
                //fairy on tile
                return new ImmutablePoint(62, 52);
            } else {
                return new ImmutablePoint(50, 50);
            }
        }
        if (fig instanceof Barn) {
            return rm.getBarnPlacement();
        } else {
            FeaturePointer fp = ptr.asFeaturePointer();
            return rm.getMeeplePlacement(tile.getTile(), tile.getRotation(), fp.getLocation());
        }
    }

    private void initFigureImage(FigureImage fi, PlacedTile tile, Figure<?> fig, BoardPointer ptr) {
        double baseScale = FIGURE_SIZE_RATIO * gridPanel.getMeepleScaleFactor();
        if (fig instanceof NeutralFigure<?>) {
            final boolean mageOrWitch = fig instanceof Mage || fig instanceof Witch;
            final boolean count = fig instanceof Count;

            Image image = rm.getImage("neutral/"+fig.getClass().getSimpleName().toLowerCase());
            fi.img = image;

            if (mageOrWitch || count) {
                fi.scaleX = 1.2 * baseScale;
                fi.scaleY = 1.2 * baseScale;
            } else if (fig instanceof Fairy) {
                fi.scaleX = baseScale;
                fi.scaleY = baseScale;
            }
            // no scale for dragon
        } else {
            Meeple m = (Meeple) fig;
            FeaturePointer fp = ptr.asFeaturePointer();
            //Feature feature = tile.getFeature(fp.getLocation());
            Color color = m.getPlayer().getColors().getMeepleColor();
            LayeredImageDescriptor lid = new LayeredImageDescriptor(m.getClass(), color);
            lid.setAdditionalLayer(getExtraDecoration(m.getClass(), fp));
            Image image = rm.getLayeredImage(lid);
            if (fp.getLocation() == Location.MONASTERY) {
                image = rotate(image, 90);
            }
            fi.img = image;
            fi.scaleX = baseScale;
            fi.scaleY = baseScale;
        }
        return;
    }

    private void paintFigureImages(Graphics2D g, ArrayList<FigureImage> images) {
        int baseSize = getTileWidth();
        AffineTransform originalTransform = g.getTransform();
        for (FigureImage fi : images) {
            Image scaled = fi.getScaledInstance(baseSize);
            int width = scaled.getWidth(null);
            int height = scaled.getHeight(null);

            ImmutablePoint scaledOffset = fi.offset.scale(baseSize, getTileHeight());
            int x = scaledOffset.getX();
            int y = scaledOffset.getY();

            g.rotate(-gridPanel.getBoardRotation().getTheta(), x, y);
            g.drawImage(scaled, x - width / 2, y - height / 2, gridPanel);
            g.setTransform(originalTransform);
        }
    }

    @Override
    public void paint(Graphics2D g) {
        paintFigureImages(g, model.outsideBridge);
    }

    public void paintMeeplesOnBridges(Graphics2D g) {
        paintFigureImages(g, model.onBridge);
    }


    //TODO path from Theme
    private String getExtraDecoration(Class<? extends Meeple> type, FeaturePointer fp) {
        if (Follower.class.isAssignableFrom(type) && fp.getLocation().isFarmLocation()) {
            return "player-meeples/decorations/farm";
        }
        if (fp.getLocation() == Location.TOWER) {
            if (BigFollower.class.isAssignableFrom(type)) {
                return "player-meeples/decorations/big_tower";
            } else {
                return "player-meeples/decorations/tower";
            }
        }
        return null;
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