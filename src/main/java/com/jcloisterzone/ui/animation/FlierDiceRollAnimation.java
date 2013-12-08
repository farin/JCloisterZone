package com.jcloisterzone.ui.animation;

import java.awt.Color;
import java.awt.Graphics2D;

import com.jcloisterzone.board.Position;
import com.jcloisterzone.ui.grid.layer.AnimationLayer;

public class FlierDiceRollAnimation extends AbstractAnimation {

    private final Position tilePosition;
    private final int roll;

    public FlierDiceRollAnimation(Position tilePosition, int roll) {
        assert roll >= 1 && roll <= 3;
        this.tilePosition = tilePosition;
        this.roll = roll;
        nextFrame = System.currentTimeMillis() + 5000;
    }

    @Override
    public void paint(AnimationLayer l, Graphics2D g2) {
        int sqSize = l.getSquareSize(),
            s = (int)(sqSize * 0.4),
            padding = (int)(sqSize * 0.05),
            dotSize = (int)(sqSize * 0.08),
            x = l.getOffsetX(tilePosition),
            y = l.getOffsetY(tilePosition),
            boxX = x+sqSize-padding-s,
            boxY = y+padding;
        g2.setColor(Color.WHITE);
        g2.fillRect(boxX, boxY, s, s);
        g2.setColor(Color.BLACK);
        if (roll == 2) {
            int s1_3 = (int) (s*0.33), s2_3 = (int) (s*0.67);
            g2.fillOval(boxX + s1_3 - dotSize/2, boxY + s2_3 - dotSize/2, dotSize, dotSize);
            g2.fillOval(boxX + s2_3 - dotSize/2, boxY + s1_3 - dotSize/2, dotSize, dotSize);
        } else {
            g2.fillOval(boxX + s/2 - dotSize/2, boxY + s/2 - dotSize/2, dotSize, dotSize);
            if (roll == 3) {
                int s1_4 = (int) (s*0.25), s3_4 = (int) (s*0.75);
                g2.fillOval(boxX + s1_4 - dotSize/2, boxY + s3_4 - dotSize/2, dotSize, dotSize);
                g2.fillOval(boxX + s3_4 - dotSize/2, boxY + s1_4 - dotSize/2, dotSize, dotSize);
            }
        }
    }

}
