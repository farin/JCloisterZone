package com.jcloisterzone.figure;

import com.jcloisterzone.Immutable;
import com.jcloisterzone.Player;

@Immutable
public class Phantom extends SmallFollower {

    private static final long serialVersionUID = 1L;

    public Phantom(String id, Player player) {
        super(id, player);
    }

}
