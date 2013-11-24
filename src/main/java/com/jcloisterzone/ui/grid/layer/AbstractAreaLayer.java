package com.jcloisterzone.ui.grid.layer;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.geom.Area;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.ImageIcon;

import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.figure.SmallFollower;
import com.jcloisterzone.ui.ImmutablePoint;
import com.jcloisterzone.ui.grid.GridMouseAdapter;
import com.jcloisterzone.ui.grid.GridMouseListener;
import com.jcloisterzone.ui.grid.GridPanel;


public abstract class AbstractAreaLayer extends AbstractGridLayer implements GridMouseListener {

    private static final AlphaComposite AREA_ALPHA_COMPOSITE = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .6f);
    private static final AlphaComposite FIGURE_HIGHLIGHT_AREA_ALPHA_COMPOSITE = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .75f);

    private Map<Location, Area> areas;
    private Location selectedLocation;
    private Position selectedPosition;

    /*if true, area is displayed as placed meeple
     this method is intended for tile placement debugging and is not optimized for performace
     */
    private boolean figureHighlight = false;

    public AbstractAreaLayer(GridPanel gridPanel) {
        super(gridPanel);
        if ("figure".equals(getClient().getConfig().get("debug", "area_highlight"))) {
            figureHighlight = true;
        }
    }

    private class MoveTrackingGridMouseAdapter extends GridMouseAdapter {

        public MoveTrackingGridMouseAdapter(GridPanel gridPanel, GridMouseListener listener) {
            super(gridPanel, listener);
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            super.mouseMoved(e);
            if (areas == null) return;
            int size = getSquareSize();
            int x = e.getX() - gridPanel.getOffsetX();
            int y = e.getY() - gridPanel.getOffsetY();
            if (x < 0) x += 1000 * size; //prevent mod from negative number
            if (y < 0) y += 1000 * size; //prevent mod from negative number
            x = x % size;
            y = y % size;
            Location swap = null;
            for (Entry<Location, Area> enrty : areas.entrySet()) {
                if (enrty.getValue().contains(x, y)) {
                    if (swap != null) { // 2 areas at point - select no one
                        swap = null;
                        break;
                    }
                    swap = enrty.getKey();
                }
            }
            if (swap != selectedLocation) {
                selectedLocation = swap;
                gridPanel.repaint();
                //RepaintManager.currentManager(gridPanel).addDirtyRegion(gridPanel, x * size, y * size, size, size);
                //gridPanel.repaint(0, x * size, y * size, size, size);
            }
        }

    }

    @Override
    protected GridMouseAdapter createGridMouserAdapter(GridMouseListener listener) {
        return new MoveTrackingGridMouseAdapter(gridPanel, listener);
    }

    private void cleanAreas() {
        areas = null;
        selectedPosition = null;
        selectedLocation = null;
    }

    @Override
    public void zoomChanged(int squareSize) {
        Position prevSelectedPosition = selectedPosition;
        super.zoomChanged(squareSize);
        if (selectedPosition != null && selectedPosition.equals(prevSelectedPosition)) {
            //no square enter/leave trigger in this case - refresh areas
            areas = prepareAreas(gridPanel.getTile(selectedPosition), selectedPosition);
        }
    }

    @Override
    public void squareEntered(MouseEvent e, Position p) {
        Tile tile = gridPanel.getTile(p);
        if (tile != null) {
            selectedPosition = p;
            areas = prepareAreas(tile, p);
        }
    }

    protected abstract Map<Location, Area> prepareAreas(Tile tile, Position p);


    @Override
    public void squareExited(MouseEvent e, Position p) {
        if (selectedPosition != null) {
            cleanAreas();
            gridPanel.repaint();
        }
    }

    protected abstract void performAction(Position pos, Location selected);

    @Override
    public void mouseClicked(MouseEvent e, Position pos) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            if (selectedLocation != null) {
                performAction(pos, selectedLocation);
                e.consume();
            }
        }
    }

    @Override
    public void paint(Graphics2D g2) {
        if (selectedLocation != null && areas != null) {
            Composite old = g2.getComposite();
            if (figureHighlight) {
                paintFigureHighlight(g2);
            } else {
                paintAreaHighlight(g2);
            }
            g2.setComposite(old);
        }
    }

    /** debug purposes highlight - it always shows basic follower (doesn't important for dbg */
    private void paintFigureHighlight(Graphics2D g2) {
        //ugly copy pasted code from Meeple but uncached here
        g2.setComposite(FIGURE_HIGHLIGHT_AREA_ALPHA_COMPOSITE);
        Tile tile = getGame().getBoard().get(selectedPosition);
        ImmutablePoint point = getClient().getResourceManager().getMeeplePlacement(tile, SmallFollower.class, selectedLocation);
        Image unscaled = getClient().getFigureTheme().getFigureImage(SmallFollower.class, getClient().getPlayerColor(), null);
        int size = (int) (getSquareSize() * MeepleLayer.FIGURE_SIZE_RATIO);
        Image scaled = unscaled.getScaledInstance(size, size, Image.SCALE_SMOOTH);
        scaled = new ImageIcon(scaled).getImage();
        ImmutablePoint scaledOffset = point.scale(getSquareSize(), (int)(getSquareSize() * MeepleLayer.FIGURE_SIZE_RATIO));
        g2.drawImage(scaled, getOffsetX(selectedPosition) + scaledOffset.getX(), getOffsetY(selectedPosition) + scaledOffset.getY(), gridPanel);
    }

    /** standard highlight **/
    private void paintAreaHighlight(Graphics2D g2) {
        g2.setColor(getClient().getPlayerColor());
        g2.setComposite(AREA_ALPHA_COMPOSITE);
        g2.fill(transformArea(areas.get(selectedLocation), selectedPosition));
    }

    @Override
    public int getZIndex() {
        return 100;
    }

}
