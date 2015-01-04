package com.jcloisterzone.ui.grid;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import javax.swing.JPanel;
import javax.swing.event.MouseInputListener;

import net.miginfocom.swing.MigLayout;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.jcloisterzone.Player;
import com.jcloisterzone.XmlUtils;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.event.TileEvent;
import com.jcloisterzone.game.Snapshot;
import com.jcloisterzone.ui.Client;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.ui.animation.AnimationService;
import com.jcloisterzone.ui.animation.RecentPlacement;
import com.jcloisterzone.ui.controls.ControlPanel;
import com.jcloisterzone.ui.controls.chat.ChatPanel;
import com.jcloisterzone.ui.grid.layer.AbbeyPlacementLayer;
import com.jcloisterzone.ui.grid.layer.AbstractAreaLayer;
import com.jcloisterzone.ui.grid.layer.AbstractTilePlacementLayer;
import com.jcloisterzone.ui.grid.layer.TileActionLayer;
import com.jcloisterzone.ui.grid.layer.TileLayer;
import com.jcloisterzone.ui.view.GameView;

public class GridPanel extends JPanel implements ForwardBackwardListener {

    private static final long serialVersionUID = -7013723613801929324L;

    public static int INITIAL_SQUARE_SIZE = 120;
    private static final int STARTING_GRID_SIZE = 3;

    private static final Color MESSAGE_ERROR = new Color(186, 61, 61, 245);
    private static final Color MESSAGE_HINT = new Color(147, 146, 155, 245);

    final Client client;
    final GameView gameView;
    final GameController gc;

    private final ControlPanel controlPanel;
    private final ChatPanel chatPanel;
    private BazaarPanel bazaarPanel;
    private SelectMageWitchRemovalPanel mageWitchPanel;

    /** current board size */
    private int left, right, top, bottom;
    private int squareSize;

    //focus
    private int offsetX, offsetY;
    private double cx = 0.0, cy = 0.0;
    private MoveCenterAnimation moveAnimation;

    private List<GridLayer> layers = new ArrayList<GridLayer>();
    private String errorMessage;
    //private String hintMessage;

    public GridPanel(Client client, GameView gameView, ControlPanel controlPanel, ChatPanel chatPanel, Snapshot snapshot) {
        setDoubleBuffered(true);
        setOpaque(false);
        setLayout(new MigLayout());

        this.client = client;
        this.gameView = gameView;
        this.gc = gameView.getGameController();
        this.controlPanel = controlPanel;

        boolean networkGame = "true".equals(System.getProperty("forceChat"));
        for (Player p : gc.getGame().getAllPlayers()) {
            if (!p.getSlot().isOwn()) {
                networkGame = true;
                break;
            }
        }
        this.chatPanel = networkGame ? chatPanel : null;

        squareSize = INITIAL_SQUARE_SIZE;
        left = 0 - STARTING_GRID_SIZE / 2;
        right = 0 + STARTING_GRID_SIZE / 2;
        top = 0 - STARTING_GRID_SIZE / 2;
        bottom = 0 + STARTING_GRID_SIZE / 2;

        if (snapshot != null) {
            NodeList nl = snapshot.getTileElements();
            for (int i = 0; i < nl.getLength(); i++) {
                Element el = (Element) nl.item(i);
                Position pos = XmlUtils.extractPosition(el);
                if (pos.x <= left) left = pos.x - 1;
                if (pos.x >= right) right = pos.x + 1;
                if (pos.y <= top) top = pos.y - 1;
                if (pos.y >= bottom) bottom = pos.y + 1;
            }
        }
        registerMouseListeners();
        add(controlPanel, "pos (100%-255) 0 100% 100%");
        if (chatPanel != null) {
            chatPanel.initHidingMode();
            add(chatPanel, "pos 0 0 250 100%");
        }
    }


    @Override
    public void forward() {
        if (bazaarPanel != null) {
            bazaarPanel.forward();
        }
        controlPanel.getActionPanel().forward();
    }

    @Override
    public void backward() {
        if (bazaarPanel != null) {
            bazaarPanel.backward();
        }
        controlPanel.getActionPanel().backward();
    }

    public void removeInteractionPanels() {
        int l = getComponents().length;
        for (int i = l-1; i > 0; i--) {
            Component child = getComponent(i);
            if (child.getClass().isAnnotationPresent(InteractionPanel.class)) {
                remove(i);
            }
        }
        bazaarPanel = null;
    }

    class GridPanelMouseListener extends MouseAdapter implements MouseInputListener {

        private MouseEvent dragSource;
        double sourceCx, sourceCy;

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
                forward();
                break;
            case 4:
                backward();
                break;
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {
            dragSource = e;
            sourceCx = cx;
            sourceCy = cy;
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            dragSource = null;
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (dragSource == null) return; //threading issues
            //px values
            int dx = e.getX() - dragSource.getX();
            int dy = e.getY() - dragSource.getY();
            //relative values
            double rdx = dx/(double)squareSize;
            double rdy = dy/(double)squareSize;

            moveCenterTo(sourceCx-rdx, sourceCy-rdy);
        }
    }

    private void registerMouseListeners() {
        DragInsensitiveMouseClickListener listener = new DragInsensitiveMouseClickListener(new GridPanelMouseListener());
        addMouseListener(listener);
        addMouseMotionListener(listener);
        addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                zoom(-e.getWheelRotation());
            }
        });
    }

    public Tile getTile(Position p) {
        return gc.getGame().getBoard().get(p);
    }

    public Client getClient() {
        return client;
    }

    public AnimationService getAnimationService() {
        return gc.getGameView().getMainPanel().getAnimationService();
    }

    public int getSquareSize() {
        return squareSize;
    }


    public int getOffsetX() {
        return offsetX;
    }

    public int getOffsetY() {
        return offsetY;
    }

    public ChatPanel getChatPanel() {
        return chatPanel;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }


//    public String getHintMessage() {
//        return hintMessage;
//    }
//
//
//    public void setHintMessage(String hintMessage) {
//        this.hintMessage = hintMessage;
//    }


    public BazaarPanel getBazaarPanel() {
        return bazaarPanel;
    }


    public void setBazaarPanel(BazaarPanel bazaarPanel) {
        this.bazaarPanel = bazaarPanel;
    }




    public SelectMageWitchRemovalPanel getMageWitchPanel() {
        return mageWitchPanel;
    }


    public void setMageWitchPanel(SelectMageWitchRemovalPanel mageWitchPanel) {
        this.mageWitchPanel = mageWitchPanel;
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
            for (GridLayer layer : layers) {
                layer.zoomChanged(squareSize);
            }
        }
        moveCenterTo(cx, cy); //re-check center constraints
    }


    void addLayer(GridLayer layer) {
       addLayer(layer, true);
    }

    void addLayer(GridLayer layer, boolean visible) {
        layers.add(layer);
        if (visible) {
            layer.onShow();
        }
    }

    public void showLayer(GridLayer layer) {
        layer.onShow();
        repaint();
    }

    public void showLayer(Class<? extends GridLayer> layerType) {
        for (GridLayer layer : layers) {
            if (layerType.isInstance(layer)) {
                layer.onShow();
            }
        }
        repaint();
    }

    public void hideLayer(GridLayer layer) {
        layer.onHide();
        repaint();
    }

    public void hideLayer(Class<? extends GridLayer> layerType) {
        for (GridLayer layer : layers) {
            if (layerType.isInstance(layer)) {
                layer.onHide();
            }
        }
        repaint();
    }

    @SuppressWarnings("unchecked")
    public <T extends GridLayer> T findLayer(Class<T> type) {
        for (GridLayer layer : layers) {
            if (type.isInstance(layer)) {
                return (T) layer;
            }
        }
        throw new NoSuchElementException("Layer " + type.toString() + " doesn't exist.");
    }

    public boolean isLayerVisible(Class<? extends GridLayer> type) {
        for (GridLayer layer : layers) {
            if (layer.isVisible() && type.isInstance(layer)) {
                return true;
            }
        }
        return false;
    }

    public void clearActionDecorations() {
        hideLayer(AbstractAreaLayer.class);
        hideLayer(TileActionLayer.class);
        hideLayer(AbbeyPlacementLayer.class);
    }

    // delegated UI methods

    public void tileEvent(TileEvent ev, TileLayer tileLayer) {
        Tile tile = ev.getTile();
        Position p = ev.getPosition();

        hideLayer(AbstractTilePlacementLayer.class);

        if (ev.getType() == TileEvent.PLACEMENT) {
            if (p.x == left) --left;
            if (p.x == right) ++right;
            if (p.y == top) --top;
            if (p.y == bottom) ++bottom;

            tileLayer.tilePlaced(tile);

            boolean initialPlacement = ev.getTriggeringPlayer() == null;//if triggering player is null we are placing initial tiles
            if ((!initialPlacement && !ev.getTriggeringPlayer().isLocalHuman()) ||
                (initialPlacement && tile.equals(gameView.getGame().getCurrentTile()))) {
                getAnimationService().registerAnimation(new RecentPlacement(tile.getPosition()));
            }
        } else if (ev.getType() == TileEvent.REMOVE) {
            tileLayer.tileRemoved(tile);
        }
        repaint();
    }

    private int calculateCenterX() {
        return (getWidth() - ControlPanel.PANEL_WIDTH - squareSize)/2;
    }

    private int calculateCenterY() {
        return (getHeight() - squareSize)/2;
    }

//    //TODO remove profile code
//    long ts, last;
//    public void profile(String msg) {
//        long now = System.currentTimeMillis();
//        System.out.println((now-ts) + " (" + (now-last) +") : " + msg);
//        last = now;
//    }

//    private void paintGrid(Graphics2D g2) {
//        g2.setColor(UIManager.getColor("Panel.background"));
//        g2.fillRect(left*squareSize, top*squareSize, (right+2)*squareSize-1, (bottom+2)*squareSize-1);
//        g2.setColor(Color.LIGHT_GRAY);
//        for (int i = left; i <= right; i++) {
//            g2.drawLine(i*squareSize, top*squareSize, i*squareSize, (bottom+1)*squareSize);
//            g2.drawLine((i+1)*squareSize-1, top*squareSize, (i+1)*squareSize-1, (bottom+1)*squareSize);
//        }
//        for (int i = top; i <= bottom; i++) {
//            g2.drawLine(left*squareSize, i*squareSize, (right+1)*squareSize, i*squareSize);
//            g2.drawLine(left*squareSize, (i+1)*squareSize-1, (right+1)*squareSize, (i+1)*squareSize-1);
//        }

//        profile("grid");
//    }

    @Override
    protected void paintChildren(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        //System.err.println("GP " + g2.getTransform());

//        System.out.println("------------------------");
//        ts = last = System.currentTimeMillis();

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
        //paintGrid(g2);

        //paint layers

        for (GridLayer layer : layers) {
            if (layer.isVisible()) {
                layer.paint(g2);
            }
//          profile(layer.getClass().getSimpleName());
        }


        g2.setTransform(origTransform);
        g2.translate(w - ControlPanel.PANEL_WIDTH, 0);


        int innerWidth;
//        if (secondPanel != null) {
//            g2.translate(-secondPanel.getWidth()-60, 0);
//            secondPanel.paintComponent(g2);
//            innerWidth = (int) g2.getTransform().getTranslateX();
//        } else {
            innerWidth = (int) g2.getTransform().getTranslateX() - ControlPanel.LEFT_PADDING - ControlPanel.PANEL_SHADOW_WIDTH;
       // }
        g2.setTransform(origTransform);

        paintMessages(g2, innerWidth);
        super.paintChildren(g);
    }

    private void paintMessages(Graphics2D g2, int innerWidth) {
        int y = 0;
//        if (hintMessage != null) {
//            g2.setColor(MESSAGE_HINT);
//            g2.fillRect(0, y, innerWidth, 36);
//            g2.setFont(new Font(null, Font.PLAIN, 16));
//            g2.setColor(Color.WHITE);
//            g2.drawString(hintMessage, 30, y+23);
//            y += 42;
//        }
        if (errorMessage != null) {
            g2.setColor(MESSAGE_ERROR);
            g2.fillRect(0, y, innerWidth, 36);
            g2.setFont(new Font(null, Font.PLAIN, 16));
            g2.setColor(Color.WHITE);
            g2.drawString(errorMessage, 30, y+23);
            y += 42;
        }

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
