package com.jcloisterzone.game.capability;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.jcloisterzone.XmlUtils;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.SnapshotCorruptedException;
import com.jcloisterzone.game.phase.ActionPhase;
import com.jcloisterzone.game.phase.Phase;

public class FlierCapability extends Capability {

    private int flierDistance;

    public FlierCapability(Game game) {
        super(game);
    }

    @Override
    public Object backup() {
        return flierDistance;
    }

    @Override
    public void restore(Object data) {
        flierDistance = (Integer) data;
    }

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

    @Override
    public void saveToSnapshot(Document doc, Element node) {
        if (flierDistance > 0) {
            node.setAttribute("flierDistance", ""+flierDistance);
        }
    }

    @Override
    public void loadFromSnapshot(Document doc, Element node) throws SnapshotCorruptedException {
        if (node.hasAttribute("flierDistance")) {
             flierDistance = Integer.parseInt(node.getAttribute("flierDistance"));
        }
    }

}
