package com.jcloisterzone.game.capability;

import java.util.List;

import com.jcloisterzone.Player;
import com.jcloisterzone.action.MeepleAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.collection.LocationsMap;
import com.jcloisterzone.figure.BigFollower;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.Game;

public class BigFollowerCapability extends Capability {

    public BigFollowerCapability(Game game) {
        super(game);
    }

    @Override
    public void initPlayer(Player player) {
        player.addMeeple(new BigFollower(game, player));
    }

    @Override
    public void prepareFollowerActions(List<PlayerAction> actions, LocationsMap followerLocMap) {
        if (game.getActivePlayer().hasFollower(BigFollower.class) && !followerLocMap.isEmpty()) {
            actions.add(new MeepleAction(BigFollower.class, followerLocMap));
        }
    }

    @Override
    public void prepareActions(List<PlayerAction> actions, LocationsMap commonSites) {
        prepareFollowerActions(actions, commonSites);
    }


}
