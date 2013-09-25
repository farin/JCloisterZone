package com.jcloisterzone.ui.grid;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;


public class DragInsensitiveMouseClickListener implements MouseListener {

    public static final int PRECISION = 15;
    private final MouseListener target;

    public MouseEvent pressed;

    public DragInsensitiveMouseClickListener(MouseListener target) {
        this.target = target;
    }

    @Override
    public final void mousePressed(MouseEvent e) {
        pressed = e;
        target.mousePressed(e);
    }

    @Override
    public final void mouseReleased(MouseEvent e) {
        double horizontalTravel = Math.abs(pressed.getXOnScreen() - e.getXOnScreen());
        double verticalTravel = Math.abs(pressed.getYOnScreen() - e.getYOnScreen());

        target.mouseReleased(e);
        if (horizontalTravel + verticalTravel < PRECISION) {
            MouseEvent clickEvent = new MouseEvent((Component) pressed.getSource(),
                    MouseEvent.MOUSE_CLICKED, e.getWhen(), pressed.getModifiers(),
                    pressed.getX(), pressed.getY(), pressed.getXOnScreen(), pressed.getYOnScreen(),
                    pressed.getClickCount(), pressed.isPopupTrigger(), pressed.getButton());
            target.mouseClicked(clickEvent);
        }
        pressed = null;
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
}
