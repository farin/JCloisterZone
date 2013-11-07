package com.jcloisterzone.ui;

import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JTextArea;

public class MultiLineLabel extends JTextArea {

	private static final long serialVersionUID = 3195043205605956817L;

	private static Font font = (new JLabel()).getFont(); //TODO without label

	public MultiLineLabel() {
		super();
		initialize();
	}

	public MultiLineLabel(String text) {
		super(text);
		initialize();
	}

	private void initialize() {
		setEditable(false);
		setOpaque(false);
		setFont(font);
		setLineWrap(true);
		setWrapStyleWord(true);
	}

}
