package com.jcloisterzone.ui;

import java.awt.Color;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Transparency;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;
import javax.swing.Timer;

public final class UiUtils {

    public static final Color HIGHLIGHT = new Color(255, 253, 200);

    private static GraphicsConfiguration graphicsConfiguration = GraphicsEnvironment
          .getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();

    private UiUtils() {}

    public static BufferedImage newTransparentImage(int w, int h) {
        return graphicsConfiguration.createCompatibleImage(w, h, Transparency.TRANSLUCENT);
    }

    public static boolean isBrightColor(Color c) {
        return c.getRed() > 192 && c.getGreen() > 192 && c.getBlue() < 64;
    }

    public static void highlightComponent(final JComponent c) {
        if (c.getBackground() == HIGHLIGHT) return; //prevent two timers
        c.setBackground(HIGHLIGHT);
        Timer t = new Timer(800, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                c.setBackground(null);
            }
        });
        t.setRepeats(false);
        t.start();
    }

}
