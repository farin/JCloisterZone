package com.jcloisterzone.ui.grid;

import java.awt.event.MouseEvent;

import com.jcloisterzone.board.Position;

public interface GridMouseListener {

	void squareEntered(MouseEvent e, Position p);
	void squareExited(MouseEvent e, Position p);

	void mouseClicked(MouseEvent e, Position p);
}
