package com.jcloisterzone.ui.grid.layer;

import static com.jcloisterzone.ui.resources.ResourceManager.NORMALIZED_SIZE;
import static com.jcloisterzone.ui.resources.ResourceManager.POINT_NORMALIZED_SIZE;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;

import javax.swing.event.MouseInputListener;

import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.PlacedTile;
import com.jcloisterzone.ui.Client;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.ui.ImmutablePoint;
import com.jcloisterzone.ui.UIEventListener;
import com.jcloisterzone.ui.grid.DragInsensitiveMouseClickListener;
import com.jcloisterzone.ui.grid.GridLayer;
import com.jcloisterzone.ui.grid.GridPanel;
import com.jcloisterzone.ui.resources.ConvenientResourceManager;
import com.jcloisterzone.ui.resources.FeatureArea;
import com.jcloisterzone.ui.resources.TileImage;

public abstract class AbstractGridLayer implements GridLayer, UIEventListener {

    protected boolean visible;
    protected final GridPanel gridPanel;
    protected final GameController gc;
    protected final ConvenientResourceManager rm;
    private MouseInputListener mouseListener;

    public AbstractGridLayer(GridPanel gridPanel, GameController gc) {
        this.gridPanel = gridPanel;
        this.gc = gc;
        this.rm = gc.getClient().getResourceManager();
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

    @Override
    public void boardRotated(Rotation boardRotation) {
        if (mouseListener != null) {
            triggerFakeMouseEvent();
        }
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    public void attachMouseInputListener(MouseInputListener mouseListener) {
        assert this.mouseListener == null;
        this.mouseListener = new DragInsensitiveMouseClickListener(mouseListener);
        gridPanel.addMouseListener(this.mouseListener);
        gridPanel.addMouseMotionListener(this.mouseListener);
        triggerFakeMouseEvent();
    }

    @Override
    public void onShow() {
        assert !visible;
        visible = true;
    }

    @Override
    public void onHide() {
        assert visible;
        visible = false;
        if (mouseListener != null) {
            gridPanel.removeMouseMotionListener(mouseListener);
            gridPanel.removeMouseListener(mouseListener);
            mouseListener = null;
        }
    }

    public AffineTransform getAffineTransform(TileImage tileImage, Position pos) {
        Insets offset = tileImage.getOffset();
        Image img = tileImage.getImage();
        int w = img.getWidth(null) - offset.left - offset.right;
        int h = img.getHeight(null) - offset.top - offset.bottom;
        return getAffineTransform(w, h, pos, offset);
    }

    public AffineTransform getAffineTransform(int fromWidth, int fromHeight, Position pos, Insets offset) {
        AffineTransform t = getAffineTransform(fromWidth, fromHeight, pos, Rotation.R0);
        t.concatenate(AffineTransform.getTranslateInstance(-offset.left, -offset.top));
        return t;
    }

    public AffineTransform getAffineTransform(int fromWidth, int fromHeight, Position pos) {
        return getAffineTransform(fromWidth, fromHeight, pos, Rotation.R0);
    }

    public AffineTransform getAffineTransform(Position pos) {
        return AffineTransform.getTranslateInstance(pos.x * getTileWidth(), pos.y * getTileHeight());
    }

    public AffineTransform getAffineTransform(Position pos, Rotation rotation) {
        AffineTransform r;
        if (rotation == Rotation.R0 || rotation == Rotation.R180) {
            r =  rotation.getAffineTransform(getTileWidth(), getTileHeight());
        } else {
            r =  rotation.getAffineTransform(getTileHeight(), getTileWidth());
        }
        AffineTransform t =  AffineTransform.getTranslateInstance(getOffsetX(pos), getOffsetY(pos));
        t.concatenate(r);
        return t;
    }

    //called only with R0
    @Deprecated
    private AffineTransform getAffineTransform(int fromWidth, int fromHeight, Position pos, Rotation rotation) {
        double ratioX, ratioY;
        if (rotation == Rotation.R0 || rotation == Rotation.R180) {
            ratioX =  getTileWidth() / (double) fromWidth;
            ratioY =  getTileHeight() / (double) fromHeight;
        } else {
            ratioX =  getTileHeight() / (double) fromWidth;
            ratioY =  getTileWidth() / (double) fromHeight;
        }
        return getAffineTransform(pos, rotation, ratioX, ratioY);
    }

    public AffineTransform getAffineTransform(Position pos, Rotation rotation, double ratioX, double ratioY) {
        AffineTransform t = getAffineTransform(pos, rotation);
        AffineTransform scale =  AffineTransform.getScaleInstance(ratioX, ratioY);
        t.concatenate(scale);
        return t;
    }

    protected AffineTransform getAffineTransformIgnoringRotation(Position pos) {
        int x = getOffsetX(pos);
        int y = getOffsetY(pos);
        AffineTransform at = AffineTransform.getTranslateInstance(x, y);
        at.concatenate(gridPanel.getBoardRotation().inverse().getAffineTransform(getTileWidth(), getTileHeight()));
        return at;
    }

    protected void drawImageIgnoringRotation(Graphics2D g2, Image img, Position pos, int tx, int ty, int width, int height) {
        AffineTransform at = getAffineTransformIgnoringRotation(pos);
        at.concatenate(AffineTransform.getTranslateInstance(tx, ty));
        at.concatenate(AffineTransform.getScaleInstance(width / (double) img.getWidth(null), height / (double) img.getHeight(null)));
        g2.drawImage(img, at, null);
    }

    public int getOffsetX(Position pos) {
        return getTileWidth() * pos.x;
    }

    public int getOffsetY(Position pos) {
        return getTileHeight() * pos.y;
    }

    public AffineTransform getZoomScale() {
        //TODO move imple on gridPanel with caching
        double ratioX = gridPanel.getTileWidth() / (double)NORMALIZED_SIZE;
        //double ratioY = gridPanel.getTileHeight() / (double)ResourceManager.NORMALIZED_SIZE / getImageSizeRatio();
        // TODO ignoring image image size ratio
        double ratioY = gridPanel.getTileHeight() / (double)NORMALIZED_SIZE;
        return AffineTransform.getScaleInstance(ratioX, ratioY);
    }

    @Deprecated
    public AffineTransform getPointZoomScale() {
        //TODO move imple on gridPanel with caching
        double ratioX = gridPanel.getTileWidth() / (double)POINT_NORMALIZED_SIZE;
        //double ratioY = gridPanel.getTileHeight() / (double)ResourceManager.NORMALIZED_SIZE / getImageSizeRatio();
        // TODO ignoring image image size ratio
        double ratioY = gridPanel.getTileHeight() / (double)POINT_NORMALIZED_SIZE;
        return AffineTransform.getScaleInstance(ratioX, ratioY);
    }

    public int getTileWidth() {
        return gridPanel.getTileWidth();
    }

    public int getTileHeight() {
        return gridPanel.getTileHeight();
    }

    protected Client getClient() {
        return gridPanel.getClient();
    }

    protected Game getGame() {
        return gc.getGame();
    }

    @Deprecated //TODO use absolute coordinates instead
    protected Area transformArea(Area area, Position pos) {
        return area.createTransformedArea(getAffineTransform(pos));
    }

    // LEGACY CODE - TODO REFACTOR

    @Deprecated
    private int scale(int x) {
        return (int) (getTileWidth() * (x / 100.0));
    }

    @Deprecated
    private Font getFont(int relativeSize) {
        int realSize = scale(relativeSize);
        return new Font(null, Font.BOLD, realSize);
    }

    // TODO don't use pos arg, use abs coords instead
    public void drawAntialiasedTextCentered(Graphics2D g2, String text, int fontSize, Position pos, ImmutablePoint centerNoScaled, Color fgColor, Color bgColor) {
        //gridPanel.getBoardRotation().
        ImmutablePoint center = centerNoScaled.scale(getTileWidth(), getTileHeight());
        drawAntialiasedTextCenteredNoScale(g2, text, fontSize, pos, center, fgColor, bgColor);
    }


    // TODO don't use pos arg, use abs coords instead
    // TODO misleading name - is centered around point and scaled font but not scale center point (probably :)
    public void drawAntialiasedTextCenteredNoScale(Graphics2D g2, String text, int fontSize, Position pos, ImmutablePoint center, Color fgColor, Color bgColor) {
        Color original = g2.getColor();
        FontRenderContext frc = g2.getFontRenderContext();
        TextLayout tl = new TextLayout(text, getFont(fontSize),frc);
        Rectangle2D bounds = tl.getBounds();

        int w = (int) bounds.getWidth();
        int h = (int) bounds.getHeight();
        center = center.translate(-w/2, -h/2);
        int x = getOffsetX(pos) + center.getX();
        int y = getOffsetY(pos) + center.getY();

        AffineTransform orig = g2.getTransform();
        g2.rotate(-gridPanel.getBoardRotation().getTheta(), x+w/2, y+h/2);
        if (bgColor != null) {
            g2.setColor(bgColor);
            g2.fillRect(x-6, y-5, w+12, h+10);
        }

        g2.setColor(fgColor);
        tl.draw(g2, x,  y+h);
        g2.setColor(original);
        g2.setTransform(orig);
    }

    protected Area getFeatureArea(GameState state, Feature f) {
        Area area = new Area();

        for (FeaturePointer fp : f.getPlaces()) {
            Position pos = fp.getPosition();
            Location loc = fp.getLocation();
            PlacedTile pt = state.getPlacedTile(pos);

            FeatureArea fa = rm.getFeatureArea(pt.getTile(), pt.getRotation(), loc).translateTo(pos);
            area.add(fa.getDisplayArea());
        }
        return area;
    }

}
