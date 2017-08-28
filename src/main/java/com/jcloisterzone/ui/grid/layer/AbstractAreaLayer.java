package com.jcloisterzone.ui.grid.layer;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import javax.swing.ImageIcon;
import javax.swing.event.MouseInputAdapter;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.pointer.BoardPointer;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.config.Config.DebugConfig;
import com.jcloisterzone.figure.SmallFollower;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.PlacedTile;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.ui.ImmutablePoint;
import com.jcloisterzone.ui.controls.action.ActionWrapper;
import com.jcloisterzone.ui.grid.ActionLayer;
import com.jcloisterzone.ui.grid.GridPanel;
import com.jcloisterzone.ui.resources.FeatureArea;
import com.jcloisterzone.ui.resources.LayeredImageDescriptor;

import io.vavr.Tuple2;
import io.vavr.collection.Map;


public abstract class AbstractAreaLayer extends AbstractGridLayer implements ActionLayer {

    private static final AlphaComposite AREA_ALPHA_COMPOSITE = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .6f);
    private static final AlphaComposite FIGURE_HIGHLIGHT_AREA_ALPHA_COMPOSITE = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .75f);

    private Player player;
    private ActionWrapper actionWrapper;
    private Map<BoardPointer, FeatureArea> areas;
    private Map<BoardPointer, FeatureArea> scaledAreas;
    private FeatureArea selectedArea;
    private BoardPointer selectedFeaturePointer;

    boolean refreshAreas;

    /*if true, area is displayed as placed meeple
     this method is intended for tile placement debugging and is not optimized for performance
     */
    private boolean figureHighlight = false;

    public AbstractAreaLayer(GridPanel gridPanel, GameController gc) {
        super(gridPanel, gc);
        DebugConfig debugConfig = getClient().getConfig().getDebug();
        if (debugConfig != null && "figure".equals(debugConfig.getArea_highlight())) {
            figureHighlight = true;
        }
    }

    @Override
    public void setActionWrapper(boolean active, ActionWrapper actionWrapper) {
        this.actionWrapper = actionWrapper;
        if (actionWrapper == null) {
            cleanAreas();
        } else {
            areas = prepareAreas();
        }
    }

    @Override
    public ActionWrapper getActionWrapper() {
        return actionWrapper;
    }

    @Override
    public void onShow() {
        super.onShow();
        //TODO should be based on event player
        player = getGame().getState().getActivePlayer();
        attachMouseInputListener(new AreaLayerMouseMotionListener());
    }

    @Override
    public void onHide() {
        super.onHide();
        player = null;
        cleanAreas();
    }

    protected Map<BoardPointer, FeatureArea> scaleAreas() {
        return areas.mapValues(fa -> fa.transform(getZoomScale()));
    }

    class AreaLayerMouseMotionListener extends MouseInputAdapter {

        @Override
        public void mouseMoved(MouseEvent e) {
            if (scaledAreas == null) {
                scaledAreas = scaleAreas();
            }
            FeatureArea swap = null;
            BoardPointer swapPointer = null;
            Point2D point = gridPanel.getRelativePoint(e.getPoint());
            int x = (int) point.getX();
            int y = (int) point.getY();
            for (Tuple2<BoardPointer, FeatureArea> entry : scaledAreas) {
                FeatureArea fa = entry._2;
                if (fa.getTrackingArea().contains(x, y)) {
                    if (swap == null) {
                        swap = fa;
                        swapPointer = entry._1;
                    } else {
                        if (swap.getzIndex() == fa.getzIndex()) {
                            // two overlapping areas at same point with same zIndex - select no one
                            swap = null;
                            swapPointer = null;
                            break;
                        } else if (fa.getzIndex() > swap.getzIndex()) {
                           swap = fa;
                           swapPointer = entry._1;
                        } //else do nothing
                    }
                }
            }
            boolean doSwap = (swapPointer == null && selectedFeaturePointer != null) || (swapPointer != null && !swapPointer.equals(selectedFeaturePointer));
            if (doSwap || refreshAreas) { //reassign if refreshAreas is true - needs to keep proper sized area!!!
                selectedArea = swap;
                selectedFeaturePointer = swapPointer;
                gridPanel.repaint();
                refreshAreas = false;
            }
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON1) {
                if (selectedArea != null) {
                    performAction(selectedFeaturePointer);
                    e.consume();
                }
            }
        }
    }

    private void cleanAreas() {
        areas = null;
        scaledAreas = null;
        selectedFeaturePointer = null;
        selectedArea = null;
    }

    @Override
    public void zoomChanged(int squareSize) {
        scaledAreas = null;
        super.zoomChanged(squareSize);
    }

    protected abstract Map<BoardPointer, FeatureArea> prepareAreas();
    protected abstract void performAction(BoardPointer selected);

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
        GameState state = getGame().getState();
        FeaturePointer fp = (FeaturePointer) selectedFeaturePointer;
        Position pos = fp.getPosition();
        //ugly copy pasted code from Meeple but uncached here
        g2.setComposite(FIGURE_HIGHLIGHT_AREA_ALPHA_COMPOSITE);
        PlacedTile placedTile = state.getPlacedTile(pos);
        ImmutablePoint point = rm.getMeeplePlacement(placedTile.getTile(), placedTile.getRotation(), fp.getLocation());
        Player p = state.getActivePlayer();
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
        GameState state = getGame().getState();
        Player p = state.getActivePlayer();
        if (p != null && p.equals(player)) { //sync issue
            Color color = selectedArea.getForceAreaColor();
            g2.setColor(color == null ? p.getColors().getMeepleColor() : color);
            g2.setComposite(AREA_ALPHA_COMPOSITE);
            g2.fill(selectedArea.getDisplayArea());
        }
    }
}
