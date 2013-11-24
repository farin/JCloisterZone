package com.jcloisterzone.ui.controls;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

import com.jcloisterzone.ui.Client;

public abstract class FakeComponent extends ComponentAdapter {

    private static final int DEFAULT_PANEL_WIDTH = 250;

    protected final Client client;
    private List<MouseListeningRegion> mouseRegions = new ArrayList<>();
    private MouseListeningRegion mouseOver = null;
    private AffineTransform transform;

    public FakeComponent(Client client) {
        this.client = client;
    }

    public int getWidth() {
        return DEFAULT_PANEL_WIDTH;
    }

    public void paintComponent(Graphics2D g) {
        transform = g.getTransform();
    }

    @Override
    public void componentResized(ComponentEvent e) {
        layoutSwingComponents((JComponent) e.getComponent());
        e.getComponent().repaint();
    }

    public void registerSwingComponents(JComponent parent) {
    }

    public void destroySwingComponents(JComponent parent) {
    }

    public void layoutSwingComponents(JComponent parent) {
    }

    protected List<MouseListeningRegion> getMouseRegions() {
        return mouseRegions;
    }

    public void dispatchMouseEvent(MouseEvent e) {
        switch (e.getID()) {
        case MouseEvent.MOUSE_CLICKED:
            dispatchMouseClick(e);
            break;
        case MouseEvent.MOUSE_MOVED:
            dispatchMouseMove(e);
            break;
        }
    }

    public void dispatchMouseClick(MouseEvent e) {
        for (MouseListeningRegion mlr : mouseRegions) {
            Area a = transformRegion(mlr.getRegion());
            if (a.contains(e.getX(), e.getY())) {
                 mlr.getListener().mouseClicked(e, mlr);
                 e.consume();
                 break;
            }
        }
    }

    public void dispatchMouseMove(MouseEvent e) {
        if (mouseOver == null) {
             for (MouseListeningRegion mlr : mouseRegions) {
                 Area a = transformRegion(mlr.getRegion());
                 if (a.contains(e.getX(), e.getY())) {
                     mouseOver = mlr;
                     mouseOver.getListener().mouseEntered(e, mouseOver);
                     e.consume();
                     break;
                 }
             }
        } else {
            Area a = transformRegion(mouseOver.getRegion());
            if (a.contains(e.getX(), e.getY())) {
                e.consume();
                return;
            } else {
                mouseOver.getListener().mouseExited(e, mouseOver);
                mouseOver = null;
            }
        }

    }

    protected Area transformRegion(Rectangle r) {
        Area a = new Area(r);
        a.transform(transform);
        return a;
    }

//    protected Rectangle computeBounds(int x, int y, int width, int height) {
//        return transformRegion(new Rectangle(x, y, width, height)).getBounds();
//    }

}
