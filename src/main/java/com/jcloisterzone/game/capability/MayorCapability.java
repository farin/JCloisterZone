package com.jcloisterzone.game.capability;

import com.jcloisterzone.Player;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.Mayor;
import com.jcloisterzone.figure.MeepleIdProvider;
import com.jcloisterzone.game.Capability;

import io.vavr.collection.List;

public class MayorCapability extends Capability<Void> {

    @Override
    public List<Follower> createPlayerFollowers(Player player, MeepleIdProvider idProvider) {
        return List.of(new Mayor(idProvider.generateId(Mayor.class), player));
    }
}
