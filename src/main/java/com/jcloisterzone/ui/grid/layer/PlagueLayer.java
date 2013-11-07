package com.jcloisterzone.ui.grid.layer;

import java.awt.Color;
import java.awt.Graphics2D;

import com.jcloisterzone.game.capability.PlagueCapability;
import com.jcloisterzone.game.capability.PlagueCapability.PlagueSource;
import com.jcloisterzone.ui.ImmutablePoint;
import com.jcloisterzone.ui.grid.GridPanel;

public class PlagueLayer extends AbstractGridLayer {

    private static final Color ACTIVE_PLAGUE = new Color(235, 57, 43);
    private static final Color ERADICTED_PLAGUE = new Color(141, 178, 145);

    private final PlagueCapability plague;

    public PlagueLayer(GridPanel gridPanel) {
        super(gridPanel);
        plague = getGame().getCapability(PlagueCapability.class);
    }

    @Override
    public void paint(Graphics2D g2) {
        int sqSize = getSquareSize();
        int boxSize = (int)(sqSize*0.4);
        int i = 0;
        for (PlagueSource source: plague.getPlagueSources()) {
            i++;

            g2.setColor(source.active ? ACTIVE_PLAGUE : ERADICTED_PLAGUE);
            int x = sqSize-boxSize-sqSize/10;
            int y = sqSize/10;
            g2.fillRect(getOffsetX(source.pos)+x, getOffsetY(source.pos)+y, boxSize, boxSize);
            if (source.active) {
                ImmutablePoint center = new ImmutablePoint(x+boxSize/2, y+boxSize/2);
                drawAntialiasedTextCenteredNoScale(g2, i+"", 26, source.pos, center, Color.WHITE, null);
            }
        }
    }

    @Override
    public int getZIndex() {
        return 45;
    }

}
