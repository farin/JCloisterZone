package com.jcloisterzone.game.capability;

import com.jcloisterzone.Player;
import com.jcloisterzone.figure.BigFollower;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.Game;

public class BigFollowerCapability extends Capability {

    public BigFollowerCapability(Game game) {
        super(game);
    }

    @Override
    public void initPlayer(Player player) {
        player.addMeeple(new BigFollower(game, null, player));
    }
}
