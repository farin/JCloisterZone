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

    public static Color stringToColor(String colorName) {
        colorName = colorName.trim();
        if (colorName.startsWith("#")) {
            if (colorName.length() != 7) throw new  IllegalArgumentException("Invalid #RRGGBB sytnax: "+ colorName);
            //RGB format
            int r = Integer.parseInt(colorName.substring(1,3),16);
            int g = Integer.parseInt(colorName.substring(3,5),16);
            int b = Integer.parseInt(colorName.substring(5,7),16);
            return new Color(r,g,b);
        } else {
            //constant format
            java.lang.reflect.Field f;
            try {
                f = Color.class.getField(colorName);
                return (Color) f.get(null);
            } catch (Exception e1) {
                throw new IllegalArgumentException("Unknow Color constant: " + colorName);
            }
        }
    }

}
