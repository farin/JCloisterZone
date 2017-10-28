package com.jcloisterzone.ui.component;

import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.text.Document;

import com.jcloisterzone.ui.gtk.ThemedJTextArea;

public class MultiLineLabel extends ThemedJTextArea {

    private static final long serialVersionUID = 3195043205605956817L;

    private static Font font = (new JLabel()).getFont(); //TODO without label

    public MultiLineLabel() {
        super();
    }

    public MultiLineLabel(Document doc, String text, int rows, int columns) {
        super(doc, text, rows, columns);
    }

    public MultiLineLabel(Document doc) {
        super(doc);
    }

    public MultiLineLabel(int rows, int columns) {
        super(rows, columns);
    }

    public MultiLineLabel(String text, int rows, int columns) {
        super(text, rows, columns);
    }


    public MultiLineLabel(String text) {
        super(text);
    }

    {
        setEditable(false);
        setOpaque(false);
        setFont(font);
        setLineWrap(true);
        setWrapStyleWord(true);
    }

}
