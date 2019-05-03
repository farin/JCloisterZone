package com.jcloisterzone.game.capability;

import com.jcloisterzone.Player;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.MeepleIdProvider;
import com.jcloisterzone.figure.SmallFollower;
import com.jcloisterzone.game.Capability;

import io.vavr.collection.List;
import io.vavr.collection.Stream;

public class StandardGameCapability extends Capability<Void> {

	private static final long serialVersionUID = 1L;

    public static final int SMALL_FOLLOWER_QUANTITY = 7;

    @Override
    public List<Follower> createPlayerFollowers(Player player, MeepleIdProvider idProvider) {
        return Stream.range(0, SMALL_FOLLOWER_QUANTITY)
            .map(i -> (Follower) new SmallFollower(idProvider.generateId(SmallFollower.class), player))
            .toList();
    }

}
