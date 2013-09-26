package com.jcloisterzone.game.capability;

import java.util.List;

import com.jcloisterzone.Player;
import com.jcloisterzone.action.MeepleAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.collection.Sites;
import com.jcloisterzone.figure.Phantom;
import com.jcloisterzone.game.Capability;

public class PhantomCapability extends Capability {

    @Override
    public void initPlayer(Player player) {
        player.addMeeple(new Phantom(game, player));
    }

    @Override
    public void prepareFollowerActions(List<PlayerAction> actions, Sites commonSites) {
        if (game.getActivePlayer().hasFollower(Phantom.class) && ! commonSites.isEmpty()) {
            actions.add(new MeepleAction(Phantom.class, commonSites));
        }
    }

    @Override
    public void prepareActions(List<PlayerAction> actions, Sites commonSites) {
        prepareFollowerActions(actions, commonSites);
    }


}
