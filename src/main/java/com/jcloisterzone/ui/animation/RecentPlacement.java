package com.jcloisterzone.ui.animation;

import java.awt.Color;
import java.awt.Graphics2D;

import com.jcloisterzone.board.Position;
import com.jcloisterzone.ui.grid.layer.AnimationLayer;

public class RecentPlacement extends AbstractAnimation {

	private static final int FRAME_DURATION = 75;
	private static final int ALPHA_STEP = 8;

	private int alpha = 24 * ALPHA_STEP;
	private Color color;

	private Position position;


	public RecentPlacement(Position position) {
		this.position = position;
		nextFrame = System.currentTimeMillis() + FRAME_DURATION;
		color = createColor();
	}

	private Color createColor() {
		return new Color(0,0,0,alpha);
	}

	public boolean switchFrame() {
		alpha -= ALPHA_STEP;
		if (alpha <= 0) {
			return false;
		}
		color = createColor();
		nextFrame += FRAME_DURATION;
		return true;
	}

	@Override
	public void paint(AnimationLayer l, Graphics2D g) {
		g.setColor(color);
		g.fillRect(l.getOffsetX(position), l.getOffsetY(position), l.getSquareSize(), l.getSquareSize());
	}
}
