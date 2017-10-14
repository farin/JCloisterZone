package com.jcloisterzone.figure;

import com.jcloisterzone.Immutable;
import com.jcloisterzone.Player;

@Immutable
public class SmallFollower extends Follower {

    private static final long serialVersionUID = 1L;

    public SmallFollower(String id, Player player) {
        super(id, player);
    }

}
