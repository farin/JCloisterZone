package com.jcloisterzone.game.expansion;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.XmlUtils;
import com.jcloisterzone.game.ExpandedGame;
import com.jcloisterzone.game.phase.ActionPhase;
import com.jcloisterzone.game.phase.Phase;

public class FlierGame extends ExpandedGame {

    private int flierDistance;

    public int getFlierDistance() {
        return flierDistance;
    }

    public void setFlierDistance(int flierDistance) {
        this.flierDistance = flierDistance;
    }

    @Override
    public void initTile(Tile tile, Element xml) {
        NodeList nl = xml.getElementsByTagName("flier");
        assert nl.getLength() <= 1;
        if (nl.getLength() == 1) {
            Location flier = XmlUtils.union(XmlUtils.asLocation((Element) nl.item(0)));
            tile.setFlier(flier);
        }
    }

    public boolean isFlierRollAllowed() {
        Phase phase = game.getPhase();
        if (phase instanceof ActionPhase && game.getActivePlayer().hasFollower()) {
            return game.getCurrentTile().getFlier() != null;
        }
        return false;
    }
}
