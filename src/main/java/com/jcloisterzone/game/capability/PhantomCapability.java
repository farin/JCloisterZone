package com.jcloisterzone.game.capability;

import java.util.List;

import com.jcloisterzone.Player;
import com.jcloisterzone.action.MeepleAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.collection.LocationsMap;
import com.jcloisterzone.figure.Phantom;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.Game;

public class PhantomCapability extends Capability {

    public PhantomCapability(Game game) {
        super(game);
    }

    @Override
    public void initPlayer(Player player) {
        player.addMeeple(new Phantom(game, player));
    }

    @Override
    public void prepareFollowerActions(List<PlayerAction> actions, LocationsMap followerLocMap) {
        if (game.getActivePlayer().hasFollower(Phantom.class) && !followerLocMap.isEmpty()) {
            actions.add(new MeepleAction(Phantom.class, followerLocMap));
        }
    }

    @Override
    public void prepareActions(List<PlayerAction> actions, LocationsMap followerLocMap) {
        prepareFollowerActions(actions, followerLocMap);
    }


}
