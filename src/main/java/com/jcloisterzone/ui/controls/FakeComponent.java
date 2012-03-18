package com.jcloisterzone.ui.controls;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.util.List;

import javax.swing.JComponent;

import com.google.common.collect.Lists;

public abstract class FakeComponent {

    private List<MouseListeningRegion> mouseRegions = Lists.newArrayList();
    private AffineTransform transform;

    public void paintComponent(Graphics2D g) {
        transform = g.getTransform();
    }

    List<MouseListeningRegion> getMouseRegions() {
        return mouseRegions;
    }

    public void registerListener(JComponent parent) {
        parent.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                for(MouseListeningRegion mlr : mouseRegions) {
                    Area a = transformRegion(mlr.getRegion());
                    if (a.contains(e.getX(), e.getY())) {
                        mlr.getListener().mouseClicked(e, mlr);
                    }
                }
            }

        });
    }

    private Area transformRegion(Rectangle r) {
        Area a = new Area(r);
        a.transform(transform);
        return a;
    }
}
