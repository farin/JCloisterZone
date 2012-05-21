package com.jcloisterzone.ui.controls;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.util.List;

import javax.swing.JComponent;

import com.google.common.collect.Lists;
import com.jcloisterzone.ui.Client;

public abstract class FakeComponent extends ComponentAdapter {

    protected final Client client;
    private List<MouseListeningRegion> mouseRegions = Lists.newArrayList();
    private AffineTransform transform;

    public FakeComponent(Client client) {
        this.client = client;
    }

    public void paintComponent(Graphics2D g) {
        transform = g.getTransform();
    }

    public void registerSwingComponents(JComponent parent) {
    }

    public void destroySwingComponents(JComponent parent) {
    }

    protected List<MouseListeningRegion> getMouseRegions() {
        return mouseRegions;
    }

    public void dispatchMouseEvent(MouseEvent e) {
        for(MouseListeningRegion mlr : mouseRegions) {
            Area a = transformRegion(mlr.getRegion());
            if (a.contains(e.getX(), e.getY())) {
                mlr.getListener().mouseClicked(e, mlr);
                e.consume();
                break;
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
