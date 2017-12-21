package com.jcloisterzone.ui.grid;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;
import com.jcloisterzone.Expansion;
import com.jcloisterzone.Player;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.config.ConfigLoader;
import com.jcloisterzone.event.GameChangedEvent;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.plugin.Plugin;
import com.jcloisterzone.ui.Client;
import com.jcloisterzone.ui.EventProxyUiController;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.ui.UIEventListener;
import com.jcloisterzone.ui.UiUtils;
import com.jcloisterzone.ui.annotations.LinkedPanel;
import com.jcloisterzone.ui.controls.ControlPanel;
import com.jcloisterzone.ui.controls.chat.ChatPanel;
import com.jcloisterzone.ui.grid.actionpanel.ActionInteractionPanel;
import com.jcloisterzone.ui.grid.layer.AbstractAreaLayer;
import com.jcloisterzone.ui.grid.layer.EventsOverlayLayer;
import com.jcloisterzone.ui.grid.layer.TileActionLayer;
import com.jcloisterzone.ui.view.GameView;

import net.miginfocom.swing.MigLayout;

public class GridPanel extends JPanel implements ForwardBackwardListener, UIEventListener {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private static final long serialVersionUID = -7013723613801929324L;

    public static int INITIAL_TILE_WIDTH = 120;

    private static final Color MESSAGE_ERROR = new Color(186, 61, 61, 245);
    private static final Color MESSAGE_HINT = new Color(147, 146, 155, 245);

    final Client client;
    final GameView gameView;
    final GameController gc;

    private final ControlPanel controlPanel;
    private final ChatPanel chatPanel;
    private ActionInteractionPanel<?> actionInteractionPanel;
    private final GameEventsPanel eventsPanel;
    private boolean isEventsPanelVisible;

    /** current board size */
    private int left, right, top, bottom;
    private int tileWidth, tileHeight;
    private Rotation boardRotation = Rotation.R0;
    private double meepleScaleFactor = 1.0;

    //focus
    private int offsetX, offsetY;
    private double cx = 0.0, cy = 0.0;
    private MoveCenterAnimation moveAnimation;

    private List<GridLayer> layers = new ArrayList<GridLayer>();
    private ErrorMessagePanel errorMsg;
    private String errorCode;

    public GridPanel(Client client, GameView gameView, ControlPanel controlPanel, ChatPanel chatPanel) {
        setDoubleBuffered(true);
        setOpaque(false);
        setLayout(new MigLayout());

        this.client = client;
        this.gameView = gameView;
        this.gc = gameView.getGameController();
        this.controlPanel = controlPanel;

        boolean networkGame = "true".equals(System.getProperty("forceChat"));
        for (Player p : gc.getGame().getState().getPlayers().getPlayers()) {
            if (!p.getSlot().isOwn()) {
                networkGame = true;
                break;
            }
        }
        this.chatPanel = networkGame ? chatPanel : null;

        Plugin rp = getBaseExpansionPlugin();
        if (rp != null) {
            //sqrt -> geometric average between width and height
            meepleScaleFactor = Math.sqrt(rp.getImageSizeRatio());
        }

        updateTileSize((int)(INITIAL_TILE_WIDTH / rp.getImageSizeRatio()));

        registerMouseListeners();
        add(controlPanel, "pos (100%-255) 0 100% 100%");
        if (chatPanel != null) {
            chatPanel.initHidingMode();
            add(chatPanel, "pos 0 0 250 100%");
        }

        eventsPanel = new GameEventsPanel(gc);
        //client.is
        add(eventsPanel, "pos 0 0 (100%-242) 36");
        setComponentZOrder(eventsPanel, chatPanel == null ? 1 : 2);
    }

    @Override
    public void registerTo(EventProxyUiController<?> gc) {
        UIEventListener.super.registerTo(gc);
        for (GridLayer layer : layers) {
            if (layer instanceof UIEventListener) {
                ((UIEventListener)layer).registerTo(gc);
            }
        }
    }

    @Override
    public void unregisterFrom(EventProxyUiController<?> gc) {
        UIEventListener.super.unregisterFrom(gc);
        for (GridLayer layer : layers) {
            if (layer instanceof UIEventListener) {
                ((UIEventListener)layer).unregisterFrom(gc);
            }
        }
    }

    public double getMeepleScaleFactor() {
        return meepleScaleFactor;
    }

    private Plugin getBaseExpansionPlugin() {
        for (Plugin plugin : client.getPlugins()) {
            if (!plugin.isEnabled()) continue;
            if (plugin.isExpansionSupported(Expansion.BASIC)) {
                return plugin;
            }
        }
        return null;
    }

    private void updateTileSize(int baseWidth) {
        Plugin plugin = getBaseExpansionPlugin();
        double ratio = plugin == null ? 1.0 : plugin.getImageSizeRatio();
        tileWidth = baseWidth;
        tileHeight = (int)(ratio * baseWidth);
    }

    public void toggleGameEvents(boolean visible) {
        isEventsPanelVisible = visible;
        eventsPanel.setVisible(visible);
        if (visible) {
            showLayer(EventsOverlayLayer.class);
        } else {
            hideLayer(EventsOverlayLayer.class);
        }
    }

    @Override
    public void forward() {
        if (actionInteractionPanel instanceof ForwardBackwardListener) {
            ((ForwardBackwardListener) actionInteractionPanel).forward();
        }
        controlPanel.getActionPanel().forward();
    }

    @Override
    public void backward() {
        if (actionInteractionPanel instanceof ForwardBackwardListener) {
            ((ForwardBackwardListener) actionInteractionPanel).backward();
        }
        controlPanel.getActionPanel().backward();
    }

    public void removeInteractionPanels() {
        int l = getComponents().length;
        for (int i = l-1; i > 0; i--) {
            Component child = getComponent(i);
            if (child == actionInteractionPanel) {
                remove(i);
                break;
            }
        }
        actionInteractionPanel = null;
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

    public GameEventsPanel getEventsPanel() {
        return eventsPanel;
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

    @Subscribe
    public void handleGameChanged(GameChangedEvent ev) {
        GameState state = ev.getCurrentState();
        PlayerAction<?> first = state.getAction();

        LinkedPanel panelAnnotation = first == null ? null : first.getClass().getAnnotation(LinkedPanel.class);
        if (panelAnnotation == null) {
            removeInteractionPanels();
        } else {
            Class<? extends ActionInteractionPanel<?>> cls = panelAnnotation.value();
            if (!cls.isInstance(actionInteractionPanel)) {
                if (actionInteractionPanel != null) {
                    removeInteractionPanels();
                }
                try {
                    actionInteractionPanel = cls.getConstructor(Client.class, GameController.class).newInstance(client, gc);
                    add(actionInteractionPanel, "pos (100%-525) 0 (100%-275) 100%"); //TODO more robust layouting
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
            actionInteractionPanel.setGameState(state);
            revalidate();
        }

        if (ev.hasPlacedTilesChanged()) {
            Rectangle rect = state.getBoardBounds();
            left = rect.x;
            right = rect.x + rect.width;
            top = rect.y;
            bottom = rect.y + rect.height;
        }

        eventsPanel.handleGameChanged(ev);

        repaint();
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
        if (!layer.isVisible()) {
            layer.onShow();
            repaint();
        }
    }

    public void showLayer(Class<? extends GridLayer> layerType) {
        for (GridLayer layer : layers) {
            if (layerType.isInstance(layer) && !layer.isVisible()) {
                layer.onShow();
            }
        }
        repaint();
    }

    public void hideLayer(GridLayer layer) {
        if (layer.isVisible()) {
            layer.onHide();
            repaint();
        }
    }

    public void hideLayer(Class<? extends GridLayer> layerType) {
        for (GridLayer layer : layers) {
            if (layerType.isInstance(layer) && layer.isVisible()) {
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
        //hideLayer(AbbeyPlacementLayer.class);
    }

    public void showErrorMessage(String errorMessage, String errCode) {
        if (errorMsg != null) {
            remove(errorMsg);
        }
        this.errorCode = errCode;
        errorMsg = new ErrorMessagePanel(errorMessage);
        errorMsg.setOpaque(true);
        add(errorMsg, "pos 0 0 (100%-242) 30");
        setComponentZOrder(errorMsg, 1);
        revalidate();
        repaint();
    }

    public void hideErrorMessage(String errorCode) {
        if (errorCode != null) {
            if (!errorCode.equals(this.errorCode)) {
                return;
            }
        }

        if (errorMsg != null) {
            remove(errorMsg);
            errorMsg = null;
            this.errorCode = null;
            repaint();
        }
    }

    // delegated UI methods

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
                    hideErrorMessage(null);
                }
            });
            add(label);
            add(icon);
        }
    }

}
