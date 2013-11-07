package com.jcloisterzone.ui.animation;

import java.awt.Color;
import java.awt.Graphics2D;

import com.jcloisterzone.board.Position;
import com.jcloisterzone.ui.ImmutablePoint;
import com.jcloisterzone.ui.UiUtils;
import com.jcloisterzone.ui.grid.layer.AnimationLayer;


public class ScoreAnimation extends AbstractAnimation {

    private static final Color POINTS_BLACK_BACKGROUND_COLOR = new Color(0.0f, 0.0f, 0.0f, 0.7f);
    private static final Color POINTS_BACKGROUND_COLOR = new Color(1.0f, 1.0f, 1.0f, 0.7f);

    private Position tilePosition;
    private String points;
    private ImmutablePoint offset;
    private Color color;

    //fuuuuj tolik parametru
    public ScoreAnimation(Position tilePosition, String points, ImmutablePoint point, Color color, Integer duration) {
        this.tilePosition = tilePosition;
        this.points = points;
        this.offset = point;
        this.color = color;
        if (duration == null) {
            nextFrame = Long.MAX_VALUE >> 20; //divisior greater then 1000000
        } else {
            nextFrame = System.currentTimeMillis() + duration * 1000;
        }
    }


    @Override
    public void paint(AnimationLayer l, Graphics2D g) {
        Color bgColor;
        if (UiUtils.isBrightColor(color)) {
            bgColor = POINTS_BLACK_BACKGROUND_COLOR;
        } else {
            bgColor = POINTS_BACKGROUND_COLOR;
        }
        l.drawAntialiasedTextCentered(g, points, 22, tilePosition, offset, color, bgColor);
    }


}
