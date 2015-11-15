package com.jcloisterzone.ui.grid;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.MouseInputListener;

import net.miginfocom.swing.MigLayout;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.jcloisterzone.Player;
import com.jcloisterzone.XMLUtils;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.config.ConfigLoader;
import com.jcloisterzone.event.TileEvent;
import com.jcloisterzone.game.Snapshot;
import com.jcloisterzone.ui.Client;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.ui.UiUtils;
import com.jcloisterzone.ui.controls.ControlPanel;
import com.jcloisterzone.ui.controls.chat.ChatPanel;
import com.jcloisterzone.ui.grid.layer.AbbeyPlacementLayer;
import com.jcloisterzone.ui.grid.layer.AbstractAreaLayer;
import com.jcloisterzone.ui.grid.layer.AbstractTilePlacementLayer;
import com.jcloisterzone.ui.grid.layer.TileActionLayer;
import com.jcloisterzone.ui.view.GameView;

public class GridPanel extends JPanel implements ForwardBackwardListener {

    private static final long serialVersionUID = -7013723613801929324L;

    public static int INITIAL_TILE_WIDTH = 120;

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
    private int tileWidth, tileHeight;
    private Rotation boardRotation = Rotation.R0;

    //focus
    private int offsetX, offsetY;
    private double cx = 0.0, cy = 0.0;
    private MoveCenterAnimation moveAnimation;

    private List<GridLayer> layers = new ArrayList<GridLayer>();
    private ErrorMessagePanel errorMsg;

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

        updateTileSize(INITIAL_TILE_WIDTH);


        if (snapshot != null) {
            NodeList nl = snapshot.getTileElements();
            for (int i = 0; i < nl.getLength(); i++) {
                Element el = (Element) nl.item(i);
                Position pos = XMLUtils.extractPosition(el);
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

    private void updateTileSize(int baseWidth) {
        tileWidth = baseWidth;
        //tileHeight = baseWidth;
        tileHeight = (int)(240.0/340.0 * baseWidth);
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

        private void moveTo(MouseEvent e) {
            int clickX = e.getX()-offsetX;
            int clickY = e.getY()-offsetY;
            moveCenterToAnimated(clickX/(double)tileWidth, clickY/(double)tileHeight);
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            switch (e.getButton()) {
            case MouseEvent.BUTTON2:
                moveTo(e);
                break;
            case MouseEvent.BUTTON3:
                if (e.isShiftDown()) {
                    moveTo(e);
                    break;
                } //else forward
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
            double rdx = dx/(double)tileWidth;
            double rdy = dy/(double)tileHeight;

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

    public int getTileWidth() {
        return tileWidth;
    }

    public int getTileHeight() {
        return tileHeight;
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

//    public String getErrorMessage() {
//        return errorMessage;
//    }
//
//    public void setErrorMessage(String errorMessage) {
//        this.errorMessage = errorMessage;
//    }


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
        double dx = xSteps * 30.0f / tileWidth;
        double dy = ySteps * 30.0f / tileHeight;
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
        int size = (int) (tileWidth * Math.pow(1.3, steps));
        if (size < 25) size = 25;
        if (size > 300) size = 300;
        setZoomSize(size);
    }

    private void setZoomSize(int size) {
        if (size == tileWidth) return;

        updateTileSize(size);
        synchronized (layers) {
            for (GridLayer layer : layers) {
                layer.zoomChanged(tileWidth);
            }
        }
        moveCenterTo(cx, cy); //re-check center constraints
    }

    public void rotateBoard() {
        boardRotation = boardRotation.next();
        //TODO rotate around current focus instead of (0,0) - need to compensate cx, cy
        //TODO smooth rotation

        synchronized (layers) {
            for (GridLayer layer : layers) {
                layer.boardRotated(boardRotation);
            }
        }
        repaint();
    }

    public Rotation getBoardRotation() {
        return boardRotation;
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

    public void showErrorMessage(String errorMessage) {
        if (errorMsg != null) {
            remove(errorMsg);
        }
        errorMsg = new ErrorMessagePanel(errorMessage);
        errorMsg.setOpaque(true);
        add(errorMsg, "pos 0 0 (100%-242) 30");
        revalidate();
        repaint();
    }

    // delegated UI methods

    public void tileEvent(TileEvent ev) {
        hideLayer(AbstractTilePlacementLayer.class);

        if (ev.getType() == TileEvent.PLACEMENT) {
            Position p = ev.getPosition();

            if (p.x == left) --left;
            if (p.x == right) ++right;
            if (p.y == top) --top;
            if (p.y == bottom) ++bottom;

        }
        repaint();
    }

    private int calculateCenterX() {
        return (getWidth() - ControlPanel.PANEL_WIDTH - tileWidth)/2;
    }

    private int calculateCenterY() {
        return (getHeight() - tileHeight)/2;
    }

//    //TODO remove profile code
//    long ts, last;
//    public void profile(String msg) {
//        long now = System.currentTimeMillis();
//        System.out.println((now-ts) + " (" + (now-last) +") : " + msg);
//        last = now;
//    }


    public Point2D getRelativePoint(Point2D point) {
        AffineTransform af = boardRotation.inverse().getAffineTransform(tileWidth, tileHeight);
        af.translate(-offsetX, -offsetY);
        return af.transform(point, null);
    }

    @Override
    protected void paintChildren(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        //System.err.println("GP " + g2.getTransform());

//        System.out.println("------------------------");
//        ts = last = System.currentTimeMillis();

        int w = getWidth();

        AffineTransform origTransform = g2.getTransform();
        offsetX = calculateCenterX() - (int)(cx * tileWidth);
        offsetY = calculateCenterY() - (int)(cy * tileHeight);
        g2.translate(offsetX, offsetY);
        if (boardRotation != Rotation.R0) {
            AffineTransform af = boardRotation.getAffineTransform(tileWidth, tileHeight);
            g2.transform(af);
        }

        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        //paint layers

        for (GridLayer layer : layers) {
            if (layer.isVisible()) {
                layer.paint(g2);
            }
//          profile(layer.getClass().getSimpleName());
        }

        g2.setTransform(origTransform);

        //paintMessages(g2, innerWidth);
        super.paintChildren(g);
    }

    public BufferedImage takeScreenshot() {
        //calculate size of play board
        Integer screenshotScaleValue = client.getConfig().getScreenshots().getScale();
        int screenshotScale = screenshotScaleValue == null ? ConfigLoader.DEFAULT_SCREENSHOT_SCALE : screenshotScaleValue;
        int totalWidth = screenshotScale*(right-left+1);
        int totalHeight = screenshotScale*(bottom-top+1);
        //if (totalHeight < getHeight()) totalHeight = getHeight();

        //centre the image
        int transX = -screenshotScale*(left);
        int transY = -screenshotScale*(top);

        //create the image
        BufferedImage im = new BufferedImage(totalWidth + controlPanel.getWidth(), totalHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = (Graphics2D) im.getGraphics();
        if (!"true".equals(System.getProperty("transparentScreenshots"))){
            graphics.setBackground(new Color(240, 240, 240, 255));
            graphics.clearRect(0, 0, im.getWidth(), im.getHeight());
        }
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        //centre the image
        graphics.translate(transX, transY);

        //Layers use squareSize for painting, make sure the squaresize (eg: zoom) is set
        //TODO is this dangerous if GridPanel is rendered while print screening?

        int origWidth = tileWidth;
        updateTileSize(screenshotScale);
        for (GridLayer layer : layers) {
            if (layer.isVisible()) {
                //TODO calling zoomChanged can broke something, don't do it
                layer.zoomChanged(screenshotScale);
                layer.paint(graphics);
                layer.zoomChanged(origWidth);
            }
        }
        //set it back
        updateTileSize(origWidth);

        //reset translation
        graphics.translate(-transX, -transY);

        //render the control panel on the far right
        graphics.translate(totalWidth+30, 0);
        controlPanel.paint(graphics);
        return im;
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

    public static final ImageIcon CLOSE_ICON = UiUtils.scaleImageIcon("sysimages/close-white.png", 20, 20);

    class ErrorMessagePanel extends JPanel {

        public ErrorMessagePanel(String text) {
            setBackground(MESSAGE_ERROR);
            setLayout(new MigLayout("fill", "[]push[]"));
            JLabel label = new JLabel(text);
            label.setForeground(Color.WHITE);
            label.setFont(new Font(null, Font.PLAIN, 16));
            JLabel icon = new JLabel(CLOSE_ICON);
            icon.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            icon.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    GridPanel.this.remove(ErrorMessagePanel.this);
                    GridPanel.this.repaint();
                }
            });
            add(label);
            add(icon);
        }

    }

}
