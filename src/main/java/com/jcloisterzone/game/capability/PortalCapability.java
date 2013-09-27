package com.jcloisterzone.game.capability;

import java.util.List;
import java.util.Set;

import org.w3c.dom.Element;

import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.TileTrigger;
import com.jcloisterzone.collection.LocationsMap;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.Game;

public class PortalCapability extends Capability {

    public PortalCapability(Game game) {
        super(game);
    }

    @Override
    public PortalCapability copy(Game gameCopy) {
        return new PortalCapability(gameCopy);
    }

    @Override
    public void initTile(Tile tile, Element xml) {
        if (xml.getElementsByTagName("portal").getLength() > 0) {
            tile.setTrigger(TileTrigger.PORTAL);
        }
    }

    @Override
    public void prepareActions(List<PlayerAction> actions, LocationsMap commonSites) {
        if (TileTrigger.PORTAL.equals(getTile().getTrigger())) {
            if (game.getActivePlayer().hasFollower()) {
                prepareMagicPortal(commonSites);
            }
        }
    }

    private void prepareMagicPortal(LocationsMap commonSites) {
        for (Tile tile : getBoard().getAllTiles()) {
            if (tile == getTile()) continue; //prepared by basic common
            Set<Location> locations = game.prepareFollowerLocations(tile, true);
            if (locations.isEmpty()) continue;
            commonSites.put(tile.getPosition(), locations);
        }
    }

}
