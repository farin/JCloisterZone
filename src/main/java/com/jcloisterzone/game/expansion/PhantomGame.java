package com.jcloisterzone.game.expansion;

import java.util.List;

import com.jcloisterzone.Player;
import com.jcloisterzone.action.MeepleAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.collection.Sites;
import com.jcloisterzone.figure.Phantom;
import com.jcloisterzone.game.ExpandedGame;

public class PhantomGame extends ExpandedGame {

    @Override
    public void initPlayer(Player player) {
        player.addMeeple(new Phantom(game, player));
    }

    @Override
    public void prepareActions(List<PlayerAction> actions, Sites commonSites) {
        if (game.getActivePlayer().hasFollower(Phantom.class) && ! commonSites.isEmpty()) {
            actions.add(new MeepleAction(Phantom.class, commonSites));
        }
    }

}
