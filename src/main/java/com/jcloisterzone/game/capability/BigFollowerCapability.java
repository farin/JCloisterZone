package com.jcloisterzone.game.capability;

import java.util.List;
import java.util.Set;

import com.jcloisterzone.Player;
import com.jcloisterzone.action.MeepleAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.board.pointer.FeaturePointer;
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
    public void prepareActions(List<PlayerAction<?>> actions, Set<FeaturePointer> followerOptions) {
        if (game.getActivePlayer().hasFollower(BigFollower.class) && !followerOptions.isEmpty()) {
            actions.add(new MeepleAction(BigFollower.class).addAll(followerOptions));
        }
    }
}
