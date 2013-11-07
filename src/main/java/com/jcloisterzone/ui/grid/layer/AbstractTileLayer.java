package com.jcloisterzone.ui.grid.layer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.ui.ImmutablePoint;
import com.jcloisterzone.ui.grid.GridPanel;

//PROBABLY TO DELETE
public abstract class AbstractTileLayer extends AbstractGridLayer {

    private Position position;
    private Rotation rotation;

    public AbstractTileLayer(GridPanel gridPanel, Position position) {
        this(gridPanel, position, Rotation.R0);
    }

    public AbstractTileLayer(GridPanel gridPanel, Position position, Rotation rotation) {
        super(gridPanel);
        this.position = position;
        this.rotation = rotation;
    }

    //convinient methods

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public Rotation getRotation() {
        return rotation;
    }

    public void setRotation(Rotation rotation) {
        this.rotation = rotation;
    }

    protected int getOffsetX() {
        return getOffsetX(position);
    }

    protected int getOffsetY() {
        return getOffsetY(position);
    }

    public AffineTransform getAffineTransform() {
        return getAffineTransform(position, rotation);
    }

    public AffineTransform getAffineTransform(Rotation rotation) {
        return getAffineTransform(position, rotation);
    }



    //TODO revise



    public void drawAntialiasedTextCentered(Graphics2D g2, String text, int fontSize, ImmutablePoint centerNoScaled, Color fgColor, Color bgColor) {
        drawAntialiasedTextCentered(g2, text, fontSize, position, centerNoScaled, fgColor, bgColor);
    }


    public void drawAntialiasedTextCenteredNoScale(Graphics2D g2, String text, int fontSize, ImmutablePoint center, Color fgColor, Color bgColor) {
        drawAntialiasedTextCenteredNoScale(g2, text, fontSize, position, center, fgColor, bgColor);
    }
}
