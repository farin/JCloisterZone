package com.jcloisterzone.game.capability;

import java.util.List;

import com.jcloisterzone.Player;
import com.jcloisterzone.action.MeepleAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.collection.Sites;
import com.jcloisterzone.figure.BigFollower;
import com.jcloisterzone.game.CapabilityController;

public class BigFollowerCapability extends CapabilityController {


    @Override
    public void initPlayer(Player player) {
        player.addMeeple(new BigFollower(game, player));
    }

    @Override
    public void prepareFollowerActions(List<PlayerAction> actions, Sites commonSites) {
        if (game.getActivePlayer().hasFollower(BigFollower.class) && !commonSites.isEmpty()) {
            actions.add(new MeepleAction(BigFollower.class, commonSites));
        }
    }

    @Override
    public void prepareActions(List<PlayerAction> actions, Sites commonSites) {
        prepareFollowerActions(actions, commonSites);
    }

}
