package com.jcloisterzone.ui.grid.layer;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.ImageIcon;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.pointer.BoardPointer;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.config.Config.DebugConfig;
import com.jcloisterzone.figure.SmallFollower;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.ui.ImmutablePoint;
import com.jcloisterzone.ui.grid.GridMouseAdapter;
import com.jcloisterzone.ui.grid.GridMouseListener;
import com.jcloisterzone.ui.grid.GridPanel;
import com.jcloisterzone.ui.resources.FeatureArea;
import com.jcloisterzone.ui.resources.LayeredImageDescriptor;


public abstract class AbstractAreaLayer extends AbstractGridLayer implements GridMouseListener {

    private static final AlphaComposite AREA_ALPHA_COMPOSITE = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .6f);
    private static final AlphaComposite FIGURE_HIGHLIGHT_AREA_ALPHA_COMPOSITE = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .75f);

    private Player player;
    private boolean active;
    private Map<BoardPointer, FeatureArea> areas = Collections.emptyMap();
    private FeatureArea selectedArea;
    private BoardPointer selectedFeaturePointer;

    boolean refreshAreas;

    /*if true, area is displayed as placed meeple
     this method is intended for tile placement debugging and is not optimized for performace
     */
    private boolean figureHighlight = false;

    public AbstractAreaLayer(GridPanel gridPanel, GameController gc) {
        super(gridPanel, gc);
        DebugConfig debugConfig = getClient().getConfig().getDebug();
        if (debugConfig != null && "figure".equals(debugConfig.getArea_highlight())) {
            figureHighlight = true;
        }
    }


    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public void onShow() {
        super.onShow();
        //TODO should be based on event player
        player = getGame().getActivePlayer();
    }

    @Override
    public void onHide() {
        super.onHide();
        player = null;
        cleanAreas();
    }

    protected Map<BoardPointer, FeatureArea> locationMapToPointers(Position pos, Map<Location, FeatureArea> locMap) {
        if (locMap == null) return Collections.emptyMap();
        Map<BoardPointer, FeatureArea> result = new HashMap<>();
        for (Entry<Location, FeatureArea> entry : locMap.entrySet()) {
            result.put(new FeaturePointer(pos, entry.getKey()), entry.getValue());
        }
        return result;
    }

    private class MoveTrackingGridMouseAdapter extends GridMouseAdapter {

        public MoveTrackingGridMouseAdapter(GridPanel gridPanel, GridMouseListener listener) {
            super(gridPanel, listener);
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            super.mouseMoved(e);
            if (refreshAreas) {
                squareEntered(e, getCurrentPosition());
            }
            FeatureArea swap = null;
            BoardPointer swapPointer = null;
            int w = getTileWidth();
            int h = getTileHeight();
            Point2D point = gridPanel.getRelativePoint(e.getPoint());
            int x = (int) point.getX();
            int y = (int) point.getY();
            if (x < 0) x += 1000 * w; //prevent mod from negative number
            if (y < 0) y += 1000 * h; //prevent mod from negative number
            x = x % w;
            y = y % h;
            for (Entry<BoardPointer, FeatureArea> entry : areas.entrySet()) {
                FeatureArea fa = entry.getValue();
                if (fa.getTrackingArea().contains(x, y)) {
                    if (swap == null) {
                        swap = fa;
                        swapPointer = entry.getKey();
                    } else {
                        if (swap.getzIndex() == fa.getzIndex()) {
                            // two overlapping areas at same point with same zIndex - select no one
                            swap = null;
                            swapPointer = null;
                            break;
                        } else if (fa.getzIndex() > swap.getzIndex()) {
                           swap = fa;
                           swapPointer = entry.getKey();
                        } //else do nothing
                    }
                }
            }
            boolean doSwap = (swapPointer == null && selectedFeaturePointer != null) || (swapPointer != null && !swapPointer.equals(selectedFeaturePointer));
            if (doSwap || refreshAreas) { //reassing if refreshAres is true - needs to keep prope sized area!!!
                selectedArea = swap;
                selectedFeaturePointer = swapPointer;
                gridPanel.repaint();
                refreshAreas = false;
            }
        }
    }

    @Override
    protected GridMouseAdapter createGridMouserAdapter(GridMouseListener listener) {
        return new MoveTrackingGridMouseAdapter(gridPanel, listener);
    }

    private void cleanAreas() {
        areas = Collections.emptyMap();
        selectedFeaturePointer = null;
        selectedArea = null;
    }

    @Override
    public void zoomChanged(int squareSize) {
        refreshAreas = true;
        super.zoomChanged(squareSize);
    }

    @Override
    public void squareEntered(MouseEvent e, Position p) {
        Tile tile = gridPanel.getTile(p);
        if (tile != null) {
            areas = prepareAreas(tile, p);
            if (!areas.isEmpty()) {
                Area a = areas.values().iterator().next().getTrackingArea();
            }
        }
    }

    protected abstract Map<BoardPointer, FeatureArea> prepareAreas(Tile tile, Position p);


    @Override
    public void squareExited(MouseEvent e, Position p) {
        if (selectedFeaturePointer != null) {
            cleanAreas();
            gridPanel.repaint();
        }
    }

    protected abstract void performAction(BoardPointer selected);

    @Override
    public void mouseClicked(MouseEvent e, Position pos) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            if (selectedArea != null) {
                performAction(selectedFeaturePointer);
                e.consume();
            }
        }
    }

    @Override
    public void paint(Graphics2D g2) {
        if (selectedArea != null) {
            Composite old = g2.getComposite();
            if (figureHighlight && selectedFeaturePointer instanceof FeaturePointer) {
                paintFigureHighlight(g2);
            } else {
                paintAreaHighlight(g2);
            }
            g2.setComposite(old);
        }
    }

    /** debug purposes highlight - it always shows basic follower (doesn't important for dbg */
    private void paintFigureHighlight(Graphics2D g2) {
        FeaturePointer fp = (FeaturePointer) selectedFeaturePointer;
        Position pos = fp.getPosition();
        //ugly copy pasted code from Meeple but uncached here
        g2.setComposite(FIGURE_HIGHLIGHT_AREA_ALPHA_COMPOSITE);
        Tile tile = getGame().getBoard().get(pos);
        ImmutablePoint point = rm.getMeeplePlacement(tile, SmallFollower.class, fp.getLocation());
        Player p = getGame().getActivePlayer();
		Image unscaled = rm.getLayeredImage(
			new LayeredImageDescriptor(SmallFollower.class, p.getColors().getMeepleColor())
		);
        int size = (int) (getTileWidth() * MeepleLayer.FIGURE_SIZE_RATIO);
        Image scaled = unscaled.getScaledInstance(size, size, Image.SCALE_SMOOTH);
        scaled = new ImageIcon(scaled).getImage();
        ImmutablePoint scaledOffset = point.scale(getTileWidth(), getTileHeight(), size);
        g2.drawImage(scaled, getOffsetX(pos) + scaledOffset.getX(), getOffsetY(pos) + scaledOffset.getY(), gridPanel);
    }

    /** standard highlight **/
    private void paintAreaHighlight(Graphics2D g2) {
        Player p = getGame().getActivePlayer();
        if (p != null && p.equals(player)) { //sync issue
            Color color = selectedArea.getForceAreaColor();
            g2.setColor(color == null ? p.getColors().getMeepleColor() : color);
            g2.setComposite(AREA_ALPHA_COMPOSITE);
            Area area = selectedArea.getDisplayArea();
            if (area == null) area = selectedArea.getTrackingArea();
            g2.fill(transformArea(area, selectedFeaturePointer.getPosition()));
        }
    }
}
