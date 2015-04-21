package com.jcloisterzone.ui.grid;

import java.awt.Component;
import java.awt.event.MouseEvent;

import javax.swing.event.MouseInputListener;

public class DragInsensitiveMouseClickListener implements MouseInputListener {

    protected static final int MAX_CLICK_DISTANCE = 15;

    private final MouseInputListener target;

    public MouseEvent pressed;

    public DragInsensitiveMouseClickListener(MouseInputListener target) {
        this.target = target;
    }

    @Override
    public final void mousePressed(MouseEvent e) {
        pressed = e;
        target.mousePressed(e);
    }

    private int getDragDistance(MouseEvent e) {
        int distance = 0;
        distance += Math.abs(pressed.getXOnScreen() - e.getXOnScreen());
        distance += Math.abs(pressed.getYOnScreen() - e.getYOnScreen());
        return distance;
    }

    @Override
    public final void mouseReleased(MouseEvent e) {
        target.mouseReleased(e);

        if (pressed != null) {
            if (getDragDistance(e) < MAX_CLICK_DISTANCE) {
                MouseEvent clickEvent = new MouseEvent((Component) pressed.getSource(),
                        MouseEvent.MOUSE_CLICKED, e.getWhen(), pressed.getModifiers(),
                        pressed.getX(), pressed.getY(), pressed.getXOnScreen(), pressed.getYOnScreen(),
                        pressed.getClickCount(), pressed.isPopupTrigger(), pressed.getButton());
                target.mouseClicked(clickEvent);
            }
            pressed = null;
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        //do nothing, handled by pressed/released handlers
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        target.mouseEntered(e);
    }

    @Override
    public void mouseExited(MouseEvent e) {
        target.mouseExited(e);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (pressed != null) {
            if (getDragDistance(e) < MAX_CLICK_DISTANCE) return; //do not trigger drag yet (distance is in "click" perimeter
            pressed = null;
        }
        target.mouseDragged(e);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        target.mouseMoved(e);
    }
}
