package com.jcloisterzone.ui.component;

import java.awt.Color;
import java.awt.event.MouseAdapter;

import javax.swing.JComponent;

public class HoverMouseListener extends MouseAdapter {

    public void mouseEntered(java.awt.event.MouseEvent evt) {
        JComponent comp = (JComponent) evt.getComponent();
        comp.setBackground(Color.BLACK);
        //comp.setBorder(BorderFactory.createLineBorder(Color.BLACK));
    }

    public void mouseExited(java.awt.event.MouseEvent evt) {
        JComponent comp = (JComponent) evt.getComponent();
        //comp.setBackground(UIManager.getColor("control"));
        //comp.setBorder(null);
    }
}
