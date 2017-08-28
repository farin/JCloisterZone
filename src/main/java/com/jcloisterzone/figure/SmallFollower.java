package com.jcloisterzone.figure;

import com.jcloisterzone.Immutable;
import com.jcloisterzone.Player;

@Immutable
public class SmallFollower extends Follower {

    private static final long serialVersionUID = 9167040308990588349L;

    public static final int QUANTITY = 7;

    public SmallFollower(String id, Player player) {
        super(id, player);
    }

}
