package com.jcloisterzone.ui.theme;

import java.awt.Color;

public class Theme {

    private boolean dark;
    private Color mainBg;
    private Color panelBg;
    private Color transparentPanelBg;
    private Color semiTransparentBg;
    private Color playerBoxBg;
    private Color panelShadow;
    private Color markerColor;
    private Color headerFontColor;
    private Color delimiterTopColor;
    private Color delimiterBottomColor;
    private Color inputBg;
    private Color transparentInputBg;
    private Color tileBorder;
    private Color tilePlacementColor;
    private Color chatNeutralColor;
    private Color textColor;

    //TODO better to based shadow on text color?
    private Color fontShadowColor;

    public boolean isDark() {
        return dark;
    }
    public Color getMainBg() {
        return mainBg;
    }
    public Color getPanelBg() {
        return panelBg;
    }
    public Color getTransparentPanelBg() {
        return transparentPanelBg;
    }
    public Color getSemiTransparentBg() {
        return semiTransparentBg;
    }
    public Color getPlayerBoxBg() {
        return playerBoxBg;
    }
    public Color getPanelShadow() {
        return panelShadow;
    }
    public Color getMarkerColor() {
        return markerColor;
    }
    public Color getHeaderFontColor() {
        return headerFontColor;
    }
    public Color getDelimiterBottomColor() {
        return delimiterBottomColor;
    }
    public Color getDelimiterTopColor() {
        return delimiterTopColor;
    }
    public Color getTileBorder() {
        return tileBorder;
    }
    public Color getInputBg() {
        return inputBg;
    }
    public Color getTransparentInputBg() {
        return transparentInputBg;
    }
    public Color getTilePlacementColor() {
        return tilePlacementColor;
    }
    public Color getChatNeutralColor() {
        return chatNeutralColor;
    }
    public Color getTextColor() {
        return textColor;
    }
    public Color getFontShadowColor() {
        return fontShadowColor;
    }

    public static final Theme LIGHT = new Theme();
    public static final Theme DARK = new Theme();

    static {
        LIGHT.dark = false;
        LIGHT.mainBg = null;
        LIGHT.panelBg = null;
        LIGHT.transparentPanelBg = new Color(255, 255, 255, 225);
        LIGHT.semiTransparentBg = new Color(255, 255, 255, 245);
        LIGHT.playerBoxBg =  new Color(219, 219, 219);
        LIGHT.panelShadow = new Color(255, 255, 255, 158);
        LIGHT.markerColor = Color.BLACK;
        LIGHT.headerFontColor =  new Color(190, 190, 190);
        LIGHT.delimiterTopColor = new Color(250,250,250);
        LIGHT.delimiterBottomColor = new Color(220,220,220);
        LIGHT.tileBorder = Color.WHITE;
        LIGHT.inputBg = Color.WHITE;
        LIGHT.transparentInputBg = new Color(255, 255, 255, 8);
        LIGHT.tilePlacementColor = Color.LIGHT_GRAY;
        LIGHT.chatNeutralColor = Color.DARK_GRAY;
        LIGHT.textColor = null;
        LIGHT.fontShadowColor = new Color(0, 0, 0, 60);

        DARK.dark = true;
        DARK.mainBg = new Color(40, 44, 52);
        DARK.panelBg = new Color(33, 37, 43);
        DARK.transparentPanelBg = new Color(33, 37, 43, 220);
        DARK.semiTransparentBg = new Color(33, 37, 43, 245);
        DARK.playerBoxBg = new Color(70, 70, 70);
        DARK.panelShadow = new Color(33, 37, 43, 150);
        DARK.markerColor = Color.WHITE;
        DARK.headerFontColor =  new Color(200, 200, 200);
        DARK.delimiterTopColor = new Color(0, 0, 0);
        DARK.delimiterBottomColor = new Color(50, 50, 50);
        DARK.tileBorder = new Color(128, 128, 128);
        DARK.inputBg = new Color(30, 33, 39);
        DARK.transparentInputBg = new Color(30, 33, 39, 245);
        DARK.tilePlacementColor = Color.GRAY;
        DARK.chatNeutralColor = Color.WHITE;
        DARK.textColor = new Color(200, 200, 200);
        DARK.fontShadowColor = null; //no shadow
    }
}
