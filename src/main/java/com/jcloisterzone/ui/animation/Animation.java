package com.jcloisterzone.ui.animation;

import java.awt.Graphics2D;
import java.util.concurrent.Delayed;

import com.jcloisterzone.ui.grid.layer.AnimationLayer;


public interface Animation extends Delayed {

    long getNextFrameTs();
    boolean switchFrame();

    void paint(AnimationLayer l, Graphics2D g2);

}
