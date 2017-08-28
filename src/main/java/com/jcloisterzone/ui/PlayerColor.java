package com.jcloisterzone.ui;

import java.awt.Color;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcloisterzone.config.Config.ColorConfig;
import com.jcloisterzone.game.Token;

public class PlayerColor {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private final Color meeple, font;
    private Map<Token, Color> tunnelColors;

    public PlayerColor() {
        meeple = Color.BLACK;
        font = Color.BLACK;
    }

    public PlayerColor(ColorConfig cfg, boolean darkTheme) {
        this.meeple = stringToColor(cfg.getMeeple(), Color.BLACK);
        this.font = stringToColor(darkTheme ? cfg.getFontDark() : cfg.getFontLight(), meeple);
    }

    public Color getMeepleColor() {
        return meeple;
    }

    public Color getFontColor() {
        return font;
    }

    public Map<Token, Color> getTunnelColors() {
        return tunnelColors;
    }

    public void setTunnelColors(Map<Token, Color> tunnelColors) {
        this.tunnelColors = tunnelColors;
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
