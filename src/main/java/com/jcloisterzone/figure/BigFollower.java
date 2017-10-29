package com.jcloisterzone.figure;

import com.jcloisterzone.Immutable;
import com.jcloisterzone.Player;
import com.jcloisterzone.feature.Scoreable;
import com.jcloisterzone.game.state.GameState;

@Immutable
public class BigFollower extends Follower {

    private static final long serialVersionUID = 1L;

    public BigFollower(String id, Player player) {
        super(id, player);
    }

    @Override
    public int getPower(GameState state, Scoreable feature) {
        return 2;
    }

}
