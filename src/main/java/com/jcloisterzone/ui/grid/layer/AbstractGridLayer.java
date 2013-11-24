package com.jcloisterzone.ui.grid.layer;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;

import javax.swing.event.MouseInputListener;

import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.ui.Client;
import com.jcloisterzone.ui.ImmutablePoint;
import com.jcloisterzone.ui.grid.DragInsensitiveMouseClickListener;
import com.jcloisterzone.ui.grid.GridLayer;
import com.jcloisterzone.ui.grid.GridMouseAdapter;
import com.jcloisterzone.ui.grid.GridMouseListener;
import com.jcloisterzone.ui.grid.GridPanel;

public abstract class AbstractGridLayer implements GridLayer {

    protected final GridPanel gridPanel;
    private MouseInputListener mouseListener;

    public AbstractGridLayer(GridPanel gridPanel) {
        this.gridPanel = gridPanel;
    }

    private void triggerFakeMouseEvent() {
        Point pt = gridPanel.getMousePosition();
        if (pt != null) {
            mouseListener.mouseMoved(
                new MouseEvent(gridPanel, 0, System.currentTimeMillis(), 0, pt.x, pt.y, -1, -1, 0, false, 0)
            );
        }
    }

    @Override
    public void zoomChanged(int squareSize) {
        if (mouseListener != null) {
            triggerFakeMouseEvent();
        }
    }

//	@Override
//	public void gridChanged(int left, int right, int top, int bottom) {
//	}

    protected GridMouseAdapter createGridMouserAdapter(GridMouseListener listener) {
        return new GridMouseAdapter(gridPanel, listener);
    }

    @Override
    public void layerAdded() {
        if (this instanceof GridMouseListener && getClient().isClientActive()) {
            mouseListener = new DragInsensitiveMouseClickListener(createGridMouserAdapter((GridMouseListener) this));
            gridPanel.addMouseListener(mouseListener);
            gridPanel.addMouseMotionListener(mouseListener);
            triggerFakeMouseEvent();
        }
    }

    @Override
    public void layerRemoved() {
        if (mouseListener != null) {
            gridPanel.removeMouseMotionListener(mouseListener);
            gridPanel.removeMouseListener(mouseListener);
            mouseListener = null;
        }
    }
    public AffineTransform getAffineTransform(int scaleFrom, Position pos) {
        return getAffineTransform(scaleFrom, pos, Rotation.R0);
    }

    public AffineTransform getAffineTransform(Position pos) {
        return getAffineTransform(pos, Rotation.R0);
    }

    public AffineTransform getAffineTransform(Position pos, Rotation rotation) {
        AffineTransform r =  rotation.getAffineTransform(getSquareSize());
        AffineTransform t =  AffineTransform.getTranslateInstance(getOffsetX(pos), getOffsetY(pos));
        t.concatenate(r);
        return t;
    }

    public AffineTransform getAffineTransform(int scaleFrom, Position pos, Rotation rotation) {
        double ratio =  getSquareSize() / (double) scaleFrom;
        return getAffineTransform(scaleFrom, pos, rotation, ratio);
    }

    public AffineTransform getAffineTransform(int scaleFrom, Position pos, Rotation rotation, double ratio) {
        AffineTransform t = getAffineTransform(pos, rotation);
        AffineTransform scale =  AffineTransform.getScaleInstance(ratio, ratio);
        t.concatenate(scale);
        return t;
    }

    public int getOffsetX(Position pos) {
        return getSquareSize() * pos.x;
    }

    public int getOffsetY(Position pos) {
        return getSquareSize() * pos.y;
    }

    public int getSquareSize() {
        return gridPanel.getSquareSize();
    }

    protected Client getClient() {
        return gridPanel.getClient();
    }

    protected Game getGame() {
        return getClient().getGame();
    }

    protected Area transformArea(Area area, Position pos) {
        Area copy = new Area(area);
        copy.transform(getAffineTransform(pos));
        return copy;
    }

    // LEGACY CODE - TODO REFACTOR

    private int scale(int x) {
        return (int) (getSquareSize() * (x / 100.0));
    }

    @Deprecated
    private Font getFont(int relativeSize) {
        int realSize = scale(relativeSize);
        return new Font(null, Font.BOLD, realSize);
//		Font font = Square.cachedFont;
//		if (font == null || font.getSize() != realSize) {
//			font = new Font(null, Font.BOLD, realSize);
//			Square.cachedFont = font;
//		}
//		return font;
    }

    public void drawAntialiasedTextCentered(Graphics2D g2, String text, int fontSize, Position pos, ImmutablePoint centerNoScaled, Color fgColor, Color bgColor) {
        ImmutablePoint center = centerNoScaled.scale(getSquareSize());
        drawAntialiasedTextCenteredNoScale(g2, text, fontSize, pos, center, fgColor, bgColor);
    }


    //TODO misleading name - is centered around point and scaled font but not scale center point (probably :)
    public void drawAntialiasedTextCenteredNoScale(Graphics2D g2, String text, int fontSize, Position pos, ImmutablePoint center, Color fgColor, Color bgColor) {
        Color original = g2.getColor();
        FontRenderContext frc = g2.getFontRenderContext();
        TextLayout tl = new TextLayout(text, getFont(fontSize),frc);
        Rectangle2D bounds = tl.getBounds();

        center = center.translate( (int) (bounds.getWidth() / -2), (int) (bounds.getHeight() / -2));

        if (bgColor != null) {
            g2.setColor(bgColor);
            g2.fillRect(getOffsetX(pos) + center.getX() - 6, getOffsetY(pos) + center.getY() - 5, 12 + (int)bounds.getWidth(),10 +(int) bounds.getHeight());
        }

        g2.setColor(fgColor);
        tl.draw(g2, getOffsetX(pos) + center.getX(),  getOffsetY(pos) + center.getY() + (int) bounds.getHeight());
        g2.setColor(original);
    }

}
