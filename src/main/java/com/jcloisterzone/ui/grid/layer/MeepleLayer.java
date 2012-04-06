package com.jcloisterzone.ui.grid.layer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.feature.Farm;
import com.jcloisterzone.feature.Tower;
import com.jcloisterzone.figure.BigFollower;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.ui.ImmutablePoint;
import com.jcloisterzone.ui.grid.GridPanel;
import com.jcloisterzone.ui.theme.FigureTheme;

public class MeepleLayer extends AbstractGridLayer {

    protected double FIGURE_SIZE_RATIO = 0.35;

    private Map<Meeple, PositionedImage> images = Maps.newHashMap();
    //TODO own layer ???
    private List<PositionedImage> permanentImages = Lists.newArrayList();

    public MeepleLayer(GridPanel gridPanel) {
        super(gridPanel);
    }

    @Override
    public int getZIndex() {
        return 50;
    }

    private void paintPositionedImage(Graphics2D g, PositionedImage mi, int boxSize ) {
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
        for(PositionedImage mi : images.values()) {
            paintPositionedImage(g, mi, boxSize );
        }
        for(PositionedImage mi : permanentImages) {
            paintPositionedImage(g, mi, boxSize );
        }

    }

    @Override
    public void zoomChanged(int squareSize) {
        for(PositionedImage mi : images.values()) {
            mi.scaledImage = null;
        }
        for(PositionedImage mi : permanentImages) {
            mi.scaledImage = null;
        }
        super.zoomChanged(squareSize);
    }

    public void meepleDeployed(Meeple m) {
        Color c = getClient().getPlayerColor(m.getPlayer());
        FigureTheme theme = getClient().getFigureTheme();

        ImmutablePoint offset = getClient().getTileTheme().getFigurePlacement(gridPanel.getTile(m.getPosition()), m);
        Image image = theme.getFigureImage(m.getClass(), c,  getExtraDecoration(m));
        images.put(m, new PositionedImage(m.getPosition(), offset, image));
    }

    public void meepleUndeployed(Meeple m) {
        images.remove(m);
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