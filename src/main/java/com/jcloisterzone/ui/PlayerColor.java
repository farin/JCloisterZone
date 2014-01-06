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

    public PlayerColor(Color meeple, Color font) {
        super();
        this.meeple = meeple;
        this.font = font;
    }

    public PlayerColor(ColorConfig cfg) {
        Color meeple, font;
        try {
            meeple = UiUtils.stringToColor(cfg.getMeeple());
            font = UiUtils.stringToColor(cfg.getFont());
        } catch (IllegalArgumentException e) {
            logger.warn(e.getMessage());
            meeple = Color.BLACK;
            font = Color.BLACK;
        }
        this.meeple = meeple;
        this.font = font;
    }

    public Color getMeepleColor() {
        return meeple;
    }

    public Color getFontColor() {
        return font;
    }



}
