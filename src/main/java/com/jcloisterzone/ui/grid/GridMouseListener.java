package com.jcloisterzone.ui.grid;

import java.awt.event.MouseEvent;

import com.jcloisterzone.board.Position;

public interface GridMouseListener {

	void tileEntered(MouseEvent e, Position p);
	void tileExited(MouseEvent e, Position p);

	void mouseClicked(MouseEvent e, Position p);
}
