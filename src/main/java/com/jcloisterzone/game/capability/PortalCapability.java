package com.jcloisterzone.game.capability;

import java.util.List;
import java.util.Set;

import org.w3c.dom.Element;

import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.TileTrigger;
import com.jcloisterzone.collection.Sites;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.CapabilityController;

public class PortalCapability extends CapabilityController {

    @Override
    public void initTile(Tile tile, Element xml) {
        if (xml.getElementsByTagName("portal").getLength() > 0) {
            tile.setTrigger(TileTrigger.PORTAL);
        }
    }

    @Override
    public void prepareActions(List<PlayerAction> actions, Sites commonSites) {
        if (TileTrigger.PORTAL.equals(getTile().getTrigger())) {
            if (game.getActivePlayer().hasFollower()) {
                prepareMagicPortal(commonSites);
            }
        }
    }

    private void prepareMagicPortal(Sites commonSites) {
        for (Tile tile : getBoard().getAllTiles()) {
            if (tile == getTile()) continue; //prepared by basic common
            if (tile.isForbidden()) continue;
            if (game.hasCapability(Capability.DRAGON)) {
                if (tile.getPosition().equals(game.getDragonCapability().getDragonPosition())) continue;
            }
            Set<Location> tileSites = getGame().prepareCommonForTile(tile, true);
            if (tileSites.isEmpty()) continue;
            commonSites.put(tile.getPosition(), tileSites);
        }
    }

}
