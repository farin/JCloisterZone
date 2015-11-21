package com.jcloisterzone.ui.gtk;

import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.text.Document;

public class ThemedJTextArea extends JTextArea {

	public ThemedJTextArea() {
		super();
	}

	public ThemedJTextArea(Document doc, String text, int rows, int columns) {
		super(doc, text, rows, columns);
	}

	public ThemedJTextArea(Document doc) {
		super(doc);
	}

	public ThemedJTextArea(int rows, int columns) {
		super(rows, columns);
	}

	public ThemedJTextArea(String text, int rows, int columns) {
		super(text, rows, columns);
	}

	public ThemedJTextArea(String text) {
		super(text);
	}

	{
		setForeground(UIManager.getColor("TextArea.foreground"));
	}

}
