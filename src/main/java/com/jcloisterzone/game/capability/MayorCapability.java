package com.jcloisterzone.game.capability;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;
import com.jcloisterzone.Player;
import com.jcloisterzone.action.MeepleAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.collection.Sites;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.figure.Mayor;
import com.jcloisterzone.game.CapabilityController;

public class MayorCapability extends CapabilityController {

    @Override
    public void initPlayer(Player player) {
        player.addMeeple(new Mayor(game, player));
    }

    private Set<Location> copyMayorLocations(Set<Location> locations) {
        Set<Location> result = Sets.newHashSet();
        for(Feature piece : getTile().getFeatures()) {
            Location loc = piece.getLocation();
            if (piece instanceof City && locations.contains(loc)) {
                result.add(loc);
            }

        }
        return result;
    }

    @Override
    public void prepareFollowerActions(List<PlayerAction> actions, Sites commonSites) {
        Position pos = getTile().getPosition();
        Set<Location> tileLocations = commonSites.get(pos);
        if (game.getActivePlayer().hasFollower(Mayor.class)) {
            if (tileLocations != null) {
                Set<Location> mayorLocations = copyMayorLocations(tileLocations);
                if (! mayorLocations.isEmpty()) {
                    actions.add(new MeepleAction(Mayor.class, pos, mayorLocations));
                }
            }
        }
    }

    @Override
    public void prepareActions(List<PlayerAction> actions, Sites commonSites) {
        prepareFollowerActions(actions, commonSites);
    }

}
