package com.jcloisterzone.game.capability;

import com.jcloisterzone.Player;
import com.jcloisterzone.figure.MeepleIdProvider;
import com.jcloisterzone.figure.Pig;
import com.jcloisterzone.figure.Special;
import com.jcloisterzone.game.Capability;

import io.vavr.collection.List;

public class PigCapability extends Capability<Void> {

	private static final long serialVersionUID = 1L;

    @Override
    public List<Special> createPlayerSpecialMeeples(Player player, MeepleIdProvider idProvider) {
        return List.of(new Pig(idProvider.generateId(Pig.class), player));
    }
}
