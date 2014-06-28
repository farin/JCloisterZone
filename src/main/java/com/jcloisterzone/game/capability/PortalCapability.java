package com.jcloisterzone.game.capability;

import java.util.List;
import java.util.Set;

import org.w3c.dom.Element;

import com.jcloisterzone.action.MeepleAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.TileTrigger;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.Game;

public class PortalCapability extends Capability {

    boolean portalUsed = false;

    public PortalCapability(Game game) {
        super(game);
    }

    @Override
    public Object backup() {
        return portalUsed;
    }

    @Override
    public void restore(Object data) {
        portalUsed = (Boolean) data;
    }

    @Override
    public void initTile(Tile tile, Element xml) {
        if (xml.getElementsByTagName("portal").getLength() > 0) {
            tile.setTrigger(TileTrigger.PORTAL);
        }
    }

    @Override
    public void prepareActions(List<PlayerAction<?>> actions, Set<FeaturePointer> commonSites) {
        if (getTile().hasTrigger(TileTrigger.PORTAL)) {
            if (game.getActivePlayer().hasFollower()) {
                prepareMagicPortal(findFollowerActions(actions), commonSites);
            }
        }
    }

    public void prepareMagicPortal(List<MeepleAction> followerActions, Set<FeaturePointer> commonSites) {
        if (portalUsed) return;
        for (Tile tile : getBoard().getAllTiles()) {
            if (tile == getTile()) continue; //prepared by basic common
            Set<FeaturePointer> locations = game.prepareFollowerLocations(tile, true);
            if (locations.isEmpty()) continue;
            for (MeepleAction ma : followerActions) {
            	ma.addAll(locations);
            }
        }
    }

    @Override
    public void turnPartCleanUp() {
        portalUsed = false;
    }

    public boolean isPortalUsed() {
        return portalUsed;
    }

    public void setPortalUsed(boolean portalUsed) {
        this.portalUsed = portalUsed;
    }


}
