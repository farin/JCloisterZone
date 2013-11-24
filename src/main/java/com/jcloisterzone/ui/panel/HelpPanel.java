package com.jcloisterzone.ui.panel;

import static com.jcloisterzone.ui.I18nUtils._;

import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

public class HelpPanel extends JPanel {

    public HelpPanel() {
        setLayout(new MigLayout("", "[grow]", "[]10[]10[]10[]"));
        JLabel paragraph;

        paragraph = new JLabel(_("<html>Use <b>A</b>, <b>S</b>, <b>W</b>, <b>D</b> or <b>cursor</b> keys to scroll the board. " +
                "Click with <b>middle</b> mouse button to center the board. " +
                "Zoom with <b>+</b> or <b>-</b> keys or use mouse <b>wheel</b>.</html>"));
        add(paragraph, "cell 0 0,grow");

        paragraph = new JLabel(_("<html>Rotate a tile by <b>Tab</b> key or <b>right</b> mouse click. Place it by <b>left</b> click.</html>"));
        add(paragraph, "cell 0 1,grow");

        paragraph = new JLabel(_("<html>When tile is placed use <b>Tab</b> or <b>right</b> click again to "+
                        "select appropriate action.</html>"));
        add(paragraph, "cell 0 2,grow");

        paragraph = new JLabel(_("<html>Alternativelly you can pass with <b>Enter</b> or <b>Space</b> to play no action.</html>"));
        add(paragraph, "cell 0 3,grow");
    }

}
