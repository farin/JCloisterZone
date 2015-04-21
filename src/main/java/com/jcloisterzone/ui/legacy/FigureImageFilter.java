package com.jcloisterzone.ui.legacy;

import java.awt.Color;
import java.awt.image.RGBImageFilter;

import com.jcloisterzone.ui.UiUtils;


/**
 * Filter to change figure color.
 * @author Roman Krejcik
 */
public class FigureImageFilter extends RGBImageFilter {
    private Color c;
    private boolean white2black = false;

    /**
     * Initializes a new instance for colorize figure to given color
     * @param c demand color of figure
     */
    public FigureImageFilter(Color c) {
        // The filter's operation does not depend on the
        // pixel's location, so IndexColorModels can be
        // filtered directly.
        canFilterIndexColorModel = true;
        this.c = c;
        white2black = UiUtils.isBrightColor(c);
    }

    public int filterRGB(int x, int y, int rgb) {
        if (rgb == Color.MAGENTA.getRGB())
            return c.getRGB();
        if (white2black && rgb == Color.WHITE.getRGB())
            return Color.BLACK.getRGB();

        return rgb;
    }
}