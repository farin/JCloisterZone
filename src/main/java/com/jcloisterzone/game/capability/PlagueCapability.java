package com.jcloisterzone.game.capability;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;

import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.TileTrigger;
import com.jcloisterzone.game.CapabilityController;

public class PlagueCapability extends CapabilityController {

    final List<PlagueSource> plagueSources = new ArrayList<>(6);

    @Override
    public void initTile(Tile tile, Element xml) {
        if (xml.getElementsByTagName("plague").getLength() > 0) {
            tile.setTrigger(TileTrigger.PLAGUE);
        }
    }

    @Override
    public String getTileGroup(Tile tile) {
        return tile.getTrigger() == TileTrigger.PLAGUE ? "plague" : null;
    }

    @Override
    public void begin() {
        //TODO replace with activation ofter 17th tile
        getTilePack().activateGroup("plague");
    }

    public List<PlagueSource> getPlagueSources() {
        return plagueSources;
    }

    public List<Position> getActiveSources() {
        List<Position> result = new ArrayList<>(6);
        for (PlagueSource source : plagueSources) {
            if (source.isActive) {
                result.add(source.pos);
            }
        }
        return result;
    }

    public static class PlagueSource {
        public Position pos;
        public boolean isActive = true;

        public PlagueSource(Position pos) {
            this.pos = pos;
        }
    }
}
