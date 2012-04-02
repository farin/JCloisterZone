package com.jcloisterzone.ui;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Transparency;
import java.awt.image.BufferedImage;

public final class UiUtils {

    private static GraphicsConfiguration graphicsConfiguration = GraphicsEnvironment
          .getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();

    private UiUtils() {}

    public static BufferedImage newTransparentImage(int w, int h) {
        return graphicsConfiguration.createCompatibleImage(w, h, Transparency.TRANSLUCENT);
    }


}
