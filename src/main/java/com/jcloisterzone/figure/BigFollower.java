package com.jcloisterzone.figure;

import com.jcloisterzone.Player;
import com.jcloisterzone.game.Game;

public class BigFollower extends Follower {

    private static final long serialVersionUID = -5506815500027084904L;

    public BigFollower(Game game, Integer idSuffix, Player player) {
        super(game, idSuffix, player);
    }

    @Override
    public int getPower() {
        return 2;
    }

}
