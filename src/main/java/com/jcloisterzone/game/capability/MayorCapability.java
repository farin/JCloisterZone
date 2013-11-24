package com.jcloisterzone.game.capability;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.jcloisterzone.Player;
import com.jcloisterzone.action.MeepleAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.collection.LocationsMap;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.figure.Mayor;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.Game;

public class MayorCapability extends Capability {

    public MayorCapability(Game game) {
        super(game);
    }

    @Override
    public void initPlayer(Player player) {
        player.addMeeple(new Mayor(game, player));
    }

    private Set<Location> copyMayorLocations(Set<Location> locations) {
        Set<Location> result = new HashSet<>();
        for (Feature piece : getTile().getFeatures()) {
            Location loc = piece.getLocation();
            if (piece instanceof City && locations.contains(loc)) {
                result.add(loc);
            }

        }
        return result;
    }

    @Override
    public void prepareFollowerActions(List<PlayerAction> actions, LocationsMap followerLocMap) {
        Position pos = getTile().getPosition();
        Set<Location> tileLocations = followerLocMap.get(pos);
        if (game.getActivePlayer().hasFollower(Mayor.class)) {
            if (tileLocations != null) {
                Set<Location> mayorLocations = copyMayorLocations(tileLocations);
                if (!mayorLocations.isEmpty()) {
                    actions.add(new MeepleAction(Mayor.class, pos, mayorLocations));
                }
            }
        }
    }

    @Override
    public void prepareActions(List<PlayerAction> actions, LocationsMap followerLocMap) {
        prepareFollowerActions(actions, followerLocMap);
    }

}
