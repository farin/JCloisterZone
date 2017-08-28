package com.jcloisterzone.ui.grid.layer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.util.ArrayList;
import java.util.List;

import com.google.common.eventbus.Subscribe;
import com.jcloisterzone.LittleBuilding;
import com.jcloisterzone.Player;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.event.LittleBuildingEvent;
import com.jcloisterzone.event.TunnelPiecePlacedEvent;
import com.jcloisterzone.figure.SmallFollower;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.ui.ImmutablePoint;
import com.jcloisterzone.ui.grid.GridPanel;
import com.jcloisterzone.ui.grid.layer.MeepleLayer.PositionedImage;
import com.jcloisterzone.ui.resources.LayeredImageDescriptor;

public class TokenLayer extends AbstractGridLayer {

    //extracted from "mutable" MeepleLayer
    //TODO IMMUTABLE complete when readding LB or TU expansion

    public TokenLayer(GridPanel gridPanel, GameController gc) {
        super(gridPanel, gc);
        gc.register(this);
    }

    @Override
    public void paint(Graphics2D g2) {
        throw new UnsupportedOperationException("TODO IMMUTABLE");

//        for (PositionedImage mi : permanentImages) {
//            paintPositionedImage(g, mi, squareSize);
//        }
    }
/*
    private List<PositionedImage> permanentImages = new ArrayList<>();

    private void addPermanentImage(Position position, ImmutablePoint offset, Image image) {
        addPermanentImage(position, offset, image, 1.0, 1.0);
    }


    //TODO hack with xScale, yScale - clean and do better
    private void addPermanentImage(Position position, ImmutablePoint offset, Image image, double xScale, double yScale) {
        PositionedImage pi = new PositionedImage(position, offset, image);
        pi.heightWidthRatio = image.getHeight(null) / image.getWidth(null);
        pi.xScaleFactor = xScale;
        pi.yScaleFactor = yScale;
        permanentImages.add(pi);
    }


    @Subscribe
    public void onTunnelPiecePlacedEvent(TunnelPiecePlacedEvent ev) {
        Player player = ev.getTriggeringPlayer();
        Color c;
        if (ev.isSecondPiece()) {
            c = player.getColors().getTunnelBColor();
        } else {
            c = player.getColors().getMeepleColor();
        }
        Image tunnelPiece = rm.getLayeredImage(new LayeredImageDescriptor("player-meeples/tunnel", c));
        Tile tile = gridPanel.getTile(ev.getPosition());
        ImmutablePoint offset = rm.getMeeplePlacement(tile, SmallFollower.class, ev.getLocation());
        addPermanentImage(ev.getPosition(), offset, tunnelPiece);
    }

    @Subscribe
    public void onLittleBuildingEvent(LittleBuildingEvent ev) {
        Image img = rm.getImage("neutral/lb-"+ev.getBuilding().name().toLowerCase());
        ImmutablePoint offset = new ImmutablePoint(65, 35);
        double xScale = 1.15, yScale = 1.15;
        //TODO tightly coupled with current theme, todo change image size in theme
        if (ev.getBuilding() == LittleBuilding.TOWER) {
            xScale = 1.0;
            yScale = 0.7;
        }
        addPermanentImage(ev.getPosition(), offset, img, xScale, yScale);
    }

    @Override
    public void zoomChanged(int squareSize) {
        for (PositionedImage mi : permanentImages) {
            paintPositionedImage(g, mi, squareSize);
        }
    }
*/


}
