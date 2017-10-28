package com.jcloisterzone.ui.gtk;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.UIManager;

/**
 * Fix for GTK+ LaF to respect UIManager defaults
 */
public class ThemedJLabel extends JLabel {

    public ThemedJLabel() {
        super();
    }

    public ThemedJLabel(Icon image, int horizontalAlignment) {
        super(image, horizontalAlignment);
    }

    public ThemedJLabel(Icon image) {
        super(image);
    }

    public ThemedJLabel(String text, Icon icon, int horizontalAlignment) {
        super(text, icon, horizontalAlignment);
    }

    public ThemedJLabel(String text, int horizontalAlignment) {
        super(text, horizontalAlignment);
    }

    public ThemedJLabel(String text) {
        super(text);
    }

    {
        setForeground(UIManager.getColor("Label.foreground"));
    }

}
