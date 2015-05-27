package com.jcloisterzone.ui.controls;

import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;


public class MouseTrackingComponent extends JComponent {

    private List<MouseListeningRegion> mouseRegions = new ArrayList<>();
    private MouseListeningRegion mouseOver = null;


    public MouseTrackingComponent() {
        super();
        initMouseTracking();
    }

    private void initMouseTracking() {
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                for (MouseListeningRegion mlr : mouseRegions) {
                    Rectangle r = mlr.getRegion();
                    if (r.contains(e.getX(), e.getY())) {
                        mlr.getListener().mouseClicked(e, mlr);
                        e.consume();
                        break;
                    }
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                if (mouseOver == null) {
                    for (MouseListeningRegion mlr : mouseRegions) {
                        Rectangle r = mlr.getRegion();
                        if (r.contains(e.getX(), e.getY())) {
                            mouseOver = mlr;
                            mouseOver.getListener().mouseEntered(e, mouseOver);
                            e.consume();
                            break;
                        }
                    }
                } else {
                    Rectangle r = mouseOver.getRegion();
                    if (r.contains(e.getX(), e.getY())) {
                        e.consume();
                        return;
                    } else {
                        mouseOver.getListener().mouseExited(e, mouseOver);
                        mouseOver = null;
                    }
                }
            }
        };
      this.addMouseListener(mouseAdapter);
      this.addMouseMotionListener(mouseAdapter);
    }

    protected List<MouseListeningRegion> getMouseRegions() {
        return mouseRegions;
    }

}
