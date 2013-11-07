package com.jcloisterzone.ui.grid.layer;

import java.awt.Graphics2D;

import com.jcloisterzone.ui.animation.Animation;
import com.jcloisterzone.ui.animation.AnimationService;
import com.jcloisterzone.ui.animation.RecentPlacement;
import com.jcloisterzone.ui.animation.ScoreAnimation;
import com.jcloisterzone.ui.grid.GridPanel;

public class AnimationLayer extends AbstractGridLayer {

    private final AnimationService service;

    public AnimationLayer(GridPanel gridPanel, AnimationService service) {
        super(gridPanel);
        this.service = service;
    }

    @Override
    public void paint(Graphics2D g2) {
        //HACK to correct animation order - TODO change animation design
        for (Animation a : service.getAnimations()) {
            if (a instanceof RecentPlacement) a.paint(this, g2);
        }
        for (Animation a : service.getAnimations()) {
            if (a instanceof ScoreAnimation) a.paint(this, g2);
        }
    }

    @Override
    public int getZIndex() {
        return 800;
    }


}
