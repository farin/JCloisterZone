package com.jcloisterzone.ui;

import java.awt.Color;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcloisterzone.config.Config.ColorConfig;

public class PlayerColor {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private final Color meeple, font;

    public PlayerColor() {
        meeple = Color.BLACK;
        font = Color.BLACK;
    }

    public PlayerColor(ColorConfig cfg) {
        this.meeple = stringToColor(cfg.getMeeple(), Color.BLACK);
        this.font = stringToColor(cfg.getFont(), meeple);
    }

    public Color getMeepleColor() {
        return meeple;
    }

    public Color getFontColor() {
        return font;
    }

    private Color stringToColor(String s, Color defaultColor) {
        if (s == null || s.equals("")) return defaultColor;
        try {
            return UiUtils.stringToColor(s);
        } catch (IllegalArgumentException e) {
            logger.warn(e.getMessage());
            return defaultColor;
        }
    }



}
