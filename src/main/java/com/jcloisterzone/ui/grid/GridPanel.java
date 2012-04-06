package com.jcloisterzone.ui.grid;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import javax.swing.JComponent;
import javax.swing.UIManager;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.XmlUtils;
import com.jcloisterzone.game.Snapshot;
import com.jcloisterzone.ui.Client;
import com.jcloisterzone.ui.animation.AnimationService;
import com.jcloisterzone.ui.animation.RecentPlacement;
import com.jcloisterzone.ui.controls.ControlPanel;
import com.jcloisterzone.ui.grid.layer.AbstractAreaLayer;
import com.jcloisterzone.ui.grid.layer.AbstractTilePlacementLayer;
import com.jcloisterzone.ui.grid.layer.PlacementHistory;
import com.jcloisterzone.ui.grid.layer.TileActionLayer;
import com.jcloisterzone.ui.grid.layer.TileLayer;
import com.jcloisterzone.ui.theme.TileTheme;

public class GridPanel extends JComponent {

    public static int INITIAL_SQUARE_SIZE = 120;
    private static final int STARTING_GRID_SIZE = 3;

    final Client client;
    final ControlPanel controlPanel;

    /** current board size */
    private int left, right, top, bottom;
    private int squareSize;

    //focus
    private int offsetX, offsetY;
    private double cx = 0.0, cy = 0.0;
    private MoveCenterAnimation moveAnimation;

    private List<GridLayer> layers = Collections.synchronizedList(new LinkedList<GridLayer>());

    public GridPanel(Client client, Snapshot snapshot) {
        setDoubleBuffered(true);
        setOpaque(false);

        this.client = client;
        this.controlPanel = client.getControlPanel();

        squareSize = INITIAL_SQUARE_SIZE;
        left = 0 - STARTING_GRID_SIZE / 2;
        right = 0 + STARTING_GRID_SIZE / 2;
        top = 0 - STARTING_GRID_SIZE / 2;
        bottom = 0 + STARTING_GRID_SIZE / 2;

        if (snapshot != null) {
            NodeList nl = snapshot.getTileElements();
            for(int i = 0; i < nl.getLength(); i++) {
                Element el = (Element) nl.item(i);
                Position pos = XmlUtils.extractPosition(el);
                if (pos.x <= left) left = pos.x - 1;
                if (pos.x >= right) right = pos.x + 1;
                if (pos.y <= top) top = pos.y - 1;
                if (pos.y >= bottom) bottom = pos.y + 1;
            }
        }
        registerMouseListeners();
    }


    private void registerMouseListeners() {
        addMouseListener(
            new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    switch (e.getButton()) {
                    case MouseEvent.BUTTON2:
                        int clickX = e.getX()-offsetX;
                        int clickY = e.getY()-offsetY;
                        moveCenterToAnimated(clickX/(double)squareSize, clickY/(double)squareSize);
                        break;
                    case MouseEvent.BUTTON3:
                    case 5:
                        if (client.isClientActive()) {
                            client.getControlPanel().getActionPanel().forward();
                        }
                        break;
                    case 4:
                        if (client.isClientActive()) {
                            client.getControlPanel().getActionPanel().backward();
                        }
                        break;
                    }
                }
            }
        );
        addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                zoom(-e.getWheelRotation());
            }
        });
    }

    public Tile getTile(Position p) {
        return client.getGame().getBoard().get(p);
    }

    public TileTheme getTileTheme() {
        return client.getTileTheme();
    }

    public Client getClient() {
        return client;
    }

    public AnimationService getAnimationService() {
        return client.getMainPanel().getAnimationService();
    }

    public int getSquareSize() {
        return squareSize;
    }

    public int getLeft() {
        return left;
    }

    public int getRight() {
        return right;
    }

    public int getTop() {
        return top;
    }

    public int getBottom() {
        return bottom;
    }

    public int getOffsetX() {
        return offsetX;
    }

    public int getOffsetY() {
        return offsetY;
    }

    public void moveCenter(int xSteps, int ySteps) {
        //step should be 30px
        double dx = xSteps * 30.0f / getSquareSize();
        double dy = ySteps * 30.0f / getSquareSize();
        moveCenterTo(cx + dx, cy + dy);
    }

    public void moveCenterToAnimated(double cx, double cy) {
        if (moveAnimation != null) {
            moveAnimation.setCancel(true);
        }
        moveAnimation = new MoveCenterAnimation(cx, cy);
        moveAnimation.start();
    }

    private void moveCenterTo(double cx, double cy) {
        if (cx < left-0.5f) cx = left-0.5f;
        if (cx > right+0.5f) cx = right+0.5f;
        if (cy < top-0.5f) cy = top-0.5f;
        if (cy > bottom+0.5f) cy = bottom+0.5f;

        this.cx = cx;
        this.cy = cy;
        repaint();
    }

    public void zoom(double steps) {
        int size = (int) (squareSize * Math.pow(1.3, steps));
        if (size < 25) size = 25;
        if (size > 300) size = 300;
        setZoomSize(size);
    }

    private void setZoomSize(int size) {
        if (size == squareSize) return;
        squareSize = size;

        synchronized (layers) {
            for(GridLayer layer : layers) {
                layer.zoomChanged(squareSize);
            }
        }
        moveCenterTo(cx, cy); //re-check center constraints
    }

    public void showRecentHistory() {
        Collection<Tile> tiles = client.getGame().getBoard().getAllTiles();
        addLayer(new PlacementHistory(this, tiles));
    }

    public void addLayer(GridLayer layer) {
        synchronized (layers) {
            ListIterator<GridLayer> iter = layers.listIterator();
            while(iter.hasNext()) {
                GridLayer sl = iter.next();
                if (GridLayer.Z_INDEX_COMPARATOR.compare(layer, sl) <= 0) {
                    iter.previous();
                    break;
                }
            }
            iter.add(layer);
        }
        layer.layerAdded();
        repaint();
    }

    public void removeLayer(GridLayer layer) {
        layers.remove(layer);
        layer.layerRemoved();
        repaint();
    }

    public void removeLayer(Class<? extends GridLayer> type) {
        Iterator<GridLayer> iter = layers.iterator();
        while(iter.hasNext()) {
            GridLayer layer = iter.next();
            if (type.isInstance(layer)) {
                iter.remove();
                layer.layerRemoved();
            }
        }
        repaint();
    }

    //TODO ok
    @SuppressWarnings("unchecked")
    synchronized
    public <T extends GridLayer> T findDecoration(Class<T> type) {
        synchronized (layers) {
            for(GridLayer layer : layers) {
                if (type.isInstance(layer)) {
                    return (T) layer;
                }
            }
        }
        return null;
    }

    public void clearActionDecorations() {
        removeLayer(AbstractAreaLayer.class);
        removeLayer(TileActionLayer.class);
    }

    // delegated UI methods

    public void tilePlaced(Tile tile, TileLayer tileLayer) {
        Position p = tile.getPosition();

        removeLayer(AbstractTilePlacementLayer.class);
        removeLayer(PlacementHistory.class);

        if (p.x == left)  --left;
        if (p.x == right) ++right;
        if (p.y == top) --top;
        if (p.y == bottom) ++bottom;

        tileLayer.tilePlaced(tile);

        if (client.getSettings().isShowHistory()) {
            showRecentHistory();
        }
        boolean initialPlacement = client.getActivePlayer() == null;//if active player is null we are placing initial tiles
        if ((!initialPlacement && !client.isClientActive()) ||
            (initialPlacement && client.getGame().getTilePack().getCurrentTile().equals(tile))) {
            getAnimationService().registerAnimation(tile.getPosition(), new RecentPlacement(tile.getPosition()));
        }
        repaint();
    }

    private int calculateCenterX() {
        return (getWidth() - ControlPanel.PANEL_WIDTH - squareSize)/2;
    }

    private int calculateCenterY() {
        return (getHeight() - squareSize)/2;
    }

    //TODO remove profile code
    long ts, last;
    public void profile(String msg) {
        long now = System.currentTimeMillis();
        System.out.println((now-ts) + " (" + (now-last) +") : " + msg);
        last = now;
    }

    private void paintGrid(Graphics2D g2) {
        g2.setColor(UIManager.getColor("Panel.background"));
        g2.fillRect(left*squareSize, top*squareSize, (right+2)*squareSize-1, (bottom+2)*squareSize-1);
        g2.setColor(Color.LIGHT_GRAY);
        for (int i = left; i <= right; i++) {
            g2.drawLine(i*squareSize, top*squareSize, i*squareSize, (bottom+1)*squareSize);
            g2.drawLine((i+1)*squareSize-1, top*squareSize, (i+1)*squareSize-1, (bottom+1)*squareSize);
        }
        for (int i = top; i <= bottom; i++) {
            g2.drawLine(left*squareSize, i*squareSize, (right+1)*squareSize, i*squareSize);
            g2.drawLine(left*squareSize, (i+1)*squareSize-1, (right+1)*squareSize, (i+1)*squareSize-1);
        }

        profile("grid");
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        //super.paintComponent(g);

        System.out.println("------------------------");
        ts = last = System.currentTimeMillis();

        int w = getWidth(), h = getHeight();
//        int w = getWidth(), h = getHeight();
//        if (blurBuffer == null || blurBuffer.getWidth() != w || blurBuffer.getHeight() != h) {
//            blurBuffer = UiUtils.newTransparentImage(w, h);
//        }
//        Graphics2D g2 = blurBuffer.createGraphics();
//        g2.setBackground(TRANSPARENT_COLOR);
//        g2.clearRect(0, 0, w, h);

        AffineTransform origTransform = g2.getTransform();
        offsetX = calculateCenterX() - (int)(cx * squareSize);
        offsetY = calculateCenterY() - (int)(cy * squareSize);
        g2.translate(offsetX, offsetY);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        paintGrid(g2);

        //paint layers
        synchronized (layers) {
            for(GridLayer layer : layers) {
                layer.paint(g2);
                profile(layer.getClass().getSimpleName());
            }
        }

        g2.setTransform(origTransform);
        g2.translate(w - ControlPanel.PANEL_WIDTH, 0);

//        g2.translate(w - ControlPanel.PANEL_WIDTH, 0);
//        int alpha = 225, x = -20;
//        Color color = new Color(255, 255, 255, alpha);
//        g2.setColor(color);
//        g2.fillRect(x, 0, -x+ControlPanel.PANEL_WIDTH, h);
//        color = new Color(255, 255, 255, (int) (alpha * 0.7));
//        g2.setColor(color);
//        g2.fillRect(x-3, 0, 3, h);
//
//        profile("gradient");

        controlPanel.paintComponent(g2);

        profile("control panel");
    }

    class MoveCenterAnimation extends Thread {
        //TODO easing ?

        private double toCx, toCy, fromCx, fromCy;
        long start, end;
        private boolean cancel;

        public MoveCenterAnimation(double toCx, double toCy) {
            moveTo(toCx, toCy);
        }

        private void moveTo(double toCx, double toCy) {
            this.toCx = toCx;
            this.toCy = toCy;
            fromCx = cx;
            fromCy = cy;
            start = System.currentTimeMillis();
            end = start + 100;
        }

        public void setCancel(boolean cancel) {
            this.cancel = cancel;
        }

        @Override
        public void run() {
            long t;
            while (!cancel && (t = System.currentTimeMillis()) < end) {
                double dx = (double)(t - start)/(end-start)*(toCx-fromCx);
                double dy = (double)(t - start)/(end-start)*(toCy-fromCy);
                //System.out.println(fromCx+dx + " " + fromCy+dy);
                moveCenterTo(fromCx+dx, fromCy+dy);
                try {
                    Thread.sleep(15);
                } catch (InterruptedException e) {
                }
            }
            if (!cancel) {
                moveCenterTo(toCx, toCy);
            }
        }

    }

}
